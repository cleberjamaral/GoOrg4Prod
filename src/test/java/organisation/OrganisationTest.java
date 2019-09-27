package organisation;

import busca.BuscaLargura;
import busca.Nodo;
import organisation.GoalNode;
import organisation.Organisation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FileUtils;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;  

public class OrganisationTest {
	
	static List<GoalNode> tree = new ArrayList<GoalNode>();
	
	GoalNode g1;
	@Before
	public void setupTest() {
		// Sample organization
		g1 = new GoalNode(null, "g1");
		g1.addSkill("s1");
		tree.add(g1);
		
		addGoalToTree("g11","g1","s1");
		addGoalToTree("g12","g1",null);
		addGoalToTree("g111","g11","s2");
		addGoalToTree("g112","g11","s2");
		addGoalToTree("g121","g12","s2");
		addGoalToTree("g122","g12",null);
		addGoalToTree("g1221","g122","s2");
		addGoalToTree("g13","g1","s3");
		addGoalToTree("g14","g1",null);
		
		plotOrganizationalGoalTree();
	}
	
	@Test
	public void testOrg() {
		
		Cost cost[] = Cost.values(); 
        for (Cost c : cost) 
        { 
    		Organisation o = new Organisation(g1, c);
    		Nodo n = new BuscaLargura().busca(o);
    		System.out.println("Tree : "+n.getEstado());
    		
    		/**
    		 * BE CAREFULL! if generateproof is true, the assertion should be always true
    		 * After generating proofs it must be checked manually and then turn this argument false for
    		 * further right assertions
    		 */
    		((Organisation) n.getEstado()).plotOrganisation(c.ordinal(),false);

    		String proof = "";
    		BufferedReader fr;
    		try {
    			fr = new BufferedReader(new FileReader("output/proofs/graph_" + c.ordinal() + ".txt"));
    			proof = fr.readLine();
        		System.out.println("Proof: "+n.getEstado());

    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    		
    		assertEquals(proof, n.getEstado().toString());
        } 
	}
	

	private void addGoalToTree(String name, String parent, String skill) {
		
		if (parent != null) {
			Iterator<GoalNode> iterator = tree.iterator(); 
			while (iterator.hasNext()) {
				GoalNode n = iterator.next(); 
				if (n.getGoalName().equals(parent)) {
					GoalNode g = new GoalNode(n, name);
					if (skill != null) g.addSkill(skill);
					tree.add(g);
					break;
				}
			}
		} else {
			GoalNode g = new GoalNode(null, name);
			if (skill != null) g.addSkill(skill);
			tree.add(g);
		}
	}
	
	private static void plotOrganizationalGoalTree() {
		try {
			File filepath = new File("output/diagrams");
			FileUtils.deleteDirectory(filepath);		
			
			File file = new File("output/diagrams/tmp");
			file.getParentFile().mkdirs();
		} catch (IOException e) {}

		try (FileWriter fw = new FileWriter("output/diagrams/gdt.gv", false);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {
			
			out.println("digraph G {");
    		for (GoalNode or : tree) {
    			if (or.getOperator().equals("parallel")) {
    				out.print("\t\"" + or.getGoalName()	
    						+ "\" [ style = \"filled\" fillcolor = \"white\" fontname = \"Courier New\" "
    						+ "shape = \"diamond\" label = <<table border=\"0\" cellborder=\"0\">"
    						+ "<tr><td align=\"center\"><font color=\"black\"><b>" 
    						+ or.getGoalName() + "</b></font></td></tr>");
    			} else {
    				out.print("\t\"" + or.getGoalName()	+ "\" [ style = \"filled\" fillcolor = \"white\" fontname = \"Courier New\" "
    						+ "shape = \"ellipse\" label = <<table border=\"0\" cellborder=\"0\">"
    						+ "<tr><td align=\"center\"><b>" 
    						+ or.getGoalName() + "</b></td></tr>");
    			}
				for (String s : or.getSkills())
					out.print("<tr><td align=\"left\"><sub><i>" + s + "</i></sub></td></tr>");
				out.println("</table>> ];");
				if (or.getParent() != null)
					out.println("\t\"" + or.getParent().getGoalName() + "\"->\"" + or.getGoalName() + "\";");
    		}
        		
        	out.println("}");
		} catch (IOException e) {
		}
	}
	
}
