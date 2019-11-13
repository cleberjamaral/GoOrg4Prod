package organisation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import busca.Antecessor;
import busca.Estado;
import properties.Performer;
import properties.Workload;
import simplelogger.SimpleLogger;

public class Organisation implements Estado, Antecessor {

	/*** STATIC ***/
	private static SimpleLogger LOG = SimpleLogger.getInstance(4);
	// list of target states, i.e., complete charts
	private static List<Organisation> isGoalList = new ArrayList<Organisation>();
	// Cost penalty used to infer bad decisions on search
	private static int costPenalty = 0;

	/*** LOCAL ***/
	// this is the chart that is being created by the algorithm, potentially a
	// complete chart
	private List<RoleNode> rolesTree = new ArrayList<RoleNode>();
	// The goals that were not explored yet, the algorithm end when all goals were
	// allocated into roles
	private List<GoalNode> goalSuccessors = new ArrayList<GoalNode>();

	// static Cost costFunction = Cost.SPECIALIST;
	static Cost costFunction = Cost.SPECIALIST;

	// Cost supporting variables
	private int cost = 0;
	private int accCost = 0;

	public String getDescricao() {
		return "Empty\n";
	}

	public Organisation(GoalNode gn, Cost costFunction) {
		this(gn);

		Organisation.costFunction = costFunction;
	}

	public Organisation(GoalNode gn, Cost costFunction, List<Object> agents) {
		this(gn);

		Organisation.costFunction = costFunction;
	}

	public Organisation(GoalNode gn) {

		// Is the first state that is going to be created
		if (gn.getParent() == null) {
			addAllGoalsSuccessors(gn);

			String roleName = "r" + this.rolesTree.size();
			RoleNode r = new RoleNode(null, roleName);
			r.assignGoal(gn);
			for (Object requirement : gn.getRequirements())
				r.addRequirement(((Workload)requirement).clone());
			this.rolesTree.add(r);

			// Used to infer a bad decision on the search
			Organisation.costPenalty = this.goalSuccessors.size() + 1;

			LOG.info("FIRST STATE CREATED: " + this.toString() + " | " + this.hashCode() + " | Cost penalty: "
					+ Organisation.costPenalty);
		}
	}

	private void addAllGoalsSuccessors(GoalNode gn) {
		for (GoalNode goal : gn.getSuccessors()) {
			this.goalSuccessors.add(goal);
			addAllGoalsSuccessors(goal);
		}
	}

	public boolean ehMeta() {

		if (this.goalSuccessors.size() <= 0) {
			if (!isGoalList.contains(this)) {
				isGoalList.add(this);
				LOG.info("GOAL ACHIEVED! Solution: #" + isGoalList.size() + " : " + this.toString() + " | "
						+ this.hashCode());

				plotOrganisation(isGoalList.size(), false);

				return true; // if only one solution is needed
			} else {
				// This should not happen again, it has occurred because searching process
				// (deepth) was calling ehMeta 2 times
				LOG.info("Goal achieved but duplicated!" + " : " + this.hashCode());
				//return true; // if only one solution is needed
			}
		}
		return false;
	}

	public void plotOrganisation(int organisationId, boolean generateProof) {
		List<String> links = new ArrayList<>();

		File diagramFile = new File("output/diagrams/tmp");
		diagramFile.getParentFile().mkdirs();

		try (FileWriter fw = new FileWriter("output/diagrams/graph_" + organisationId + ".gv", false);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {

			out.println("digraph G {");
			for (RoleNode or : this.rolesTree) {
				out.print("\t\"" + or.getRoleName()// headGoal.getGoalName()
						+ "\" [ style = \"filled\" fillcolor = \"white\" fontname = \"Courier New\" "
						+ "shape = \"Mrecord\" label = <<table border=\"0\" cellborder=\"0\" bgcolor=\"white\">"
						+ "<tr><td bgcolor=\"black\" align=\"center\"><font color=\"white\">" + or.getRoleName()
						+ "</font></td></tr><tr><td align=\"center\">" + or.getAssignedGoals() + "</td></tr>");
				for (Object s : or.getRequirements())
					out.print("<tr><td align=\"left\">" + s.toString() + "</td></tr>");
				out.println("</table>> ];");

				if (or.getParent() != null)
					links.add("\"" + or.getParent().getRoleName() + "\"->\"" + or.getRoleName() + "\"");
			}

			for (String l : links)
				out.println("\t" + l + ";");
			out.println("}");
		} catch (IOException e) {
		}

		if (generateProof) {
			File proofFile = new File("output/proofs/tmp");
			proofFile.getParentFile().mkdirs();

			try (FileWriter fw = new FileWriter("output/proofs/graph_" + organisationId + ".txt", false);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter out = new PrintWriter(bw)) {

				out.println(this.toString());
			} catch (IOException e) {
			}
		}
	}

	public int custo() {
		LOG.info("cost: " + cost + " accCost: " + accCost);
		return cost;
	}

	/** Lista de sucessores */
	public List<Estado> sucessores() {
		List<Estado> suc = new LinkedList<Estado>(); // Lista de sucessores

		if (!this.goalSuccessors.isEmpty())
			LOG.info("\nSTATE: " + this.toString() + "Tree:" + this.rolesTree.toString() + " - OpenGoals: ["
					+ goalSuccessors.toString() + "] - Size: " + goalSuccessors.size() + ", Hash: " + this.hashCode());

		// add all children as possible successors
		for (GoalNode goalToBeAssociated : goalSuccessors) {
			// add all children as possible successors
			for (RoleNode role : rolesTree) {
				// if one of assigned goals is parent, so open it as a child
				if (role.getAssignedGoals().contains(goalToBeAssociated.getParent())) {
					// creating successors, create a subordinate role (child)
					addSubordinate(role, suc, goalToBeAssociated);
					// creating successors, join goals of a pair
					if (role.matchRequirements(goalToBeAssociated.getRequirements())) {
						joinASubordinate(role, suc, goalToBeAssociated);
					}
				} else if ((role.getParent() != null)
						&& (role.getParent().getAssignedGoals().contains(goalToBeAssociated.getParent()))) {
					// creating successors, join goals of a pair
					if (role.matchRequirements(goalToBeAssociated.getRequirements())) {
						joinAPair(role, suc, goalToBeAssociated);
					}
				}
			}
		}

		return suc;
	}

	public void addSubordinate(RoleNode parentRole, List<Estado> suc, GoalNode goalToBeAssociatedToRole) {

		Organisation newState = (Organisation) createState(goalToBeAssociatedToRole);

		if ((costFunction == Cost.FLATTER) || (costFunction == Cost.GENERALIST)) {
			newState.cost = Organisation.costPenalty;
		} else {
			newState.cost = 1;
		}
		newState.accCost = this.accCost + newState.cost;

		RoleNode r = new RoleNode(parentRole, "r" + newState.rolesTree.size());
		r.assignGoal(goalToBeAssociatedToRole);
		// Copy all requirements of the goal to this new role
		for (Object requirement : goalToBeAssociatedToRole.getRequirements())
			r.addRequirement(((Workload)requirement).clone());

		newState.rolesTree.add(r);

		suc.add(newState);

		LOG.info("addSubordinate	: " + newState.toString() + ", nSucc: " + newState.goalSuccessors + ", Name: "
				+ r.getRoleName() + ", Parent: " + r.getParent().getRoleName() + ", Hash: " + newState.hashCode());
	}

	public void joinAPair(RoleNode role, List<Estado> suc, GoalNode goalToBeAssociatedToRole) {

		Organisation newState = (Organisation) createState(goalToBeAssociatedToRole);

		if (costFunction == Cost.SPECIALIST) {
			newState.cost = Organisation.costPenalty;
		} else {
			newState.cost = 1;
		}
		newState.accCost = this.accCost + newState.cost;

		// the new role is also assigned to a new goal (the joined one)
		for (RoleNode or : newState.rolesTree) {
			// if all assigned goals are same, so it found the same role of the method
			// argument 'role'
			if (or.getAssignedGoals().containsAll(role.getAssignedGoals())
					&& role.getAssignedGoals().containsAll(or.getAssignedGoals())) {
				if (!or.getAssignedGoals().contains(goalToBeAssociatedToRole)) {
					or.assignGoal(goalToBeAssociatedToRole);
					suc.add(newState);
					LOG.info("joinAPair     	: " + newState.toString() + ", nSucc: " + newState.goalSuccessors
							+ ", Name: " + role.getRoleName() + ", Parent: " + role.getParent().getRoleName()
							+ ", Hash: " + newState.hashCode());
					break;
				}
			}
		}
	}

	public void joinASubordinate(RoleNode role, List<Estado> suc, GoalNode goalToBeAssociatedToRole) {

		Organisation newState = (Organisation) createState(goalToBeAssociatedToRole);

		if ((costFunction == Cost.TALLER) || (costFunction == Cost.SPECIALIST)) {
			newState.cost = Organisation.costPenalty;
		} else {
			newState.cost = 1;
		}
		newState.accCost = this.accCost + newState.cost;

		for (RoleNode or : newState.rolesTree) {
			// if all assigned goals are same, so it found the same role of the method
			// argument 'role'
			if (or.getAssignedGoals().containsAll(role.getAssignedGoals())
					&& role.getAssignedGoals().containsAll(or.getAssignedGoals())) {
				if (!or.getAssignedGoals().contains(goalToBeAssociatedToRole)) {
					or.assignGoal(goalToBeAssociatedToRole);
					suc.add(newState);
					if (role.getParent() != null)
						LOG.info("joinASubordinate : " + newState.toString() + ", nSucc: " + newState.goalSuccessors
								+ ", Name: " + role.getRoleName() + ", Parent: " + role.getParent().getRoleName()
								+ ", Hash: " + newState.hashCode());
					else
						LOG.info("joinASubordinate : " + newState.toString() + ", nSucc: " + newState.goalSuccessors
								+ ", Name: " + role.getRoleName() + ", Parent: null, Hash: " + newState.hashCode());
					break;
				}
			}
		}
	}

	/** Lista de antecessores, para busca bidirecional */
	public List<Estado> antecessores() {
		return sucessores();
	}

	public String toString() {
		List<String> signature = new ArrayList<>();

		if ((this.rolesTree != null) && (!this.rolesTree.isEmpty())) {
			Iterator<RoleNode> iterator = this.rolesTree.iterator();
			while (iterator.hasNext()) {
				RoleNode n = iterator.next();
				signature.add(n.toString());
			}
		}

		Collections.sort(signature);

		return signature.toString();
	}

	/**
	 * Verifica se um estado eh igual a outro ja inserido na lista de sucessores
	 * (usado para poda)
	 */
	public boolean equals(Object o) {
		try {
			if (o instanceof Organisation) {
				if (this.toString().equals(((Organisation) o).toString())) {
					LOG.info("Pruned" + this.toString());
					return true;
				}
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * retorna o hashCode desse estado (usado para poda, conjunto de fechados)
	 */

	public int hashCode() {
		if (this.rolesTree != null)
			return this.toString().hashCode();
		else
			return -1;
	}

	/**
	 * Custo acumulado g
	 */
	public int custoAcumulado() {
		return accCost;
	}

	public Organisation createState(GoalNode gn) {

		// new state
		Organisation newState = new Organisation(gn);
		// Copy all roles tree
		for (RoleNode or : this.rolesTree) {
			RoleNode nnewS = (RoleNode) or.clone();
			newState.rolesTree.add((RoleNode) nnewS);
		}
		// finding right parents of cloned roles in the new tree
		for (RoleNode or : newState.rolesTree) {
			if (or.getParent() != null) {
				for (RoleNode pr : newState.rolesTree) {
					// in case of a joining the parent may have more goals, so the list does not
					// need to match exactly
					if (pr.getAssignedGoals().containsAll(or.getParent().getAssignedGoals())) {
						or.setParent(pr);
						break;
					}
				}
			}
		}

		// Add all successors of current state but not the new state itself - list of
		// goals does not need to be cloned because does not change
		for (GoalNode goal : this.goalSuccessors) {
			if (goal != gn)
				newState.goalSuccessors.add(goal);
		}

		return newState;
	}

}
