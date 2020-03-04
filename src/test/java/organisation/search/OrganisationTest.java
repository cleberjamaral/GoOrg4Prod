package organisation.search;

import busca.BuscaLargura;
import busca.MostraStatusConsole;
import busca.Nodo;
import organisation.OrganisationStatistics;
import organisation.Parameters;
import organisation.exception.GoalNotFound;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.role.RoleNode;
import organisation.search.Organisation;
import organisation.search.cost.Cost;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class OrganisationTest {

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
	public void testOrgWithNoWorkload() {
		System.out.println("\n\ntestOrgSingleGoals");
		
		//parameters
		Parameters.getInstance();
		Parameters.setMaxWorkload(0.0);
		Parameters.setWorkloadGrain(0.0);
		Parameters.setMaxDataLoad(0.0);
		Parameters.setDataLoadGrain(0.0);
		System.out.println("Parameters here are not used");

		// Sample organization
		GoalNode g1 = new GoalNode(null, "g1");
		GoalTree gTree = GoalTree.getInstance();
		gTree.setRootNode(g1);

		gTree.addGoal("g11", "g1");
		gTree.addGoal("g111", "g11");
		gTree.addGoal("g112", "g11");
		gTree.addGoal("g12", "g1");
		gTree.addGoal("g121", "g12");
		gTree.addGoal("g1211", "g121");
		System.out.println("Goals Tree:" + gTree.getTree());
		
		OrganisationStatistics s = OrganisationStatistics.getInstance();

		final Cost cost[] = Cost.values();
		for (final Cost c : cost) {
			final String org = "o0" + "_" + c.name();

			s.prepareStatisticsFile(org);
			
			final Organisation o = new Organisation(org, gTree, c, true);
			final Nodo n = new BuscaLargura().busca(o);

			try {
				System.out.println("All goals without workload results in an empty organisation, there is nothing to do!");
				System.out.println("rTree:" + c.name());
				
				assertNull((Organisation) n.getEstado());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testEstimation2Goals() {
		System.out.println("\n\ntestEstimation2Goals");

		// parameters
		Parameters.getInstance();
		Parameters.setMaxWorkload(8.0);
		Parameters.setWorkloadGrain(8.0);
		Parameters.setMaxDataLoad(8.0);
		Parameters.setDataLoadGrain(8.0);
		System.out.println("Parameters should not affect");

		try {
			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getInstance();
			gTree.setRootNode(g0);
			gTree.addGoal("g1", "g0");
			gTree.addWorkload("g0", "w0", 1);
			gTree.addWorkload("g1", "w1", 1);

			final Organisation o = new Organisation("Estimation2Goals", gTree, Cost.GENERALIST, false);

			BuscaLargura busca = new BuscaLargura();
			MostraStatusConsole status = new MostraStatusConsole(busca.getStatus());
			busca.busca(o);
			status.para();

			System.out.println("Estimated worst case: " + o.getEstimatedNumberOfOrganisations() + " (visited must be less or equal)");
			System.out.println("Visited nodes       : " + status.getStatus().getVisitados());
			assertTrue(o.getEstimatedNumberOfOrganisations() >= status.getStatus().getVisitados());
		} catch (GoalNotFound e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testEstimation3Goals() {
		System.out.println("\n\ntestEstimation3Goals");

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
		System.out.println("Parameters should not affect");

		try {
			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getInstance();
			gTree.setRootNode(g0);
			gTree.addGoal("g1", "g0");
			gTree.addGoal("g2", "g0");
			gTree.addWorkload("g0", "w0", 1);
			gTree.addWorkload("g1", "w1", 1);
			gTree.addWorkload("g2", "w2", 1);
			gTree.brakeGoalTree();

			final Organisation o = new Organisation("Estimation3Goals", gTree, Cost.GENERALIST, false);
			
			BuscaLargura busca = new BuscaLargura();
			MostraStatusConsole status = new MostraStatusConsole(busca.getStatus());
			busca.busca(o);
			status.para();

			System.out.println("Estimated worst case: " + o.getEstimatedNumberOfOrganisations() + " (visited must be less or equal)");
			System.out.println("Visited nodes       : " + status.getStatus().getVisitados());
			assertTrue(o.getEstimatedNumberOfOrganisations() >= status.getStatus().getVisitados());
		} catch (GoalNotFound e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testEstimation4Goals() {
		System.out.println("\n\ntestEstimation4Goals");

		// parameters
		Parameters.getInstance();
		Parameters.setMaxWorkload(8.0);
		Parameters.setWorkloadGrain(8.0);
		Parameters.setMaxDataLoad(8.0);
		Parameters.setDataLoadGrain(8.0);
		System.out.println("Parameters should not affect");

		try {
			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getInstance();
			gTree.setRootNode(g0);
			gTree.addGoal("g1", "g0");
			gTree.addGoal("g2", "g0");
			gTree.addGoal("g3", "g0");
			gTree.addWorkload("g0", "w0", 1);
			gTree.addWorkload("g1", "w1", 1);
			gTree.addWorkload("g2", "w2", 1);
			gTree.addWorkload("g3", "w3", 1);

			final Organisation o = new Organisation("Estimation4Goals", gTree, Cost.GENERALIST, false);
			
			BuscaLargura busca = new BuscaLargura();
			MostraStatusConsole status = new MostraStatusConsole(busca.getStatus());
			busca.busca(o);
			status.para();

			System.out.println("Estimated worst case: " + o.getEstimatedNumberOfOrganisations() + " (visited must be less or equal)");
			System.out.println("Visited nodes       : " + status.getStatus().getVisitados());
			assertTrue(o.getEstimatedNumberOfOrganisations() >= status.getStatus().getVisitados());
		} catch (GoalNotFound e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testAllCreatedOrgs2Goals() {
		System.out.println("\n\ntestAllCreatedOrgs2Goals");

		// parameters
		Parameters.getInstance();
		Parameters.setMaxWorkload(8.0);
		Parameters.setWorkloadGrain(8.0);
		Parameters.setMaxDataLoad(8.0);
		Parameters.setDataLoadGrain(8.0);
		System.out.println("Parameters should not affect");

		try {
			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getInstance();
			gTree.setRootNode(g0);
			gTree.addGoal("g1", "g0");
			gTree.addWorkload("g0", "w0", 1);
			gTree.addWorkload("g1", "w1", 1);

			final Organisation o = new Organisation("AllCreatedOrgs2Goals", gTree, Cost.GENERALIST, false);

			BuscaLargura busca = new BuscaLargura();
			MostraStatusConsole status = new MostraStatusConsole(busca.getStatus());
			busca.busca(o);
			status.para();
			
			System.out.println("Number of possible organisations is: "+4);
			assertEquals(4, o.getGoalList().size());
			
			int orgsWith2Roots = 0;
			int orgsWith2Levels = 0;
			int orgsWith1Role = 0;
			for (Organisation org : o.getGoalList()) {
				if (org.getRolesTree().size() == 1) orgsWith1Role++;
				if (org.getRolesTree().size() == 2) {
					List<RoleNode> roles = new ArrayList<>(org.getRolesTree().getTree());
					if ((roles.get(0).getParent() == null) && (roles.get(1).getParent() == null)) 
						orgsWith2Roots++;
					if (((roles.get(0).getParent() == null) && (roles.get(1).getParent() != null)) || 
						((roles.get(0).getParent() != null) && (roles.get(1).getParent() == null)))
						orgsWith2Levels++;
				}
			}
			System.out.println("Number of possible organisations with 2 roots is: 1");
			assertEquals(1, orgsWith2Roots);
			System.out.println("Number of possible organisations with 2 levels is: 2");
			assertEquals(2, orgsWith2Levels);
			System.out.println("Number of possible organisations with 1 role is: 1");
			assertEquals(1, orgsWith1Role);
		} catch (GoalNotFound e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testAllCreatedOrgs3Goals() {
		System.out.println("\n\ntestAllCreatedOrgs3Goals");

		// parameters
		Parameters.getInstance();
		Parameters.setMaxWorkload(8.0);
		Parameters.setWorkloadGrain(8.0);
		Parameters.setMaxDataLoad(8.0);
		Parameters.setDataLoadGrain(8.0);
		System.out.println("Parameters should not affect");

		try {
			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getInstance();
			gTree.setRootNode(g0);
			gTree.addGoal("g1", "g0");
			gTree.addGoal("g2", "g0");
			gTree.addWorkload("g0", "w0", 1);
			gTree.addWorkload("g1", "w1", 1);
			gTree.addWorkload("g2", "w2", 1);
			gTree.brakeGoalTree();

			final Organisation o = new Organisation("AllCreatedOrgs3Goals", gTree, Cost.GENERALIST, false);

			BuscaLargura busca = new BuscaLargura();
			MostraStatusConsole status = new MostraStatusConsole(busca.getStatus());
			busca.busca(o);
			status.para();
			
			System.out.println("Number of possible organisations is: "+26);
			assertEquals(26, o.getGoalList().size());
			
			int orgsWith2Roles = 0;
			int orgsWith2Roots = 0;
			int orgsWith3Roles = 0;
			int orgsWith3Roots = 0;
			int orgsWith1Role = 0;
			for (Organisation org : o.getGoalList()) {
				if (org.getRolesTree().size() == 1) orgsWith1Role++;
				if (org.getRolesTree().size() == 3) {
					orgsWith3Roles++;
					List<RoleNode> roles = new ArrayList<>(org.getRolesTree().getTree());
					if ((roles.get(0).getParent() == null) && (roles.get(1).getParent() == null) && (roles.get(2).getParent() == null)) 
						orgsWith3Roots++;
				}
				if (org.getRolesTree().size() == 2) {
					orgsWith2Roles++;
					List<RoleNode> roles = new ArrayList<>(org.getRolesTree().getTree());
					if ((roles.get(0).getParent() == null) && (roles.get(1).getParent() == null)) 
						orgsWith2Roots++;
				}
			}
			System.out.println("Total is 26");
			System.out.println("Number of possible organisations with 2 levels is: 9");
			assertEquals(9, orgsWith2Roles);
			System.out.println("Number of possible organisations with 1 role is: 1");
			assertEquals(1, orgsWith1Role);
			System.out.println("Number of possible organisations with 3 roles is: 16");
			assertEquals(16, orgsWith3Roles);
			
			System.out.println("Other verification is by number of orgs with root roles");
			System.out.println("Number of possible organisations with 2 roots is: 3 (g0 + g1/2, g1 + g0/2 e g2 + g0/1)");
			assertEquals(3, orgsWith2Roots);
			System.out.println("Number of possible organisations with 3 root roles is: 1");
			assertEquals(1, orgsWith3Roots);
		} catch (GoalNotFound e) {
			e.printStackTrace();
		}
	}
	
}
