package test.java.organisation;

import main.java.busca.BuscaIterativo;
import main.java.busca.BuscaLargura;
import main.java.busca.BuscaProfundidade;
import main.java.busca.Nodo;
import main.java.organisation.GoalNode;
import main.java.organisation.Organisation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.apache.commons.io.FileUtils;

import static org.junit.Assert.*;
import org.junit.Test;  

public class OrganisationTest {
	
	static List<GoalNode> tree = new ArrayList<GoalNode>();
	static Stack<GoalNode> stack = new Stack<GoalNode>();
	static GoalNode rootNode = null;
	static boolean pushGoalNode = false;
	static GoalNode referenceGoalNode = null;
    
	@Test
	public void testOrg() {
	//public static void main(String[] a) throws IOException {
	
		// Sample organization
		GoalNode g1 = new GoalNode(null, "g1");
		g1.addSkill("s1");
		tree.add(g1);
		GoalNode g11 = new GoalNode(g1, "g11");
		g11.addSkill("s1");
		tree.add(g11);
		GoalNode g12 = new GoalNode(g1, "g12");
		tree.add(g12);
		GoalNode g111 = new GoalNode(g11, "g111");
		g111.addSkill("s2");
		GoalNode g112 = new GoalNode(g11, "g112");
		g12.addSkill("s2");
		GoalNode g121 = new GoalNode(g12, "g121");
		g121.addSkill("s2");
		GoalNode g122 = new GoalNode(g12, "g122");
		GoalNode g1221 = new GoalNode(g122, "g1221");
		g1221.addSkill("s2");
		GoalNode g13 = new GoalNode(g1, "g13");
		g13.addSkill("s3");
		GoalNode g14 = new GoalNode(g1, "g14");

		
		
		
		// Test of the taller structure
		Organisation o1 = new Organisation(g1,1);
		Nodo n1 = new BuscaLargura().busca(o1);
		System.out.println("1>>>"+n1.getEstado()+"<<<\n\n\n\n\n");
		String expectedSolution1 = "[G{[g11, g14]}S{[s1]}^[g11, g14][s1], G{[g111]}S{[s2]}^[g111][s2], G{[g112]}S{[]}^[g112][], G{[g121, g122]}S{[s2]}^[g121, g122][s2], G{[g1221]}S{[s2]}^[g1221][s2], G{[g12]}S{[s2]}^[g12][s2], G{[g13]}S{[s3]}^[g13][s3], G{[g1]}S{[s1]}]";
		
		// Test of the flatter structure
		Organisation o2 = new Organisation(g1,2);
		Nodo n2 = new BuscaLargura().busca(o2);
		System.out.println("2>>>"+n2.getEstado()+"<<<\n\n\n\n\n");
		String expectedSolution2 = "[G{[g1, g11, g14]}S{[s1]}, G{[g111, g112, g12, g121, g122, g1221]}S{[s2]}^[g111, g112, g12, g121, g122, g1221][s2], G{[g13]}S{[s3]}^[g13][s3]]";

		// Test of the most specialist structure
		Organisation o3 = new Organisation(g1,3);
		Nodo n3 = new BuscaLargura().busca(o3);
		System.out.println("3>>>"+n3.getEstado()+"<<<\n\n\n\n\n");
		String expectedSolution3 = "[G{[g111]}S{[s2]}^[g111][s2], G{[g112]}S{[]}^[g112][], G{[g11]}S{[s1]}^[g11][s1], G{[g121]}S{[s2]}^[g121][s2], G{[g1221]}S{[s2]}^[g1221][s2], G{[g122]}S{[]}^[g122][], G{[g12]}S{[s2]}^[g12][s2], G{[g13]}S{[s3]}^[g13][s3], G{[g14]}S{[]}^[g14][], G{[g1]}S{[s1]}]";

		// Test of the most generalist structure
		Organisation o4 = new Organisation(g1,4);
		Nodo n4 = new BuscaLargura().busca(o4);
		System.out.println("4>>>"+n4.getEstado()+"<<<\n\n\n\n\n");
		String expectedSolution4 = "[G{[g1, g11, g14]}S{[s1]}, G{[g111, g112, g12, g121, g122, g1221]}S{[s2]}^[g111, g112, g12, g121, g122, g1221][s2], G{[g13]}S{[s3]}^[g13][s3]]";

		plotOrganizationalGoalTree();
		o1.plotOrganisation(1);
		o2.plotOrganisation(2);
		o3.plotOrganisation(3);
		o4.plotOrganisation(4);

		assertEquals(expectedSolution1, n1.getEstado().toString());
		assertEquals(expectedSolution2, n2.getEstado().toString());
		assertEquals(expectedSolution3, n3.getEstado().toString());
		assertEquals(expectedSolution4, n4.getEstado().toString());
		
		//n = new BuscaProfundidade(100).busca(inicial);
		//n = new BuscaIterativo().busca(inicial);
	}
	
	private static void plotOrganizationalGoalTree() {
		try {
			File filepath = new File("output/diagrams");
			FileUtils.deleteDirectory(filepath);		
			
			File file = new File("output/diagrams/tmp");
			file.getParentFile().mkdirs();
		} catch (IOException e) {}

		try (FileWriter fw = new FileWriter("output/diagrams/orgTree.gv", false);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {
			
			out.println("digraph G {");
    		for (GoalNode or : tree) {
    			if (or.getOperator().equals("parallel")) {
    				out.print("\t\"" + or.getGoalName()	+ "\" [ style = \"filled\" fillcolor = \"white\" fontname = \"Courier New\" "
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
