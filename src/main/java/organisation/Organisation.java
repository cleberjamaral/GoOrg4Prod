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

import org.apache.commons.io.FileUtils;

import busca.Antecessor;
import busca.Estado;
import properties.Workload;
import simplelogger.SimpleLogger;

public class Organisation implements Estado, Antecessor {

	/*** STATIC ***/
	private static SimpleLogger LOG = SimpleLogger.getInstance();
	// list of target states, i.e., complete charts
	private static List<Organisation> isGoalList = new ArrayList<Organisation>();
	// Cost penalty used to infer bad decisions on search
	private static int costPenalty = 1;
	// Number of generated states
	private static int generatedStates = 0;
	// Number of generated states
	private static int prunedStates = 0;
	
	/*** LOCAL ***/
	// the chart that is being created, potentially a complete chart
	private List<RoleNode> rolesTree = new ArrayList<RoleNode>();
	// The goals that were not explored yet
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

	public Organisation(GoalNode gn, Cost costFunction, List<Object> limits) {
		for (Object w : limits) {
			if (w instanceof Workload) {
				createOrganisation(gn,((Workload)w).getEffort());
			}
		}

		Organisation.costFunction = costFunction;
	}

	public Organisation(GoalNode gn) {
		createOrganisation(gn, 8);
	}

	private void createOrganisation(GoalNode gn, double maxEffort) {
		// If it is the first state that is going to be created
		generatedStates++;
		if (gn.getParent() == null) {
			GoalNode newRoot = gn.cloneContent();
			brakeGoalTree(gn, newRoot, maxEffort);
			
			plotOrganizationalGoalTree(newRoot);
			
			addAllGoalsSuccessors(newRoot);

			String roleName = "r" + this.rolesTree.size();
			RoleNode r = new RoleNode(null, roleName);
			r.assignGoal(newRoot);
			for (Object requirement : newRoot.getRequirements())
				r.addRequirement(((Workload)requirement).clone());
			this.rolesTree.add(r);

			// Used to infer a bad decision on the search
			Organisation.costPenalty = this.goalSuccessors.size() + 1;

			LOG.debug("#(" + generatedStates + "/" + prunedStates + ") FIRST STATE: " + this.toString() + " | "
					+ this.hashCode() + " | Cost penalty: " + Organisation.costPenalty);
		}
	}

	private static void brakeGoalTree(GoalNode original, GoalNode parent, double maxEffort) {
		original.getDescendents().forEach(s -> {
			if (!s.containsWorkload()) {
				GoalNode g = s.cloneContent();
				g.setParent(parent);
				brakeGoalTree(s, g, maxEffort);
			} else {
				// get the biggest effort and divide all workloads by the limit
				double sumEfforts = 0;
				for (Object w : s.getRequirements()) {
					if (w instanceof Workload) {
						sumEfforts += ((Workload) w).getEffort();
					}
				}

				int slices = (int) Math.ceil(sumEfforts / maxEffort);
				if (slices == 0)
					slices = 1;
				for (int i = 1; i <= slices; i++) {
					GoalNode g = s.cloneContent();
					g.setParent(parent);
					if (slices > 1)
						g.setGoalName(g.getGoalName() + "$" + i);
					for (Object w : g.getRequirements()) {

						if ((w instanceof Workload) && (slices > 1)) {
							((Workload) w).setEffort(((Workload) w).getEffort() / slices);
						}
					}
					if (i == slices)
						brakeGoalTree(s, g, maxEffort);
				}

			}
		});
	}

	private void addAllGoalsSuccessors(GoalNode gn) {
		for (GoalNode goal : gn.getDescendents()) {
			this.goalSuccessors.add(goal);
			addAllGoalsSuccessors(goal);
		}
	}

	public boolean ehMeta() {

		if (this.goalSuccessors.size() <= 0) {
			if (!isGoalList.contains(this)) {
				isGoalList.add(this);
				LOG.info("#(" + generatedStates + "/" + prunedStates + ") Solution #" + isGoalList.size() + ", "
						+ this.toString() + ", Hash: " + this.hashCode() + ", Cost: " + this.accCost + "/"
						+ this.cost);

				plotOrganisation(isGoalList.size(), false);
			} else {
				LOG.debug("#(" + generatedStates + "/" + prunedStates + ") Duplicated solution!" + ", Hash: " + this.hashCode());
			}
			return true; // true: if only one solution is needed
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
			//TODO: build the roles tree as a nodes tree like in goals node?
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

	private static void plotOrganizationalGoalTree(GoalNode gn) {
		try {
			File filepath = new File("output/diagrams");
			FileUtils.deleteDirectory(filepath);

			File file = new File("output/diagrams/tmp");
			file.getParentFile().mkdirs();
		} catch (IOException e) {}

		try (FileWriter fw = new FileWriter("output/diagrams/gdt.gv", false);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {

			out.println("digraph G {");
			plotGoalNode(out, gn);

			out.println("}");
		} catch (IOException e) {
		}
	}

	private static void plotGoalNode(PrintWriter out, GoalNode or) {
		if (or.getOperator().equals("parallel")) {
			out.print("\t\"" + or.getGoalName()
					+ "\" [ style = \"filled\" fillcolor = \"white\" fontname = \"Courier New\" "
					+ "shape = \"diamond\" label = <<table border=\"0\" cellborder=\"0\">"
					+ "<tr><td align=\"center\"><font color=\"black\"><b>" + or.getGoalName()
					+ "</b></font></td></tr>");
		} else {
			out.print("\t\"" + or.getGoalName()
					+ "\" [ style = \"filled\" fillcolor = \"white\" fontname = \"Courier New\" "
					+ "shape = \"ellipse\" label = <<table border=\"0\" cellborder=\"0\">"
					+ "<tr><td align=\"center\"><b>" + or.getGoalName() + "</b></td></tr>");
		}
		for (Object s : or.getRequirements())
			out.print("<tr><td align=\"left\"><sub><i>" + s + "</i></sub></td></tr>");
		out.println("</table>> ];");
		or.getDescendents().forEach(g -> {
			plotGoalNode(out, g);
			if (g.getParent() != null)
				out.println("\t\"" + g.getParent().getGoalName() + "\"->\"" + g.getGoalName() + "\";");

		});
	}
	
	public int custo() {
		return cost;
	}

	/** Lista de sucessores */
	public List<Estado> sucessores() {
		List<Estado> suc = new LinkedList<Estado>(); // Lista de sucessores

		if (!this.goalSuccessors.isEmpty())
			LOG.debug("#(" + generatedStates + "/" + prunedStates + ") STATE: " + this.toString() + " - Open: ["
					+ goalSuccessors.toString() + "] - Size: " + goalSuccessors.size() + ", Hash: " + this.hashCode());

		// add all possible successors
		for (GoalNode goalToBeAssociated : goalSuccessors) {
			// add all children as possible successors
			for (RoleNode role : rolesTree) {
				addRole(role, suc, goalToBeAssociated);
				joinRole(role, suc, goalToBeAssociated);
			}
		}

		return suc;
	}

	public void addRole(RoleNode aGivenRole, List<Estado> suc, GoalNode goalToBeAssociatedToRole) {

		Organisation newState = (Organisation) createState(goalToBeAssociatedToRole);

		// the given role has goal's parent associated?
		if (aGivenRole.getAssignedGoals().contains(goalToBeAssociatedToRole.getParent())) {
			newState.cost = 1;
		} else if ((aGivenRole.getParent() != null)
				&& (aGivenRole.getParent().getAssignedGoals().contains(goalToBeAssociatedToRole.getParent()))) {
			// a sibling has a little higher cost because by default aGivenROle will be parent
			newState.cost = 2;
		} else {
			// Apart from the cost function, punish association of goals with no kinship
			newState.cost = Organisation.costPenalty;
		}
		
		// Punish when for instance it is preferred more generalist and flatter structures
		if ((costFunction == Cost.FLATTER) || (costFunction == Cost.GENERALIST)) {
			newState.cost += Organisation.costPenalty * 2;
		}
		newState.accCost = this.accCost + newState.cost;

		for (RoleNode or : newState.rolesTree) {
			if (or.equals(aGivenRole)) {
				RoleNode r = new RoleNode(or, "r" + newState.rolesTree.size());
				r.assignGoal(goalToBeAssociatedToRole);
				// Copy all requirements of the goal to this new role
				for (Object requirement : goalToBeAssociatedToRole.getRequirements())
					r.addRequirement(((Workload)requirement).clone());

				newState.rolesTree.add(r);

				suc.add(newState);
				
				if (or != null)
					LOG.warn("#(" + generatedStates + "/" + prunedStates + ") addRole  : " + r.getRoleName() + "^"
							+ r.getParent().getRoleName() + " " + newState.rolesTree + ", nSucc: "
							+ newState.goalSuccessors + ", Hash: " + newState.hashCode() + ", Cost: " + newState.accCost
							+ "/" + newState.cost);
				break;
			}
		}

	}

	public void joinRole(RoleNode hostRole, List<Estado> suc, GoalNode goalToBeAssociatedToRole) {

		Organisation newState = (Organisation) createState(goalToBeAssociatedToRole);

		// the given role has goal's parent associated?
		if (hostRole.getAssignedGoals().contains(goalToBeAssociatedToRole.getParent())) {
			newState.cost = 1;
		} else if ((hostRole.getParent() != null)
				&& (hostRole.getParent().getAssignedGoals().contains(goalToBeAssociatedToRole.getParent()))) {
			//TODO: extra cost?
			newState.cost = 2;
		} else {
			// Apart from the cost function, punish association of goals with no kinship
			newState.cost = Organisation.costPenalty;
		}
		
		// Punish when for instance it is preferred more specialist and taller structures
		if ((costFunction == Cost.TALLER) || (costFunction == Cost.SPECIALIST)) {
			newState.cost += Organisation.costPenalty * 2;
		}
		newState.accCost = this.accCost + newState.cost;

		for (RoleNode or : newState.rolesTree) {
			if (or.equals(hostRole)) {
				or.assignGoal(goalToBeAssociatedToRole);
				// Copy all requirements of the goal to this new role
				for (Object requirement : goalToBeAssociatedToRole.getRequirements())
					or.addRequirement(((Workload) requirement).clone());
				suc.add(newState);
				if (or.getParent() != null)
					LOG.trace("#(" + generatedStates + "/" + prunedStates + ") joinRole : " + or.getRoleName() + "^"
							+ or.getParent().getRoleName() + " " + newState.rolesTree + ", nSucc: "
							+ newState.goalSuccessors + ", Hash: " + newState.hashCode() + ", Cost: " + newState.accCost
							+ "/" + newState.cost);
				else
					LOG.trace("#(" + generatedStates + "/" + prunedStates + ") joinRole : " + or.getRoleName() + "^__ "
							+ newState.rolesTree + ", nSucc: " + newState.goalSuccessors + ", Hash: "
							+ newState.hashCode() + ", Cost: " + newState.accCost + "/" + newState.cost);

				break;
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
					LOG.debug("#(" + generatedStates + "/" + ++prunedStates + ") Pruned" + this.toString() + ", Hash: " + o.hashCode());
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
					if (pr.equals(or.getParent())) {
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
