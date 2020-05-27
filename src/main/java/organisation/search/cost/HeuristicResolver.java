package organisation.search.cost;

import java.util.List;

import organisation.Parameters;
import organisation.exception.PositionNotFound;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.position.PositionsTree;

/**
 * @author cleber
 *
 */
public class HeuristicResolver {

	// static Cost costFunction = Cost.SPECIALIST;
	private static Cost costFunction = Cost.SPECIALIST;

	public HeuristicResolver(Cost costFunction) {
		HeuristicResolver.setCostFunction(costFunction);
	}

	public static Cost getCostFunction() {
		return costFunction;
	}

	public static void setCostFunction(Cost costFunction) {
		HeuristicResolver.costFunction = costFunction;
	}

	public int getPedictedCost(List<GoalNode> gSuc, PositionsTree rTree) throws PositionNotFound {
		int predictedCost = Parameters.getMinimalPenalty();

		// High punishment when another position could receive the workload making the tree more generalist
		if (costFunction == Cost.GENERALIST) {
			
			// publish if it is creating more positions than the ideal
			if (rTree.getTree().size() >= GoalTree.getInstance().getBestNumberOfPositions()) {
				return gSuc.size() * Parameters.getDefaultPenalty() * 2;
			} else {
				return gSuc.size() * Parameters.getMinimalPenalty();
			}
		}

		return predictedCost;
	}

}
