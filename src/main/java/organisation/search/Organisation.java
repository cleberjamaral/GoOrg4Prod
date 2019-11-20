package organisation.search;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import busca.Antecessor;
import busca.Estado;
import organisation.OrganisationPlot;
import organisation.exception.DuplicatedRootRole;
import organisation.exception.OutputDoesNotMatchWithInput;
import organisation.exception.RoleNotFound;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.role.RoleNode;
import organisation.role.RoleTree;
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
	// a reference to the goals tree used by all states (static to save memory)
	private static GoalTree goalsTree;
	// static Cost costFunction = Cost.SPECIALIST;
	static Cost costFunction = Cost.SPECIALIST;

	/*** LOCAL ***/
	// the chart that is being created, potentially a complete chart
	// private List<RoleNode> rolesTree = new ArrayList<RoleNode>();
	RoleTree rolesTree = new RoleTree();
	// The goals that were not explored yet
	private List<GoalNode> goalSuccessors = new ArrayList<GoalNode>();
	// Cost supporting variables
	private int cost = 0;
	private int accCost = 0;

	public String getDescricao() {
		return "Empty\n";
	}

	private Organisation() {
		generatedStates++;
	}
	
	public Organisation(GoalTree gt, Cost costFunction, boolean removeOldDiagrams) {
		createOrganisation(gt, removeOldDiagrams);

		Organisation.costFunction = costFunction;
	}

	public Organisation(GoalTree gt, Cost costFunction, List<Object> limits) {
		for (Object w : limits) {
			if (w instanceof Workload) {
				Organisation.maxEffort = ((Workload) w).getEffort();
				createOrganisation(gt, true);
			}
		}

		Organisation.costFunction = costFunction;
	}

	private void createOrganisation(GoalTree gt, boolean removeOldDiagrams) {
		// If it is the first state that is going to be created
		generatedStates++;
		
		goalsTree = gt;
		goalsTree.addAllDescendants(goalsTree.getRootNode());

		goalsTree.getBrokenGoalTree(Organisation.maxEffort);
		goalsTree.addSuccessorsToList(goalSuccessors, goalsTree.getRootNode());

		OrganisationPlot p = new OrganisationPlot();
		if (removeOldDiagrams)
			p.deleteExistingDiagrams();
		p.plotOrganizationalGoalTree(goalsTree.getRootNode());

		this.rolesTree.createRole(null, "r" + this.rolesTree.size(), goalsTree.getRootNode());

		// Used to infer a bad decision on the search
		Organisation.costPenalty = this.goalSuccessors.size() + 1;

		LOG.debug("#(" + generatedStates + "/" + prunedStates + ") FIRST STATE: " + this.toString() + " | "
				+ this.hashCode() + " | Cost penalty: " + Organisation.costPenalty);
	}

	public boolean ehMeta() {
		if (this.goalSuccessors.size() <= 0) {
			if (!isGoalList.contains(this)) {
				isGoalList.add(this);
				LOG.info("#(" + generatedStates + "/" + prunedStates + ") Solution #" + isGoalList.size() + ", "
						+ this.toString() + ", Hash: " + this.hashCode() + ", Cost: " + this.accCost + "/" + this.cost);

				OrganisationPlot p = new OrganisationPlot();
				p.plotOrganisation(this, Integer.toString(isGoalList.size()), false);
			} else {
				LOG.debug("#(" + generatedStates + "/" + prunedStates + ") Duplicated solution!" + ", Hash: "
						+ this.hashCode());
			}
			return true; // true: if only one solution is needed
		}
		return false;
	}

	public boolean validateOutput() throws OutputDoesNotMatchWithInput {
		if (Math.round(goalsTree.sumEfforts()) != Math.round(rolesTree.sumEfforts())) {
			throw new OutputDoesNotMatchWithInput(
					"The sum of efforts of the goals tree must match with the sum of efforts of the created roles tree! GoalsTree:"
							+ goalsTree.sumEfforts() + " RolesTree:" + rolesTree.sumEfforts());
		}
		return true;
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
			for (RoleNode role : rolesTree.getTree()) {
				addRole(role, suc, goalToBeAssociated);
				joinRole(role, suc, goalToBeAssociated);
			}
		}

		return suc;
	}

	public void addRole(RoleNode aGivenRole, List<Estado> suc, GoalNode goalToAssign) {

		try {
			Organisation newState = (Organisation) createState(goalToAssign);

			// the given role has goal's parent associated?
			if (aGivenRole.hasParentGoal(goalToAssign)) {
				newState.cost = 1;
			} else if (aGivenRole.hasSiblingGoal(goalToAssign)) {
				// a sibling has a little higher cost because by default aGivenRole will be
				// parent
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
			if ((costFunction == Cost.TALLER) && (!aGivenRole.hasParentGoal(goalToAssign))) {
				newState.cost += Organisation.costPenalty;
			}

			newState.accCost = this.accCost + newState.cost;

			RoleNode nr = newState.rolesTree.createRole(newState.rolesTree.findRoleBySignature(aGivenRole.signature()),
					"r" + newState.rolesTree.size(), goalToAssign);

			// Prune states with effort equal to 0
			if (nr.getSumWorkload() == 0) {
				LOG.debug("#(" + generatedStates + "/" + ++prunedStates + ") addRole pruned: " + nr.getAssignedGoals()
						+ ", efforts: " + nr.getSumWorkload() + " = 0");
				newState = null;
				return;
			}

			suc.add(newState);

			LOG.trace("#(" + generatedStates + "/" + prunedStates + ") addRole  : " + nr.getRoleName() + "^"
					+ nr.getParent().getRoleName() + " " + newState.rolesTree + ", nSucc: " + newState.goalSuccessors
					+ ", Hash: " + newState.hashCode() + ", Cost: " + newState.accCost + "/" + newState.cost);

		} catch (RoleNotFound e) {
			LOG.fatal("Fatal error on addRole! " + e.getMessage());
		}
	}

	public void joinRole(RoleNode hostRole, List<Estado> suc, GoalNode goalToAssign) {

		try {
			Organisation newState = (Organisation) createState(goalToAssign);

			// the given role has goal's parent associated?
			if (hostRole.hasParentGoal(goalToAssign)) {
				newState.cost = 1;
			} else if (hostRole.hasSiblingGoal(goalToAssign)) {
				// TODO: extra cost?
				newState.cost = 2;
			} else {
				// Apart from the cost function, punish association of goals with no kinship
				newState.cost = Organisation.costPenalty;
			}

			// High punishment when it is preferred taller and the role is not a child
			if (((costFunction == Cost.TALLER) && (!hostRole.hasParentGoal(goalToAssign)))
					// Punish when it is preferred more specialist structures
					|| (costFunction == Cost.SPECIALIST)) {
				newState.cost += Organisation.costPenalty * 2;
			} else
			// Low punishment when is preferred taller but is child
			if (costFunction == Cost.TALLER) {
				newState.cost += Organisation.costPenalty;
			}
			newState.accCost = this.accCost + newState.cost;

			RoleNode jr = newState.rolesTree.assignGoalToRoleBySignature(hostRole.signature(), goalToAssign);

			// Prune states with effort greater than max
			if (jr.getSumWorkload() > Organisation.maxEffort) {
				LOG.debug("#(" + generatedStates + "/" + ++prunedStates + ") joinRole pruned: " + jr.getSumWorkload() + " > "
						+ Organisation.maxEffort);
				newState = null;
				return;
			}

			if (jr.getParent() != null)
				LOG.trace("#(" + generatedStates + "/" + prunedStates + ") joinRole : " + jr.getRoleName() + "^"
						+ jr.getParent().getRoleName() + " " + newState.rolesTree + ", nSucc: "
						+ newState.goalSuccessors + ", Hash: " + newState.hashCode() + ", Cost: " + newState.accCost
						+ "/" + newState.cost);
			else
				LOG.trace("#(" + generatedStates + "/" + prunedStates + ") joinRole : " + jr.getRoleName() + "^__ "
						+ newState.rolesTree + ", nSucc: " + newState.goalSuccessors + ", Hash: " + newState.hashCode()
						+ ", Cost: " + newState.accCost + "/" + newState.cost);

			suc.add(newState);
		} catch (RoleNotFound e) {
			LOG.fatal("Fatal error on joinRole! " + e.getMessage());
		}
	}

	/** Lista de antecessores, para busca bidirecional */
	public List<Estado> antecessores() {
		return sucessores();
	}

	public String toString() {
		return rolesTree.toString();
	}

	/**
	 * Verifica se um estado eh igual a outro ja inserido na lista de sucessores
	 * (usado para poda)
	 */
	public boolean equals(Object o) {
		try {
			if (o instanceof Organisation) {
				if (this.toString().equals(((Organisation) o).toString())) {
					LOG.debug("#(" + generatedStates + "/" + ++prunedStates + ") Pruned" + this.toString() + ", Hash: "
							+ o.hashCode());
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

		Organisation newState = new Organisation();
		try {
			newState.rolesTree = this.rolesTree.cloneContent();

			// Add all successors of current state but not the new state itself
			// list of goals does not need to be cloned because does not change
			for (GoalNode goal : this.goalSuccessors) {
				if (goal != gn)
					newState.goalSuccessors.add(goal);
			}
		} catch (DuplicatedRootRole | RoleNotFound e) {
			e.printStackTrace();
		}

		return newState;
	}

	public Set<RoleNode> getRolesTree() {
		return rolesTree.getTree();
	}

}
