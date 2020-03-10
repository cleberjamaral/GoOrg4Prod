package organisation.exception;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import busca.BuscaLargura;
import busca.Nodo;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.search.Organisation;
import organisation.search.cost.Cost;

public class OrganisationException {

	@Test
	public void testOutputDoesNotMatchWithInput() {
		System.out.println("\n\ntestOutputDoesNotMatchWithInput");

		GoalNode g0 = new GoalNode(null, "g0");
		GoalTree gTree = GoalTree.getInstance();
		gTree.setRootNode(g0);
		try {
			gTree.addWorkload("g0", "w0", 1);
		} catch (Exception ex) {
			System.out.println("Exception catched: " + ex.getMessage());
		}

		System.out.println("GoalsTree: " + gTree.getTree().toString());
		Organisation o = new Organisation("testOutputDoesNotMatchWithInput", gTree, Cost.GENERALIST, true);
		Nodo n = new BuscaLargura().busca(o);

		System.out.println("Validate original gdt workloads with organisations' workloads (must be ok)");
		try {
			assertTrue(((Organisation) n.getEstado()).validateOutput());
		} catch (Exception ex) {
			System.out.println("Exception catched: " + ex.getMessage());
		}

		System.out.println("Force a wrong validation, which must throw an exception");

		try {
			gTree.addWorkload("g0", "w1", 1);
		} catch (Exception ex) {
			System.out.println("Exception catched: " + ex.getMessage());
		}
		assertThrows(OutputDoesNotMatchWithInput.class, () -> {
			((Organisation) n.getEstado()).validateOutput();
		});
	}

	@Test
	public void testRoleNotFound() {
		System.out.println("\n\ntestRoleNotFound");

		GoalNode g0 = new GoalNode(null, "g0");
		GoalTree gTree = GoalTree.getInstance();
		gTree.setRootNode(g0);
		try {
			gTree.addWorkload("g0", "w0", 1);
		} catch (Throwable ex) {
			System.out.println("Exception: " + ex.getMessage());
		}

		System.out.println("GoalsTree: " + gTree.getTree().toString());
		Organisation o = new Organisation("testRoleNotFound", gTree, Cost.GENERALIST, true);
		Nodo n = new BuscaLargura().busca(o);

		System.out.println("Roles tree: " + ((Organisation) n.getEstado()).getRolesTree());
		System.out.println("search for a nonexistant rolename");
		assertThrows(RoleNotFound.class, () -> {
			((Organisation) n.getEstado()).getRolesTree().findRoleByRoleName("nonexistant");
		});

	}

}
