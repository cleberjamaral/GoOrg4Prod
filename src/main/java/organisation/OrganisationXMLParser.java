package organisation;

import java.io.File;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import annotations.Inform;
import annotations.Workload;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import simplelogger.SimpleLogger;

public class OrganisationXMLParser {

	public GoalTree parseXMLFile(String file) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(file));

			if (!document.getDocumentElement().getNodeName().equals("organisational-specification"))
				throw new IllegalArgumentException(
						"Error! It is expected an 'organisational-specification' XML structure");

			document.getDocumentElement().normalize();

			// Visit all possible schemes from Moise 'functional-specification'
			NodeList nList = document.getElementsByTagName("scheme");
			Stack<GoalNode> stack = new Stack<GoalNode>();
			GoalNode rootNode = new GoalNode(null, null);
			visitNodes(nList, rootNode, null, stack);
			
	    	GoalTree gTree = new GoalTree(rootNode);
	    	gTree.addAllDescendants(gTree.getRootNode());
	    	gTree.updateRecipientGoals(gTree.getRootNode());
	    	
			return gTree;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void visitNodes(NodeList nList, GoalNode rootNode, GoalNode referenceGoalNode, Stack<GoalNode> stack) {
		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node node = nList.item(temp);

			if (node.getNodeType() == Node.ELEMENT_NODE) {

				Element eGoal = (Element) node;
				if (node.getNodeName().equals("goal")) {
					SimpleLogger.getInstance().debug("Node id = " + eGoal.getAttribute("id"));

					if (referenceGoalNode == null) {
						rootNode.setGoalName(eGoal.getAttribute("id"));
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
				} else if (node.getNodeName().equals("workload")) {
					referenceGoalNode.addWorkload(new Workload(eGoal.getAttribute("id"), Double.parseDouble(eGoal.getAttribute("value"))));
					SimpleLogger.getInstance()
							.debug("W=" + referenceGoalNode.toString() + ":" + referenceGoalNode.getWorkloads());
				} else if (node.getNodeName().equals("inform")) {
                    Inform i = new Inform(eGoal.getAttribute("id"), eGoal.getAttribute("recipient"), Double.parseDouble(eGoal.getAttribute("value")));
					referenceGoalNode.addInform(i);
					SimpleLogger.getInstance().debug("I=" + referenceGoalNode.toString() + ":" + referenceGoalNode.getInforms());
				} else if (node.getNodeName().equals("mission")) {
					return; // end of scheme goals
				}
				if (node.hasChildNodes()) {

					visitNodes(node.getChildNodes(), rootNode, referenceGoalNode, stack);
					if (node.getNodeName().equals("plan")) {
						GoalNode tempGN = stack.pop();
						SimpleLogger.getInstance().debug("Poping = " + tempGN.toString());
					}
				}
			}
		}
	}
}
