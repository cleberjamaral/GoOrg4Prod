package organisation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import organisation.goal.GoalNode;
import organisation.role.RoleNode;
import organisation.search.Organisation;

public class OrganisationPlot {

	public OrganisationPlot() {

	}
	
	public void plotOrganisation(Organisation o, int organisationId, boolean generateProof) {
		List<String> links = new ArrayList<>();

		File diagramFile = new File("output/diagrams/tmp");
		diagramFile.getParentFile().mkdirs();

		try (FileWriter fw = new FileWriter("output/diagrams/graph_" + organisationId + ".gv", false);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {

			out.println("digraph G {");
			//TODO: build the roles tree as a nodes tree like in goals node?
			for (RoleNode or : o.getRolesTree()) {
				out.print("\t\"" + or.getRoleName()// headGoal.getGoalName()
						+ "\" [ style = \"filled\" fillcolor = \"white\" fontname = \"Courier New\" "
						+ "shape = \"Mrecord\" label = <<table border=\"0\" cellborder=\"0\" bgcolor=\"white\">"
						+ "<tr><td bgcolor=\"black\" align=\"center\"><font color=\"white\">" + or.getRoleName()
						+ "</font></td></tr><tr><td align=\"center\">" + or.getAssignedGoals() + "</td></tr>");
				for (Object s : or.getWorkloads())
					out.print("<tr><td align=\"left\">" + s.toString() + "</td></tr>");
				out.println("</table>> ];");

				if (or.getParent() != null)
					links.add("\"" + or.getParent().getRoleName() + "\"->\"" + or.getRoleName() + "\"");
			}

			for (String l : links)
				out.println("\t" + l + ";");
			out.println("}");
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (generateProof) {
			File proofFile = new File("output/proofs/tmp");
			proofFile.getParentFile().mkdirs();

			try (FileWriter fw = new FileWriter("output/proofs/graph_" + organisationId + ".txt", false);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter out = new PrintWriter(bw)) {

				out.println(o.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void plotOrganizationalGoalTree(GoalNode gn) {
		File file = new File("output/diagrams/tmp");
		file.getParentFile().mkdirs();

		try (FileWriter fw = new FileWriter("output/diagrams/gdt.gv", false);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {

			out.println("digraph G {");
			plotGoalNode(out, gn);

			out.println("}");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void deleteExistingDiagrams() {
		try {
			File filepath = new File("output/diagrams");
			FileUtils.deleteDirectory(filepath);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void plotGoalNode(PrintWriter out, GoalNode or) {
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
		for (Object s : or.getWorkloads())
			out.print("<tr><td align=\"left\"><sub><i>" + s + "</i></sub></td></tr>");
		out.println("</table>> ];");
		or.getDescendents().forEach(g -> {
			plotGoalNode(out, g);
			if (g.getParent() != null)
				out.println("\t\"" + g.getParent().getGoalName() + "\"->\"" + g.getGoalName() + "\";");

		});
	}
}
