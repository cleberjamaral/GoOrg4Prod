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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import simplelogger.SimpleLogger;

public class OrganisationApp {

	static List<GoalNode> tree = new ArrayList<GoalNode>();
	static Stack<GoalNode> stack = new Stack<GoalNode>();
	static GoalNode rootNode = null;
	static GoalNode referenceGoalNode = null;

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {

		// set verbose level
		SimpleLogger.getInstance(3);

		Organisation inicial;
		// if a Moise XML file was not provided, use a sample organisation
		if ((args.length < 1) || (args[0].equals("0"))) {
			
			// Sample goals tree
			GoalNode g0 = new GoalNode(null, "PaintHouse");
			tree.add(g0);
			GoalNode g1 = new GoalNode(g0, "GetInputs");
			Workload w1 = new Workload("Contract",0);
			g1.addRequirement(w1);
			tree.add(g1);
			GoalNode g2 = new GoalNode(g0, "Paint");
			tree.add(g2);
			GoalNode g3 = new GoalNode(g1, "BuyInputs");
			tree.add(g3);
			Workload w2 = new Workload("Purchase",4);
			g3.addRequirement(w2);
			GoalNode g4 = new GoalNode(g0, "Inspect");
			tree.add(g4);
			GoalNode g5 = new GoalNode(g2, "PaintInt");
			tree.add(g5);
			Workload w5 = new Workload("Paint",8);
			g5.addRequirement(w5);
			GoalNode g6 = new GoalNode(g2, "PaintExt");
			tree.add(g6);
			Workload w4 = new Workload("Paint",4);
			g6.addRequirement(w4);
			Workload w5b = new Workload("Scaffold",2);
			g6.addRequirement(w5b);
			
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
			Workload w = new Workload("s",8);
			limits.add(w);
			
			//GoalNode newRoot = g0;
			GoalNode newRoot = g0.cloneContent();
			brakeGoalTree(g0, newRoot);

			// if an argument to choose a cost function was given
			if (args.length == 2) {
				inicial = new Organisation(newRoot, Cost.valueOf(args[1]));
			} else {
				inicial = new Organisation(newRoot, Cost.SPECIALIST);
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
	
	private static void brakeGoalTree(GoalNode original, GoalNode parent) {
		original.getDescendents().forEach(s -> {
			GoalNode g = s.cloneContent();
			g.setParent(parent);
			brakeGoalTree(s, g);
		});
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


}
