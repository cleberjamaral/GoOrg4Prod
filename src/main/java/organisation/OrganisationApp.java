package organisation;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import annotations.Throughput;
import annotations.Workload;
//import busca.BuscaLargura;
import busca.BuscaProfundidade;
import busca.Nodo;
import organisation.exception.OutputDoesNotMatchWithInput;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.search.Cost;
import organisation.search.Organisation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import simplelogger.SimpleLogger;

public class OrganisationApp {

	static Stack<GoalNode> stack = new Stack<GoalNode>();
	static GoalNode rootNode = null;
	static GoalNode referenceGoalNode = null;
	static double maxEffort = 8;
	static double maxThroughput = 8;
	static SimpleLogger LOG;

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {

		// set verbose level
		LOG = SimpleLogger.getInstance(2);

		Organisation inicial;
		// if a Moise XML file was not provided, use a sample organisation
		if ((args.length < 1) || (args[0].equals("0"))) {
			// Sample organization
			GoalTree gTree = new GoalTree("PaintHouse");
			gTree.addGoal("GetInputs", "PaintHouse");
			gTree.addWorkload("GetInputs", "Contract", 2);
			gTree.addGoal("Paint", "PaintHouse");
			gTree.addWorkload("Paint", "paint", 2);
			gTree.addGoal("BuyInputs", "GetInputs");
			gTree.addWorkload("BuyInputs", "purchase", 7);
			gTree.addWorkload("BuyInputs","messages",10);
			gTree.addThroughput("BuyInputs", "reports", 2);
			gTree.addThroughput("BuyInputs", "registerSuppliers", 2);
			gTree.addAccountableFor("GetInputs", "BuyInputs");
			gTree.addGoal("Inspect", "PaintHouse");
			gTree.addGoal("Report", "Inspect");
			gTree.addWorkload("Report", "paint", 0);
			gTree.addGoal("CheckInt", "Inspect");
			gTree.addGoal("CheckExt", "CheckInt");
			gTree.addWorkload("CheckExt", "paint", 3.4);
			gTree.addGoal("PaintInt", "Paint");
			gTree.addWorkload("PaintInt", "paint", 8);
			gTree.addGoal("PaintExt", "Paint");
			gTree.addWorkload("PaintExt", "paint", 2);
			gTree.addWorkload("PaintExt", "scaffold", 3);
			
			// Sample list of agents
//			List<Object> agents = new ArrayList<>();
//			Agent a0 = new Agent();
//			agents.add(a0);
//			Agent a1 = new Agent();
//			agents.add(a1);
//			Workload wa1 = new Workload("s1",0);
//			a1.addProperty(wa1);
//			Agent a2 = new Agent();
//			Workload wa2 = new Workload("s2",0);
//			a1.addProperty(wa2);
//			agents.add(a2);
//			Agent a3 = new Agent();
//			agents.add(a3);
//			Agent a4 = new Agent();
//			Workload wa4 = new Workload("s4",0);
//			a4.addProperty(wa4);
//			agents.add(a4);
//			Agent a5 = new Agent();
//			Workload wa5 = new Workload("s5",0);
//			a5.addProperty(wa5);
//			agents.add(a5);
//			Agent a6 = new Agent();
//			agents.add(a6);
			
			List<Object> limits = new ArrayList<>();
			Workload w = new Workload("maxEffort", maxEffort);
			Throughput t = new Throughput("maxThroughput", maxThroughput);
			limits.add(w);
			limits.add(t);

			// if an argument to choose a cost function was given
			if (args.length == 2) {
				inicial = new Organisation("orgApp", gTree, Cost.valueOf(args[1]), limits);
			} else {
				inicial = new Organisation("orgApp", gTree, Cost.SPECIALIST, limits);
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
			GoalTree t = new GoalTree(rootNode);
			t.addAllDescendants(rootNode);
			inicial = new Organisation("orgApp", t, Cost.SPECIALIST, true);
		}

		Nodo n = null;

		//n = new BuscaLargura().busca(inicial);
		n = new BuscaProfundidade().busca(inicial);
		
		/**
		 * To create the proof for the current gdt:
		 * ((Organisation) n.getEstado()).plotOrganisation(Cost.SPECIALIST.ordinal(),true);
		 */
		if (n != null) {
			try {
				((Organisation)n.getEstado()).validateOutput();
			} catch (OutputDoesNotMatchWithInput e) {
				e.printStackTrace();
				LOG.fatal(e.getMessage());
			}

			String expectedSolution = "[G{[g0]}S{[]}, G{[g1]}S{[s1]}^[g1][s1], G{[g2]}S{[]}^[g2][], G{[g3]}S{[s2]}^[g3][s2], G{[g4]}S{[]}^[g4][], G{[g5]}S{[s5]}^[g5][s5], G{[g6]}S{[s4, s5]}^[g6][s4, s5]]";
			System.out.println("\n\nProduced output:" + n.getEstado().toString());
			System.out.println("Given proof    :" + expectedSolution);
			                           
			if (n.getEstado().toString().equals(expectedSolution))
				System.out.println("\nThe given solution is correct according to the given proof, the project seems to be creating correct organisations!\n\n");
			else
				System.out.println("\nFALSE!!! Something went wrong on comparing the created organisation with the available proof.\n\n");
		} else {
			System.out.println("\nThe resulting state is null. This behaviour is expected when Organisation.ehMeta() method is set to always return false.\nDid you set the algorithm to find all possible solutions?\n\n");
		}
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
						referenceGoalNode = rootNode;
					} else {
						GoalNode gn = new GoalNode(stack.peek(), eGoal.getAttribute("id"));
						referenceGoalNode = gn;
					}

				} else if (node.getNodeName().equals("plan")) {
					Element ePlan = (Element) node;
					stack.push(referenceGoalNode);
					referenceGoalNode.setOperator(ePlan.getAttribute("operator"));
					SimpleLogger.getInstance().debug(
							"Push = " + referenceGoalNode.toString() + " - Op: " + referenceGoalNode.getOperator());
				} else if (node.getNodeName().equals("skill")) {
					referenceGoalNode.addWorkload(new Workload(eGoal.getAttribute("id"), 0));
					SimpleLogger.getInstance()
							.debug("Skill = " + referenceGoalNode.toString() + " : " + referenceGoalNode.getWorkloads());
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


}
