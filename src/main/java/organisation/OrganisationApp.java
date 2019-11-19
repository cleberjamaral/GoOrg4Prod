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
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.search.Cost;
import organisation.search.Organisation;
import properties.Workload;

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

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {

		// set verbose level
		SimpleLogger.getInstance(4);

		Organisation inicial;
		// if a Moise XML file was not provided, use a sample organisation
		if ((args.length < 1) || (args[0].equals("0"))) {
			GoalTree gTree;
			// Sample organization
			gTree = new GoalTree("g1");
			gTree.addGoal("g11","g1");
			gTree.addWorkload("g11", "s1", 2);
			gTree.addGoal("g12","g1");
			gTree.addGoal("g111","g11");
			gTree.addWorkload("g111", "s2", 8);
			gTree.addGoal("g112","g11");
			gTree.addWorkload("g112", "s2", 2);
			gTree.addGoal("g121","g12");
			gTree.addWorkload("g121", "s2", 0);
			gTree.addGoal("g122","g12");
			gTree.addWorkload("g122", "s2", 3.4);
			gTree.addGoal("g1221","g122");
			gTree.addWorkload("g1221", "s2", 3.4);
			/*
			GoalTree t = new GoalTree("PaintHouse");
			//t.addGoalToTree("GetInputs", "PaintHouse");
			//t.addWorkloadToGoal("GetInputs", "Contract", 2);
			t.addGoalToTree("Paint", "PaintHouse");
			t.addWorkloadToGoal("Paint", "Paint1", 2);
			//t.addGoalToTree("BuyInputs", "GetInputs");
			//t.addWorkloadToGoal("BuyInputs", "Purchase", 4);
			//t.addThroughputToGoal("BuyInputs","messages",100);
			//t.addThroughputToGoal("BuyInputs","reports",100);
			t.addGoalToTree("Inspect", "PaintHouse");
			t.addGoalToTree("Report", "Inspect");
			t.addWorkloadToGoal("Report", "Paint", 0);
			t.addGoalToTree("CheckInt", "Inspect");
			t.addGoalToTree("CheckExt", "CheckInt");
			t.addWorkloadToGoal("CheckExt", "Paint", 3.4);
			t.addGoalToTree("PaintInt", "Paint");
			t.addWorkloadToGoal("PaintInt", "Paint", 8);
			t.addGoalToTree("PaintExt", "Paint");
			t.addWorkloadToGoal("PaintExt", "Paint", 2);
			//t.addWorkloadToGoal("PaintExt", "Scaffold", 3);
			*/
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
			Workload w = new Workload("maxEffort",maxEffort);
			limits.add(w);

			// if an argument to choose a cost function was given
			if (args.length == 2) {
				inicial = new Organisation(gTree.getBrokenGoalTree(maxEffort), Cost.valueOf(args[1]), limits);
			} else {
				inicial = new Organisation(gTree.getBrokenGoalTree(maxEffort), Cost.UNITARY, limits);
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

		Nodo n = null;

		n = new BuscaLargura().busca(inicial);
		//n = new BuscaProfundidade().busca(inicial);
		
		/**
		 * To create the proof for the current gdt:
		 * ((Organisation) n.getEstado()).plotOrganisation(Cost.SPECIALIST.ordinal(),true);
		 */
		if (n != null) {
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
						//tree.add(rootNode);
						referenceGoalNode = rootNode;
					} else {
						GoalNode gn = new GoalNode(stack.peek(), eGoal.getAttribute("id"));
						//tree.add(gn);
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
