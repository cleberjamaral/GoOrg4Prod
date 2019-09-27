package organisation;

import busca.BuscaLargura;
import busca.Nodo;
import organisation.GoalNode;
import organisation.Organisation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
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
	}
	
	@Test
	public void testOrg() {
		
		
		
		// Test of the taller structure
		Organisation o1 = new Organisation(g1,Cost.TALLER);
		Nodo n1 = new BuscaLargura().busca(o1);
		System.out.println("1>>>"+n1.getEstado()+"<<<\n\n\n\n\n");
		String expectedSolution1 = "[G{[g111]}S{[s2]}^[g111][s2], G{[g112]}S{[s2]}^[g112][s2], G{[g11]}S{[s1]}^[g11][s1], G{[g12, g13]}S{[s3]}^[g12, g13][s3], G{[g121, g122]}S{[s2]}^[g121, g122][s2], G{[g1221]}S{[s2]}^[g1221][s2], G{[g14]}S{[]}^[g14][], G{[g1]}S{[s1]}]";
//		BufferedReader fr;
//		try {
//			fr = new BufferedReader(new FileReader("output/diagrams/graph_1.txt"));
//			expectedSolution1 = fr.readLine();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		// Test of the flatter structure
		Organisation o2 = new Organisation(g1,Cost.FLATTER);
		Nodo n2 = new BuscaLargura().busca(o2);
		System.out.println("2>>>"+n2.getEstado()+"<<<\n\n\n\n\n");
		String expectedSolution2 = "[G{[g1, g11, g12, g122]}S{[s1]}, G{[g111, g112, g121, g1221]}S{[s2]}^[g111, g112, g121, g1221][s2], G{[g13, g14]}S{[s3]}^[g13, g14][s3]]";

		// Test of the most specialist structure
		Organisation o3 = new Organisation(g1,Cost.SPECIALIST);
		Nodo n3 = new BuscaLargura().busca(o3);
		System.out.println("3>>>"+n3.getEstado()+"<<<\n\n\n\n\n");
		String expectedSolution3 = "[G{[g111]}S{[s2]}^[g111][s2], G{[g112]}S{[s2]}^[g112][s2], G{[g11]}S{[s1]}^[g11][s1], G{[g121]}S{[s2]}^[g121][s2], G{[g1221]}S{[s2]}^[g1221][s2], G{[g122]}S{[]}^[g122][], G{[g12]}S{[]}^[g12][], G{[g13]}S{[s3]}^[g13][s3], G{[g14]}S{[]}^[g14][], G{[g1]}S{[s1]}]";

		// Test of the most generalist structure
		Organisation o4 = new Organisation(g1,Cost.GENERALIST);
		Nodo n4 = new BuscaLargura().busca(o4);
		System.out.println("4>>>"+n4.getEstado()+"<<<\n\n\n\n\n");
		String expectedSolution4 = "[G{[g1, g11, g12, g122]}S{[s1]}, G{[g111, g112, g121, g1221]}S{[s2]}^[g111, g112, g121, g1221][s2], G{[g13, g14]}S{[s3]}^[g13, g14][s3]]";
		
		
		plotOrganizationalGoalTree();
		((Organisation) n1.getEstado()).plotOrganisation(1);
		((Organisation) n2.getEstado()).plotOrganisation(2);
		((Organisation) n3.getEstado()).plotOrganisation(3);
		((Organisation) n4.getEstado()).plotOrganisation(4);

		assertEquals(expectedSolution1, n1.getEstado().toString());
		assertEquals(expectedSolution2, n2.getEstado().toString());
		assertEquals(expectedSolution3, n3.getEstado().toString());
		assertEquals(expectedSolution4, n4.getEstado().toString());
		
		//n = new BuscaProfundidade(100).busca(inicial);
		//n = new BuscaIterativo().busca(inicial);
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

		try (FileWriter fw = new FileWriter("output/diagrams/graph_0.gv", false);
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
