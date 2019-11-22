package organisation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import annotations.Workload;
import organisation.goal.GoalNode;
import organisation.role.RoleNode;

public class RoleNodeTest {

	@Test
	public void testSimilarRoles() {
		// a root role named r0 with g0 assigned and s0 workload
		RoleNode r00 = new RoleNode(null, "r0");
		GoalNode g00 = new GoalNode(null, "g0");
		r00.addWorkload(new Workload("s0", 8));
		r00.assignGoal(g00);

		// a role name r1 with g0 assigned and s0 workload
		// it has r00 as parent which must be ignored
		RoleNode r01 = new RoleNode(r00, "r0");
		GoalNode g01 = new GoalNode(null, "g0");
		r01.addWorkload(new Workload("s0", 8));
		r01.assignGoal(g01);

		System.out.println("\n\ntestSimilarRoles");
		System.out.println("r00: " + r00.signature());
		System.out.println("r01: " + r01.signature());
		assertEquals(r00, r01);
		
		//adding another workload to r01 to make than different
		r01.addWorkload(new Workload("s1", 2));
		System.out.println("must be different");
		System.out.println("r00: " + r00.signature());
		System.out.println("r01: " + r01.signature());
		assertNotEquals(r00, r01);
		
		//adding another workload to r00 to be added up 
		r00.addWorkload(new Workload("s1", 1.5));
		r00.addWorkload(new Workload("s1", 0.5));
		System.out.println("must be equal");
		System.out.println("r00: " + r00.signature());
		System.out.println("r01: " + r01.signature());
		assertEquals(r00, r01);
		
		//adding new goals to r00
		GoalNode g10 = new GoalNode(g00, "a");
		GoalNode g11 = new GoalNode(g00, "z");
		r00.assignGoal(g10);
		r00.assignGoal(g11);
		System.out.println("must be different");
		System.out.println("r00: " + r00.signature());
		System.out.println("r01: " + r01.signature());
		assertNotEquals(r00, r01);
		
		//adding new goals to r01 in a different order
		GoalNode g20 = new GoalNode(g01, "a");
		GoalNode g21 = new GoalNode(g01, "z");
		r01.assignGoal(g21);
		r01.assignGoal(g20);
		System.out.println("must be equal");
		System.out.println("r00: " + r00.signature());
		System.out.println("r01: " + r01.signature());
		assertEquals(r00, r01);
	}

	@Test
	public void testCloneRoleContent() {
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

		// parent's signature
		System.out.println("\n\ntestCloneRoleContent");
		System.out.println("r0    : " + r0.signature());
		System.out.println("parent: " + r1.getParentSignature());
		assertEquals(r0.toString(), r1.getParentSignature());

		RoleNode r0clone = r0.cloneContent();
		RoleNode r1clone = r1.cloneContent();
		r1clone.setParent(r0clone);

		assertEquals(r1.toString(), r1clone.toString());

		System.out.println("\n\ntestCloneRoleContent");
		System.out.println("r1     : " + r1);
		System.out.println("r1clone: " + r1clone);
	}
}
