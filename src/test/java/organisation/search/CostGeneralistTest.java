package organisation.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import annotations.Workload;
import busca.BuscaLargura;
import busca.Nodo;
import organisation.OrganisationStatistics;
import organisation.Parameters;
import organisation.exception.CircularReference;
import organisation.exception.GoalNotFound;
import organisation.exception.OutputDoesNotMatchWithInput;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.position.PositionNode;
import organisation.search.Organisation;
import organisation.search.cost.Cost;

public class CostGeneralistTest {

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
    public void testOnePositionGeneralistOrg() {
    	try {
    		System.out.println("\n\ntestOnePositionGeneralistOrg");

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
			s.prepareGenerationStatisticsFile("testOnePositionGeneralistOrg");
			
			System.out.println("Total workload is 4 (less than 8 - max) -> goals must be assigned to one position.");
			Organisation o = new Organisation("testOnePositionGeneralistOrg", gTree,
					Arrays.asList(Cost.GENERALIST, Cost.EFFICIENT), true);
			Nodo n = new BuscaLargura().busca(o);

			System.out.println("Generated tree: " + ((Organisation)n.getEstado()).getPositionsTree().getTree());

			assertEquals(1, ((Organisation)n.getEstado()).getPositionsTree().getTree().size());
			assertEquals(4, ((Organisation)n.getEstado()).getPositionsTree().getSumWorkload(), 0);

			for (PositionNode r : ((Organisation)n.getEstado()).getPositionsTree().getTree()) {
				System.out.println("Position: " + r + ", workloads: "+r.getWorkloads() + ", informs: "+r.getInforms());
				// workload equals only checks the id of the workload
				assertTrue(r.getWorkloads().contains((new Workload("w1",0)))); 
				assertEquals(1, r.getWorkloads().size(), 0);
				assertEquals(4, r.getSumWorkload(), 0);
			}
			
			assertTrue(((Organisation) n.getEstado()).isValid());
			assertEquals(1.0, ((Organisation) n.getEstado()).getPositionsTree().getGeneralness(), 0);

			System.out.println("The hierarchy is not being checked.");
			
    	} catch (CircularReference e) {
			e.printStackTrace();
		} catch (GoalNotFound e) {
			e.printStackTrace();
		} catch (OutputDoesNotMatchWithInput e) {
			e.printStackTrace();
		}
    }

	@Test
	public void testTwoPositionsGeneralistOrg() {
		try {
			System.out.println("\n\ntestTwoPositionsGeneralistOrg");

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
			s.prepareGenerationStatisticsFile("testTwoPositionsGeneralistOrg");

			System.out.println("Total workload is 10 -> goals must be assigned to two positions.");
			Organisation o = new Organisation("testTwoPositionsGeneralistOrg", gTree,
					Arrays.asList(Cost.GENERALIST, Cost.EFFICIENT), true);
			Nodo n = new BuscaLargura().busca(o);

			assertEquals(2, ((Organisation) n.getEstado()).getPositionsTree().getTree().size());
			assertEquals(10, ((Organisation) n.getEstado()).getPositionsTree().getSumWorkload(), 0);
			System.out.println("Generated tree: " + ((Organisation)n.getEstado()).getPositionsTree().getTree());
			for (PositionNode r : ((Organisation)n.getEstado()).getPositionsTree().getTree()) {
				System.out.println("Position: " + r + ", workloads: "+r.getWorkloads() + ", informs: "+r.getInforms());
				assertTrue(r.getWorkloads().size() >= 2);
				assertTrue(r.getSumWorkload() >= 3.75);
				// workload equals only checks the id of the workload
				assertTrue(r.getWorkloads().contains((new Workload("w0",0)))); 
				assertTrue(r.getWorkloads().contains((new Workload("w1",0))));
			}
			System.out.println("In generalist case, if granularity allows, each position must receive at least one workload w1 and one w2.");
			
			assertTrue(((Organisation) n.getEstado()).isValid());
			assertEquals(1.0, ((Organisation) n.getEstado()).getPositionsTree().getGeneralness(), 0);
			
			System.out.println("The hierarchy is not being checked.");
			
		} catch (CircularReference e) {
			e.printStackTrace();
		} catch (GoalNotFound e) {
			e.printStackTrace();
		} catch (OutputDoesNotMatchWithInput e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testThreePositionsGeneralistOrg() {
		try {
			System.out.println("\n\ntestThreePositionsGeneralistOrg");

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
			s.prepareGenerationStatisticsFile("testThreePositionsGeneralistOrg");

			System.out.println("Total workload is 21 -> goals must be assigned to three positions.");
			Organisation o = new Organisation("testThreePositionsGeneralistOrg", gTree,
					Arrays.asList(Cost.GENERALIST, Cost.EFFICIENT), true);
			Nodo n = new BuscaLargura().busca(o);

			assertEquals(3, ((Organisation) n.getEstado()).getPositionsTree().getTree().size());
			assertEquals(21, ((Organisation) n.getEstado()).getPositionsTree().getSumWorkload(), 0);
			System.out.println("Generated tree: " + ((Organisation)n.getEstado()).getPositionsTree().getTree());
			for (PositionNode r : ((Organisation)n.getEstado()).getPositionsTree().getTree()) {
				System.out.println("Position: " + r + ", workloads: "+r.getWorkloads() + ", informs: "+r.getInforms());
				assertEquals(2, r.getWorkloads().size(), 0);
				assertEquals(7, r.getSumWorkload(), 0);
				// workload equals only checks the id of the workload
				assertTrue(r.getWorkloads().contains((new Workload("w1",0)))); 
				assertTrue(r.getWorkloads().contains((new Workload("w2",0))));
			}
			System.out.println("In generalist case, if granularity allows, each position must receive at least one workload w1 and one w2.");
			
			assertTrue(((Organisation) n.getEstado()).isValid());
			// Generalness = (nAllOriginalGoalsAssigned - nMinOriginalGoalsSpread) / (nMaxOriginalGoalsSpread - nMinOriginalGoalsSpread);
			// since g0 has no workload, it cannot be broken and spread across the positions
			assertEquals((7.0 - 3.0)/(9.0 - 3.0), ((Organisation) n.getEstado()).getPositionsTree().getGeneralness(), 0.1);

			System.out.println("The hierarchy is not being checked.");
			
		} catch (CircularReference e) {
			e.printStackTrace();
		} catch (GoalNotFound e) {
			e.printStackTrace();
		} catch (OutputDoesNotMatchWithInput e) {
			e.printStackTrace();
		}
	}

}
