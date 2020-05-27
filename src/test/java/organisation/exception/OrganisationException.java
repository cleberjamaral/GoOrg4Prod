package organisation.exception;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import busca.BuscaLargura;
import busca.Nodo;
import organisation.OrganisationStatistics;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.search.Organisation;
import organisation.search.cost.Cost;

public class OrganisationException {

	@Test
	public void testOutputDoesNotMatchWithInput() {
		System.out.println("\n\ntestOutputDoesNotMatchWithInput");

		try {
			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getInstance();
			gTree.setRootNode(g0);
			gTree.addWorkload("g0", "w0", 1);

			System.out.println("GoalsTree: " + gTree.getTree().toString());

			OrganisationStatistics s = OrganisationStatistics.getInstance();
			s.prepareGenerationStatisticsFile("testOutputDoesNotMatchWithInput");

			Organisation o = new Organisation("testOutputDoesNotMatchWithInput", gTree,
					Arrays.asList(Cost.GENERALIST, Cost.EFFICIENT), true);
			Nodo n = new BuscaLargura().busca(o);

			System.out.println("Force a wrong validation, which must throw an exception");

			gTree.addWorkload("g0", "w1", 1);
			assertThrows(OutputDoesNotMatchWithInput.class, () -> {
				((Organisation) n.getEstado()).isValid();
			});
			System.out.println("Exception thrown successfully!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testPositionNotFound() {
		System.out.println("\n\ntestPositionNotFound");

		try {
			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getInstance();
			gTree.setRootNode(g0);
			gTree.addWorkload("g0", "w0", 1);

			System.out.println("GoalsTree: " + gTree.getTree().toString());
			Organisation o = new Organisation("testPositionNotFound", gTree,
					Arrays.asList(Cost.GENERALIST, Cost.EFFICIENT), true);
			Nodo n = new BuscaLargura().busca(o);

			System.out.println("Positions tree: " + ((Organisation) n.getEstado()).getPositionsTree());
			System.out.println("search for a nonexistant positionname");
			assertThrows(PositionNotFound.class, () -> {
				((Organisation) n.getEstado()).getPositionsTree().findPositionByName("nonexistant");
			});
			System.out.println("Exception thrown successfully!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
