package organisation.search;

import busca.BuscaLargura;
import busca.MostraStatusConsole;
import busca.Nodo;
import organisation.OrganisationStatistics;
import organisation.Parameters;
import organisation.exception.GoalNotFound;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.search.Organisation;
import organisation.search.cost.Cost;

import java.lang.reflect.Field;

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
	
}
