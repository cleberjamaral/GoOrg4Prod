package organisation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
	// max effort per role
	private static double maxEffort = 8;
	
	/*** LOCAL ***/
	// the chart that is being created, potentially a complete chart
	//private List<RoleNode> rolesTree = new ArrayList<RoleNode>();
	RoleTree rolesTree = new RoleTree();

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
		createOrganisation(gn);

		Organisation.costFunction = costFunction;
	}

	public Organisation(GoalNode gn, Cost costFunction, List<Object> limits) {
		for (Object w : limits) {
			if (w instanceof Workload) {
				Organisation.maxEffort = ((Workload)w).getEffort();
				createOrganisation(gn);
			}
		}

		Organisation.costFunction = costFunction;
	}

	public Organisation(GoalNode gn) {
		createOrganisation(gn);
	}

	private void createOrganisation(GoalNode gn) {
		// If it is the first state that is going to be created
		generatedStates++;
		if (gn.getParent() == null) {
			GoalTree t = new GoalTree(gn);
			GoalNode newRoot = t.getBrokenGoalTree(Organisation.maxEffort);
			addAllGoalsSuccessors(newRoot);
			
			OrganisationPlot p = new OrganisationPlot();
			p.deleteExistingDiagrams();
			p.plotOrganizationalGoalTree(newRoot);

			String roleName = "r" + this.rolesTree.size();
			RoleNode r = new RoleNode(null, roleName);
			r.assignGoal(newRoot);
			for (Object requirement : newRoot.getRequirements())
				r.addWorkload(((Workload)requirement).clone());
			this.rolesTree.add(r);

			// Used to infer a bad decision on the search
			Organisation.costPenalty = this.goalSuccessors.size() + 1;

			LOG.debug("#(" + generatedStates + "/" + prunedStates + ") FIRST STATE: " + this.toString() + " | "
					+ this.hashCode() + " | Cost penalty: " + Organisation.costPenalty);
		}
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

				OrganisationPlot p = new OrganisationPlot();
				p.plotOrganisation(this, isGoalList.size(), false);
			} else {
				LOG.debug("#(" + generatedStates + "/" + prunedStates + ") Duplicated solution!" + ", Hash: " + this.hashCode());
			}
			return true; // true: if only one solution is needed
		}
		return false;
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
			for (RoleNode role : rolesTree.tree()) {
				addRole(role, suc, goalToBeAssociated);
				joinRole(role, suc, goalToBeAssociated);
			}
		}

		return suc;
	}

	public void addRole(RoleNode aGivenRole, List<Estado> suc, GoalNode goalToBeAssociatedToRole) {

		Organisation newState = (Organisation) createState(goalToBeAssociatedToRole);

		// the given role has goal's parent associated?
		if (aGivenRole.hasParentGoal(goalToBeAssociatedToRole)) {
			newState.cost = 1;
		} else if (aGivenRole.hasSiblingGoal(goalToBeAssociatedToRole)) {
			// a sibling has a little higher cost because by default aGivenROle will be parent
			newState.cost = 2;
		} else {
			// Apart from the cost function, punish association of goals with no kinship
			newState.cost = Organisation.costPenalty;
		}
		
		// High punishment when it is preferred more generalist and flatter structures
		if ((costFunction == Cost.FLATTER) || (costFunction == Cost.GENERALIST)) {
			newState.cost += Organisation.costPenalty * 2;
		} else
		// Low punishment when is preferred taller but is not child 
		if ((costFunction == Cost.TALLER) && (!aGivenRole.hasParentGoal(goalToBeAssociatedToRole))) {
				newState.cost += Organisation.costPenalty;
		} 
		
		newState.accCost = this.accCost + newState.cost;

		//TODO: create and start using RoleTree facilities
		for (RoleNode or : newState.rolesTree.tree()) {
			if (or.equals(aGivenRole)) {
				RoleNode r = new RoleNode(or, "r" + newState.rolesTree.size());
				r.assignGoal(goalToBeAssociatedToRole);
				// Copy all requirements of the goal to this new role
				for (Object requirement : goalToBeAssociatedToRole.getRequirements())
					r.addWorkload(((Workload)requirement).clone());

				// Prune states with effort equal to 0
				double sumEfforts = 0;
				for (Object requirement : r.getRequirements()) {
					if (requirement instanceof Workload) {
						sumEfforts += ((Workload) requirement).getEffort();
					}
				}
				if (sumEfforts == 0) {
					LOG.debug("#(" + generatedStates + "/" + ++prunedStates + ") addRole pruned: "
							+ r.getAssignedGoals() + ", efforts: " + sumEfforts + " = 0");
					newState = null;
					return;
				}

				newState.rolesTree.add(r);

				suc.add(newState);
				
				if (or != null)
					LOG.trace("#(" + generatedStates + "/" + prunedStates + ") addRole  : " + r.getRoleName() + "^"
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
		if (hostRole.hasParentGoal(goalToBeAssociatedToRole)) {
			newState.cost = 1;
		} else if (hostRole.hasSiblingGoal(goalToBeAssociatedToRole)) {
			//TODO: extra cost?
			newState.cost = 2;
		} else {
			// Apart from the cost function, punish association of goals with no kinship
			newState.cost = Organisation.costPenalty;
		}
		
		// High punishment when it is preferred taller and the role is not a child
		if (((costFunction == Cost.TALLER) && (!hostRole.hasParentGoal(goalToBeAssociatedToRole)))
				// Punish when it is preferred more specialist structures
				|| (costFunction == Cost.SPECIALIST)) {
			newState.cost += Organisation.costPenalty * 2;
		} else
		// Low punishment when is preferred taller but is child
		if (costFunction == Cost.TALLER) {
			newState.cost += Organisation.costPenalty;
		}
		newState.accCost = this.accCost + newState.cost;

		//TODO: create and start using RoleTree facilities
		for (RoleNode or : newState.rolesTree.tree()) {
			if (or.equals(hostRole)) {
				or.assignGoal(goalToBeAssociatedToRole);
				
				// Copy all requirements of the goal to this new role
				for (Object requirement : goalToBeAssociatedToRole.getRequirements()) {
					or.addWorkload(((Workload) requirement).clone());
				}

				// Prune states with effort greater than max
				double sumEfforts = 0;
				for (Object requirement : or.getRequirements()) {
					if (requirement instanceof Workload) {
						sumEfforts += ((Workload) requirement).getEffort();
					}
				}
				if (sumEfforts > Organisation.maxEffort) {
					LOG.debug("#(" + generatedStates + "/" + ++prunedStates + ") joinRole pruned: " + sumEfforts + " > "
							+ Organisation.maxEffort);
					newState = null;
					return;
				}

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
		return rolesTree.getSignature();
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

		//TODO: make a RoleTree and clone it, it is too confusing making it by nodes
		// new state
		Organisation newState = new Organisation(gn);
		// Copy all roles tree
		for (RoleNode or : this.rolesTree.tree()) {
			RoleNode nnewS = (RoleNode) or.clone();
			newState.rolesTree.add((RoleNode) nnewS);
		}
		// finding right parents of cloned roles in the new tree
		for (RoleNode or : newState.rolesTree.tree()) {
			if (or.getParent() != null) {
				for (RoleNode pr : newState.rolesTree.tree()) {
					// In a joining case the list of goals sometimes does not match
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

	public Set<RoleNode> getRolesTree() {
		return rolesTree.tree();
	}

}
