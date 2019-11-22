package organisation.search;

import organisation.exception.RoleNotFound;
import organisation.goal.GoalNode;
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

	public int getNonKindshipPenalty(RoleNode role, GoalNode goal) {
		// the given role has goal's parent associated?
		if (role.hasParentGoal(goal))
			return Parameters.getMinimalPenalty();

		// the given role has a goal which is sibling of the given goal?
		if (role.hasSiblingGoal(goal))
			return Parameters.getMinimalPenalty();

		// Punish association of goals with no kinship
		return Parameters.getDefaultPenalty();
	}

	public int getAddRolePenalty(RoleNode role, GoalNode goal, RoleTree oldTree, RoleTree newTree) throws RoleNotFound {

		int cost = getNonKindshipPenalty(role, goal);

		// High punishment when it is preferred more generalist structures
		if (costFunction == Cost.GENERALIST)
			return cost + Parameters.getExtraPenalty();

		// High punishment when it is creating more levels in a preferable flatter
		// structure
		if ((costFunction == Cost.FLATTER) && (newTree.getNumberOfLevels() > oldTree.getNumberOfLevels()))
			return cost + Parameters.getExtraPenalty();

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

	public int getJoinRolePenalty(RoleNode role, GoalNode goal, RoleTree oldTree, RoleTree newTree)
			throws RoleNotFound {

		int cost = getNonKindshipPenalty(role, goal);

		// High punishment when it is preferred taller and the role is not a child
		if ((costFunction == Cost.TALLER) && (!role.hasParentGoal(goal)))
			return cost + Parameters.getExtraPenalty();

		// Low punishment when is preferred taller but is child
		if (costFunction == Cost.TALLER)
			return cost + Parameters.getDefaultPenalty();

		// Punish when it is preferred more specialist structures and some workload was
		// added
		RoleNode old = oldTree.findRoleBySignature(role.signature());
		if ((costFunction == Cost.SPECIALIST) && (!old.getWorkloads().containsAll(goal.getWorkloads())))
			return cost + Parameters.getExtraPenalty();

		return cost;
	}

}
