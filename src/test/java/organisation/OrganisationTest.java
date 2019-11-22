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
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class OrganisationTest {

	// BE CAREFULL! if generateproof is true, the assertion should be always true
	// After generating proofs it must be checked manually and then turn this
	// argument false for further right assertions
	static boolean generatingProofsInCheckingMode = false;
	static OrganisationPlot p;
	
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
		t.addGoal("g12", "g1");

		List<String> proofs = new ArrayList<>();
		List<String> outputs = new ArrayList<>();

		Cost cost[] = Cost.values();
		for (Cost c : cost) {
			String orgName = "o0_" + c.name();
			Organisation o = new Organisation(orgName, t, c, !generatingProofsInCheckingMode);
			Nodo n = new BuscaLargura().busca(o);
			outputs.add(n.getEstado().toString());
			try {
				assertTrue(((Organisation) n.getEstado()).validateOutput());
			} catch (OutputDoesNotMatchWithInput e1) {
				e1.printStackTrace();
			}

			p.plotOrganisation((Organisation) n.getEstado(), orgName, generatingProofsInCheckingMode);

			String proof = "";
			BufferedReader fr;
			try {
				fr = new BufferedReader(new FileReader("output/proofs/" + orgName + ".txt"));
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
	
	@Test
	public void testOrgGoalsWithWorkload() {

		// Sample organization
		GoalTree t = new GoalTree("g1");
		t.addGoal("g11", "g1");
		t.addWorkload("g11", "s1", 2);
		t.addGoal("g12", "g1");
		t.addWorkload("g12", "s2", 4);

		//t.addGoal("g111", "g11");
		//t.addWorkload("g111", "s1", 4);
		//t.addGoal("g112", "g11");
		//t.addWorkload("g112", "s1", 2);
		//t.addGoal("g121", "g12");
		//t.addWorkload("g121", "s2", 0);
		//t.addGoal("g122", "g12");
		//t.addGoal("g1221", "g122");
		//t.addWorkload("g1221", "s2", 2);

		List<String> proofs = new ArrayList<>();
		List<String> outputs = new ArrayList<>();

		Cost cost[] = Cost.values();
		for (Cost c : cost) {
			String orgName = "o1_" + c.name();
			Organisation o = new Organisation(orgName, t, c, !generatingProofsInCheckingMode);
			Nodo n = new BuscaLargura().busca(o);
			outputs.add(n.getEstado().toString());
			try {
				assertTrue(((Organisation) n.getEstado()).validateOutput());
			} catch (OutputDoesNotMatchWithInput e1) {
				e1.printStackTrace();
			}

			p.plotOrganisation((Organisation) n.getEstado(), orgName, generatingProofsInCheckingMode);

			String proof = "";
			BufferedReader fr;
			try {
				fr = new BufferedReader(new FileReader("output/proofs/" + orgName + ".txt"));
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
