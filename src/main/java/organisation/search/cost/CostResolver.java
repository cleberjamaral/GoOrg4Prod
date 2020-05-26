package organisation.search.cost;

import organisation.Parameters;
import organisation.exception.PositionNotFound;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.position.PositionNode;
import organisation.position.PositionsTree;

/**
 * @author cleber
 *
 */
public class CostResolver {

	private static Cost costFunction = Cost.GENERALIST;
	private static boolean searchMostEfficient = true;

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
	 * return a penalty when an workload is added to a position, indicating
	 * it needs to switch the context adding setup cost
	 * 
	 * @param position that is receiving a goal
	 * @param goal to me assigned to a position
	 * @return the cost
	 */
	public int getSetupPenalty(PositionNode position, GoalNode goal) {
		// the position will receive a different workload, adding setup time
		if (!position.getWorkloads().containsAll(goal.getWorkloads())) {
			return Parameters.getExtraPenalty();
		}
		return 0;
		
	}

	public int getAddSupremePenalty(GoalNode goal, PositionsTree oldTree, PositionsTree newTree) throws PositionNotFound {
		int cost = Parameters.getMinimalPenalty();

		// GENERALIST
		if (costFunction == Cost.GENERALIST) {
			
			// punish if it is creating more position than the ideal
			if (isDecreasingEfficiency(oldTree))
				cost += Parameters.getDefaultPenalty();

			// penalize according to generalness of the new tree
			cost += (int) ((1 - newTree.getGeneralness()) * Parameters.getExtraPenalty());
		}

		// SPECIALIST
		if (costFunction == Cost.SPECIALIST) {
			// punish if it is creating more positions than the ideal
			if (isDecreasingEfficiency(oldTree))
				return cost + Parameters.getDefaultPenalty();
			
			// compare specificness of old and new trees
			if (newTree.getSpecificness() < oldTree.getSpecificness()) {
				return cost + Parameters.getExtraPenalty();
			}
		}

		return cost;
	}

	public int getAddSubordinatePenalty(PositionNode position, GoalNode goal, PositionsTree oldTree, PositionsTree newTree) throws PositionNotFound {

		int cost = Parameters.getMinimalPenalty();

		// High punishment when another position could receive the workload making the tree more generalist
		if (costFunction == Cost.GENERALIST) {
			
			// punish if it is creating more positions than the ideal
			if ((isDecreasingEfficiency(oldTree)))
				cost += Parameters.getDefaultPenalty();

			// penalize according to generalness of the new tree
			cost += (int) ((1 - newTree.getGeneralness()) * Parameters.getExtraPenalty());
		}

		// High punishment when it is creating more levels in a preferable flatter
		// structure
		if ((costFunction == Cost.FLATTER) && (newTree.getNumberOfLevels() > oldTree.getNumberOfLevels())) {
			return cost + Parameters.getExtraPenalty();
		}
		
		// Low punishment when is preferred taller but is not child
		if ((costFunction == Cost.TALLER) && (!position.hasParentGoal(goal)))
			return cost + Parameters.getDefaultPenalty();

		// SPECIALIST
		if (costFunction == Cost.SPECIALIST) {
			// punish if it is creating more positions than the ideal
			if (isDecreasingEfficiency(oldTree))
				return cost + Parameters.getDefaultPenalty();

			// compare specificness of old and new trees
			if (newTree.getSpecificness() < oldTree.getSpecificness())
				return cost + Parameters.getExtraPenalty();
		}

		return cost;
	}

	public int getJoinExistingPenalty(PositionNode position, GoalNode goal, PositionsTree oldTree, PositionsTree newTree) throws PositionNotFound {

		int cost = Parameters.getMinimalPenalty();

		// Punish when goal and workloads already exist, better to put it to another position
		if (costFunction == Cost.GENERALIST) { 
			// penalize according to generalness of the new tree
			return (int) ((1 - newTree.getGeneralness()) * Parameters.getExtraPenalty());
		}

		// High punishment when it is preferred taller and the position is not a child
		if ((costFunction == Cost.TALLER) && (!position.hasParentGoal(goal)))
			return cost + Parameters.getExtraPenalty();

		// Low punishment when is preferred taller but is child
		if (costFunction == Cost.TALLER)
			return cost + Parameters.getDefaultPenalty();

		// SPECIALIST
		if ((costFunction == Cost.SPECIALIST) && (newTree.getSpecificness() < oldTree.getSpecificness()))
			return cost + Parameters.getExtraPenalty();

		return cost;
	}

	private boolean isDecreasingEfficiency(PositionsTree oldTree) {
		if ((searchMostEfficient) && (oldTree.getTree().size() >= GoalTree.getInstance().getBestNumberOfPositions()))
			return true;

		return false;
	}

}
