package organisation.search;

import organisation.exception.RoleNotFound;
import organisation.goal.GoalNode;
import organisation.role.RoleNode;
import organisation.role.RoleTree;

public class CostResolver {

	// Minimal penalty for creating a new state
	private static int minimalPenalty = 1;
	// Cost penalty used to infer bad decisions on search
	private static int defaultPenalty = 2;
	// Cost penalty used to infer VERY bad decisions on search
	private static int extraPenalty = 4;
	// static Cost costFunction = Cost.SPECIALIST;
	private static Cost costFunction = Cost.SPECIALIST;

	public CostResolver(int costPenalty, Cost costFunction) {
		CostResolver.setMinimalPenalty(1);
		CostResolver.setCostPenalty(costPenalty);
		CostResolver.setExtraPenalty(costPenalty * 2);
		CostResolver.setCostFunction(costFunction);
	}
	
	public static Cost getCostFunction() {
		return costFunction;
	}

	public static void setCostFunction(Cost costFunction) {
		CostResolver.costFunction = costFunction;
	}

	public static int getMinimalPenalty() {
		return minimalPenalty;
	}

	public static void setMinimalPenalty(int minimalPenalty) {
		CostResolver.minimalPenalty = minimalPenalty;
	}

	public static int getCostPenalty() {
		return defaultPenalty;
	}

	public static void setCostPenalty(int costPenalty) {
		CostResolver.defaultPenalty = costPenalty;
	}

	public static int getExtraPenalty() {
		return extraPenalty;
	}

	public static void setExtraPenalty(int extraPenalty) {
		CostResolver.extraPenalty = extraPenalty;
	}

	public int getNonKindshipPenalty(RoleNode role, GoalNode goal) {
		// the given role has goal's parent associated?
		if (role.hasParentGoal(goal)) 
			return getMinimalPenalty();

		// the given role has a goal which is sibling of the given goal?
		if (role.hasSiblingGoal(goal)) 
			return getMinimalPenalty();
		
		// Punish association of goals with no kinship
		return getCostPenalty();
	}

	public int getAddRolePenalty(RoleNode role, GoalNode goal, RoleTree oldTree, RoleTree newTree) throws RoleNotFound {
		
		int cost = getNonKindshipPenalty(role, goal);
		
		// High punishment when it is preferred more generalist structures
		if (costFunction == Cost.GENERALIST) 
			return cost + CostResolver.getCostPenalty() * 2;
		
		// High punishment when it is creating more levels in a preferable flatter structure
		if ((costFunction == Cost.FLATTER) && (newTree.getNumberOfLevels() > oldTree.getNumberOfLevels()))
			return cost + CostResolver.getCostPenalty() * 2;
		
		// Low punishment when is preferred taller but is not child
		if ((costFunction == Cost.TALLER) && (!role.hasParentGoal(goal))) 
			return cost + CostResolver.getCostPenalty();

		//TODO: check if it is creating a sibling with same skills, punishing it
		//TODO: bring maxEffort
		// Punish when it would be possible to join
		RoleNode old = oldTree.findRoleBySignature(role.signature());
		if ((costFunction == Cost.SPECIALIST) && (old.getWorkloads().containsAll(goal.getWorkloads())) && (old.getSumWorkload() + goal.getSumWorkload() < 8)) {
			System.out.println("\n\n");
			System.out.println("**** " + old.getWorkloads());
			System.out.println(old.getAssignedGoals());
			System.out.println(goal);
			System.out.println(oldTree);
			System.out.println(newTree);
			System.out.println("\n\n");
			return cost + CostResolver.getCostPenalty() * 2;

		}
		
		return cost;
	}
	
	public int getJoinRolePenalty(RoleNode role, GoalNode goal, RoleTree oldTree, RoleTree newTree) throws RoleNotFound {

		int cost = getNonKindshipPenalty(role, goal);

		// High punishment when it is preferred taller and the role is not a child
		if ((costFunction == Cost.TALLER) && (!role.hasParentGoal(goal)))
			return cost + CostResolver.getCostPenalty() * 2;
		
		// Low punishment when is preferred taller but is child
		if (costFunction == Cost.TALLER) 
			return cost + CostResolver.getCostPenalty();
		
		// Punish when it is preferred more specialist structures and some workload was added
		RoleNode old = oldTree.findRoleBySignature(role.signature());
		if ((costFunction == Cost.SPECIALIST) && (!old.getWorkloads().containsAll(goal.getWorkloads()))) 
			return cost + CostResolver.getCostPenalty() * 2;

		return cost;
	}
	
}
