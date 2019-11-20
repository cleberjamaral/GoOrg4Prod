package organisation.search;

import organisation.goal.GoalNode;
import organisation.role.RoleNode;

public class CostResolver {

	// Cost penalty used to infer bad decisions on search
	private static int costPenalty = 1;
	// static Cost costFunction = Cost.SPECIALIST;
	private static Cost costFunction = Cost.SPECIALIST;

	public CostResolver(int costPenalty, Cost costFunction) {
		CostResolver.setCostPenalty(costPenalty);
		CostResolver.setCostFunction(costFunction);
	}
	
	public static Cost getCostFunction() {
		return costFunction;
	}

	public static void setCostFunction(Cost costFunction) {
		CostResolver.costFunction = costFunction;
	}


	public static int getCostPenalty() {
		return costPenalty;
	}

	public static void setCostPenalty(int costPenalty) {
		CostResolver.costPenalty = costPenalty;
	}

	public int getNonKindshipPenalty(RoleNode role, GoalNode goal) {
		// the given role has goal's parent associated?
		if (role.hasParentGoal(goal)) {
			return 1;
		} else if (role.hasSiblingGoal(goal)) {
			// TODO: extra cost?
			return 2;
		} else {
			// Apart from the cost function, punish association of goals with no kinship
			return getCostPenalty();
		}
	}

	public int getAddRolePenalty(RoleNode role, GoalNode goal) {
		// High punishment when it is preferred more generalist and flatter structures
		if ((costFunction == Cost.FLATTER) || (costFunction == Cost.GENERALIST)) {
			return CostResolver.getCostPenalty() * 2;
		} 
		// Low punishment when is preferred taller but is not child
		if ((costFunction == Cost.TALLER) && (!role.hasParentGoal(goal))) {
			return CostResolver.getCostPenalty();
		}
		return 0;
	}
	
	public int getJoinRolePenalty(RoleNode role, GoalNode goal) {
		// High punishment when it is preferred taller and the role is not a child
		if (((costFunction == Cost.TALLER) && (!role.hasParentGoal(goal)))
				// Punish when it is preferred more specialist structures
				|| (costFunction == Cost.SPECIALIST)) {
			return CostResolver.getCostPenalty() * 2;
		}
		// Low punishment when is preferred taller but is child
		if (costFunction == Cost.TALLER) {
			return CostResolver.getCostPenalty();
		}
		return 0;
	}
	
	
	
}
