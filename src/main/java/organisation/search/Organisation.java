package organisation.search;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import busca.Antecessor;
import busca.Estado;
import organisation.OrganisationPlot;
import organisation.OrganisationStatistics;
import organisation.Parameters;
import organisation.exception.DuplicatedRootRole;
import organisation.exception.OutputDoesNotMatchWithInput;
import organisation.exception.RoleNotFound;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.role.RoleNode;
import organisation.role.RoleTree;
import organisation.search.cost.Cost;
import organisation.search.cost.CostResolver;
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
	// stop algorithm after finding the first solution
	private static boolean oneSolution = true;
	// any name for an organisation
	private static String orgName;
	
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

	public String getOrgName() {
		return orgName;
	}

	/**
	 * This constructor is used on every new state
	 */
	private Organisation() {
		generatedStates++;
	}

	/**
	 * This constructor should be used only once for generating the root role
	 * and setup the algorithm
	 * 
	 * @param orgName is an arbitrary name for this organisation
	 * @param gTree the goal tree, supposed to be a broken tree ready to process
	 * @param costFunction the desired cost function
	 */
	public Organisation(String orgName, GoalTree gTree, Cost costFunction, Boolean oneSolution) {
		Organisation.orgName = orgName;
		Organisation.oneSolution = oneSolution;
		Organisation.generatedStates = 0;

		goalsTree = gTree;
		this.goalSuccessors.add(goalsTree.getRootNode());
		goalsTree.addSuccessorsToList(this.goalSuccessors, goalsTree.getRootNode());
		
		// Used to infer a bad decision on the search
		Parameters.setDefaultPenalty(this.goalSuccessors.size() + 1);
		penalty = new CostResolver(costFunction);
	}

	public boolean ehMeta() {
		if (this.goalSuccessors.size() <= 0) {
			
			if (!isGoalList.contains(this)) {
				isGoalList.add(this);
				LOG.info("#(" + generatedStates + "/" + prunedStates + ") Solution #" + isGoalList.size() + ", "
						+ this.toString() + ", Hash: " + this.hashCode() + ", Cost: " + this.accCost + "/" + this.cost);
		
				OrganisationPlot p = new OrganisationPlot();
				OrganisationStatistics s = OrganisationStatistics.getInstance();
				if (oneSolution) {
					isGoalList.clear();
					
                    final String dot = p.plotOrganisation(this, "");
        			p.saveDotAsPNG(this.getOrgName(), dot);

                    s.saveOnStatistics(this);
                    
                    return true;
				} else {
                    p.plotOrganisation(this, Integer.toString(isGoalList.size()));
					
					s.saveOnStatistics(this);

					return false;
				}
			} else {
				LOG.debug("#(" + generatedStates + "/" + prunedStates + ") Duplicated solution!" + ", Hash: "
						+ this.hashCode());
			}
		}
		return false;
	}

	public boolean validateOutput() throws OutputDoesNotMatchWithInput {
		
		matchSumWorkload();
		
		checkNumberOfWorkloads();

		matchNumberOfGoals();
		
		return true;
	}

	private void matchSumWorkload() throws OutputDoesNotMatchWithInput {
		// checking if sum of efforts match
		if (Math.abs(goalsTree.getSumEfforts() - rolesTree.getSumWorkload()) > 0.01) {
			throw new OutputDoesNotMatchWithInput(
					"The sum of efforts of the goals tree and the created organisation does not match!");
		}
	}

	private void checkNumberOfWorkloads() throws OutputDoesNotMatchWithInput {
		// number of workloads must be equal or lower (similar workloads can be joined)
		int goalsTreeNumberOfWorkloads = 0;
		for (GoalNode g : goalsTree.getTree())
			goalsTreeNumberOfWorkloads += g.getWorkloads().size();
		int organisationNumberOfWorkloads = 0;
		for (RoleNode r : rolesTree.getTree())
			goalsTreeNumberOfWorkloads += r.getWorkloads().size();
		if (organisationNumberOfWorkloads > goalsTreeNumberOfWorkloads)
			throw new OutputDoesNotMatchWithInput("There are more workloads in the output than in the input!");
	}

	private void matchNumberOfGoals() throws OutputDoesNotMatchWithInput {
		// number of goals in the goals tree must be same as the allocated ones
		int nAssignedGoals = 0;
		for (final RoleNode or : this.getRolesTree().getTree()) nAssignedGoals += or.getAssignedGoals().size();
		GoalTree gTree = GoalTree.getInstance();
		if (nAssignedGoals != gTree.getTree().size())
			throw new OutputDoesNotMatchWithInput("There are more workloads in the output than in the input!");
	}

	public int custo() {
		return cost;
	}

	/** Lista de sucessores */
	public List<Estado> sucessores() {
		List<Estado> suc = new LinkedList<Estado>(); // Lista de sucessores

		// add all possible successors
		for (GoalNode goalToBeAssociated : goalSuccessors) {
			// add each goal as root
			if (rolesTree.getTree().size() == 0) {
				addRootRole(suc, goalToBeAssociated);
			} else {
				// add all children as possible successors
				for (RoleNode role : rolesTree.getTree()) {
					addRootRole(suc, goalToBeAssociated);
					addRole(role, suc, goalToBeAssociated);
					joinRole(role, suc, goalToBeAssociated);
				}
			}
		}

		return suc;
	}

	public void addRootRole(List<Estado> suc, GoalNode goalToAssign) {
		try {
			// Prune states with effort equal to 0
			if (goalToAssign.getSumWorkload() == 0) {
				LOG.debug("#(" + generatedStates + "/" + ++prunedStates + ") addRole pruned#1: "
						+ goalToAssign.getSumWorkload());
				return;
			}

			Organisation newState = (Organisation) createState(goalToAssign);

			RoleNode nr = newState.rolesTree.createRole(null, "r" + newState.rolesTree.size(), goalToAssign);

			newState.cost = penalty.getAddRootRolePenalty(goalToAssign, this.getRolesTree(), newState.getRolesTree());
			newState.accCost = this.accCost + newState.cost;

			suc.add(newState);

			logTransformation("addRootRole", newState, nr);

		} catch (Exception e) {
			LOG.fatal("Fatal error on addRole! " + e.getMessage());
		}
	}

	
	public void addRole(RoleNode aGivenRole, List<Estado> suc, GoalNode goalToAssign) {

		try {
			// cannot create add a role without a root
			if (this.rolesTree.size() < 1) {
				LOG.debug(
						"#(" + generatedStates + "/" + ++prunedStates + ") addRole pruned#0");
				return;
			}

			// Prune states with effort equal to 0
			if (goalToAssign.getSumWorkload() == 0) {
				LOG.debug("#(" + generatedStates + "/" + ++prunedStates + ") addRole pruned#1");
				return;
			}

			// Prune states with effort greater than max (should never happen if the goals were broken properly)
			if (goalToAssign.getSumWorkload() > Parameters.getMaxWorkload()) {
				LOG.debug("#(" + generatedStates + "/" + ++prunedStates + ") addRole pruned#2");
				return;
			}

			// Prune states which parent cannot afford data amount
			if (aGivenRole.getParentSumDataAmount() + aGivenRole.calculateAddedDataLoad(goalToAssign) > Parameters.getMaxDataLoad()) {
				LOG.debug("#(" + generatedStates + "/" + ++prunedStates + ") addRole pruned#3");
				return;
			}

			Organisation newState = (Organisation) createState(goalToAssign);

			RoleNode nr = newState.rolesTree.createRole(newState.rolesTree.findRoleByRoleName(aGivenRole.getRoleName()),
					"r" + newState.rolesTree.size(), goalToAssign);
			

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
			// cannot create add a role without a root
			if (this.rolesTree.size() < 1) {
				LOG.debug("#(" + generatedStates + "/" + ++prunedStates + ") addRole pruned#0");
				return;
			}

			// Prune states with effort equal to 0 (should never happen since a role without effort should not be created)
			if (hostRole.getSumWorkload() + goalToAssign.getSumWorkload() == 0) {
				LOG.debug("#(" + generatedStates + "/" + ++prunedStates + ") addRole pruned#1");
				return;
			}
			
			// Prune states with effort greater than max
			if (hostRole.getSumWorkload() + goalToAssign.getSumWorkload() > Parameters.getMaxWorkload()) {
				LOG.debug("#(" + generatedStates + "/" + ++prunedStates + ") joinRole pruned#2");
				return;
			}

			// Prune states which parent cannot afford data amount 
			if (hostRole.getParentSumDataAmount() + hostRole.calculateAddedDataLoad(goalToAssign) > Parameters.getMaxDataLoad()) {
				LOG.debug("#(" + generatedStates + "/" + ++prunedStates + ") joinRole pruned#3");
				return;
			}

			Organisation newState = (Organisation) createState(goalToAssign);

			RoleNode jr = newState.rolesTree.assignGoalToRoleByRoleName(hostRole.getRoleName(), goalToAssign);

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
	
	public int getGeneratedStates() {
		return Organisation.generatedStates;
	}

	/**
	 * This is the worst case scenario of created states in the search tree
	 * It is not considering any possible pruning, even the ones that occurred
	 * without constraints 
	 * 
	 * @return an integer of worst case number organisations that will be created 
	 */
	public int getEstimatedNumberOfOrganisations() {
		int openedStates = 0;
		int nStates = 1;
		for (int i = 0; i < goalsTree.getTree().size(); i++) {
			if (i == 0) {
				openedStates = goalsTree.getTree().size();
			} else {
				int lastOpenedStates = openedStates;
				openedStates = lastOpenedStates * i * 3;
			}
			nStates += openedStates;
		}
		return nStates;
	}

	
}
