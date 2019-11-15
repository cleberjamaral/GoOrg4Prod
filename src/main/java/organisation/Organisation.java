package organisation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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

	public List<RoleNode> getRolesTree() {
		return rolesTree;
	}

	public Organisation(GoalNode gn, Cost costFunction) {
		createOrganisation(gn, 8);

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
			GoalTree t = new GoalTree(gn);
			GoalNode newRoot = t.getBrokenGoalTree(maxEffort);
			addAllGoalsSuccessors(newRoot);
			
			OrganisationPlot p = new OrganisationPlot();
			p.deleteExistingDiagrams();
			p.plotOrganizationalGoalTree(newRoot);

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

}
