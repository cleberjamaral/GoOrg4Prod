package organisation;

import busca.BuscaLargura;
import busca.Nodo;
import organisation.exception.MoreThanOneRootRoleFound;
import organisation.exception.OutputDoesNotMatchWithInput;
import organisation.exception.RoleNotFound;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.role.RoleNode;
import organisation.role.RoleTree;
import organisation.search.Cost;
import organisation.search.Organisation;
import properties.Workload;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.Test;

public class OrganisationTest {

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
		System.out.println("r0    : " + r0.toString());
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
		} catch (MoreThanOneRootRoleFound | RoleNotFound e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testOrg() {

		double maxEffort = 8;
		// Sample organization
		GoalTree t = new GoalTree("g1");
		t.addGoal("g11", "g1");
		t.addWorkload("g11", "s1", 2);
		t.addGoal("g12", "g1");
		t.addGoal("g111", "g11");
		t.addWorkload("g111", "s2", 8);
		t.addGoal("g112", "g11");
		t.addWorkload("g112", "s2", 2);
		t.addGoal("g121", "g12");
		t.addWorkload("g121", "s2", 0);
		t.addGoal("g122", "g12");
		t.addGoal("g1221", "g122");
		t.addWorkload("g1221", "s2", 3.4);
		
		List<String> proofs = new ArrayList<>();
		List<String> outputs = new ArrayList<>();

		OrganisationPlot p = new OrganisationPlot();
		p.deleteExistingDiagrams();

		// BE CAREFULL! if generateproof is true, the assertion should be always true
		// After generating proofs it must be checked manually and then turn this
		// argument false for further right assertions
		boolean generatingProofsInCheckingMode = false;
		if (generatingProofsInCheckingMode) p.deleteExistingProofs();

		Cost cost[] = Cost.values();
		for (Cost c : cost) {
			Organisation o = new Organisation(t.getBrokenGoalTree(maxEffort), c, !generatingProofsInCheckingMode);
			Nodo n = new BuscaLargura().busca(o);
			outputs.add(n.getEstado().toString());
			try {
				assertTrue(((Organisation)n.getEstado()).validateOutput());
			} catch (OutputDoesNotMatchWithInput e1) {
				e1.printStackTrace();
			}

			p.plotOrganisation((Organisation) n.getEstado(), c.name(), generatingProofsInCheckingMode);

			String proof = "";
			BufferedReader fr;
			try {
				fr = new BufferedReader(new FileReader("output/proofs/graph_" + c.name() + ".txt"));
				proof = fr.readLine();
				proofs.add(proof);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Test all outputs against their proofs
		for (int i = 0; i < outputs.size(); i++) {
			System.out.println("\n\ntestOrg");
			System.out.println("proof : " + proofs.get(i));
			System.out.println("output: " + outputs.get(i));
			assertEquals(proofs.get(i), outputs.get(i));
		}
	}
}
