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
import organisation.exception.CircularReference;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import simplelogger.SimpleLogger;

public class OrganisationXMLParser {

    public void parseXMLFile(final String file) {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.parse(new File(file));

            if (!document.getDocumentElement().getNodeName().equals("organisational-specification"))
                throw new IllegalArgumentException(
                        "Error! It is expected an 'organisational-specification' XML structure");

            document.getDocumentElement().normalize();

            // Visit all possible schemes from Moise 'functional-specification'
            final NodeList nList = document.getElementsByTagName("scheme");
            final Stack<GoalNode> stack = new Stack<GoalNode>();

            final GoalTree gTree = GoalTree.getInstance();
            visitNodes(nList, gTree, null, stack);

            gTree.addAllDescendants(gTree.getRootNode());
            gTree.updateInformAndDataLoadReferences(gTree.getRootNode());

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void visitNodes(final NodeList nList, final GoalTree gTree, GoalNode referenceGoalNode,
            final Stack<GoalNode> stack) {
        try {
            for (int temp = 0; temp < nList.getLength(); temp++) {

                final Node node = nList.item(temp);

                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    final Element eGoal = (Element) node;
                    if (node.getNodeName().equals("goal")) {
                        SimpleLogger.getInstance().debug("Node id = " + eGoal.getAttribute("id"));

                        if (referenceGoalNode == null) {
                            final GoalNode rootNode = new GoalNode(null, eGoal.getAttribute("id"));
                            gTree.setRootNode(rootNode);
                            
                            referenceGoalNode = gTree.getRootNode();
                        } else {
                            referenceGoalNode = gTree.addGoal(eGoal.getAttribute("id"), stack.peek());
                        }

                    } else if (node.getNodeName().equals("plan")) {
                        final Element ePlan = (Element) node;
                        stack.push(referenceGoalNode);
                        referenceGoalNode.setOperator(ePlan.getAttribute("operator"));
                        SimpleLogger.getInstance().debug(
                                "Push = " + referenceGoalNode.toString() + " - Op: " + referenceGoalNode.getOperator());
                    } else if (node.getNodeName().equals("workload")) {
                        referenceGoalNode.addWorkload(new Workload(eGoal.getAttribute("id"),
                                Double.parseDouble(eGoal.getAttribute("value"))));
                        SimpleLogger.getInstance()
                                .debug("W=" + referenceGoalNode.toString() + ":" + referenceGoalNode.getWorkloads());
                    } else if (node.getNodeName().equals("inform")) {
                        final Inform i = new Inform(eGoal.getAttribute("id"), eGoal.getAttribute("recipient"),
                                Double.parseDouble(eGoal.getAttribute("value")));
                        referenceGoalNode.addInform(i);
                        SimpleLogger.getInstance()
                                .debug("I=" + referenceGoalNode.toString() + ":" + referenceGoalNode.getInforms());
                    } else if (node.getNodeName().equals("mission")) {
                        return; // end of scheme goals
                    }
                    if (node.hasChildNodes()) {

                        visitNodes(node.getChildNodes(), gTree, referenceGoalNode, stack);
                        if (node.getNodeName().equals("plan")) {
                            final GoalNode tempGN = stack.pop();
                            SimpleLogger.getInstance().debug("Poping = " + tempGN.toString());
                        }
                    }
                }
            }
        } catch (CircularReference e) {
            e.printStackTrace();
        }
    }
}
