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

public class Organization implements Estado, Antecessor {

	/*** STATIC ***/
	private static SimpleLogger LOG = SimpleLogger.getInstance(1);
	// list of target states, i.e., complete charts
	private static List<Organization> isGoalList = new ArrayList<Organization>();

	/*** LOCAL ***/
	// this is the chart that is being created by the algorithm, potentially a complete chart
	private List<RoleNode> rolesTree = new ArrayList<RoleNode>();
	// The goals that were not explored yet, the algorithm end when all goals were allocated into roles
	private List<GoalNode> goalSuccessors = new ArrayList<GoalNode>();
	// graphlinks is a list because it is used as hash and it allows multiple identical links when joining function is used
	private List<String> graphLinks = new ArrayList<String>();
	
	// the orgRoleId is a reference, like an index for this state (node of the orgchart), other goals can be assigned besides the head
	GoalNode orgRoleId;
	// orgRoleIdLinks is a graph connecting ids, which leads to unique structures allowing to prune similar ones
	private List<String> orgRoleIdLinks = new ArrayList<String>();
	
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

	public Organization(GoalNode gn, int costFunction) {
		this(gn);
		
		Organization.costFunction = costFunction;
	}

	public Organization(GoalNode gn) {
		orgRoleId = gn;

		if (gn.getParent() == null) {
			for (GoalNode goal : gn.getSuccessors())
				goalSuccessors.add(goal);

			String roleName = "r"+this.rolesTree.size();
			RoleNode r = new RoleNode(null, roleName);
			r.assignGoal(gn);
			rolesTree.add(r);
		}
	}

	public boolean ehMeta() {

		if (goalSuccessors.size() <= 0) 
		{
			if (!isGoalList.contains(this)) {
				isGoalList.add(this);
				LOG.info("GOAL ACHIEVED! Solution: #" + isGoalList.size() + " : " + this.rolesTree + " : " + this.graphLinks + " | " + this.hashCode());

				try (FileWriter fw = new FileWriter("graph_" + isGoalList.size() + ".gv", false);
						BufferedWriter bw = new BufferedWriter(fw);
						PrintWriter out = new PrintWriter(bw)) {
					out.println("digraph G {");
					for (RoleNode or : rolesTree) {
						out.print("\t\"" + or.getRoleName()//headGoal.getGoalName()
								+ "\" [ style = \"filled\" fillcolor = \"white\" fontname = \"Courier New\" "
								+ "shape = \"Mrecord\" label = <<table border=\"0\" cellborder=\"0\" bgcolor=\"white\">"
								+ "<tr><td bgcolor=\"black\" align=\"center\"><font color=\"white\">"
								+ or.getRoleName() + "</font></td></tr><tr><td align=\"center\">" + or.getAssignedGoals() + "</td></tr>");
						for (String s : or.getSkills())
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
				LOG.warn("Goal achieved but duplicated!" + " : " + this.hashCode());
				//return true; // if only one solution is needed
			}
		}
		return false;
	}

	public int custo() {
		if (costFunction == 2) {
			//LOG.debug("flatCost: " + flatCost);
			return flatCost;
		}
		else if (costFunction == 3) {
			//LOG.debug("divisionalCost: " + divisionalCost);
			return divisionalCost;
		} else {
			// default cost function is unitary (any openning has same cost)
			return 1; 
		}
	}

	/** Lista de sucessores */
	public List<Estado> sucessores() {
		List<Estado> suc = new LinkedList<Estado>(); // Lista de sucessores

		if (!goalSuccessors.isEmpty())
			LOG.debug("\nSUCESSORES: Tree: " + this.rolesTree + " - OpenGoals: [" + goalSuccessors.toString() + "] - Size: "
					+ goalSuccessors.size() + ", Hash: " + this.hashCode() + ", Str: " + this.toString());

		// add all children as possible successors
		for (GoalNode goalToBeAssociated : goalSuccessors) {
			// add all children as possible successors
			for (RoleNode role : rolesTree) {
				// if one of assigned goals is parent, so open it as a child
				if (role.getAssignedGoals().contains(goalToBeAssociated.getParent())) {
					// creating successors, create a subordinate role (child)
					addSubordinate(role, suc, goalToBeAssociated);
					// creating successors, join goals of a pair
					if ((role.getSkills().containsAll(goalToBeAssociated.getSkills())) || 
						(goalToBeAssociated.getSkills().isEmpty())) {
							joinASubordinate(role, suc, goalToBeAssociated);
					} 
				} else if ((role.getParent() != null) && (role.getParent().getAssignedGoals().contains(goalToBeAssociated.getParent()))) {
					// creating successors, create a pair role (sibling)
					//addPair(role.getParent(), suc, goalToBeAssociated);

					// creating successors, join goals of a pair
					if (((role.getSkills().containsAll(goalToBeAssociated.getSkills())
							) || (goalToBeAssociated.getSkills().isEmpty()))) {
						joinAPair(role, suc, goalToBeAssociated);
					} 
				}
			}
		}

		return suc;
	}

	public void addSubordinate(RoleNode parentRole, List<Estado> suc, GoalNode goalToBeAssociatedToRole) {

		Organization newState = (Organization) createState(goalToBeAssociatedToRole);

		RoleNode r = new RoleNode(parentRole, "r"+newState.rolesTree.size());
		r.assignGoal(goalToBeAssociatedToRole);
		newState.rolesTree.add(r);
		
		newState.orgRoleIdLinks.add("\""+parentRole.getRoleName() + "\"->\"" + goalToBeAssociatedToRole.getGoalName()+"\"");
		newState.graphLinks.add("\""+parentRole.getRoleName() + "\"->\"" + r.getRoleName()+"\"");

		suc.add(newState);

		LOG.debug("addSubordinate	: " + newState.rolesTree + ", nSucc: " + newState.goalSuccessors + ", Name: " + r.getRoleName() + ", Parent: " + r.getParent().getRoleName() + ", Hash: " + newState.hashCode() + ", Str: " + newState.toString());
	}

	public void addPair(RoleNode parentRole, List<Estado> suc, GoalNode goalToBeAssociatedToRole) {

		Organization newState = (Organization) createState(goalToBeAssociatedToRole);
		RoleNode r = new RoleNode(parentRole, "r"+newState.rolesTree.size());
		r.assignGoal(goalToBeAssociatedToRole);
		newState.rolesTree.add(r);
		
		newState.orgRoleIdLinks.add("\""+parentRole.getRoleName() + "\"->\"" + goalToBeAssociatedToRole.getGoalName()+"\"");
		newState.graphLinks.add("\""+parentRole.getRoleName() + "\"->\"" + r.getRoleName()+"\"");

		suc.add(newState);

		LOG.debug("addPair       	: " + newState.rolesTree + ", nSucc: " + newState.goalSuccessors + ", Name: " + r.getRoleName() + ", Parent: " + r.getParent().getRoleName() + ", Hash: " + newState.hashCode() + ", Str: " + newState.toString());
	}
	
	public void joinAPair(RoleNode role, List<Estado> suc, GoalNode goalToBeAssociatedToRole) {

		Organization newState = (Organization) createState(goalToBeAssociatedToRole);
		
		// this organization is being compressed in few divisions, so division cost increased
		newState.divisionalCost = this.divisionalCost + 1;

		// the new role is also assigned to a new goal (the joined one)
		for (RoleNode or : newState.rolesTree) {
			if (or.getAssignedGoals().containsAll(role.getAssignedGoals())) {
				if (!or.getAssignedGoals().contains(goalToBeAssociatedToRole)) or.assignGoal(goalToBeAssociatedToRole);
				// create a link which is same as another existing, in fact it will only change the hashcode of this state
				newState.orgRoleIdLinks.add("\""+role.getParent().getRoleName() + "\"->\"" + or.getRoleName()+"\"");
				newState.graphLinks.add("\""+role.getParent().getRoleName() + "\"->\"" + role.getRoleName()+"\"");
				break;
			}
		}

		suc.add(newState);

		LOG.debug("joinAPair     	: " + newState.rolesTree + ", nSucc: " + newState.goalSuccessors + ", Name: " + role.getRoleName() + ", Parent: " + role.getParent().getRoleName() + ", Hash: " + newState.hashCode() + ", Str: " + newState.toString());
	}

	public void joinASubordinate(RoleNode role, List<Estado> suc, GoalNode goalToBeAssociatedToRole) {

		Organization newState = (Organization) createState(goalToBeAssociatedToRole);
		
		for (RoleNode or : newState.rolesTree) {
			if (or.getAssignedGoals().containsAll(role.getAssignedGoals())) {
				if (!role.getAssignedGoals().contains(goalToBeAssociatedToRole)) {
					or.assignGoal(goalToBeAssociatedToRole);
				}
				// create a link which is same as another existing, in fact it will only change the hashcode of this state
				newState.orgRoleIdLinks.add("\""+role.getRoleName() + "\"->\"" + role.getRoleName()+"\"");
				newState.graphLinks.add("\""+role.getRoleName() + "\"->\"" + role.getRoleName()+"\"");
			}
		}

		suc.add(newState);

		LOG.debug("joinASubordinate : " + newState.rolesTree + ", nSucc: " + newState.goalSuccessors + ", Name: " + role.getRoleName() + ", Hash: " + newState.hashCode() + ", Str: " + newState.toString());
	}
	
	/** Lista de antecessores, para busca bidirecional */
	public List<Estado> antecessores() {
		return sucessores();
	}

	public String toString() {
		String r = "{Roles{";
		if ((this.rolesTree != null) && (!this.rolesTree.isEmpty())) {
			for (int i = 0; i < this.rolesTree.size(); i++) {
				r += this.rolesTree.get(i).getRoleName();
				r += this.rolesTree.get(i).getAssignedGoals();
			}
		}
		r += "}";
		if ((this.graphLinks != null) && (!this.graphLinks.isEmpty())) r += " Links" + this.graphLinks + "";
		r += "} ";
		return r;
		
	}

	/**
	 * Verifica se um estado eh igual a outro ja inserido na lista de sucessores
	 * (usado para poda)
	 */
	public boolean equals(Object o) {
		try {
			if (o instanceof Organization) {
				Collections.sort(this.orgRoleIdLinks);
				Collections.sort(((Organization) o).orgRoleIdLinks);
				if (this.orgRoleIdLinks.equals(((Organization) o).orgRoleIdLinks)) {
					LOG.debug("Pruned" + this.orgRoleIdLinks + " - " + ((Organization) o).orgRoleIdLinks);
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
		if ((this.graphLinks != null) && (this.rolesTree != null))
			return this.toString().hashCode();
		else
			return -1;
	}

	/**
	 * Custo acumulado g
	 */
	public int custoAcumulado() {
		return 0; 
	}
	
	public Organization createState(GoalNode gn) {

		// new state
		Organization newState = new Organization(gn);
		// Copy all graph links tree
		for (String s : this.graphLinks) newState.graphLinks.add(s);
		// Copy all roles tree
		for (RoleNode or : rolesTree) {
			RoleNode nnewS = (RoleNode) or.clone();  			
			newState.rolesTree.add((RoleNode) nnewS);
		}
		// Add all successors of current state but not the new state itself - list of goals does not need to be cloned because does not change
		for (GoalNode goal : this.goalSuccessors) {
			if (goal != gn) newState.goalSuccessors.add(goal);
		}
		
	    return newState;
	}
	
}
