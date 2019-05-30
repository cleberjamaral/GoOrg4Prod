package organizational;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import busca.Antecessor;
import busca.Estado;

import simplelogger.SimpleLogger;

public class OrganizationalRole3 implements Estado, Antecessor {

	/*** STATIC ***/
	private static SimpleLogger LOG = SimpleLogger.getInstance(1);
	// list of target states, i.e., complete charts
	private static List<OrganizationalRole3> isGoalList = new ArrayList<OrganizationalRole3>();

	/*** LOCAL ***/
	// this is the chart that is being created by the algorithm, potentially a complete chart
	private List<OrganizationalRole3> rolesTree = new ArrayList<OrganizationalRole3>();
	// The goals that were not explored yet, the algorithm end when all goals were allocated into roles
	private List<GoalNode> goalSuccessors = new ArrayList<GoalNode>();
	
	// the headGoal is a reference, like an index for this state (node of the orgchart), other goals can be assigned besides the head
	GoalNode headGoal;
	// list of goals that this role is assigned to achieve
	private List<GoalNode> assignedGoals = new ArrayList<GoalNode>();
	
	// State description
	private OrganizationalRole3 parentRole;
	private Set<String> roleSkills = new HashSet<String>();
	// graphlinks is a list because it is used as hash and it allows multiple identical links when joining function is used
	private List<String> graphLinks = new ArrayList<String>();
	
	// Cost supporting variables
	private int flatCost = 0;
	private int divisionalCost = 0;
	
	/**
	 * 1: unitary cost (no function)
	 * 2: based on flat cost (flatter structures are more expensive)
	 * 3: based on divisional cost (fewer divisions means the divisions are more populated and expensive) 
	 */
	static int costFunction = 1;

	public String getDescricao() {
		return "Empty\n";
	}

	public OrganizationalRole3(GoalNode gn, int costFunction) {
		this(gn);
		
		OrganizationalRole3.costFunction = costFunction;
	}

	public OrganizationalRole3(GoalNode gn) {
		headGoal = gn;
		assignedGoals.add(gn);

		if (gn.getParent() == null) {
			for (GoalNode goal : gn.getSuccessors())
				goalSuccessors.add(goal);

			rolesTree.add(this);
		}
	}

	public boolean ehMeta() {

		if (goalSuccessors.size() <= 0) 
		{
			if (!isGoalList.contains(this)) {
				isGoalList.add(this);
				LOG.info("GOAL ACHIEVED! Solution: #" + isGoalList.size() + " : " + this.rolesTree + " : " + this.assignedGoals);

				try (FileWriter fw = new FileWriter("graph_" + isGoalList.size() + ".gv", false);
						BufferedWriter bw = new BufferedWriter(fw);
						PrintWriter out = new PrintWriter(bw)) {
					out.println("digraph G {");
					for (OrganizationalRole3 or : rolesTree) {
						out.print("\t\"" + or.headGoal.getGoalName()
								+ "\" [ style = \"filled\" fillcolor = \"white\" fontname = \"Courier New\" "
								+ "shape = \"Mrecord\" label = <<table border=\"0\" cellborder=\"0\" bgcolor=\"white\">"
								+ "<tr><td bgcolor=\"black\" align=\"center\"><font color=\"white\">"
								+ or.assignedGoals + "</font></td></tr>");
						for (String s : or.roleSkills)
							out.print("<tr><td align=\"left\">" + s + "</td></tr>");
						out.println("</table>> ];");
					}

					for (String s : this.graphLinks)
						out.println("\t" + s + ";");
					out.println("}");
				} catch (IOException e) {
				}
				
				//return true; // if only one solution is needed
			} else {
				// This should not happen again, it has occurred because searching process
				// (deepth) was calling ehMeta 2 times
				LOG.warn("Goal achieved but duplicated!");
				//return true; // if only one solution is needed
			}
		}
		return false;
	}

	public OrganizationalRole3 getRootRole(OrganizationalRole3 node) {
		if (node.parentRole == null)
			return node;
		else
			return getRootRole(node.parentRole);
	}

	public int custo() {
		if (costFunction == 2) {
			//LOG.debug("flatCost: " + flatCost);
			return flatCost;
		}
		else if (costFunction == 3) {
			//LOG.debug("divisionalCost: " + divisionalCost);
			return divisionalCost;
		}
		else {
			// default cost function is unitary (any openning has same cost)
			return 1; 
		}
	}

	/** Lista de sucessores */
	public List<Estado> sucessores() {
		List<Estado> suc = new LinkedList<Estado>(); // Lista de sucessores

		if (!goalSuccessors.isEmpty())
			LOG.debug("\nCURRENT HEAD GOAL DATA: " + this.toString() + " - OpenGoals: [" + goalSuccessors.toString() + "] - Size: "
					+ goalSuccessors.size());

		// add all children as possible successors
		for (GoalNode goalToBeAssociated : goalSuccessors) {
			
			// if one of assigned goals is parent, so open it as a child
			if (this.assignedGoals.contains(goalToBeAssociated.getParent())) {
				// creating successors, no matter what creating a separated role is always a possible solution
				addSubordinate(suc, goalToBeAssociated);
			}
		
			// A role with assigned goals that match to the parent of the goal to be
			// assigned, may have siblings
			// Note that earlier joining processes may made a role a "step parent" of many
			// other goals since it can be concentrating many goals
			if ((this.parentRole != null) && (this.parentRole.assignedGoals.contains(goalToBeAssociated.getParent()))) {
				// creating successors, no matter what creating a separated role is always a
				// possible solution
				addPair(suc, goalToBeAssociated);
			}
			
			//  if it is not the rootnode and parent is same, they are pairs and can be joined
			if ((this.parentRole != null) && (this.parentRole.assignedGoals.contains(goalToBeAssociated.getParent()))) {
				// creating successors, if parent is same any skills match, join this goal to an existing role
				if (((this.roleSkills.containsAll(goalToBeAssociated.getSkills())
						// && !goalToBeAssociated.getParent().getOperator().equals("parallel") //does it
						// makes sense?
						) || (goalToBeAssociated.getSkills().isEmpty()))) {
					joinAPair(suc, goalToBeAssociated);
				} 
			}
		}

		return suc;
	}

	public void addSubordinate(List<Estado> suc, GoalNode goalToBeAssociatedToRole) {
		OrganizationalRole3 newState = new OrganizationalRole3(goalToBeAssociatedToRole);

		// Copy all skills of the goal to this new role
		for (String skill : goalToBeAssociatedToRole.getSkills()) newState.roleSkills.add(skill);
		// Make this state bwing parent of the new one and copy current links
		newState.parentRole = this;
		// Copy all roles tree
		for (OrganizationalRole3 or : this.rolesTree) newState.rolesTree.add(or);
		// Copy all graph links tree
		for (String s : this.graphLinks) newState.graphLinks.add(s);

		newState.graphLinks.add("\""+this.headGoal.getGoalName() + "\"->\"" + newState.headGoal.getGoalName()+"\"");
		
		// Add all successors of current state but not the new state itself
		for (GoalNode goal : this.goalSuccessors) {
			if (goal != newState.headGoal)
				newState.goalSuccessors.add(goal);
		}

		suc.add(newState);
		newState.rolesTree.add(newState);

		LOG.debug("addSubordinate: " + newState.toString() + ", Roles: " + newState.rolesTree + ", Links: " + newState.graphLinks + ", nSucc: " + newState.goalSuccessors.size());
	}

	public void addPair(List<Estado> suc, GoalNode goalToBeAssociatedToRole) {
		OrganizationalRole3 newState = new OrganizationalRole3(goalToBeAssociatedToRole);

		// Copy all skills of the goal to this new role
		for (String skill : goalToBeAssociatedToRole.getSkills()) newState.roleSkills.add(skill);
		// Make the parent of this state being parent of the new one (they are siblings) and copy current links
		newState.parentRole = this.parentRole;
		// Copy all roles tree
		for (OrganizationalRole3 or : rolesTree) newState.rolesTree.add(or);
		// Copy all graph links tree
		for (String s : this.graphLinks) newState.graphLinks.add(s);
		
		newState.graphLinks.add("\""+this.headGoal.getParent().getGoalName() + "\"->\"" + newState.headGoal.getGoalName()+"\"");
		
		// Add all successors of current state but not the new state itself
		for (GoalNode goal : this.goalSuccessors) {
			if (goal != newState.headGoal)
				newState.goalSuccessors.add(goal);
		}

		suc.add(newState);
		newState.rolesTree.add(newState);

		LOG.debug("addPair       : " + newState.toString() + ", Roles: " + newState.rolesTree + ", Links: " + newState.graphLinks + ", nSucc: " + newState.goalSuccessors.size());
	}
	public void joinAPair(List<Estado> suc, GoalNode goalToBeAssociatedToRole) {
		// Creates a new state which is the same role but with another equal link (just
		// to make it different)
		OrganizationalRole3 newState = new OrganizationalRole3(goalToBeAssociatedToRole);
		// Copy all skills of the goal to this new role
		for (String skill : goalToBeAssociatedToRole.getSkills()) newState.roleSkills.add(skill);
		// Make the parent of this state being parent of the new one (they are siblings) and copy current links
		newState.parentRole = this.parentRole;
		// Copy all roles tree
		for (OrganizationalRole3 or : rolesTree) newState.rolesTree.add(or);
		// Copy all graph links tree
		for (String s : this.graphLinks) newState.graphLinks.add(s);
		
		// this organization is being compressed in few divisions, so division cost
		// increased
		newState.divisionalCost = this.divisionalCost + 1;

		// create link between goal's parent and the already existing equivalent role
		//newRole.graphLinks.add(goalToBeAssociatedToRole.getParent().getGoalName() + "->" + this.headGoal.getGoalName());
		// create a link which is same as another existing, in fact it will only change the hashcode of this state
		newState.graphLinks.add("\""+goalToBeAssociatedToRole.getParent().getGoalName() + "\"->\"" + this.headGoal.getGoalName()+"\"");
//		newRole.graphLinks.add("\""+this.headGoal.getParent().getGoalName() + "\"->\"" + newRole.headGoal.getGoalName()+"\"");
	
		for (GoalNode goal : this.goalSuccessors) {
			if (goal != newState.headGoal)
				newState.goalSuccessors.add(goal);
		}

		suc.add(newState);
		
		// adding just to show that this role was joined
		newState.rolesTree.add(newState);
		
//		// the new role is also assigned to a new goal (the joined one)
//		for (OrganizationalRole3 or : newState.rolesTree) {
//			if (or.assignedGoals.contains(this.headGoal)) {
//				or.assignedGoals.add(newState.headGoal);
//				LOG.debug("assignedGoals: " + or.assignedGoals + " - " + or.graphLinks + " - " + newState.rolesTree
//						+ " - " + or.headGoal);
//				break;
//			}
//		}

		LOG.debug("joinAPair     : " + newState.toString() + ", Roles: " + newState.rolesTree + ", Links: " + newState.graphLinks + ", nSucc: " + newState.goalSuccessors.size());
	}

	/** Lista de antecessores, para busca bidirecional */
	public List<Estado> antecessores() {
		return sucessores();
	}

	public String toString() {
		String r = "";
		r += headGoal.toString();
		if (!this.roleSkills.isEmpty())
			r += "[" + this.roleSkills.toString() + "]";

		return r;
	}

	/**
	 * Verifica se um estado eh igual a outro ja inserido na lista de sucessores
	 * (usado para poda)
	 */
	public boolean equals(Object o) {
		try {
			if (o instanceof OrganizationalRole3) {
				Collections.sort(this.graphLinks);
				Collections.sort(((OrganizationalRole3) o).graphLinks);
				if (this.graphLinks.equals(((OrganizationalRole3) o).graphLinks)) {
					LOG.debug("Pruned" + this.graphLinks + " - " + ((OrganizationalRole3) o).graphLinks);
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
		return this.graphLinks.hashCode();
	}

	/**
	 * Custo acumulado g
	 */
	public int custoAcumulado() {
//		if (costFunction == 2) {
//			int cost = 0;
//			OrganizationalRole2 countParent = this;
//			while (countParent.headGoal != null) {
//				countParent = countParent.roleParent;
//				cost++;
//			}
//			LOG.debug("\tACC-flatCost: " + cost);
//			return cost;
//		}
//		else if (costFunction == 3) {
//			LOG.debug("\tdivisionalCost: " + divisionalCost);
//			return divisionalCost;
//		}
//		else {
//			// default cost function is unitary (any opening has same cost)
			return 0; 
//		}
	}
}
