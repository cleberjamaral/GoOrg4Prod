package organisation.search.cost;

import java.util.List;

import organisation.Parameters;
import organisation.exception.PositionNotFound;
import organisation.goal.GoalNode;
import organisation.position.PositionNode;
import organisation.position.PositionsTree;

/**
 * @author cleber
 *
 */
public class CostResolver {

	private static List<Cost> preferences = null;

	public CostResolver(List<Cost> preferences) {
		CostResolver.setPreferences(preferences);
	}

	public static List<Cost> getPreferences() {
		return preferences;
	}

	public static void setPreferences(List<Cost> preferences) {
		CostResolver.preferences = preferences;
	}

	public int getAddSupremePenalty(GoalNode goal, PositionsTree oldTree, PositionsTree newTree) throws PositionNotFound {
		int cost = Parameters.getMinimalPenalty();

		// LESS_IDLENESS - punish if it is creating more position than the ideal
		if (preferences.contains(Cost.LESS_IDLENESS)) {
			cost += (int) ((1 - newTree.getLessIdlenessRate()) * Parameters.getExtraPenalty());
		}

		// GENERALIST - penalize according to generalness of the new tree
		if (preferences.contains(Cost.GENERALIST)) {
			cost += (int) ((1 - newTree.getGeneralness()) * Parameters.getDefaultPenalty());
		}

		// SPECIALIST - penalize according to specificness of the new tree
		if (preferences.contains(Cost.SPECIALIST)) {
			cost += (int) ((1 - newTree.getSpecificness()) * Parameters.getDefaultPenalty());
		}

		return cost;
	}

	public int getAddSubordinatePenalty(PositionNode position, GoalNode goal, PositionsTree oldTree, PositionsTree newTree) throws PositionNotFound {

		int cost = Parameters.getMinimalPenalty();

		// LESS_IDLENESS - punish if it is creating more position than the ideal
		if (preferences.contains(Cost.LESS_IDLENESS)) {
			cost += (int) ((1 - newTree.getLessIdlenessRate()) * Parameters.getExtraPenalty());
		}

		// GENERALIST - penalize according to generalness of the new tree 
		if (preferences.contains(Cost.GENERALIST)) {
			cost += (int) ((1 - newTree.getGeneralness()) * Parameters.getDefaultPenalty());
		}

		// High punishment when it is creating more levels in a preferable flatter
		// structure
		if ((preferences.contains(Cost.FLATTER)) && (newTree.getNumberOfLevels() > oldTree.getNumberOfLevels())) {
			cost += Parameters.getExtraPenalty();
		}
		
		// Low punishment when is preferred taller but is not child
		if ((preferences.contains(Cost.TALLER)) && (!position.hasParentGoal(goal)))
			cost += Parameters.getDefaultPenalty();

		// SPECIALIST - penalize according to specificness of the new tree
		if (preferences.contains(Cost.SPECIALIST)) {
			cost += (int) ((1 - newTree.getSpecificness()) * Parameters.getDefaultPenalty());
		}

		return cost;
	}

	public int getJoinExistingPenalty(PositionNode position, GoalNode goal, PositionsTree oldTree, PositionsTree newTree) throws PositionNotFound {

		int cost = Parameters.getMinimalPenalty();

		// LESS_IDLENESS - punish if it is creating more position than the ideal
		if (preferences.contains(Cost.LESS_IDLENESS)) {
			cost += (int) ((1 - newTree.getLessIdlenessRate()) * Parameters.getExtraPenalty());
		}

		// GENERALIST - penalize according to generalness of the new tree 
		if (preferences.contains(Cost.GENERALIST)) { 
			cost += (int) ((1 - newTree.getGeneralness()) * Parameters.getDefaultPenalty());
		}

		// High punishment when it is preferred taller and the position is not a child
		if ((preferences.contains(Cost.TALLER)) && (!position.hasParentGoal(goal)))
			cost += Parameters.getExtraPenalty();

		// Low punishment when is preferred taller but is child
		if (preferences.contains(Cost.TALLER))
			cost += Parameters.getDefaultPenalty();

		// SPECIALIST - penalize according to specificness of the new tree
		if (preferences.contains(Cost.SPECIALIST)) {
			cost += (int) ((1 - newTree.getSpecificness()) * Parameters.getDefaultPenalty());
		}

		return cost;
	}

}
