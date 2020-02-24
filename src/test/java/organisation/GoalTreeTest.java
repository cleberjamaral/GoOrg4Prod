package organisation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import organisation.exception.CircularReference;
import organisation.exception.GoalNotFound;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.search.Parameters;

public class GoalTreeTest {
	
	@Test
	public void testFindAGoalByName() {
		System.out.println("\n\ntestFindAGoalByName");
		
		GoalNode g0 = new GoalNode(null, "g0");
		GoalTree gTree = GoalTree.getCleanInstance();
		gTree.setRootNode(g0);

		gTree.addGoal("g1", "g0");
		try {
			gTree.addGoal("g11", "g1", 1.333);
			gTree.addGoal("g12", "g1", 1);
			gTree.addWorkload("g11", "w11", 1);
			gTree.addWorkload("g12", "w12", 0.2);
			gTree.addInform("g11", "i11-i12", "g12", 1.6);
		} catch (CircularReference e) {
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
		GoalTree gTree = GoalTree.getCleanInstance();
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
		System.out.println("Max workload is 8");
		Parameters.setMaxWorkload(8.0);
		try {
			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getCleanInstance();
			gTree.setRootNode(g0);
			gTree.addGoal("g1", "g0");
			System.out.println("g1 must be split into two goals with 7.5 of workload each");
			gTree.addWorkload("g1", "w1", 15);
			gTree.brakeGoalTree();
			
			GoalNode g;
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g1$0"));
			System.out.println("g1$0 has a sum of workload of: "+g.getSumWorkload());
			assertTrue(7.50 == g.getSumWorkload());
			
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g1$1"));
			System.out.println("g1$1 has a sum of workload of: "+g.getSumWorkload());
			assertTrue(7.50 == g.getSumWorkload());
		} catch (GoalNotFound e) {
			e.printStackTrace();
		}
	}
		
	@Test
	public void testBrakeSimpleGDTByDataload() {
		System.out.println("\n\ntestBrakeSimpleGDTByDataload");
		System.out.println("Max dataload is 8");
		Parameters.setMaxDataLoad(8.0);
		try {
			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getCleanInstance();
			gTree.setRootNode(g0);
			gTree.addGoal("g1", "g0");
			gTree.addGoal("g2", "g1");
			System.out.println("g2 must be split into two goals with 6.75 of dataload each");
			gTree.addInform("g1", "i1", "g2", 13.5);
			gTree.brakeGoalTree();
			
			GoalNode g;
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g0"));
			System.out.println("g0 has no inform and dataloads");
			assertTrue(0 == g.getSumInform());
			assertTrue(0 == g.getSumDataLoad());

			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g1"));
			System.out.println("g1 has a sum of inform of: "+g.getSumInform());
			assertTrue(13.5 == g.getSumInform());
			
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g2$0"));
			System.out.println("g2$0 has a sum of dataload of: "+g.getSumDataLoad());
			assertTrue(6.75 == g.getSumDataLoad());
			
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g2$1"));
			System.out.println("g2$1 has a sum of dataload of: "+g.getSumDataLoad());
			assertTrue(6.75 == g.getSumDataLoad());
		} catch (GoalNotFound e) {
			e.printStackTrace();
		} catch (CircularReference e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void testBrakeRootByDataload() {
		System.out.println("\n\ntestBrakeRootByDataload");
		System.out.println("Max dataload is 8");
		try {
			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getCleanInstance();
			gTree.setRootNode(g0);
			gTree.addGoal("g1", "g0");
			gTree.addInform("g1", "i1", "g0", 12.5);
			System.out.println("g0 must be split into two goals with 6.25 of dataload each");
			gTree.brakeGoalTree();
			
			GoalNode g;
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g1"));
			System.out.println("g1 has a sum of inform of: "+g.getSumInform());
			assertTrue(12.5 == g.getSumInform());

			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g0$0"));
			System.out.println("g0$0 has a sum of dataload of: "+g.getSumDataLoad());
			assertTrue(6.25 == g.getSumDataLoad());
			
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g0$1"));
			System.out.println("g0$1 has a sum of dataload of: "+g.getSumDataLoad());
			assertTrue(6.25 == g.getSumDataLoad());
		} catch (CircularReference e) {
			e.printStackTrace();
		} catch (GoalNotFound e) {
			e.printStackTrace();
		}
	}

}
