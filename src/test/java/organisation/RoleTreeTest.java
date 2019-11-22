package organisation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import annotations.Workload;
import organisation.exception.DuplicatedRootRole;
import organisation.exception.RoleNotFound;
import organisation.goal.GoalNode;
import organisation.role.RoleNode;
import organisation.role.RoleTree;

public class RoleTreeTest {

	@Test
	public void testCloneTree() {
		RoleTree rolesTree = new RoleTree();

		// root role
		RoleNode r0 = new RoleNode(null, "r0");
		GoalNode g0 = new GoalNode(null, "g0");
		r0.assignGoal(g0);
		// a goal with its child assigned to the same role
		GoalNode g01 = new GoalNode(g0, "g01");
		r0.assignGoal(g01);
		r0.addWorkload(new Workload("s0", 8));

		RoleNode r1 = new RoleNode(r0, "r1");
		GoalNode g1 = new GoalNode(g0, "g1");
		r1.assignGoal(g1);
		r1.addWorkload(new Workload("s1", 1));

		RoleNode r2 = new RoleNode(r0, "r2");
		GoalNode g2 = new GoalNode(g0, "g2");
		r2.assignGoal(g2);
		// a goal which has as parent a sibling goal
		GoalNode g12 = new GoalNode(g1, "g12");
		r1.assignGoal(g12);
		r2.addWorkload(new Workload("s2", 2));
		r2.addWorkload(new Workload("s1", 1));

		RoleNode r21 = new RoleNode(r2, "r21");
		GoalNode g21 = new GoalNode(g2, "g21");
		r21.assignGoal(g21);
		r21.addWorkload(new Workload("s2", 2));
		r21.addWorkload(new Workload("s1", 1));

		rolesTree.add(r0);
		rolesTree.add(r1);
		rolesTree.add(r2);
		rolesTree.add(r21);

		// Test if each node was cloned and has same properties
		RoleTree clonedTree;
		try {
			clonedTree = rolesTree.cloneContent();
			System.out.println("\n\ntestCloneTree");
			System.out.println("rolesTree : " + rolesTree);
			System.out.println("clonedTree: " + clonedTree);
			assertEquals(rolesTree.toString(), clonedTree.toString());
		} catch (DuplicatedRootRole | RoleNotFound e) {
			e.printStackTrace();
		}
	}
	
}
