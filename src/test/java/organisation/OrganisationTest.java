package organisation;

import busca.BuscaLargura;
import busca.Nodo;
import organisation.Organisation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;  

public class OrganisationTest {
	
	GoalTree t;
	static double maxEffort = 8;
	
	@Before
	public void setupTest() {
		// Sample organization
		t = new GoalTree("g1");
		t.addGoal("g11","g1");
		t.addWorkload("g11", "s1", 2);
		t.addGoal("g12","g1");
		t.addGoal("g111","g11");
		t.addWorkload("g111", "s2", 8);
		t.addGoal("g112","g11");
		t.addWorkload("g112", "s2", 2);
		t.addGoal("g121","g12");
		t.addWorkload("g121", "s2", 0);
		t.addGoal("g122","g12");
		t.addGoal("g1221","g122");
		t.addWorkload("g1221", "s2", 3.4);
	}
	
	@Test
	public void testOrg() {
		
		Cost cost[] = Cost.values(); 
        for (Cost c : cost) 
        { 
    		Organisation o = new Organisation(t.getBrokenGoalTree(maxEffort), c);
    		Nodo n = new BuscaLargura().busca(o);
    		System.out.println("Tree : "+n.getEstado());
    		
    		/**
    		 * BE CAREFULL! if generateproof is true, the assertion should be always true
    		 * After generating proofs it must be checked manually and then turn this argument false for
    		 * further right assertions
    		 */
    		OrganisationPlot p = new OrganisationPlot();
    		p.plotOrganisation((Organisation) n.getEstado(), c.ordinal(), false);

    		String proof = "";
    		BufferedReader fr;
    		try {
    			fr = new BufferedReader(new FileReader("output/proofs/graph_" + c.ordinal() + ".txt"));
    			proof = fr.readLine();
        		System.out.println("Proof: "+n.getEstado());

    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    		
    		System.out.println("\n\n\nGENER: "+n.getEstado().toString());
    		System.out.println("PROOF: "+proof);
    		
    		assertEquals(proof, n.getEstado().toString());
        } 
	}
}
