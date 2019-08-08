package organizational;

import java.io.BufferedWriter;
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
	
	// Cost supporting variables
	private int cost = 0;
	private int accCost = 0;
	
	/**
	 * 1: unitary cost (no function)
	 * 2: based on flat cost (flatter structures are more expensive)
	 * 3: based on specializationCost (joinAPair increments this cost making the organisation more generalist) 
	 */
	static int costFunction = 3;

	public String getDescricao() {
		return "Empty\n";
	}

	public Organization(GoalNode gn, int costFunction) {
		this(gn);
		
		Organization.costFunction = costFunction;
	}

	public Organization(GoalNode gn) {

		if (gn.getParent() == null) {
			for (GoalNode goal : gn.getSuccessors())
				this.goalSuccessors.add(goal);

			String roleName = "r"+this.rolesTree.size();
			RoleNode r = new RoleNode(null, roleName);
			r.assignGoal(gn);
			this.rolesTree.add(r);
			LOG.info("FIRST STATE CREATED: " + this.toString() + " | " + this.hashCode());
		}
	}

	public boolean ehMeta() {

		if (this.goalSuccessors.size() <= 0) 
		{
			if (!isGoalList.contains(this)) {
				isGoalList.add(this);
				LOG.info("GOAL ACHIEVED! Solution: #" + isGoalList.size() + " : " + this.toString() + " | " + this.hashCode());

				List<String> links = new ArrayList<>();

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
						
						if (or.getParent() != null)
							links.add("\""+or.getParent().getRoleName() + "\"->\"" + or.getRoleName()+"\"");
					}

					for (String s : links)
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
		if (costFunction == 3) {
			LOG.debug("cost: " + cost + " accCost: " + accCost);
			return cost;
		} else {
			// default cost function is unitary (any openning has same cost)
			return 1; 
		}
	}

	/** Lista de sucessores */
	public List<Estado> sucessores() {
		List<Estado> suc = new LinkedList<Estado>(); // Lista de sucessores

		if (!this.goalSuccessors.isEmpty())
			LOG.debug("\nSTATE: " + this.toString() + "Tree:" + this.rolesTree.toString() + " - OpenGoals: [" + goalSuccessors.toString() + "] - Size: "
					+ goalSuccessors.size() + ", Hash: " + this.hashCode());

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

		newState.cost = 1;
		newState.accCost = this.accCost + newState.cost;

		RoleNode r = new RoleNode(parentRole, "r"+newState.rolesTree.size());
		r.assignGoal(goalToBeAssociatedToRole);
		// Copy all skills of the goal to this new role
		for (String skill : goalToBeAssociatedToRole.getSkills()) r.addSkill(skill);
		
		newState.rolesTree.add(r);
		
		suc.add(newState);

		LOG.debug("addSubordinate	: " + newState.toString() + ", nSucc: " + newState.goalSuccessors + ", Name: " + r.getRoleName() + ", Parent: " + r.getParent().getRoleName() + ", Hash: " + newState.hashCode());
	}
	
	public void joinAPair(RoleNode role, List<Estado> suc, GoalNode goalToBeAssociatedToRole) {

		Organization newState = (Organization) createState(goalToBeAssociatedToRole);

		// this organization is being compressed in few divisions, so division cost increased
		newState.cost = 100;
		newState.accCost = this.accCost + newState.cost;

		// the new role is also assigned to a new goal (the joined one)
		for (RoleNode or : newState.rolesTree) {
			// if all assigned goals are same, so it found the same role of the method argument 'role'
			if (or.getAssignedGoals().containsAll(role.getAssignedGoals()) && role.getAssignedGoals().containsAll(or.getAssignedGoals())) {
				if (!or.getAssignedGoals().contains(goalToBeAssociatedToRole)) {
					or.assignGoal(goalToBeAssociatedToRole);
					suc.add(newState);
					LOG.debug("joinAPair     	: " + newState.toString() + ", nSucc: " + newState.goalSuccessors + ", Name: " + role.getRoleName() + ", Parent: " + role.getParent().getRoleName() + ", Hash: " + newState.hashCode());
					break;
				}
			}
		}
	}

	public void joinASubordinate(RoleNode role, List<Estado> suc, GoalNode goalToBeAssociatedToRole) {

		Organization newState = (Organization) createState(goalToBeAssociatedToRole);
		
		newState.cost = 10;
		newState.accCost = this.accCost + newState.cost;

		for (RoleNode or : newState.rolesTree) {
			// if all assigned goals are same, so it found the same role of the method argument 'role'
			if (or.getAssignedGoals().containsAll(role.getAssignedGoals()) && role.getAssignedGoals().containsAll(or.getAssignedGoals())) {
				if (!or.getAssignedGoals().contains(goalToBeAssociatedToRole)) {
					or.assignGoal(goalToBeAssociatedToRole);
					suc.add(newState);
					if (role.getParent() != null)
						LOG.debug("joinASubordinate : " + newState.toString() + ", nSucc: " + newState.goalSuccessors + ", Name: " + role.getRoleName() + ", Parent: " + role.getParent().getRoleName() + ", Hash: " + newState.hashCode());
					else
						LOG.debug("joinASubordinate : " + newState.toString() + ", nSucc: " + newState.goalSuccessors + ", Name: " + role.getRoleName() + ", Parent: null, Hash: " + newState.hashCode());
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
		return signature.toString() + " TreeSize: "+signature.size();	
	}

	/**
	 * Verifica se um estado eh igual a outro ja inserido na lista de sucessores
	 * (usado para poda)
	 */
	public boolean equals(Object o) {
		try {
			if (o instanceof Organization) {
				if (this.toString().equals(((Organization) o).toString())) {
					LOG.debug("Pruned" + this.toString());
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
	
	public Organization createState(GoalNode gn) {

		// new state
		Organization newState = new Organization(gn);
		// Copy all roles tree
		for (RoleNode or : this.rolesTree) {
			RoleNode nnewS = (RoleNode) or.clone();  			
			newState.rolesTree.add((RoleNode) nnewS);
		}
		// finding right parents of cloned roles in the new tree
		for (RoleNode or : newState.rolesTree) {
			if (or.getParent() != null) {
				for (RoleNode pr : newState.rolesTree) {
					// in case of a joining the parent may have more goals, so the list does not need to match exactly
					if (pr.getAssignedGoals().containsAll(or.getParent().getAssignedGoals())) {
						or.setParent(pr);
						break;
					}
				}
			}
		}
		
		// Add all successors of current state but not the new state itself - list of goals does not need to be cloned because does not change
		for (GoalNode goal : this.goalSuccessors) {
			if (goal != gn) newState.goalSuccessors.add(goal);
		}
		
	    return newState;
	}
	
}
