package test.java.organisation;

import main.java.busca.BuscaIterativo;
import main.java.busca.BuscaLargura;
import main.java.busca.BuscaProfundidade;
import main.java.busca.Nodo;
import main.java.organisation.GoalNode;
import main.java.organisation.Organisation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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
		GoalNode g0 = new GoalNode(null, "g0");
		tree.add(g0);
		GoalNode g1 = new GoalNode(g0, "g1");
		g1.addSkill("s1");
		tree.add(g1);
		GoalNode g2 = new GoalNode(g0, "g2");
		tree.add(g2);
		//GoalNode g3 = new GoalNode(g1, "g3");
		//g3.addSkill("s2");
		//GoalNode g4 = new GoalNode(g0, "g4");
		//GoalNode g5 = new GoalNode(g4, "g5");
		//g5.addSkill("s5");
		//GoalNode g6 = new GoalNode(g4, "g6");
		//g6.addSkill("s4");
		//g6.addSkill("s5");
		Organisation inicial = new Organisation(g0,3);
	
/*
		// Sample organization : paint a house
		GoalNode paintHouse = new GoalNode(null, "paintHouse");
		GoalNode contracting = new GoalNode(paintHouse, "contracting");
		contracting.setOperator("parallel");
		contracting.addSkill("getBids");
		GoalNode bidIPaint = new GoalNode(contracting, "bidIPaint");
		bidIPaint.addSkill("bid");
		bidIPaint.addSkill("paint");
		GoalNode bidEPaint = new GoalNode(contracting, "bidEPaint");
		bidEPaint.addSkill("bid");
		bidEPaint.addSkill("paint");
		GoalNode execute = new GoalNode(paintHouse, "execute");
		GoalNode ePaint = new GoalNode(execute, "ePaint");
		ePaint.addSkill("paint");
		GoalNode iPaint = new GoalNode(execute, "iPaint");
		iPaint.addSkill("bid");
		iPaint.addSkill("paint");
		GoalNode contractWinner = new GoalNode(execute, "contractWinner");
		contractWinner.addSkill("contract");
		OrganizationalRole3 inicial = new OrganizationalRole3(paintHouse,3);
*/		
		plotOrganizationalGoalTree();
		
		Nodo n = new BuscaLargura().busca(inicial);
		//n = new BuscaProfundidade(100).busca(inicial);
		//n = new BuscaIterativo().busca(inicial);
		System.out.println(n.getEstado()); 
		assertEquals(n, null);
		
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
