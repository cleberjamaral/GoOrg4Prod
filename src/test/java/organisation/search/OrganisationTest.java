package organisation.search;

import busca.BuscaLargura;
import busca.MostraStatusConsole;
import busca.Nodo;
import organisation.OrganisationStatistics;
import organisation.Parameters;
import organisation.exception.GoalNotFound;
import organisation.exception.OutputDoesNotMatchWithInput;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.position.PositionNode;
import organisation.search.Organisation;
import organisation.search.cost.Cost;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		
		System.out.println("Originals (unbroken) goals are: "+GoalTree.getInstance().getOriginalGoals());
		assertEquals(7, GoalTree.getInstance().getOriginalGoals().size(), 0);

		OrganisationStatistics s = OrganisationStatistics.getInstance();

		final Cost cost[] = Cost.values();
		for (final Cost c : cost) {
			final String org = "o0" + "_" + c.name();

			s.prepareGenerationStatisticsFile(org);
			
			final Organisation o = new Organisation(org, gTree, Arrays.asList(c), true);
			final Nodo n = new BuscaLargura().busca(o);

			try {
				System.out.println("All goals without workload results in an empty organisation, there is nothing to do!");
				System.out.println("rTree:" + c.name());
				
				assertNull(n);
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
			
			System.out.println("Originals (unbroken) goals are: "+GoalTree.getInstance().getOriginalGoals());
			assertEquals(2, GoalTree.getInstance().getOriginalGoals().size(), 0);
			
			OrganisationStatistics s = OrganisationStatistics.getInstance();
			s.prepareGenerationStatisticsFile("Estimation2Goals");

			final Organisation o = new Organisation("Estimation2Goals", gTree,
					Arrays.asList(Cost.GENERALIST, Cost.EFFICIENT), false);

			BuscaLargura busca = new BuscaLargura();
			MostraStatusConsole status = new MostraStatusConsole(busca.getStatus());
			busca.busca(o);
			status.para();

			System.out.println("Estimated worst case: " + o.getEstimatedNumberOfOrganisations(gTree.getTree().size()) + " (visited must be less or equal)");
			System.out.println("Visited nodes       : " + status.getStatus().getVisitados());
			assertTrue(o.getEstimatedNumberOfOrganisations(gTree.getTree().size()) >= status.getStatus().getVisitados());
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

			System.out.println("Originals (unbroken) goals are: "+GoalTree.getInstance().getOriginalGoals());
			assertEquals(3, GoalTree.getInstance().getOriginalGoals().size(), 0);

			OrganisationStatistics s = OrganisationStatistics.getInstance();
			s.prepareGenerationStatisticsFile("Estimation3Goals");

			final Organisation o = new Organisation("Estimation3Goals", gTree,
					Arrays.asList(Cost.GENERALIST, Cost.EFFICIENT), false);
			
			BuscaLargura busca = new BuscaLargura();
			MostraStatusConsole status = new MostraStatusConsole(busca.getStatus());
			busca.busca(o);
			status.para();

			System.out.println("Estimated worst case: " + o.getEstimatedNumberOfOrganisations(gTree.getTree().size()) + " (visited must be less or equal)");
			System.out.println("Visited nodes       : " + status.getStatus().getVisitados());
			assertTrue(o.getEstimatedNumberOfOrganisations(gTree.getTree().size()) >= status.getStatus().getVisitados());
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

			System.out.println("Originals (unbroken) goals are: "+GoalTree.getInstance().getOriginalGoals());
			assertEquals(4, GoalTree.getInstance().getOriginalGoals().size(), 0);

			OrganisationStatistics s = OrganisationStatistics.getInstance();
			s.prepareGenerationStatisticsFile("Estimation4Goals");

			final Organisation o = new Organisation("Estimation4Goals", gTree,
					Arrays.asList(Cost.GENERALIST, Cost.EFFICIENT), false);
			
			BuscaLargura busca = new BuscaLargura();
			MostraStatusConsole status = new MostraStatusConsole(busca.getStatus());
			busca.busca(o);
			status.para();

			System.out.println("Estimated worst case: " + o.getEstimatedNumberOfOrganisations(gTree.getTree().size()) + " (visited must be less or equal)");
			System.out.println("Visited nodes       : " + status.getStatus().getVisitados());
			assertTrue(o.getEstimatedNumberOfOrganisations(gTree.getTree().size()) >= status.getStatus().getVisitados());
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

			System.out.println("Originals (unbroken) goals are: "+GoalTree.getInstance().getOriginalGoals());
			assertEquals(2, GoalTree.getInstance().getOriginalGoals().size(), 0);

			OrganisationStatistics s = OrganisationStatistics.getInstance();
			s.prepareGenerationStatisticsFile("AllCreatedOrgs2Goals");
			
			final Organisation o = new Organisation("AllCreatedOrgs2Goals", gTree,
					Arrays.asList(Cost.GENERALIST, Cost.EFFICIENT), false);

			BuscaLargura busca = new BuscaLargura();
			MostraStatusConsole status = new MostraStatusConsole(busca.getStatus());
			busca.busca(o);
			status.para();
			
			System.out.println("Number of possible organisations is: "+4);
			assertEquals(4, o.getGoalList().size());
			
			int orgsWith2Supremes = 0;
			int orgsWith2Levels = 0;
			int orgsWith1Position = 0;
			for (Organisation org : o.getGoalList()) {
				if (org.getPositionsTree().size() == 1) orgsWith1Position++;
				if (org.getPositionsTree().size() == 2) {
					List<PositionNode> positions = new ArrayList<>(org.getPositionsTree().getTree());
					if ((positions.get(0).getParent() == null) && (positions.get(1).getParent() == null)) 
						orgsWith2Supremes++;
					if (((positions.get(0).getParent() == null) && (positions.get(1).getParent() != null)) || 
						((positions.get(0).getParent() != null) && (positions.get(1).getParent() == null)))
						orgsWith2Levels++;
				}
			}
			System.out.println("Number of possible organisations with 2 roots is: 1");
			assertEquals(1, orgsWith2Supremes);
			System.out.println("Number of possible organisations with 2 levels is: 2");
			assertEquals(2, orgsWith2Levels);
			System.out.println("Number of possible organisations with 1 position is: 1");
			assertEquals(1, orgsWith1Position);
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

			System.out.println("Originals (unbroken) goals are: "+GoalTree.getInstance().getOriginalGoals());
			assertEquals(3, GoalTree.getInstance().getOriginalGoals().size(), 0);

			OrganisationStatistics s = OrganisationStatistics.getInstance();
			s.prepareGenerationStatisticsFile("AllCreatedOrgs3Goals");

			final Organisation o = new Organisation("AllCreatedOrgs3Goals", gTree,
					Arrays.asList(Cost.GENERALIST, Cost.EFFICIENT), false);

			BuscaLargura busca = new BuscaLargura();
			MostraStatusConsole status = new MostraStatusConsole(busca.getStatus());
			busca.busca(o);
			status.para();
			
			System.out.println("Number of possible organisations is: "+26);
			assertEquals(26, o.getGoalList().size());
			
			int orgsWith2Positions = 0;
			int orgsWith2Supremes = 0;
			int orgsWith3Positions = 0;
			int orgsWith3Supremes = 0;
			int orgsWith1Position = 0;
			for (Organisation org : o.getGoalList()) {
				if (org.getPositionsTree().size() == 1) orgsWith1Position++;
				if (org.getPositionsTree().size() == 3) {
					orgsWith3Positions++;
					List<PositionNode> positions = new ArrayList<>(org.getPositionsTree().getTree());
					if ((positions.get(0).getParent() == null) && (positions.get(1).getParent() == null) && (positions.get(2).getParent() == null)) 
						orgsWith3Supremes++;
				}
				if (org.getPositionsTree().size() == 2) {
					orgsWith2Positions++;
					List<PositionNode> positions = new ArrayList<>(org.getPositionsTree().getTree());
					if ((positions.get(0).getParent() == null) && (positions.get(1).getParent() == null)) 
						orgsWith2Supremes++;
				}
			}
			System.out.println("Total is 26");
			System.out.println("Number of possible organisations with 2 levels is: 9");
			assertEquals(9, orgsWith2Positions);
			System.out.println("Number of possible organisations with 1 position is: 1");
			assertEquals(1, orgsWith1Position);
			System.out.println("Number of possible organisations with 3 positions is: 16");
			assertEquals(16, orgsWith3Positions);
			
			System.out.println("Other verification is by number of orgs with root positions");
			System.out.println("Number of possible organisations with 2 roots is: 3 (g0 + g1/2, g1 + g0/2 e g2 + g0/1)");
			assertEquals(3, orgsWith2Supremes);
			System.out.println("Number of possible organisations with 3 root positions is: 1");
			assertEquals(1, orgsWith3Supremes);
		} catch (GoalNotFound e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testOrganisationIsValid() {
		System.out.println("\n\ntestOrganisationIsValid");
		try {
			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getInstance();
			gTree.setRootNode(g0);
			gTree.addWorkload("g0", "w0", 1);

			System.out.println("GoalsTree: " + gTree.getTree().toString());

			OrganisationStatistics s = OrganisationStatistics.getInstance();
			s.prepareGenerationStatisticsFile("testOrganisationIsValid");

			Organisation o = new Organisation("testOrganisationIsValid", gTree,
					Arrays.asList(Cost.GENERALIST, Cost.EFFICIENT), true);
			Nodo n = new BuscaLargura().busca(o);

			System.out.println("Validate original gdt workloads with organisations' workloads (must be ok)");
			assertTrue(((Organisation) n.getEstado()).isValid());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testSumOfInformAndWorkload() {
		System.out.println("\n\ntestSumOfInformAndWorkload");
		try {
	   		// parameters
    		Parameters.getInstance();
    		Parameters.setMaxWorkload(8.0);
    		Parameters.setWorkloadGrain(2.0);
    		Parameters.setMaxDataLoad(8.0);
    		Parameters.setDataLoadGrain(2.0);
    		
			GoalNode g0 = new GoalNode(null, "g0");
			GoalTree gTree = GoalTree.getInstance();
			gTree.setRootNode(g0);
			gTree.addGoal("g1", "g0");
			gTree.addGoal("g2", "g1");
			System.out.println("g1 must be split into two goals with 2 of workload each");
			gTree.addWorkload("g1", "w1", 4);
			System.out.println("g2 must be split into two goals with 2 of dataload each");
			gTree.addInform("g1", "i1", "g2", 4);
			
			gTree.brakeGoalTree();
			
			System.out.println("Originals (unbroken) goals are: "+GoalTree.getInstance().getOriginalGoals());
			assertEquals(3, GoalTree.getInstance().getOriginalGoals().size(), 0);
			
			GoalNode g;
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g0"));
			System.out.println("g0 has no inform and dataloads: " + g.getInforms() + " - " + g.getDataLoads());
			assertEquals(0, g.getSumInform(), 0);
			assertEquals(0, g.getSumDataLoad(), 0);

			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g1$0"));
			System.out.println("g1$0 ~ g1$1 all have sum of inform: " + g.getSumInform() + ", details: " + g.getInforms());
			System.out.println("g1$0 ~ g1$1 all have sum of workload: " + g.getSumWorkload() + ", details: " + g.getWorkloads());
			assertEquals(2, g.getSumWorkload(), 0);
			assertEquals(1, g.getWorkloads().size(), 0);
			assertEquals(2, g.getSumInform(), 0);
			assertEquals(2, g.getInforms().size(), 0); 
			
			assertNotNull(g = gTree.findAGoalByName(gTree.getRootNode(),"g2$0"));
			System.out.println("g2$0 ~ g2$1 all have sum of dataload: " + g.getSumDataLoad() + ", details: " + g.getDataLoads());
			assertEquals(2, g.getSumDataLoad(), 0);
			assertEquals(2, g.getDataLoads().size(), 0);
			
			OrganisationStatistics s = OrganisationStatistics.getInstance();
			s.prepareGenerationStatisticsFile("testOnePositionFlatterOrg");
			
			Organisation o = new Organisation("testOnePositionFlatterOrg", gTree,
					Arrays.asList(Cost.FLATTER, Cost.EFFICIENT), true);
			Nodo n = new BuscaLargura().busca(o);

			System.out.println("Generated tree: " + ((Organisation)n.getEstado()).getPositionsTree().getTree());

			assertEquals(4, ((Organisation)n.getEstado()).getPositionsTree().getSumWorkload(), 0);
			System.out.println("Original informs should be removed since they are circular");
			assertEquals(0, ((Organisation)n.getEstado()).getPositionsTree().getSumDataload(), 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
