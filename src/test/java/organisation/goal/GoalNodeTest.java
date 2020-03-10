package organisation.goal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import annotations.Workload;
import organisation.exception.CircularReference;
import organisation.goal.GoalNode;

public class GoalNodeTest {

	@Test
	public void testSimilarGoals() {
		// a goal named g0
		GoalNode g00 = new GoalNode(null, "g0");
		// a goal name g1 which parent is g00 (but it must be ignored)
		GoalNode g10 = new GoalNode(g00, "g1");
		// a goal name g0 which parent is g00 (but it must be ignored)
		GoalNode g01 = new GoalNode(g00, "g0");
		// a goal name g1 with workload (but it must be ignored)
		GoalNode g11 = new GoalNode(g00, "g1");
		g11.addWorkload(new Workload("s1", 1));
		
		assertEquals(g00, g01);
		assertNotEquals(g00, g10);
		assertEquals(g10, g11);
	}
	
	@Test
	public void testContainsWorkload() {
		// a small tree, goal g11 has workload
		GoalNode g00 = new GoalNode(null, "g0");
		GoalNode g10 = new GoalNode(g00, "g1");
		GoalNode g11 = new GoalNode(g10, "g11");
		g11.addWorkload(new Workload("s1", 1));

		assertFalse(g00.containsWorkload());
		assertFalse(g10.containsWorkload());
		assertTrue(g11.containsWorkload());
	}
	
	@Test
	public void testSimilarCollections() {
		// a goal named g0
		GoalNode g00 = new GoalNode(null, "g0");
		// adding a workload s1 with value 1
		g00.addWorkload(new Workload("s1", 1));
		// a goal name g1 which parent is g00 (but it must be ignored)
		GoalNode g10 = new GoalNode(g00, "g0");
		// adding a workload s1 with value 3 (name is same, so it is equal)
		g10.addWorkload(new Workload("s1", 3));

		System.out.println("\n\ntestSimilarCollections");
		System.out.println("r00: " + g00.getWorkloads());
		System.out.println("r01: " + g10.getWorkloads());
		assertEquals(g00.getWorkloads(), g10.getWorkloads());

		// adding a same s1 with value 1, sum should be 4 (name is same, so it is equal)
		g10.addWorkload(new Workload("s1", 1));
		System.out.println("r00: " + g00.getWorkloads());
		System.out.println("r01: " + g10.getWorkloads());
		assertEquals(g00.getWorkloads(), g10.getWorkloads());
		assertTrue(g00.getWorkloads().containsAll(g10.getWorkloads()));
		assertTrue(g10.getWorkloads().containsAll(g00.getWorkloads()));

		double sumS1 = 0;
		for (Workload w : g10.getWorkloads()) sumS1 += (double) w.getValue();
		System.out.println("sumS1: " + (int)sumS1);
		assertEquals((int)sumS1, 4);

		// adding a different element
		g10.addWorkload(new Workload("s2", 0));
		System.out.println("r00: " + g00.getWorkloads());
		System.out.println("r01: " + g10.getWorkloads());

		assertNotEquals(g00.getWorkloads(), g10.getWorkloads());
		assertFalse(g00.getWorkloads().containsAll(g10.getWorkloads()));
		assertTrue(g10.getWorkloads().containsAll(g00.getWorkloads()));	
	}
	
	@Test
	public void testCircularReference() {
		System.out.println("\n\ntestOutputDoesNotMatchWithInput");

		GoalNode g0 = new GoalNode(null, "g0");
		GoalTree gTree = GoalTree.getInstance();
		gTree.setRootNode(g0);

		System.out.println("Adding a circular inform");

		assertThrows(CircularReference.class, () -> {
			gTree.addInform("g0", "i0", "g0", 1);
		});
	}
}
