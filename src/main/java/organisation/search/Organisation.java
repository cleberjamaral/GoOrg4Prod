package organisation.search;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
import simplelogger.SimpleLogger;

public class Organisation implements Estado, Antecessor {

	/*** STATIC ***/
	private static SimpleLogger LOG = SimpleLogger.getInstance();
	// list of target states, i.e., complete charts
	private static List<Organisation> isGoalList = new ArrayList<Organisation>();
	// Cost penalty used to infer bad decisions on search
	private static CostResolver penalty;
	// Number of generated states
	private static int generatedStates = 0;
	// Number of generated states
	private static int prunedStates = 0;
	// a reference to the goals tree used by all states (static to save memory)
	private static GoalTree goalsTree;
	
	/*** LOCAL ***/
	// the chart that is being created, potentially a complete chart
	// private List<RoleNode> rolesTree = new ArrayList<RoleNode>();
	RoleTree rolesTree = new RoleTree();
	// The goals that were not explored yet
	private List<GoalNode> goalSuccessors = new ArrayList<GoalNode>();
	// Cost supporting variables
	private int cost = 0;
	private int accCost = 0;
	// any name for an organisation
	private String orgName;

	public String getDescricao() {
		return "Empty\n";
	}

	public String getOrgName() {
		return this.orgName;
	}

	private Organisation() {
		generatedStates++;
	}

	public Organisation(String orgName, GoalTree gt, Cost costFunction) {
		this.orgName = orgName;
		createOrganisation(gt, costFunction);
	}

	private void createOrganisation(GoalTree gt, Cost costFunction) {
		// If it is the first state that is going to be created
		generatedStates++;

		goalsTree = gt;
		goalsTree.brakeGoalTree();
		goalsTree.addSuccessorsToList(goalSuccessors, goalsTree.getRootNode());

		RoleNode root = this.rolesTree.createRole(null, "r" + this.rolesTree.size(), goalsTree.getRootNode());

		// Used to infer a bad decision on the search
		Parameters.setDefaultPenalty(this.goalSuccessors.size() + 1);
		penalty = new CostResolver(costFunction);

		logTransformation("rootRole", this, root);
	}

	public boolean ehMeta() {
		boolean oneSolutionNeeded = true;
		
		if (this.goalSuccessors.size() <= 0) {
			
			if (!isGoalList.contains(this)) {
				isGoalList.add(this);
				LOG.info("#(" + generatedStates + "/" + prunedStates + ") Solution #" + isGoalList.size() + ", "
						+ this.toString() + ", Hash: " + this.hashCode() + ", Cost: " + this.accCost + "/" + this.cost);
		
				OrganisationPlot p = new OrganisationPlot();
				if (oneSolutionNeeded) {
					p.plotOrganisation(this, "");
					isGoalList.clear();
				} else {
					p.plotOrganisation(this, Integer.toString(isGoalList.size()));
				}
			} else {
				LOG.debug("#(" + generatedStates + "/" + prunedStates + ") Duplicated solution!" + ", Hash: "
						+ this.hashCode());
			}
			return oneSolutionNeeded;
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

			RoleNode nr = newState.rolesTree.createRole(newState.rolesTree.findRoleBySignature(aGivenRole.signature()),
					"r" + newState.rolesTree.size(), goalToAssign);
			
			// Prune states with effort equal to 0
			if (nr.getSumWorkload() == 0) {
				LOG.debug("#(" + generatedStates + "/" + ++prunedStates + ") addRole pruned#1: " + nr.getAssignedGoals()
						+ ", efforts: " + nr.getSumWorkload() + " = 0");
				return;
			}

			// Prune states which parent cannot afford data amount 
			if (nr.getParentSumDataAmount() > Parameters.getMaxDataLoad()) {
				LOG.debug("#(" + generatedStates + "/" + ++prunedStates + ") addRole pruned#2: " + nr.getAssignedGoals()
						+ ", amount: " + nr.getParentSumDataAmount() + " > " + Parameters.getMaxDataLoad());
				return;
			}

			newState.cost = penalty.getAddRolePenalty(aGivenRole, goalToAssign, this.getRolesTree(), newState.getRolesTree());
			newState.accCost = this.accCost + newState.cost;

			suc.add(newState);

			logTransformation("addRole", newState, nr);

		} catch (RoleNotFound e) {
			LOG.fatal("Fatal error on addRole! " + e.getMessage());
		}
	}

	public void joinRole(RoleNode hostRole, List<Estado> suc, GoalNode goalToAssign) {

		try {
			Organisation newState = (Organisation) createState(goalToAssign);

			RoleNode jr = newState.rolesTree.assignGoalToRoleBySignature(hostRole.signature(), goalToAssign);

			// Prune states with effort greater than max
			if (jr.getSumWorkload() > Parameters.getMaxWorkload()) {
				LOG.debug("#(" + generatedStates + "/" + ++prunedStates + ") joinRole pruned#1: " + jr.getSumWorkload()
						+ " > " + Parameters.getMaxWorkload());
				return;
			}

			// Prune states which parent cannot afford data amount 
			if (jr.getParentSumDataAmount() > Parameters.getMaxDataLoad()) {
				LOG.debug("#(" + generatedStates + "/" + ++prunedStates + ") joinRole pruned#2: " + jr.getAssignedGoals()
						+ ", amount: " + jr.getParentSumDataAmount() + " > " + Parameters.getMaxDataLoad());
				return;
			}

			newState.cost = penalty.getJoinRolePenalty(hostRole, goalToAssign, this.getRolesTree(), newState.getRolesTree());
			newState.accCost = this.accCost + newState.cost;

			logTransformation("joinRole", newState, jr);

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
		return getOrgName() + rolesTree.toString();
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
		if (rolesTree != null)
			return toString().hashCode();
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
		newState.orgName = getOrgName();
		try {
			newState.rolesTree = rolesTree.cloneContent();

			// Add all successors of current state but not the new state itself
			// list of goals does not need to be cloned because does not change
			for (GoalNode goal : goalSuccessors) {
				if (goal != gn)
					newState.goalSuccessors.add(goal);
			}
		} catch (DuplicatedRootRole | RoleNotFound e) {
			e.printStackTrace();
		}

		return newState;
	}

	public RoleTree getRolesTree() {
		return rolesTree;
	}

	private void logTransformation(String transformation, Organisation state, RoleNode role) {
		String parent = "__";
		if (role.getParent() != null)
			parent = role.getParent().getRoleName();
		LOG.trace("#(" + generatedStates + "/" + prunedStates + ") " + transformation + ": " + role.getRoleName() + "^"
				+ parent + " " + state.rolesTree + ", nSucc: " + state.goalSuccessors + ", Hash: " + state.hashCode()
				+ ", Cost: " + state.accCost + "/" + state.cost);
	}

}
