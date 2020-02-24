package organisation.search;

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

	public int getNonKinshipPenalty(RoleNode role, GoalNode goal) {
		return Parameters.getMinimalPenalty();
/*		// the given role has goal's parent associated?
		if (role.hasParentGoal(goal))
			return Parameters.getMinimalPenalty();

		// the given role has a goal which is sibling of the given goal?
		if (role.hasSiblingGoal(goal))
			return Parameters.getMinimalPenalty();

		// Punish association of goals with no kinship
		return Parameters.getDefaultPenalty();*/
	}
	
	public int getAddRolePenalty(RoleNode role, GoalNode goal, RoleTree oldTree, RoleTree newTree) throws RoleNotFound {

		int cost = getNonKinshipPenalty(role, goal);

		// High punishment when another role could receive the workload making the tree more generalist
		if (costFunction == Cost.GENERALIST) {
			
			// publish if it is creating more roles than the ideal
			if (oldTree.getTree().size() >= GoalTree.getInstance().idealNumberOfRoles()) {
				return cost + Parameters.getExtraPenalty();
			}

			// check if another role should receive this workload to become generalist
			for (RoleNode r : oldTree.getTree()) {
				// no cost: If there is a role that does not have the workloads and could receive it
				if ((r.getWorkloads().containsAll(goal.getWorkloads()))
						&& (r.getSumWorkload() + goal.getSumWorkload() <= Parameters.getMaxWorkload())) {

					return cost + Parameters.getExtraPenalty();
				}
			}
		}

		// High punishment when it is creating more levels in a preferable flatter
		// structure
		if ((costFunction == Cost.FLATTER) && (newTree.getNumberOfLevels() > oldTree.getNumberOfLevels())) {
			return cost + Parameters.getExtraPenalty();
		}
		
		// Low punishment when is preferred taller but is not child
		if ((costFunction == Cost.TALLER) && (!role.hasParentGoal(goal)))
			return cost + Parameters.getDefaultPenalty();

		// TODO: check if it is creating a sibling with same skills, punishing it
		// Preferring specialist structure potentially punish the creation of roles
		RoleNode old = oldTree.findRoleBySignature(role.signature());
		if (costFunction == Cost.SPECIALIST) {
			
			// Punish when it would be possible to join with the give role
			if ((old.getWorkloads().containsAll(goal.getWorkloads()))
					&& (old.getSumWorkload() + goal.getSumWorkload() <= Parameters.getMaxWorkload())) {

				return cost + Parameters.getExtraPenalty();
			}

			// Punish when it would be possible to join with any sibling role
			for (RoleNode r : old.getDescendants()) {
				if ((r.getWorkloads().containsAll(goal.getWorkloads()))
						&& (r.getSumWorkload() + goal.getSumWorkload() <= Parameters.getMaxWorkload())) {

					return cost + Parameters.getExtraPenalty();
				}
			}

		}

		return cost;
	}

	public int getJoinRolePenalty(RoleNode role, GoalNode goal, RoleTree oldTree, RoleTree newTree) throws RoleNotFound {

		int cost = getNonKinshipPenalty(role, goal);
		// Punish when workload already exists, trying to put it to another generalist role
		if (costFunction == Cost.GENERALIST) {

			// Preferring specialist structure potentially punish the creation of roles
			RoleNode old = oldTree.findRoleBySignature(role.signature());
			
			// Punish when it would be possible to join with the give role
			if (old.getWorkloads().containsAll(goal.getWorkloads())) {
				return Parameters.getDefaultPenalty();
			}
		}

		// High punishment when it is preferred taller and the role is not a child
		if ((costFunction == Cost.TALLER) && (!role.hasParentGoal(goal)))
			return cost + Parameters.getExtraPenalty();

		// Low punishment when is preferred taller but is child
		if (costFunction == Cost.TALLER)
			return cost + Parameters.getDefaultPenalty();

		// Punish when an extra workload is added incrementing setup costs
		if (costFunction == Cost.SPECIALIST) {
			return cost + getSetupPenalty(role, goal);
		}

		return cost;
	}

}
