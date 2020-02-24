package organisation;

import busca.BuscaLargura;
import busca.Nodo;
import organisation.exception.GoalNotFound;
import organisation.exception.OutputDoesNotMatchWithInput;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.search.Cost;
import organisation.search.Organisation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class OrganisationTest {

	// BE CAREFULL! if generateproof is true, the assertion should be always true
	// After generating proofs it must be checked manually and then turn this
	// argument false for further right assertions
	final static boolean generatingProofs = true;

	@BeforeClass
	public static void beforeTests() {
		final OrganisationPlot p = new OrganisationPlot();
        p.deleteExistingDiagrams();
        p.deleteExistingGraphs();
        
		OrganisationStatistics s = OrganisationStatistics.getInstance();
		s.deleteExistingStatistics();

        if (generatingProofs)
            p.deleteExistingProofs();
    }

	@Before
	public void resetGoalTreeSingleton() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
	   Field instance = GoalTree.class.getDeclaredField("instance");
	   instance.setAccessible(true);
	   instance.set(null, null);
	}

    @Test
    public void testOrgSingleGoals() {

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

        generateOrgForAllCosts("o0", gTree);
    }

    @Test
    public void testOrgGoalsWithWorkload() {

        // Sample organization
		GoalNode root = new GoalNode(null, "PaintHouse");
		
		GoalTree gTree = GoalTree.getInstance();
		gTree.setRootNode(root);
		
        gTree.addGoal("GetInputs", "PaintHouse");
        try {
			gTree.addWorkload("GetInputs", "Contract", 2);
	        gTree.addGoal("HireScaffold", "GetInputs");
	        gTree.addWorkload("HireScaffold", "Contract", 1);
	        gTree.addGoal("BuyInputs", "GetInputs");
	        gTree.addWorkload("BuyInputs", "purchase", 2);
	        gTree.addWorkload("BuyInputs", "report", 3);
	        gTree.addGoal("Paint", "PaintHouse");
	        gTree.addWorkload("Paint", "paint", 13);
	        gTree.addGoal("Inspect", "PaintHouse");
	        gTree.addWorkload("Inspect", "report", 1);
		} catch (GoalNotFound e) {
			e.printStackTrace();
		}

        generateOrgForAllCosts("o1", gTree);
    }

    private void generateOrgForAllCosts(final String orgName, final GoalTree t) {
        final OrganisationPlot op = new OrganisationPlot();
        op.plotGoalTree(orgName, t);

        OrganisationStatistics s = OrganisationStatistics.getInstance();

        final Cost cost[] = Cost.values();
        for (final Cost c : cost) {
            final String org = orgName + "_" + c.name();

            s.prepareStatisticsFile(org);

            final Organisation o = new Organisation(org, t, c);
            final Nodo n = new BuscaLargura().busca(o);
            op.plotOrganisation((Organisation) n.getEstado(), "");

            if (generatingProofs)
                op.generateProof((Organisation) n.getEstado(), org);

            try {
                assertTrue(((Organisation) n.getEstado()).validateOutput());
                final BufferedReader fr = new BufferedReader(new FileReader("output/proofs/" + org + ".txt"));
                final String proof = fr.readLine();
				fr.close();

				assertEquals(n.getEstado().toString(), proof);
			} catch (IOException | OutputDoesNotMatchWithInput e) {
				e.printStackTrace();
			}
		}
	}
}
