package organisation.position;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import annotations.Workload;
import organisation.exception.PositionNotFound;
import organisation.goal.GoalNode;
import organisation.position.PositionNode;
import organisation.position.PositionsTree;

public class PositionsTreeTest {

	@Test
	public void testCloneTree() {
		PositionsTree positionsTree = new PositionsTree();

		// supreme
		PositionNode r0 = new PositionNode(null, "r0");
		GoalNode g0 = new GoalNode(null, "g0");
		r0.assignGoal(g0);
		// a goal with its child assigned to the same position
		GoalNode g01 = new GoalNode(g0, "g01");
		r0.assignGoal(g01);
		r0.addWorkload(new Workload("s0", 8));

		PositionNode r1 = new PositionNode(r0, "r1");
		GoalNode g1 = new GoalNode(g0, "g1");
		r1.assignGoal(g1);
		r1.addWorkload(new Workload("s1", 1));

		PositionNode r2 = new PositionNode(r0, "r2");
		GoalNode g2 = new GoalNode(g0, "g2");
		r2.assignGoal(g2);
		// a goal which has as parent a sibling goal
		GoalNode g12 = new GoalNode(g1, "g12");
		r1.assignGoal(g12);
		r2.addWorkload(new Workload("s2", 2));
		r2.addWorkload(new Workload("s1", 1));

		PositionNode r21 = new PositionNode(r2, "r21");
		GoalNode g21 = new GoalNode(g2, "g21");
		r21.assignGoal(g21);
		r21.addWorkload(new Workload("s2", 2));
		r21.addWorkload(new Workload("s1", 1));

		positionsTree.addPositionToTree(r0);
		positionsTree.addPositionToTree(r1);
		positionsTree.addPositionToTree(r2);
		positionsTree.addPositionToTree(r21);

		// Test if each node was cloned and has same properties
		PositionsTree clonedTree;
		try {
			clonedTree = positionsTree.cloneContent();
			System.out.println("\n\ntestCloneTree");
			System.out.println("positionsTree : " + positionsTree);
			System.out.println("clonedTree: " + clonedTree);
			assertEquals(positionsTree.toString(), clonedTree.toString());
		} catch (PositionNotFound e) {
			e.printStackTrace();
		}
	}

	@Test
	public void countLevelsTest() {
		PositionsTree positionsTree = new PositionsTree();

		System.out.println("\n\ncountLevelsTest");

		// First level
		PositionNode r0 = new PositionNode(null, "r0");
		r0.assignGoal(new GoalNode(null, "g0"));
		positionsTree.addPositionToTree(r0);
		System.out.println("positionsTree : " + positionsTree.toString());
		assertEquals(1, positionsTree.getNumberOfLevels());

		// Second level
		PositionNode r1 = new PositionNode(r0, "r1");
		PositionNode r2 = new PositionNode(r0, "r2");
		r1.assignGoal(new GoalNode(null, "g1"));
		r2.assignGoal(new GoalNode(null, "g2"));
		positionsTree.addPositionToTree(r1);
		positionsTree.addPositionToTree(r2);
		System.out.println("positionsTree : " + positionsTree.toString());
		assertEquals(2, positionsTree.getNumberOfLevels());

		// Third level
		PositionNode r11 = new PositionNode(r1, "r11");
		PositionNode r12 = new PositionNode(r1, "r12");
		r11.assignGoal(new GoalNode(null, "g11"));
		r12.assignGoal(new GoalNode(null, "g12"));
		positionsTree.addPositionToTree(r11);
		positionsTree.addPositionToTree(r12);
		assertEquals(3, positionsTree.getNumberOfLevels());

		// Still third level
		PositionNode r21 = new PositionNode(r2, "r21");
		positionsTree.addPositionToTree(r21);
		System.out.println("positionsTree : " + positionsTree);
		assertEquals(3, positionsTree.getNumberOfLevels());

		// Forth level
		PositionNode r121 = new PositionNode(r12, "r121");
		positionsTree.addPositionToTree(r121);
		System.out.println("positionsTree : " + positionsTree);
		assertEquals(4, positionsTree.getNumberOfLevels());

		// Fifth level
		PositionNode r1211 = new PositionNode(r121, "r1211");
		positionsTree.addPositionToTree(r1211);
		System.out.println("positionsTree : " + positionsTree);
		assertEquals(5, positionsTree.getNumberOfLevels());
	}
}
