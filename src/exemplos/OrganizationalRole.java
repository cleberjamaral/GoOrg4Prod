package exemplos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import busca.Antecessor;
import busca.BuscaIterativo;
import busca.BuscaLargura;
import busca.BuscaProfundidade;
import busca.Estado;
import busca.Nodo;

import simplelogger.SimpleLogger;

public class OrganizationalRole implements Estado, Antecessor {

	private static SimpleLogger LOG = SimpleLogger.getInstance(3);

	private static List<OrganizationalRole> isGoalList = new ArrayList<OrganizationalRole>();
	private static List<OrganizationalRole> rolesList = new ArrayList<OrganizationalRole>();

	GoalNode headGoal;
	private Set<String> roleSkills = new HashSet<String>();
	private List<GoalNode> goalSuccessors = new ArrayList<GoalNode>();

	private OrganizationalRole roleParent;

	private Set<String> graphLinks = new HashSet<String>();

	public String getDescricao() {
		return "Empty\n";
	}

	public OrganizationalRole(GoalNode gn) {
		headGoal = gn;

		if (gn.parent == null) {
			for (GoalNode goal : headGoal.getSuccessors())
				goalSuccessors.add(goal);
			rolesList.add(this);
		}
	}

	public boolean ehMeta() {

		if (goalSuccessors.size() <= 0) {
			if (!isGoalList.contains(this)) {
				isGoalList.add(this);
				LOG.info("GOAL ACHIEVED! Solution: #" + isGoalList.size());

				try (FileWriter fw = new FileWriter("graph_" + isGoalList.size() + ".gv", false);
						BufferedWriter bw = new BufferedWriter(fw);
						PrintWriter out = new PrintWriter(bw)) {
					out.println("digraph G {");
					for (OrganizationalRole or : rolesList) {
						// out.println(or.headGoal.goalName + ";");
						out.print("\t\"" + or.headGoal.goalName
								+ "\" [ style = \"filled\" fillcolor = \"white\" fontname = \"Courier New\" "
								+ "shape = \"Mrecord\" label = <<table border=\"0\" cellborder=\"0\" bgcolor=\"white\">"
								+ "<tr><td bgcolor=\"black\" align=\"center\"><font color=\"white\">"
								+ or.headGoal.goalName + "</font></td></tr>");
						for (String s : or.roleSkills)
							out.print("<tr><td align=\"left\">" + s + "</td></tr>");
						out.println("</table>> ];");
					}

					for (String s : this.graphLinks)
						out.println("\t" + s + ";");
					out.println("}");
				} catch (IOException e) {
				}
			} else {
				// This should not happen again, it has occurred because searching process
				// (deepth) was calling ehMeta 2 times
				LOG.warn("* * * Goal achieved but duplicated * * *");
			}
		}
		return false;
	}

	public OrganizationalRole getRootRole(OrganizationalRole node) {
		if (node.roleParent == null)
			return node;
		else
			return getRootRole(node.roleParent);
	}

	/** * Java method to print leaf nodes using recursion * * @param root * */

	public int custo() {
		return 1;
	}

	/** Lista de sucessores */
	public List<Estado> sucessores() {
		List<Estado> suc = new LinkedList<Estado>(); // Lista de sucessores

		if (!goalSuccessors.isEmpty())
			LOG.debug("\nPARENT: " + this.toString() + " - OpenGoals: [" + goalSuccessors.toString() + "] - Size: "
					+ headGoal.getSuccessors().size());

		for (GoalNode goalToBeAssociated : goalSuccessors) {
			addRelation(suc, goalToBeAssociated);
			joinAnother(suc, goalToBeAssociated);
		}

		return suc;
	}

	public void addRelation(List<Estado> suc, GoalNode goalToBeAssociatedToRole) {
		OrganizationalRole newRole = new OrganizationalRole(goalToBeAssociatedToRole);

		// Copy all skills of the goal to this new role
		for (String skill : goalToBeAssociatedToRole.getSkills())
			newRole.roleSkills.add(skill);

		newRole.roleParent = this;
		for (String s : this.graphLinks)
			newRole.graphLinks.add(s);

		// The new role is a child of the current state (current role)
		OrganizationalRole parentOR = null;
		for (OrganizationalRole or : rolesList)
			if (or.headGoal == goalToBeAssociatedToRole.parent) {
				parentOR = or;
				break;
			}
		newRole.graphLinks.add(parentOR.headGoal.goalName + "->" + newRole.headGoal.goalName);

		for (GoalNode goal : this.goalSuccessors) {
			if (goal != newRole.headGoal)
				newRole.goalSuccessors.add(goal);
		}

		String logg;
		logg = "\taddRelation: " + newRole.toString() + ", Links: [ ";
		for (String s : newRole.graphLinks)
			logg += s + " ";
		logg += "], nSucc: " + newRole.goalSuccessors.size();
		LOG.debug(logg);

		suc.add(newRole);

		boolean isNew = true;
		for (OrganizationalRole or : rolesList)
			if (or.headGoal == newRole.headGoal) {
				isNew = false;
				break;
			}
		if (isNew) {
			rolesList.add(newRole);
		}
	}

	public void joinAnother(List<Estado> suc, GoalNode goalToBeAssociatedToRole) {
		// if the goal has skills (not null) and this node has all skills, so we can
		// join the goals in an unique role
		if ((this.roleSkills.containsAll(goalToBeAssociatedToRole.getSkills()))
				&& (!goalToBeAssociatedToRole.getSkills().isEmpty())
				&& (!goalToBeAssociatedToRole.getParent().getOperator().equals("parallel"))) {
			// Creates a new state which is the same role but with another equal link (just
			// to make it different)
			OrganizationalRole newRole = new OrganizationalRole(goalToBeAssociatedToRole);
			for (String skill : goalToBeAssociatedToRole.getSkills())
				newRole.roleSkills.add(skill);
			newRole.roleParent = this.roleParent;
			for (String s : this.graphLinks)
				newRole.graphLinks.add(s);
			// create link between goal's parent and the already existing equivalent role
			newRole.graphLinks.add(goalToBeAssociatedToRole.parent.goalName + "->" + this.headGoal.goalName);

			for (GoalNode goal : this.goalSuccessors) {
				if (goal != newRole.headGoal)
					newRole.goalSuccessors.add(goal);
			}

			String logg;
			logg = "\tjoinAnother: " + newRole.toString() + ", Links: [ ";
			for (String s : newRole.graphLinks)
				logg += s + " ";
			logg += "], nSucc: " + newRole.goalSuccessors.size();
			LOG.debug(logg);

			suc.add(newRole);

			boolean isNew = true;
			for (OrganizationalRole or : rolesList)
				if (or.headGoal == newRole.headGoal) {
					isNew = false;
					break;
				}
			if (isNew)
				rolesList.add(newRole);
		}
	}

	/** Lista de antecessores, para busca bidirecional */
	public List<Estado> antecessores() {
		return sucessores();
	}

	public String toString() {
		String r = "";
		r += headGoal.toString();
		if (!this.roleSkills.isEmpty())
			r += "[" + this.roleSkills.toString() + "]";

		return r;
	}

	/**
	 * Verifica se um estado � igual a outro j� inserido na lista de sucessores
	 * (usado para poda)
	 */
	public boolean equals(Object o) {
		try {
			if (o instanceof OrganizationalRole) {
				return this.graphLinks.equals(((OrganizationalRole) o).graphLinks);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * retorna o hashCode desse estado (usado para poda, conjunto de fechados)
	 */

	public int hashCode() {
		return this.graphLinks.hashCode();

	}

	/**
	 * Custo acumulado g
	 */
	public int custoAcumulado() {
		return 0;
	}

	public static void main(String[] a) throws IOException {

		// Sample organization : paint a house
		GoalNode paintHouse = new GoalNode(null, "paintHouse");
		GoalNode contracting = new GoalNode(paintHouse, "contracting");
		contracting.operator = "parallel";
		contracting.addSkill("getBids");
		GoalNode bidIPaint = new GoalNode(contracting, "bidIPaint");
		bidIPaint.addSkill("bid");
		bidIPaint.addSkill("paint");
		GoalNode bidEPaint = new GoalNode(contracting, "bidEPaint");
		bidEPaint.addSkill("bid");
		bidEPaint.addSkill("paint");
		GoalNode execute = new GoalNode(paintHouse, "execute");
		GoalNode contractWinner = new GoalNode(execute, "contractWinner");
		contractWinner.addSkill("contract");
		GoalNode iPaint = new GoalNode(execute, "iPaint");
		iPaint.addSkill("bid");
		iPaint.addSkill("paint");
		GoalNode ePaint = new GoalNode(execute, "ePaint");
		ePaint.addSkill("paint");

		OrganizationalRole inicial = new OrganizationalRole(paintHouse);

		String str;
		BufferedReader teclado;
		teclado = new BufferedReader(new InputStreamReader(System.in));

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

	public static class GoalNode {
		private List<String> skills = new ArrayList<String>();
		private List<GoalNode> successors = new ArrayList<GoalNode>();
		private String goalName;
		private GoalNode parent;
		private String operator;

		public GoalNode(GoalNode p, String name) {
			goalName = name;
			parent = p;
			operator = "sequence";
			if (parent != null) {
				parent.addSuccessors(this);
			}
		}

		public void addSkill(String newSkill) {
			skills.add(newSkill);
		}

		public List<String> getSkills() {
			return skills;
		}

		private void addSuccessors(GoalNode newSuccessor) {
			successors.add(newSuccessor);
			if (parent != null)
				parent.addSuccessors(newSuccessor);
		}

		public List<GoalNode> getSuccessors() {
			return successors;
		}

		public String getGoalName() {
			return goalName;
		}

		public GoalNode getParent() {
			return parent;
		}

		public void setOperator(String op) {
			this.operator = op;
		}

		public String getOperator() {
			return operator;
		}

		public String toString() {
			return goalName;
		}
	}

}
