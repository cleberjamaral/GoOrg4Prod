package organisation.search.cost;

import java.util.List;

import organisation.Parameters;
import organisation.exception.RoleNotFound;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.role.RoleTree;

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

	public int getPedictedCost(List<GoalNode> gSuc, RoleTree rTree) throws RoleNotFound {
		int predictedCost = Parameters.getMinimalPenalty();

		// High punishment when another role could receive the workload making the tree more generalist
		if (costFunction == Cost.GENERALIST) {
			
			// publish if it is creating more roles than the ideal
			if (rTree.getTree().size() >= GoalTree.getInstance().idealNumberOfRoles()) {
				return gSuc.size() * Parameters.getExtraPenalty();
			} else {
				return gSuc.size() * Parameters.getMinimalPenalty();
			}
		}

		return predictedCost;
	}

}
