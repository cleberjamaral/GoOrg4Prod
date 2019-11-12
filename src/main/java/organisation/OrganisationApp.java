package organisation;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import busca.BuscaLargura;
import busca.Nodo;
import properties.Workload;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.apache.commons.io.FileUtils;

import simplelogger.SimpleLogger;

public class OrganisationApp {

	static List<GoalNode> tree = new ArrayList<GoalNode>();
	static Stack<GoalNode> stack = new Stack<GoalNode>();
	static GoalNode rootNode = null;
	static GoalNode referenceGoalNode = null;

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {

		// set verbose level
		SimpleLogger.getInstance(1);

		Organisation inicial;
		if ((args.length < 1) || (args[0].equals("0"))) {
			// Sample organization
			GoalNode g0 = new GoalNode(null, "g0");
			tree.add(g0);
			GoalNode g1 = new GoalNode(g0, "g1");
			Workload w1 = new Workload("s1",0);
			g1.addRequirement(w1);
			tree.add(g1);
			GoalNode g2 = new GoalNode(g0, "g2");
			tree.add(g2);
			GoalNode g3 = new GoalNode(g1, "g3");
			tree.add(g3);
			Workload w2 = new Workload("s2",0);
			g3.addRequirement(w2);
			GoalNode g4 = new GoalNode(g0, "g4");
			tree.add(g4);
			GoalNode g5 = new GoalNode(g4, "g5");
			tree.add(g5);
			Workload w5 = new Workload("s5",0);
			g5.addRequirement(w5);
			GoalNode g6 = new GoalNode(g4, "g6");
			tree.add(g6);
			Workload w4 = new Workload("s4",0);
			g6.addRequirement(w4);
			g6.addRequirement(w5);
			if (args.length == 2) {
				inicial = new Organisation(g0, Cost.valueOf(args[1]));
			} else {
				inicial = new Organisation(g0, Cost.SPECIALIST);
			}
		} else {
			String file = args[0];

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(file));

			if (!document.getDocumentElement().getNodeName().equals("organisational-specification"))
				throw new IllegalArgumentException(
						"Error! It is expected an 'organisational-specification' XML structure");

			document.getDocumentElement().normalize();

			// Visit all possible schemes from Moise 'functional-specification'
			NodeList nList = document.getElementsByTagName("scheme");
			visitNodes(nList);

			inicial = new Organisation(rootNode, Cost.SPECIALIST);
		}

		plotOrganizationalGoalTree();

		Nodo n = null;

		n = new BuscaLargura().busca(inicial);
		
		/**
		 * To create the proof for the current gdt:
		 * ((Organisation) n.getEstado()).plotOrganisation(Cost.SPECIALIST.ordinal(),true);
		 */
		String expectedSolution = "[G{[g0]}S{[]}, G{[g1]}S{[s1]}^[g1][s1], G{[g2]}S{[]}^[g2][], G{[g3]}S{[s2]}^[g3][s2], G{[g4]}S{[]}^[g4][], G{[g5]}S{[s5]}^[g5][s5], G{[g6]}S{[s4, s5]}^[g6][s4, s5]]";
		System.out.println("\n\nProduced output:" + n.getEstado().toString());
		System.out.println("Given proof    :" + expectedSolution);
		                           
		if (n.getEstado().toString().equals(expectedSolution))
			System.out.println("\nThe given solution is correct according to the given proof, the project seems to be creating correct organisations!\n\n");
		else
			System.out.println("\nFALSE!!! Something went wrong on comparing the created organisation with the available proof.\n\n");
	}

	private static void visitNodes(NodeList nList) {
		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node node = nList.item(temp);

			if (node.getNodeType() == Node.ELEMENT_NODE) {

				Element eGoal = (Element) node;
				if (node.getNodeName().equals("goal")) {
					SimpleLogger.getInstance().debug("Node id = " + eGoal.getAttribute("id"));

					if (rootNode == null) {
						rootNode = new GoalNode(null, eGoal.getAttribute("id"));
						tree.add(rootNode);
						referenceGoalNode = rootNode;
					} else {
						GoalNode gn = new GoalNode(stack.peek(), eGoal.getAttribute("id"));
						tree.add(gn);
						referenceGoalNode = gn;
					}

				} else if (node.getNodeName().equals("plan")) {
					Element ePlan = (Element) node;
					stack.push(referenceGoalNode);
					referenceGoalNode.setOperator(ePlan.getAttribute("operator"));
					SimpleLogger.getInstance().debug(
							"Push = " + referenceGoalNode.toString() + " - Op: " + referenceGoalNode.getOperator());
				} else if (node.getNodeName().equals("skill")) {
					referenceGoalNode.addRequirement(eGoal.getAttribute("id"));
					SimpleLogger.getInstance()
							.debug("Skill = " + referenceGoalNode.toString() + " : " + referenceGoalNode.getRequirements());
				} else if (node.getNodeName().equals("mission")) {
					return; // end of scheme goals
				}
				if (node.hasChildNodes()) {

					visitNodes(node.getChildNodes());
					if (node.getNodeName().equals("plan")) {
						GoalNode tempGN = stack.pop();
						SimpleLogger.getInstance().debug("Poping = " + tempGN.toString());
					}
				}
			}
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
							+ "<tr><td align=\"center\"><font color=\"black\"><b>" + or.getGoalName()
							+ "</b></font></td></tr>");
				} else {
					out.print("\t\"" + or.getGoalName()
							+ "\" [ style = \"filled\" fillcolor = \"white\" fontname = \"Courier New\" "
							+ "shape = \"ellipse\" label = <<table border=\"0\" cellborder=\"0\">"
							+ "<tr><td align=\"center\"><b>" + or.getGoalName() + "</b></td></tr>");
				}
				for (Object s : or.getRequirements())
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
