package organisation.search.cost;

import organisation.Parameters;
import organisation.exception.RoleNotFound;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.role.RoleNode;
import organisation.role.RoleTree;

public class CostResolver {

	// static Cost costFunction = Cost.SPECIALIST;
	private static Cost costFunction = Cost.SPECIALIST;

	public CostResolver(Cost costFunction) {
		CostResolver.setCostFunction(costFunction);
	}

	public static Cost getCostFunction() {
		return costFunction;
	}

	public static void setCostFunction(Cost costFunction) {
		CostResolver.costFunction = costFunction;
	}

	/**
	 * return a penalty when an workload is added to a role, indicating
	 * it needs to switch the context adding setup cost
	 * 
	 * @param role that is receiving a goal
	 * @param goal to me assigned to a role
	 * @return the cost
	 */
	public int getSetupPenalty(RoleNode role, GoalNode goal) {
		// the role will receive a different workload, adding setup time
		if (!role.getWorkloads().containsAll(goal.getWorkloads())) {
			return Parameters.getExtraPenalty();
		}
		return 0;
		
	}

	public int getAddRootRolePenalty(GoalNode goal, RoleTree oldTree, RoleTree newTree) throws RoleNotFound {
		int cost = Parameters.getMinimalPenalty();

		// GENERALIST
		if (costFunction == Cost.GENERALIST) {
			
			// punish if it is creating more roles than the ideal
			if (isDecreasingEfficiency(oldTree))
				return cost + Parameters.getDefaultPenalty();

			// check if another role should receive this workload to become more generalist
			if (isDecreasingEfficiencyForGeneralist(goal, oldTree))
				return cost + Parameters.getDefaultPenalty();
		}

		// SPECIALIST
		if (costFunction == Cost.SPECIALIST) {
			// punish if it is creating more roles than the ideal
			if (isDecreasingEfficiency(oldTree))
				return cost + Parameters.getDefaultPenalty();
			
			// compare specificness of old and new trees
			if (newTree.getSpecificness() < oldTree.getSpecificness()) {
				return cost + Parameters.getExtraPenalty();
			}
		}

		return cost;
	}

	public int getAddRolePenalty(RoleNode role, GoalNode goal, RoleTree oldTree, RoleTree newTree) throws RoleNotFound {

		int cost = Parameters.getMinimalPenalty();

		// High punishment when another role could receive the workload making the tree more generalist
		if (costFunction == Cost.GENERALIST) {
			
			// punish if it is creating more roles than the ideal
			if (isDecreasingEfficiency(oldTree))
				return cost + Parameters.getDefaultPenalty();

			// check if another role should receive this workload to become more generalist
			if (isDecreasingEfficiencyForGeneralist(goal, oldTree))
				return cost + Parameters.getDefaultPenalty();
		}

		// High punishment when it is creating more levels in a preferable flatter
		// structure
		if ((costFunction == Cost.FLATTER) && (newTree.getNumberOfLevels() > oldTree.getNumberOfLevels())) {
			return cost + Parameters.getExtraPenalty();
		}
		
		// Low punishment when is preferred taller but is not child
		if ((costFunction == Cost.TALLER) && (!role.hasParentGoal(goal)))
			return cost + Parameters.getDefaultPenalty();

		// SPECIALIST
		if (costFunction == Cost.SPECIALIST) {
			// punish if it is creating more roles than the ideal
			if (isDecreasingEfficiency(oldTree))
				return cost + Parameters.getDefaultPenalty();

			// compare specificness of old and new trees
			if (newTree.getSpecificness() < oldTree.getSpecificness())
				return cost + Parameters.getExtraPenalty();
		}

		return cost;
	}

	public int getJoinRolePenalty(RoleNode role, GoalNode goal, RoleTree oldTree, RoleTree newTree) throws RoleNotFound {

		int cost = Parameters.getMinimalPenalty();

		// Punish when goal and workloads already exist, better to put it to another role
		if ((costFunction == Cost.GENERALIST) && (goalsAndWorkloadsAlreadyExist(role, goal, oldTree)))
			return Parameters.getDefaultPenalty();

		// High punishment when it is preferred taller and the role is not a child
		if ((costFunction == Cost.TALLER) && (!role.hasParentGoal(goal)))
			return cost + Parameters.getExtraPenalty();

		// Low punishment when is preferred taller but is child
		if (costFunction == Cost.TALLER)
			return cost + Parameters.getDefaultPenalty();

		// SPECIALIST
		if ((costFunction == Cost.SPECIALIST) && (newTree.getSpecificness() < oldTree.getSpecificness()))
			return cost + Parameters.getExtraPenalty();

		return cost;
	}

	private boolean isDecreasingEfficiency(RoleTree oldTree) {
		if (oldTree.getTree().size() >= GoalTree.getInstance().getBestNumberOfRoles())
			return true;
		
		return false;
	}

	private boolean isDecreasingEfficiencyForGeneralist(GoalNode goal, RoleTree oldTree) {
		for (RoleNode r : oldTree.getTree()) {
			// Check if there is a role that has the same goal and could receive this other one
			if ((r.containsGoalByOriginalName(goal))
				&& (r.getSumWorkload() + goal.getSumWorkload() <= Parameters.getMaxWorkload())) {
				
				return true;
			}
			
			// Check if there is a role that has the workloads and could receive this other one
			if ((r.getWorkloads().containsAll(goal.getWorkloads()))
					&& (r.getSumWorkload() + goal.getSumWorkload() <= Parameters.getMaxWorkload())) {
				
				return true;
			}
		}
		return false;
	}

	private boolean goalsAndWorkloadsAlreadyExist(RoleNode role, GoalNode goal, RoleTree oldTree) throws RoleNotFound {
		RoleNode old = oldTree.findRoleByRoleName(role.getRoleName());
		
		// Punish when it would be possible to join with the give role
		if (old.getWorkloads().containsAll(goal.getWorkloads()))
			return true;
		
		return false;
	}

}
