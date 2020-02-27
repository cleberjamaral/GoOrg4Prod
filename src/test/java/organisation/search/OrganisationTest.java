package organisation.search;

import busca.BuscaLargura;
import busca.Nodo;
import organisation.OrganisationStatistics;
import organisation.Parameters;
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
				System.out.println("All goals without workload results in an empty organsiation, there is nothing to do!");
				System.out.println("rTree:" + c.name());
				
				assertNull((Organisation) n.getEstado());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
