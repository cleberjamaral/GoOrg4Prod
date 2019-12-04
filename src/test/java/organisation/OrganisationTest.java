package organisation;

import busca.BuscaLargura;
import busca.Nodo;
import organisation.exception.OutputDoesNotMatchWithInput;
import organisation.goal.GoalTree;
import organisation.search.Cost;
import organisation.search.Organisation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class OrganisationTest {

	// BE CAREFULL! if generateproof is true, the assertion should be always true
	// After generating proofs it must be checked manually and then turn this
	// argument false for further right assertions
	private static boolean generatingProofsInCheckingMode = true;
	private static OrganisationPlot p;
	private BufferedReader fr;

	@BeforeClass
	public static void beforeTests() {
		p = new OrganisationPlot();
		p.deleteExistingDiagrams();

		if (generatingProofsInCheckingMode)
			p.deleteExistingProofs();
	}

	@Test
	public void testOrgSingleGoals() {

		// Sample organization
		GoalTree t = new GoalTree("g1");
		t.addGoal("g11", "g1");
		t.addGoal("g111", "g11");
		t.addGoal("g112", "g11");
		t.addGoal("g12", "g1");
		t.addGoal("g121", "g12");
		t.addGoal("g1211", "g121");

		generateOrgForAllCosts("o0", t);
	}

	@Test
	public void testOrgGoalsWithWorkload() {

		// Sample organization
		GoalTree gTree = new GoalTree("PaintHouse");
		gTree.addGoal("GetInputs", "PaintHouse");
		gTree.addWorkload("GetInputs", "Contract", 2);
		gTree.addGoal("HireScaffold", "GetInputs");
		gTree.addWorkload("HireScaffold", "Contract", 1);
		gTree.addGoal("BuyInputs", "GetInputs");
		gTree.addWorkload("BuyInputs", "purchase", 2);
		gTree.addWorkload("BuyInputs","report",3);
		gTree.addGoal("Paint", "PaintHouse");
		gTree.addWorkload("Paint", "paint", 13);
		gTree.addGoal("Inspect", "PaintHouse");
		gTree.addWorkload("Inspect", "report", 1);

		generateOrgForAllCosts("o1", gTree);
	}

	private void generateOrgForAllCosts(String orgName, GoalTree t) {
		p.plotGoalTree(orgName, t);

		Cost cost[] = Cost.values();
		for (Cost c : cost) {
			String org = orgName + "_" + c.name();

			Organisation o = new Organisation(org, t, c);
			Nodo n = new BuscaLargura().busca(o);

			if (generatingProofsInCheckingMode)
				p.generateProof((Organisation) n.getEstado(), org);
			
			try {
				assertTrue(((Organisation) n.getEstado()).validateOutput());
				fr = new BufferedReader(new FileReader("output/proofs/" + org + ".txt"));
				String proof = fr.readLine();

				assertEquals(n.getEstado().toString(), proof);
			} catch (IOException | OutputDoesNotMatchWithInput e) {
				e.printStackTrace();
			}
		}
	}
}
