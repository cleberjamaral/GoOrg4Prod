package organisation.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import busca.BuscaLargura;
import busca.Nodo;
import organisation.OrganisationStatistics;
import organisation.Parameters;
import organisation.exception.CircularReference;
import organisation.exception.GoalNotFound;
import organisation.exception.OutputDoesNotMatchWithInput;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.search.Organisation;
import organisation.search.cost.Cost;

public class CostIdleTest {

	@BeforeClass
	public static void beforeTests() {
		OrganisationStatistics s = OrganisationStatistics.getInstance();
		s.deleteExistingStatistics();
    }

	@Before
	public void resetGoalTreeSingleton() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
	   Field instance = GoalTree.class.getDeclaredField("instance");
	   instance.setAccessible(true);
	   instance.set(null, null);
	}
	
    @Test
    public void testOnePositionIdleOrg() {
    	try {
    		System.out.println("\n\ntestOnePositionIdleOrg");

    		// parameters
    		Parameters.getInstance();
    		Parameters.setMaxWorkload(8.0);
    		Parameters.setWorkloadGrain(2.0);
    		Parameters.setMaxDataLoad(8.0);
    		Parameters.setDataLoadGrain(2.0);
    		
			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getInstance();
			gTree.setRootNode(g0);
			gTree.addGoal("g1", "g0");
			gTree.addGoal("g2", "g1");
			System.out.println("g1 must be split into two goals with 2 of workload each");
			gTree.addWorkload("g1", "w1", 4);
			System.out.println("g2 must be split into two goals with 2 of dataload each");
			gTree.addInform("g1", "i1", "g2", 4);
			
			gTree.brakeGoalTree();
			
			System.out.println("Originals (unbroken) goals are: "+GoalTree.getInstance().getOriginalGoals());
			assertEquals(3, GoalTree.getInstance().getOriginalGoals().size(), 0);
			
			OrganisationStatistics s = OrganisationStatistics.getInstance();
			s.prepareGenerationStatisticsFile("testOnePositionIdleOrg");
			
			System.out.println("Total workload is 4 (less than 8 - max) -> goals must be assigned to one position.");
			Organisation o = new Organisation("testOnePositionIdleOrg", gTree,
					Arrays.asList(Cost.IDLE), true);
			Nodo n = new BuscaLargura().busca(o);

			System.out.println("Generated tree: " + ((Organisation)n.getEstado()).getPositionsTree().getTree());

			assertEquals(2, ((Organisation)n.getEstado()).getPositionsTree().getTree().size());
			assertEquals(4, ((Organisation)n.getEstado()).getPositionsTree().getSumWorkload(), 0);
			
			assertTrue(((Organisation) n.getEstado()).isValid());
			// 2 goals with WL => 2 positions, occupancy / Max WL per position = 1 - 4/16
			assertEquals((1.0 - (double) 4 / 16),
					((Organisation) n.getEstado()).getPositionsTree().getIdleness(), 0.1);

    	} catch (CircularReference e) {
			e.printStackTrace();
		} catch (GoalNotFound e) {
			e.printStackTrace();
		} catch (OutputDoesNotMatchWithInput e) {
			e.printStackTrace();
		}
    }

	@Test
	public void testTwoPositionsIdleOrg() {
		try {
			System.out.println("\n\ntestTwoPositionsIdleOrg");

			Parameters.getInstance();
			Parameters.setMaxWorkload(8.0);
			Parameters.setWorkloadGrain(4.0);
			Parameters.setMaxDataLoad(8.0);
			Parameters.setDataLoadGrain(2.0);

			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getInstance();
			gTree.setRootNode(g0);
			gTree.addGoal("g1", "g0");
			System.out.println("g1 must be split into two goals with 2.5 of workload each");
			gTree.addWorkload("g0", "w0", 5);
			gTree.addWorkload("g1", "w1", 5);
			System.out.println("g0 must be split into four goals with 1.75 of dataload each");
			gTree.addInform("g1", "i1", "g0", 7);
			
			gTree.brakeGoalTree();
			
			System.out.println("Originals (unbroken) goals are: "+GoalTree.getInstance().getOriginalGoals());
			assertEquals(2, GoalTree.getInstance().getOriginalGoals().size(), 0);

			OrganisationStatistics s = OrganisationStatistics.getInstance();
			s.prepareGenerationStatisticsFile("testTwoPositionsIdleOrg");

			System.out.println("Total workload is 10 -> goals must be assigned to two positions.");
			Organisation o = new Organisation("testTwoPositionsIdleOrg", gTree,
					Arrays.asList(Cost.IDLE), true);
			Nodo n = new BuscaLargura().busca(o);

			assertEquals(6, ((Organisation) n.getEstado()).getPositionsTree().getTree().size());
			assertEquals(10, ((Organisation) n.getEstado()).getPositionsTree().getSumWorkload(), 0);
			System.out.println("Generated tree: " + ((Organisation)n.getEstado()).getPositionsTree().getTree());
			
			assertTrue(((Organisation) n.getEstado()).isValid());
			// 2 goals with WL => 2 positions, occupancy / Max WL per position = 1 - 10/48
			assertEquals((1.0 - (double) 10 / 48),
					((Organisation) n.getEstado()).getPositionsTree().getIdleness(), 0.1);
			
		} catch (CircularReference e) {
			e.printStackTrace();
		} catch (GoalNotFound e) {
			e.printStackTrace();
		} catch (OutputDoesNotMatchWithInput e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testThreePositionsIdleOrg() {
		try {
			System.out.println("\n\ntestThreePositionsIdleOrg");

			Parameters.getInstance();
			Parameters.setMaxWorkload(8.0);
			Parameters.setWorkloadGrain(4.0);
			Parameters.setMaxDataLoad(8.0);
			Parameters.setDataLoadGrain(2.0);

			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getInstance();
			gTree.setRootNode(g0);
			gTree.addGoal("g1", "g0");
			gTree.addWorkload("g1", "w1", 12);
			gTree.addGoal("g2", "g0");
			gTree.addWorkload("g2", "w2", 9);
			gTree.addInform("g1", "i1", "g2", 1);
			
			gTree.brakeGoalTree();

			System.out.println("Originals (unbroken) goals are: "+GoalTree.getInstance().getOriginalGoals());
			assertEquals(3, GoalTree.getInstance().getOriginalGoals().size(), 0);

			OrganisationStatistics s = OrganisationStatistics.getInstance();
			s.prepareGenerationStatisticsFile("testThreePositionsIdleOrg");

			System.out.println("Total workload is 21 -> goals must be assigned to three positions.");
			Organisation o = new Organisation("testThreePositionsTallerOrg", gTree,
					Arrays.asList(Cost.IDLE), true);
			Nodo n = new BuscaLargura().busca(o);

			assertEquals(6, ((Organisation) n.getEstado()).getPositionsTree().getTree().size());
			assertEquals(21, ((Organisation) n.getEstado()).getPositionsTree().getSumWorkload(), 0);
			System.out.println("Generated tree: " + ((Organisation)n.getEstado()).getPositionsTree().getTree());
			
			assertTrue(((Organisation) n.getEstado()).isValid());
			// 6 goals with WL => 6 positions, occupancy / Max WL per position = 1 - 21/48
			assertEquals((1.0 - (double) 21 / 48),
					((Organisation) n.getEstado()).getPositionsTree().getIdleness(), 0.1);

		} catch (CircularReference e) {
			e.printStackTrace();
		} catch (GoalNotFound e) {
			e.printStackTrace();
		} catch (OutputDoesNotMatchWithInput e) {
			e.printStackTrace();
		}
	}

}
