package organisation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import organisation.goal.GoalNode;
import organisation.goal.GoalTree;

public class GoalTreeTest {
	
	@Test
	public void testFindAGoalByName() {
		GoalTree gTree = new GoalTree("g0");
		gTree.addGoal("g1", "g0");
		gTree.addGoal("g11", "g1", 1.333);
		gTree.addGoal("g12", "g1", 1);
		gTree.addWorkload("g11", "w11", 1);
		gTree.addWorkload("g12", "w12", 0.2);
		gTree.addInform("g11", "i11-i12", "g12", 1.6);
		
		assertEquals(gTree.getRootNode(), gTree.findAGoalByName(gTree.getRootNode(),"g0"));
		assertNotNull(gTree.findAGoalByName(gTree.getRootNode(),"g0"));
		assertNotNull(gTree.findAGoalByName(gTree.getRootNode(),"g1"));
		assertNotNull(gTree.findAGoalByName(gTree.getRootNode(),"g11"));
		assertNotNull(gTree.findAGoalByName(gTree.getRootNode(),"g12"));
		assertNull(gTree.findAGoalByName(gTree.getRootNode(),"g"));
		assertNull(gTree.findAGoalByName(gTree.getRootNode(),"g123"));
	}

	@Test
	public void testAddSuccessorsToList() {
		// create a set of nodes
		GoalNode g00 = new GoalNode(null, "g0");
		GoalNode g10 = new GoalNode(g00, "g1");
		GoalNode g11 = new GoalNode(g10, "g11");
		GoalNode g12 = new GoalNode(g10, "g12");
		
		// goals not linked with the others
		GoalNode g000 = new GoalNode(null, "g000");
		GoalNode g001 = new GoalNode(g000, "g001");
		
		// create a tree just with the root
		GoalTree gTree = new GoalTree(g00);
		
		// add all descendants of the given root
		gTree.addAllDescendants(g00);
		
		assertTrue(gTree.treeContains(g00));
		assertTrue(gTree.treeContains(g10));
		assertTrue(gTree.treeContains(g11));
		assertTrue(gTree.treeContains(g12));
		assertFalse(gTree.treeContains(g000));
		assertFalse(gTree.treeContains(g001));
		
				
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
}
