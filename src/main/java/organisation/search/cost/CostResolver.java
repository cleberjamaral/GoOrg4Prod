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
			cost += (int) ((1 - newTree.getLessIdlenessRate()) * Parameters.getDefaultPenalty()
					* (preferences.indexOf(Cost.LESS_IDLENESS) + 1));
		}

		// GENERALIST - penalize according to generalness of the new tree
		if (preferences.contains(Cost.GENERALIST)) {
			cost += (int) ((1 - newTree.getGeneralness()) * Parameters.getDefaultPenalty()
					* (preferences.indexOf(Cost.GENERALIST) + 1));
		}

		// SPECIALIST - penalize according to specificness of the new tree
		if (preferences.contains(Cost.SPECIALIST)) {
			cost += (int) ((1 - newTree.getSpecificness()) * Parameters.getDefaultPenalty()
					* (preferences.indexOf(Cost.SPECIALIST) + 1));
		}

		// FLATTER - penalize according to the height of the new tree
		if (preferences.contains(Cost.FLATTER)) {
			cost += (int) ((1 - newTree.getFlatness()) * Parameters.getDefaultPenalty()
					* (preferences.indexOf(Cost.FLATTER) + 1));
		}

		// TALLER - penalize according to the height of the new tree
		if (preferences.contains(Cost.TALLER)) {
			cost += (int) ((1 - newTree.getTallness()) * Parameters.getDefaultPenalty()
					* (preferences.indexOf(Cost.TALLER) + 1));
		}
		
		return cost;
	}

	public int getAddSubordinatePenalty(PositionNode position, GoalNode goal, PositionsTree oldTree, PositionsTree newTree) throws PositionNotFound {

		int cost = Parameters.getMinimalPenalty();

		// LESS_IDLENESS - punish if it is creating more position than the ideal
		if (preferences.contains(Cost.LESS_IDLENESS)) {
			cost += (int) ((1 - newTree.getLessIdlenessRate()) * Parameters.getDefaultPenalty()
					* (preferences.indexOf(Cost.LESS_IDLENESS) + 1));
		}

		// GENERALIST - penalize according to generalness of the new tree
		if (preferences.contains(Cost.GENERALIST)) {
			cost += (int) ((1 - newTree.getGeneralness()) * Parameters.getDefaultPenalty()
					* (preferences.indexOf(Cost.GENERALIST) + 1));
		}

		// FLATTER - penalize according to the height of the new tree
		if (preferences.contains(Cost.FLATTER)) {
			cost += (int) ((1 - newTree.getFlatness()) * Parameters.getDefaultPenalty()
					* (preferences.indexOf(Cost.FLATTER) + 1));
		}

		// TALLER - penalize according to the height of the new tree
		if (preferences.contains(Cost.TALLER)) {
			cost += (int) ((1 - newTree.getTallness()) * Parameters.getDefaultPenalty()
					* (preferences.indexOf(Cost.TALLER) + 1));
		}

		// SPECIALIST - penalize according to specificness of the new tree
		if (preferences.contains(Cost.SPECIALIST)) {
			cost += (int) ((1 - newTree.getSpecificness()) * Parameters.getDefaultPenalty()
					* (preferences.indexOf(Cost.SPECIALIST) + 1));
		}

		return cost;
	}

	public int getJoinExistingPenalty(PositionNode position, GoalNode goal, PositionsTree oldTree, PositionsTree newTree) throws PositionNotFound {

		int cost = Parameters.getMinimalPenalty();

		// LESS_IDLENESS - punish if it is creating more position than the ideal
		if (preferences.contains(Cost.LESS_IDLENESS)) {
			cost += (int) ((1 - newTree.getLessIdlenessRate()) * Parameters.getDefaultPenalty()
					* (preferences.indexOf(Cost.LESS_IDLENESS) + 1));
		}

		// GENERALIST - penalize according to generalness of the new tree
		if (preferences.contains(Cost.GENERALIST)) {
			cost += (int) ((1 - newTree.getGeneralness()) * Parameters.getDefaultPenalty()
					* (preferences.indexOf(Cost.GENERALIST) + 1));
		}

		// FLATTER - penalize according to the height of the new tree
		if (preferences.contains(Cost.FLATTER)) {
			cost += (int) ((1 - newTree.getFlatness()) * Parameters.getDefaultPenalty()
					* (preferences.indexOf(Cost.FLATTER) + 1));
		}

		// TALLER - penalize according to the height of the new tree
		if (preferences.contains(Cost.TALLER)) {
			cost += (int) ((1 - newTree.getTallness()) * Parameters.getDefaultPenalty()
					* (preferences.indexOf(Cost.TALLER) + 1));
		}

		// SPECIALIST - penalize according to specificness of the new tree
		if (preferences.contains(Cost.SPECIALIST)) {
			cost += (int) ((1 - newTree.getSpecificness()) * Parameters.getDefaultPenalty()
					* (preferences.indexOf(Cost.SPECIALIST) + 1));
		}

		return cost;
	}

}
