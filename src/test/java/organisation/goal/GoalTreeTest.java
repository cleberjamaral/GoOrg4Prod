package organisation.goal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import organisation.Parameters;
import organisation.exception.CircularReference;
import organisation.exception.GoalNotFound;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;

public class GoalTreeTest {

	@Before
	public void resetGoalTreeSingleton() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
	   Field instance = GoalTree.class.getDeclaredField("instance");
	   instance.setAccessible(true);
	   instance.set(null, null);
	}

	@Test
	public void testFindAGoalByName() {
		System.out.println("\n\ntestFindAGoalByName");
		
		GoalNode g0 = new GoalNode(null, "g0");
		GoalTree gTree = GoalTree.getInstance();
		gTree.setRootNode(g0);

		try {
			gTree.addGoal("g1", "g0");
			gTree.addGoal("g11", "g1", 1.333);
			gTree.addGoal("g12", "g1", 1);
			gTree.addWorkload("g11", "w11", 1);
			gTree.addWorkload("g12", "w12", 0.2);
			gTree.addInform("g11", "i11-i12", "g12", 1.6);
		} catch (CircularReference e) {
			e.printStackTrace();
		} catch (GoalNotFound e) {
			e.printStackTrace();
		}
		
		System.out.println("Making sure root goal can be found");
		assertEquals(gTree.getRootNode(), gTree.findAGoalByName(gTree.getRootNode(),"g0"));
		System.out.println("Asserting if existing goals are found");
		assertNotNull(gTree.findAGoalByName(gTree.getRootNode(),"g0"));
		assertNotNull(gTree.findAGoalByName(gTree.getRootNode(),"g1"));
		assertNotNull(gTree.findAGoalByName(gTree.getRootNode(),"g11"));
		assertNotNull(gTree.findAGoalByName(gTree.getRootNode(),"g12"));
		System.out.println("Asserting if unexisting goals are not returned");
		assertNull(gTree.findAGoalByName(gTree.getRootNode(),"g"));
		assertNull(gTree.findAGoalByName(gTree.getRootNode(),"g123"));
	}

	@Test
	public void testAddSuccessorsToList() {
		System.out.println("\n\ntestAddSuccessorsToList");

		// create a set of nodes
		GoalNode g00 = new GoalNode(null, "g0");
		GoalNode g10 = new GoalNode(g00, "g1");
		GoalNode g11 = new GoalNode(g10, "g11");
		GoalNode g12 = new GoalNode(g10, "g12");
		
		// goals not linked with the others
		GoalNode g000 = new GoalNode(null, "g000");
		GoalNode g001 = new GoalNode(g000, "g001");
		
		// create a tree just with the root
		GoalTree gTree = GoalTree.getInstance();
		gTree.setRootNode(g00);
		
		// add all descendants of the given root
		System.out.println("Adding descendants of the root to the tree");
		gTree.addAllDescendants(g00);
		
		System.out.println("Asserting if the tree has all descendants and only them");
		assertTrue(gTree.treeContains(g00));
		assertTrue(gTree.treeContains(g10));
		assertTrue(gTree.treeContains(g11));
		assertTrue(gTree.treeContains(g12));
		assertFalse(gTree.treeContains(g000));
		assertFalse(gTree.treeContains(g001));
		
				
		System.out.println("Asserting if the goals successors is showing correct list");
		List<GoalNode> goalSuccessors = new ArrayList<GoalNode>();
		gTree.addSuccessorsToList(goalSuccessors, gTree.getRootNode());
		
		assertTrue(goalSuccessors.contains(g10));
		assertTrue(goalSuccessors.contains(g11));
		assertTrue(goalSuccessors.contains(g12));
		assertFalse(goalSuccessors.contains(g000));
		assertFalse(goalSuccessors.contains(g001));
		// cannot contain the root itself
		assertFalse(goalSuccessors.contains(g00));
	}
	
	@Test
	public void testBrakeSimpleGDTByWorkload() {
		System.out.println("\n\ntestBrakeSimpleGDTByWorkload");

		// parameters
		Parameters.getInstance();
		Parameters.setMaxWorkload(8.0);
		Parameters.setWorkloadGrain(8.0);
		System.out.println("Max workload is 8");
		System.out.println("workload grain is 8");
		
		try {
			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getInstance();
			gTree.setRootNode(g0);
			gTree.addGoal("g1", "g0");
			System.out.println("g1 must be split into two goals with 7.5 of workload each");
			gTree.addWorkload("g1", "w1", 15);
			gTree.brakeGoalTree();
			
			GoalNode g;
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g1$0"));
			System.out.println("g1$0 has a sum of workload of: "+g.getSumWorkload());
			assertEquals(7.50, g.getSumWorkload(), 0);
			assertEquals(1, g.getWorkloads().size(), 0);
			
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g1$1"));
			System.out.println("g1$1 has a sum of workload of: "+g.getSumWorkload());
			assertEquals(7.50, g.getSumWorkload(), 0);
			assertEquals(1, g.getWorkloads().size(), 0);
		} catch (GoalNotFound e) {
			e.printStackTrace();
		}
	}
		
	@Test
	public void testBrakeSimpleGDTByDataload() {
		System.out.println("\n\ntestBrakeSimpleGDTByDataload");
		
		// parameters
		Parameters.getInstance();
		Parameters.setMaxDataLoad(8.0);
		Parameters.setDataLoadGrain(8.0);
		System.out.println("Max dataload is 8");
		System.out.println("dataload grain is 8");


		try {
			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getInstance();
			gTree.setRootNode(g0);
			gTree.addGoal("g1", "g0");
			gTree.addGoal("g2", "g1");
			System.out.println("g2 must be split into two goals with 6.75 of dataload each");
			gTree.addInform("g1", "i1", "g2", 13.5);
			gTree.brakeGoalTree();
			
			GoalNode g;
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g0"));
			System.out.println("g0 has no inform and dataloads: " + g.getInforms() + " - " + g.getDataLoads());
			assertEquals(0, g.getSumInform(), 0);
			assertEquals(0, g.getSumDataLoad(), 0);
			assertEquals(0, g.getInforms().size(), 0);
			assertEquals(0, g.getDataLoads().size(), 0);

			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g1"));
			System.out.println("g1 sum of inform: " + g.getSumInform() + ", details: " + g.getInforms());
			assertEquals(13.5, g.getSumInform(), 0);
			assertEquals(2, g.getInforms().size(), 0);
			
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g2$0"));
			System.out.println("g2$0 sum of dataload: " + g.getSumDataLoad() + ", details: " + g.getDataLoads());
			assertEquals(6.75, g.getSumDataLoad(), 0);
			assertEquals(1, g.getDataLoads().size(), 0);
			
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g2$1"));
			System.out.println("g2$1 sum of dataload: " + g.getSumDataLoad() + ", details: " + g.getDataLoads());
			assertEquals(6.75, g.getSumDataLoad(), 0);
			assertEquals(1, g.getDataLoads().size(), 0);
		} catch (GoalNotFound e) {
			e.printStackTrace();
		} catch (CircularReference e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testBrakeRootByDataload() {
		System.out.println("\n\ntestBrakeRootByDataload");
		
		// parameters
		Parameters.getInstance();
		Parameters.setMaxDataLoad(8.0);
		Parameters.setDataLoadGrain(8.0);
		System.out.println("Max dataload is 8");
		System.out.println("dataload grain is 8");
		
		try {
			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getInstance();
			gTree.setRootNode(g0);
			gTree.addGoal("g1", "g0");
			gTree.addInform("g1", "i1", "g0", 12.5);
			System.out.println("g0 must be split into two goals with 6.25 of dataload each");
			gTree.brakeGoalTree();
			
			GoalNode g;
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g1"));
			System.out.println("g1 sum of inform: " + g.getSumInform() + ", details: " + g.getInforms());
			assertEquals(12.5, g.getSumInform(), 0);
			assertEquals(2, g.getInforms().size(), 0);

			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g0$0"));
			System.out.println("g0$0 sum of dataload: "+g.getSumDataLoad() + ", details: " + g.getDataLoads());
			assertEquals(6.25, g.getSumDataLoad(), 0);
			assertEquals(1, g.getDataLoads().size(), 0);
			
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g0$1"));
			System.out.println("g0$1 sum of dataload: "+g.getSumDataLoad() + ", details: " + g.getDataLoads());
			assertEquals(6.25, g.getSumDataLoad(), 0);
			assertEquals(1, g.getDataLoads().size(), 0);
		} catch (CircularReference e) {
			e.printStackTrace();
		} catch (GoalNotFound e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testBrakeWithGranularityGDTByWorkload() {
		System.out.println("\n\ntestBrakeSimpleGDTByWorkload");

		// parameters
		Parameters.getInstance();
		Parameters.setMaxWorkload(8.0);
		Parameters.setWorkloadGrain(4.0);
		System.out.println("Max workload is 8");
		System.out.println("workload grain is 4");
		
		try {
			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getInstance();
			gTree.setRootNode(g0);
			gTree.addGoal("g1", "g0");
			System.out.println("g1 must be split into four goals with 3.75 of workload each");
			gTree.addWorkload("g1", "w1", 15);
			gTree.brakeGoalTree();
			
			GoalNode g;
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g1$0"));
			System.out.println("g1$0 ~ g1$3 sum workload of: " + g.getSumWorkload() + ", details: " + g.getWorkloads());
			assertEquals(3.75, g.getSumWorkload(), 0);
			assertEquals(1, g.getWorkloads().size(), 0);
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g1$1"));
			assertEquals(3.75, g.getSumWorkload(), 0);
			assertEquals(1, g.getWorkloads().size(), 0);
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g1$2"));
			assertEquals(3.75, g.getSumWorkload(), 0);
			assertEquals(1, g.getWorkloads().size(), 0);
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g1$3"));
			assertEquals(3.75, g.getSumWorkload(), 0);
			assertEquals(1, g.getWorkloads().size(), 0);
		} catch (GoalNotFound e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testBrakeWithGranularityGDTByDataload() {
		System.out.println("\n\ntestBrakeWithGranularityGDTByDataload");
		
		// parameters
		Parameters.getInstance();
		Parameters.setMaxDataLoad(8.0);
		Parameters.setDataLoadGrain(2.0);
		System.out.println("Max dataload is 8");
		System.out.println("dataloadgrain is 2");

		try {
			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getInstance();
			gTree.setRootNode(g0);
			gTree.addGoal("g1", "g0");
			gTree.addGoal("g2", "g1");
			System.out.println("g2 must be split into two goals with 6.75 of dataload each");
			gTree.addInform("g1", "i1", "g2", 13.5);
			gTree.brakeGoalTree();
			
			GoalNode g;
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g0"));
			System.out.println("g0 has no inform and dataloads: " + g.getInforms() + " - " + g.getDataLoads());
			assertEquals(0, g.getSumInform(), 0);
			assertEquals(0, g.getSumDataLoad(), 0);

			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g1"));
			System.out.println("g1 sum of inform: " + g.getSumInform() + ", details: " + g.getInforms());
			assertEquals(13.5, g.getSumInform(), 0);
			assertEquals(7, g.getInforms().size(), 0);
			
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g2$0"));
			System.out.println("g2$0 ~ g2$6 all have sum of dataload: " + g.getSumDataLoad() + ", details: " + g.getDataLoads());
			assertEquals(1.93, g.getSumDataLoad(), 0.01);
			assertEquals(1, g.getDataLoads().size(), 0);
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g2$1"));
			assertEquals(1.93, g.getSumDataLoad(), 0.01);
			assertEquals(1, g.getDataLoads().size(), 0);
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g2$2"));
			assertEquals(1.93, g.getSumDataLoad(), 0.01);
			assertEquals(1, g.getDataLoads().size(), 0);
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g2$3"));
			assertEquals(1.93, g.getSumDataLoad(), 0.01);
			assertEquals(1, g.getDataLoads().size(), 0);
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g2$4"));
			assertEquals(1.93, g.getSumDataLoad(), 0.01);
			assertEquals(1, g.getDataLoads().size(), 0);
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g2$5"));
			assertEquals(1.93, g.getSumDataLoad(), 0.01);
			assertEquals(1, g.getDataLoads().size(), 0);
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g2$6"));
			assertEquals(1.93, g.getSumDataLoad(), 0.01);
			assertEquals(1, g.getDataLoads().size(), 0);
		} catch (GoalNotFound e) {
			e.printStackTrace();
		} catch (CircularReference e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testBrakeWithGranularityGDTByWorkloadAndDataload() {
		System.out.println("\n\ntestBrakeWithGranularityGDTByWorkloadAndDataload");
		
		// parameters
		Parameters.getInstance();
		Parameters.setMaxWorkload(8.0);
		Parameters.setWorkloadGrain(4.0);
		System.out.println("Max workload is 8");
		System.out.println("workload grain is 4");
		Parameters.setMaxDataLoad(8.0);
		Parameters.setDataLoadGrain(2.0);
		System.out.println("Max dataload is 8");
		System.out.println("dataloadgrain is 2");

		try {
			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getInstance();
			gTree.setRootNode(g0);
			gTree.addGoal("g1", "g0");
			gTree.addGoal("g2", "g1");
			System.out.println("g1 must be split into four goals with 3.5 of workload each");
			gTree.addWorkload("g1", "w1", 14);
			System.out.println("g2 must be split into four goals with 1.85 of dataload each");
			gTree.addInform("g1", "i1", "g2", 7.4);
			gTree.brakeGoalTree();
			
			GoalNode g;
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g0"));
			System.out.println("g0 has no inform and dataloads: " + g.getInforms() + " - " + g.getDataLoads());
			assertEquals(0, g.getSumInform(), 0);
			assertEquals(0, g.getSumDataLoad(), 0);

			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g1$0"));
			System.out.println("g1$0 ~ g1$3 all have sum of inform: " + g.getSumInform() + ", details: " + g.getInforms());
			System.out.println("g1$0 ~ g1$3 all have sum of workload: " + g.getSumWorkload() + ", details: " + g.getWorkloads());
			assertEquals(3.5, g.getSumWorkload(), 0);
			assertEquals(1, g.getWorkloads().size(), 0);
			assertEquals(1.85, g.getSumInform(), 0);
			assertEquals(4, g.getInforms().size(), 0);
			
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g2$0"));
			System.out.println("g2$0 ~ g2$3 all have sum of dataload: " + g.getSumDataLoad() + ", details: " + g.getDataLoads());
			assertEquals(1.85, g.getSumDataLoad(), 0);
			assertEquals(4, g.getDataLoads().size(), 0);
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g2$1"));
			assertEquals(1.85, g.getSumDataLoad(), 0);
			assertEquals(4, g.getDataLoads().size(), 0);
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g2$2"));
			assertEquals(1.85, g.getSumDataLoad(), 0);
			assertEquals(4, g.getDataLoads().size(), 0);
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g2$3"));
			assertEquals(1.85, g.getSumDataLoad(), 0);
			assertEquals(4, g.getDataLoads().size(), 0);
		} catch (GoalNotFound e) {
			e.printStackTrace();
		} catch (CircularReference e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testBrakeRootWithGranularityGDTByWorkloadAndDataload() {
		System.out.println("\n\ntestBrakeWithGranularityGDTByWorkloadAndDataload");
		
		// parameters
		Parameters.getInstance();
		Parameters.setMaxWorkload(8.0);
		Parameters.setWorkloadGrain(4.0);
		System.out.println("Max workload is 8");
		System.out.println("workload grain is 4");
		Parameters.setMaxDataLoad(8.0);
		Parameters.setDataLoadGrain(2.0);
		System.out.println("Max dataload is 8");
		System.out.println("dataloadgrain is 2");

		try {
			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getInstance();
			gTree.setRootNode(g0);
			gTree.addGoal("g1", "g0");
			System.out.println("g1 must be split into four goals with 3.5 of workload each");
			gTree.addWorkload("g1", "w1", 14);
			System.out.println("g0 must be split into four goals with 1.85 of dataload each");
			gTree.addInform("g1", "i1", "g0", 7.4);
			gTree.brakeGoalTree();
			
			GoalNode g;
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g0$0"));
			System.out.println("g0$0 ~ g0$3 have sum of dataload: " + g.getSumDataLoad() + " - " + g.getDataLoads());
			assertEquals(0, g.getSumInform(), 0);
			assertEquals(0, g.getSumWorkload(), 0);
			assertEquals(1.85, g.getSumDataLoad(), 0);
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g0$1"));
			assertEquals(0, g.getSumInform(), 0);
			assertEquals(0, g.getSumWorkload(), 0);
			assertEquals(1.85, g.getSumDataLoad(), 0);
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g0$2"));
			assertEquals(0, g.getSumInform(), 0);
			assertEquals(0, g.getSumWorkload(), 0);
			assertEquals(1.85, g.getSumDataLoad(), 0);
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g0$3"));
			assertEquals(0, g.getSumInform(), 0);
			assertEquals(0, g.getSumWorkload(), 0);
			assertEquals(1.85, g.getSumDataLoad(), 0);

			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g1$0"));
			System.out.println("g1$0 ~ g1$3 all have sum of inform: " + g.getSumInform() + ", details: " + g.getInforms());
			System.out.println("g1$0 ~ g1$3 all have sum of workload: " + g.getSumWorkload() + ", details: " + g.getWorkloads());
			assertEquals(3.5, g.getSumWorkload(), 0);
			assertEquals(1, g.getWorkloads().size(), 0);
			assertEquals(1.85, g.getSumInform(), 0);
			assertEquals(4, g.getInforms().size(), 0);
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g1$1"));
			assertEquals(3.5, g.getSumWorkload(), 0);
			assertEquals(1, g.getWorkloads().size(), 0);
			assertEquals(1.85, g.getSumInform(), 0);
			assertEquals(4, g.getInforms().size(), 0);
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g1$2"));
			assertEquals(3.5, g.getSumWorkload(), 0);
			assertEquals(1, g.getWorkloads().size(), 0);
			assertEquals(1.85, g.getSumInform(), 0);
			assertEquals(4, g.getInforms().size(), 0);
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g1$3"));
			assertEquals(3.5, g.getSumWorkload(), 0);
			assertEquals(1, g.getWorkloads().size(), 0);
			assertEquals(1.85, g.getSumInform(), 0);
			assertEquals(4, g.getInforms().size(), 0);
			
		} catch (GoalNotFound e) {
			e.printStackTrace();
		} catch (CircularReference e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testPositionNotFound() {
		System.out.println("\n\ntestPositionNotFound");
		
        Throwable e = null;
        try {
    		// Sample organization
    		GoalNode g1 = new GoalNode(null, "g1");
    		GoalTree gTree = GoalTree.getInstance();
    		gTree.setRootNode(g1);
    		System.out.println("Goals Tree:" + gTree.getTree());
    		
    		System.out.println("adding a workload to g1, which really exists");
    		gTree.addWorkload("g1", "w1", 1);
    		
    		System.out.println("adding a workload to g2 which does not exist, must throw an exception");
    		gTree.addWorkload("g2", "w2", 1);
        } catch(Throwable ex) {
            e = ex;
        }
        assertTrue(e instanceof GoalNotFound);
	}

}
