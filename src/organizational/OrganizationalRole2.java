package organizational;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import busca.Antecessor;
import busca.Estado;

import simplelogger.SimpleLogger;

public class OrganizationalRole2 implements Estado, Antecessor {

	private static SimpleLogger LOG = SimpleLogger.getInstance(1);

	private static List<OrganizationalRole2> isGoalList = new ArrayList<OrganizationalRole2>();
	private static List<OrganizationalRole2> rolesList = new ArrayList<OrganizationalRole2>();

	GoalNode headGoal;
	private List<GoalNode> goalSuccessors = new ArrayList<GoalNode>();
	
	//State description
	private OrganizationalRole2 roleParent;
	private Set<String> roleSkills = new HashSet<String>();
	//graphlinks is a list because it is used as hash and it allows multiple identical links when joining function is used
	private List<String> graphLinks = new ArrayList<String>();
	
	//Cost supporting variables
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

	public OrganizationalRole2(GoalNode gn, int costFunction) {
		this(gn);
		
		OrganizationalRole2.costFunction = costFunction;
	}

	public OrganizationalRole2(GoalNode gn) {
		headGoal = gn;

		if (gn.getParent() == null) {
			for (GoalNode goal : headGoal.getSuccessors())
				goalSuccessors.add(goal);
			rolesList.add(this);
		}
	}

	public boolean ehMeta() {

		if (goalSuccessors.size() <= 0) 
		{
			if (!isGoalList.contains(this)) {
				isGoalList.add(this);
				LOG.info("GOAL ACHIEVED! Solution: #" + isGoalList.size());

				try (FileWriter fw = new FileWriter("graph_" + isGoalList.size() + ".gv", false);
						BufferedWriter bw = new BufferedWriter(fw);
						PrintWriter out = new PrintWriter(bw)) {
					out.println("digraph G {");
					for (OrganizationalRole2 or : rolesList) {
						out.print("\t\"" + or.headGoal.getGoalName()
								+ "\" [ style = \"filled\" fillcolor = \"white\" fontname = \"Courier New\" "
								+ "shape = \"Mrecord\" label = <<table border=\"0\" cellborder=\"0\" bgcolor=\"white\">"
								+ "<tr><td bgcolor=\"black\" align=\"center\"><font color=\"white\">"
								+ or.headGoal.getGoalName() + "</font></td></tr>");
						for (String s : or.roleSkills)
							out.print("<tr><td align=\"left\">" + s + "</td></tr>");
						out.println("</table>> ];");
					}

					for (String s : this.graphLinks)
						out.println("\t" + s + ";");
					out.println("}");
				} catch (IOException e) {
				}
			} else {
				// This should not happen again, it has occurred because searching process
				// (deepth) was calling ehMeta 2 times
				LOG.warn("Goal achieved but duplicated!");
			}
		}
		return false;
	}

	public OrganizationalRole2 getRootRole(OrganizationalRole2 node) {
		if (node.roleParent == null)
			return node;
		else
			return getRootRole(node.roleParent);
	}

	public int custo() {
		if (costFunction == 2) {
			LOG.debug("flatCost: " + flatCost);
			return flatCost;
		}
		else if (costFunction == 3) {
			LOG.debug("divisionalCost: " + divisionalCost);
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
					+ headGoal.getSuccessors().size());

		if (!goalSuccessors.isEmpty()) {
			GoalNode goalToBeAssociated = goalSuccessors.get(0);
			
			//open a successor
			if (this.headGoal.equals(goalToBeAssociated.getParent())) {
				//the successor is a child node
				addRelation(suc, goalToBeAssociated);
			} else if ((this.headGoal.getParent() != null) //parent node cannot have siblings
					&& (this.headGoal.getParent().equals(goalToBeAssociated.getParent())
					)) { 
				//the successor is a sibling node
				System.out.println("$$$$ "+this.headGoal.getParent());
				
				// if successor needs same skills of its sibling, so join it
				if ((this.roleSkills.containsAll(goalToBeAssociated.getSkills())
						&& !goalToBeAssociated.getParent().getOperator().equals("parallel"))
						|| (goalToBeAssociated.getSkills().isEmpty())) {
					joinAnother(suc, goalToBeAssociated);
				} else {
					addRelation(suc, goalToBeAssociated);
				}
			} else {
				// the successor is any other node
				System.out.println("XXXX " + this.headGoal.getParent());
				addRelation(suc, goalToBeAssociated);
			}
		}

		return suc;
	}

	public void addRelation(List<Estado> suc, GoalNode goalToBeAssociatedToRole) {
		OrganizationalRole2 newRole = new OrganizationalRole2(goalToBeAssociatedToRole);

		// Copy all skills of the goal to this new role
		for (String skill : goalToBeAssociatedToRole.getSkills())
			newRole.roleSkills.add(skill);

		newRole.roleParent = this;
		for (String s : this.graphLinks)
			newRole.graphLinks.add(s);

		// The new role is a child of the current state (current role)
		OrganizationalRole2 parentOR = null;
		for (OrganizationalRole2 or : rolesList)
			if (or.headGoal == goalToBeAssociatedToRole.getParent()) {
				parentOR = or;
				break;
			}
		newRole.graphLinks.add("\""+parentOR.headGoal.getGoalName() + "\"->\"" + newRole.headGoal.getGoalName()+"\"");
		
		// Add all successors of current state but not the new state itself
		for (GoalNode goal : this.goalSuccessors) {
			if (goal != newRole.headGoal)
				newRole.goalSuccessors.add(goal);
		}

		String logg;
		logg = "\taddRelation: " + newRole.toString() + ", Links: [ ";
		for (String s : newRole.graphLinks)
			logg += s + " ";
		logg += "], nSucc: " + newRole.goalSuccessors.size();
		LOG.debug(logg);

		suc.add(newRole);

		boolean isNew = true;
		for (OrganizationalRole2 or : rolesList)
			if (or.headGoal == newRole.headGoal) {
				isNew = false;
				break;
			}
		if (isNew) {
			rolesList.add(newRole);
		}
	}

	public void joinAnother(List<Estado> suc, GoalNode goalToBeAssociatedToRole) {
		// Creates a new state which is the same role but with another equal link (just
		// to make it different)
		OrganizationalRole2 newRole = new OrganizationalRole2(goalToBeAssociatedToRole);
		for (String skill : goalToBeAssociatedToRole.getSkills())
			newRole.roleSkills.add(skill);

		// this organization is being compressed in few divisions, so division cost
		// increased
		newRole.divisionalCost = this.divisionalCost + 1;

		newRole.roleParent = this.roleParent;
		for (String s : this.graphLinks)
			newRole.graphLinks.add(s);
		// create link between goal's parent and the already existing equivalent role
		//newRole.graphLinks.add(goalToBeAssociatedToRole.getParent().getGoalName() + "->" + this.headGoal.getGoalName());
		newRole.graphLinks.add("\""+goalToBeAssociatedToRole.getParent().getGoalName() + "\"->\"" + this.headGoal.getGoalName()+"\"");
		
		for (GoalNode goal : this.goalSuccessors) {
			if (goal != newRole.headGoal)
				newRole.goalSuccessors.add(goal);
		}

		String logg;
		logg = "\tjoinAnother: " + newRole.toString() + ", Links: [ ";
		for (String s : newRole.graphLinks)
			logg += s + " ";
		logg += "], nSucc: " + newRole.goalSuccessors.size();
		LOG.debug(logg);

		suc.add(newRole);

		boolean isNew = true;
		for (OrganizationalRole2 or : rolesList)
			if (or.headGoal == newRole.headGoal) {
				isNew = false;
				break;
			}
		if (isNew)
			rolesList.add(newRole);
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
	 * Verifica se um estado � igual a outro j� inserido na lista de sucessores
	 * (usado para poda)
	 */
	public boolean equals(Object o) {
		try {
			if (o instanceof OrganizationalRole2) {
				return this.graphLinks.equals(((OrganizationalRole2) o).graphLinks);
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
