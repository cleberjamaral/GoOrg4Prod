package organisation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import annotations.Workload;
import busca.BuscaLargura;
import busca.Nodo;
import organisation.exception.CircularReference;
import organisation.exception.GoalNotFound;
import organisation.exception.OutputDoesNotMatchWithInput;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.role.RoleNode;
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
    public void testOneRoleGeneralistOrg() {
    	try {
    		System.out.println("\n\ntestOneRoleGeneralistOrg");

    		// parameters
    		Parameters.getInstance();
    		Parameters.setMaxWorkload(8.0);
    		Parameters.setWorkloadGrain(2.0);
    		System.out.println("Max workload is 8");
    		System.out.println("workload grain is 2");
    		Parameters.setMaxDataLoad(8.0);
    		Parameters.setDataLoadGrain(2.0);
    		System.out.println("Max dataload is 8");
    		System.out.println("dataloadgrain is 2");
    		
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
			
			GoalNode g;
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g0"));
			System.out.println("g0 has no inform and dataloads: " + g.getInforms() + " - " + g.getDataLoads());
			assertEquals(0, g.getSumInform(), 0);
			assertEquals(0, g.getSumDataLoad(), 0);

			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g1$0"));
			System.out.println("g1$0 ~ g1$1 all have sum of inform: " + g.getSumInform() + ", details: " + g.getInforms());
			System.out.println("g1$0 ~ g1$1 all have sum of workload: " + g.getSumWorkload() + ", details: " + g.getWorkloads());
			assertEquals(2, g.getSumWorkload(), 0);
			assertEquals(1, g.getWorkloads().size(), 0);
			assertEquals(2, g.getSumInform(), 0);
			assertEquals(2, g.getInforms().size(), 0); 
			
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g2$0"));
			System.out.println("g2$0 ~ g2$1 all have sum of dataload: " + g.getSumDataLoad() + ", details: " + g.getDataLoads());
			assertEquals(2, g.getSumDataLoad(), 0);
			assertEquals(2, g.getDataLoads().size(), 0);

			//TODO: assert if inform was removed since it is circular
			
			OrganisationStatistics s = OrganisationStatistics.getInstance();
			s.prepareStatisticsFile("testOneRoleGeneralistOrg");
			
			System.out.println("Total workload is 4 (less than 8 - max) -> goals must be assigned to one role.");
			Organisation o = new Organisation("testOneRoleGeneralistOrg", gTree, Cost.GENERALIST, true);
			Nodo n = new BuscaLargura().busca(o);

			System.out.println("Generated rolesTree: " + ((Organisation)n.getEstado()).getRolesTree().getTree());

			assertEquals(1, ((Organisation)n.getEstado()).getRolesTree().getTree().size());
			assertEquals(4, ((Organisation)n.getEstado()).getRolesTree().getSumWorkload(), 0);

			for (RoleNode r : ((Organisation)n.getEstado()).getRolesTree().getTree()) {
				System.out.println("Role: " + r + ", workloads: "+r.getWorkloads() + ", informs: "+r.getInforms());
				// workload equals only checks the id of the workload
				assertTrue(r.getWorkloads().contains((new Workload("w1",0)))); 
				assertEquals(1, r.getWorkloads().size(), 0);
				assertEquals(4, r.getSumWorkload(), 0);
			}
			
			assertTrue(((Organisation) n.getEstado()).validateOutput());

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
	public void testTwoRolesGeneralistOrg() {
		try {
			System.out.println("\n\ntestTwoRolesGeneralistOrg");

			Parameters.getInstance();
			Parameters.setMaxWorkload(8.0);
			Parameters.setWorkloadGrain(4.0);
			System.out.println("Max workload is 8");
			System.out.println("workload grain is 4");
			Parameters.setMaxDataLoad(8.0);
			Parameters.setDataLoadGrain(2.0);
			System.out.println("Max dataload is 8");
			System.out.println("dataloadgrain is 2");

			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getInstance();
			gTree.setRootNode(g0);
			gTree.addGoal("g1", "g0");
			System.out.println("g1 must be split into three goals with 3.33 of workload each");
			gTree.addWorkload("g1", "w1", 5);
			gTree.addWorkload("g1", "w2", 5);
			System.out.println("g0 must be split into four goals with 1.75 of dataload each");
			gTree.addInform("g1", "i1", "g0", 7);
			
			gTree.brakeGoalTree();

			OrganisationStatistics s = OrganisationStatistics.getInstance();
			s.prepareStatisticsFile("testTwoRolesGeneralistOrg");

			System.out.println("Total workload is 10 -> goals must be assigned to two roles.");
			Organisation o = new Organisation("testTwoRolesGeneralistOrg", gTree, Cost.GENERALIST, true);
			Nodo n = new BuscaLargura().busca(o);

			assertEquals(2, ((Organisation) n.getEstado()).getRolesTree().getTree().size());
			assertEquals(10, ((Organisation) n.getEstado()).getRolesTree().getSumWorkload(), 0);
			System.out.println("Generated rolesTree: " + ((Organisation)n.getEstado()).getRolesTree().getTree());
			for (RoleNode r : ((Organisation)n.getEstado()).getRolesTree().getTree()) {
				System.out.println("Role: " + r + ", workloads: "+r.getWorkloads() + ", informs: "+r.getInforms());
				assertTrue(r.getWorkloads().size() >= 1);
				assertTrue(r.getSumWorkload() >= 3.33);
				// workload equals only checks the id of the workload
				assertTrue(r.getWorkloads().contains((new Workload("w1",0)))); 
				assertTrue(r.getWorkloads().contains((new Workload("w2",0))));
			}
			System.out.println("In generalist case, if granularity allows, each role must receive at least one workload w1 and one w2.");
			
			assertTrue(((Organisation) n.getEstado()).validateOutput());
			
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
	public void testThreeRolesGeneralistOrg() {
		try {
			System.out.println("\n\ntestThreeRolesGeneralistOrg");

			Parameters.getInstance();
			Parameters.setMaxWorkload(8.0);
			Parameters.setWorkloadGrain(4.0);
			System.out.println("Max workload is 8");
			System.out.println("workload grain is 4");
			Parameters.setMaxDataLoad(8.0);
			Parameters.setDataLoadGrain(2.0);
			System.out.println("Max dataload is 8");
			System.out.println("dataloadgrain is 2");

			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getInstance();
			gTree.setRootNode(g0);
			gTree.addGoal("g1", "g0");
			gTree.addWorkload("g1", "w1", 12);
			gTree.addGoal("g2", "g0");
			gTree.addWorkload("g2", "w2", 9);
			gTree.addInform("g1", "i1", "g2", 1);
			
			gTree.brakeGoalTree();

			OrganisationStatistics s = OrganisationStatistics.getInstance();
			s.prepareStatisticsFile("testThreeRolesGeneralistOrg");

			System.out.println("Total workload is 21 -> goals must be assigned to three roles.");
			Organisation o = new Organisation("testThreeRolesGeneralistOrg", gTree, Cost.GENERALIST, true);
			Nodo n = new BuscaLargura().busca(o);

			assertEquals(3, ((Organisation) n.getEstado()).getRolesTree().getTree().size());
			assertEquals(21, ((Organisation) n.getEstado()).getRolesTree().getSumWorkload(), 0);
			System.out.println("Generated rolesTree: " + ((Organisation)n.getEstado()).getRolesTree().getTree());
			for (RoleNode r : ((Organisation)n.getEstado()).getRolesTree().getTree()) {
				System.out.println("Role: " + r + ", workloads: "+r.getWorkloads() + ", informs: "+r.getInforms());
				assertEquals(2, r.getWorkloads().size(), 0);
				assertEquals(7, r.getSumWorkload(), 0);
				// workload equals only checks the id of the workload
				assertTrue(r.getWorkloads().contains((new Workload("w1",0)))); 
				assertTrue(r.getWorkloads().contains((new Workload("w2",0))));
			}
			System.out.println("In generalist case, if granularity allows, each role must receive at least one workload w1 and one w2.");
			
			assertTrue(((Organisation) n.getEstado()).validateOutput());

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
