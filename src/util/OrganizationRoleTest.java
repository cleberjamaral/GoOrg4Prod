package util;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import busca.BuscaIterativo;
import busca.BuscaLargura;
import busca.BuscaProfundidade;
import busca.Nodo;
import exemplos.OrganizationalRole;
import exemplos.OrganizationalRole.GoalNode;

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

import simplelogger.SimpleLogger;

public class OrganizationRoleTest {
	
	static List<GoalNode> tree = new ArrayList<GoalNode>();
	static Stack<GoalNode> stack = new Stack<GoalNode>();
	static GoalNode rootNode = null;
	static boolean pushGoalNode = false;
	static GoalNode referenceGoalNode = null;

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		
		// set verbose level
		if (args[1].isEmpty())
			SimpleLogger.getInstance(3);
		else
			SimpleLogger.getInstance(Integer.valueOf(args[1]));

		String str;
		BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
		if (args[0].isEmpty()) {
			System.out.print("Digite o nome do arquivo XML organizational-specification: ");
			str = teclado.readLine();
		} else {
			str = args[0];
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new File(str));

		if (!document.getDocumentElement().getNodeName().equals("organisational-specification"))
			throw new IllegalArgumentException("Error! It is expected an 'organisational-specification' XML structure");

		document.getDocumentElement().normalize();
		//Visit all possible schemes from Moise 'functional-specification'
		NodeList nList = document.getElementsByTagName("scheme");

		visitNodes(nList);		
		
		plotOrganizationalGoalTree();
		
		OrganizationalRole inicial = new OrganizationalRole(rootNode);

		Nodo n = null;

		System.out.print("Digite sua opcao de busca { Digite S para finalizar }\n");
		System.out.print("\t1  -  Largura\n");
		System.out.print("\t2  -  Profundidade\n");
		System.out.print("\t3  -  Pronfundidade Iterativo\n");
		System.out.print("Opcao: ");
		str = teclado.readLine().toUpperCase();
		if (!str.equals("S")) {
			if (str.equals("1")) {
				
				System.out.println("Busca em Largura");
				n = new BuscaLargura().busca(inicial);
			} else {
				if (str.equals("2")) {
					System.out.println("Busca em Profundidade");
					n = new BuscaProfundidade(100).busca(inicial);
				} else {
					if (str.equals("3")) {
						System.out.println("Busca em Profundidade Iterativo");
						n = new BuscaIterativo().busca(inicial);
					}
				}
			}
			if (str.equals("1") || str.equals("2") || str.equals("3")) {
				if (n == null) {
					System.out.println("Sem Solucao!");
				} else {
					System.out.println("Solucao:\n" + n.montaCaminho() + "\n\n");
				}
			}
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
						rootNode = new GoalNode(null,eGoal.getAttribute("id"));
						tree.add(rootNode);
						referenceGoalNode = rootNode;
					} else {
						GoalNode gn = new GoalNode(stack.peek(),eGoal.getAttribute("id"));
						tree.add(gn);
						referenceGoalNode = gn;
					}
					
				} else if (node.getNodeName().equals("plan")) {
					Element ePlan = (Element) node;
					stack.push(referenceGoalNode);
					referenceGoalNode.setOperator(ePlan.getAttribute("operator"));
					SimpleLogger.getInstance().debug("Push = " + referenceGoalNode.toString() + " - Op: " + referenceGoalNode.getOperator());
				} else if (node.getNodeName().equals("skill")) {
					referenceGoalNode.addSkill(eGoal.getAttribute("id"));
					SimpleLogger.getInstance().debug("Skill = " + referenceGoalNode.toString() + " : " + referenceGoalNode.getSkills());
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
		try (FileWriter fw = new FileWriter("orgTree.gv", false);
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
					out.println("\t" + or.getParent().getGoalName() + "->" + or.getGoalName() + ";");
    		}
        		
        	out.println("}");
		} catch (IOException e) {
		}
	}
}
