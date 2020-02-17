package organisation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import annotations.Inform;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.role.RoleNode;
import organisation.search.Organisation;

public class OrganisationPlot {

	public OrganisationPlot() {

	}

	public void plotOrganisation(Organisation o, String plotIndex) {
		List<String> links = new ArrayList<>();

		File diagramFile = new File("output/diagrams/tmp");
		diagramFile.getParentFile().mkdirs();
		
		String index = "";
		if (!plotIndex.equals("")) index = "_" + plotIndex;

		try (FileWriter fw = new FileWriter("output/diagrams/" + o.getOrgName() + index + ".gv",
				false); 
				BufferedWriter bw = new BufferedWriter(fw); PrintWriter out = new PrintWriter(bw)) 
		{

			out.println("digraph G {");
			// TODO: build the roles tree as a nodes tree like in goals node?
			for (RoleNode or : o.getRolesTree().getTree()) {
				out.print("\t\"" + or.getRoleName()// headGoal.getGoalName()
						+ "\" [ style = \"filled\" fillcolor = \"white\" fontname = \"Courier New\" "
						+ "shape = \"Mrecord\" label = <<table border=\"0\" cellborder=\"0\" bgcolor=\"white\">"
						+ "<tr><td bgcolor=\"black\" align=\"center\"><font color=\"white\">" + or.getRoleName()
						+ "</font></td></tr><tr><td align=\"center\">" + or.getAssignedGoals() + "</td></tr>");

				for (Object s : or.getWorkloads())
					out.print("<tr><td align=\"left\">" + s.toString() + "</td></tr>");

				out.println("</table>> ];");

				Set<String> uniqueInformArrows = new HashSet<>();
				Iterator<GoalNode> iterator = or.getAssignedGoals().iterator();
				while (iterator.hasNext()) {
					iterator.next();
					for (Inform s : or.getInforms()) {
						for (RoleNode rnn : o.getRolesTree().getTree()) {
							if (rnn.getAssignedGoals().contains(s.getRecipient()) && (rnn != or)) {
								uniqueInformArrows.add("\t\"" + or.getRoleName() + "\"->\"" + rnn.getRoleName() + "\" [label=\""+ s.getId() + ":" + s.getValue() +"\"] ;");
							}
						}
					}
				}
				uniqueInformArrows.forEach(i -> {out.println(i);});
			}

			for (String l : links)
				out.println("\t" + l + ";");
			out.println("}");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void generateProof(Organisation o, String plotName) {
		File proofFile = new File("output/proofs/tmp");
		proofFile.getParentFile().mkdirs();

		try (FileWriter fw = new FileWriter("output/proofs/" + plotName + ".txt", false);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {

			out.println(o.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void plotGoalTree(String plotName, GoalTree gt) {
		File file = new File("output/diagrams/tmp");
		file.getParentFile().mkdirs();

		try (FileWriter fw = new FileWriter("output/diagrams/" + plotName + "_gdt.gv", false);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {

			out.println("digraph G {");
			plotGoalNode(out, gt.getRootNode());

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

	public void deleteExistingProofs() {
		try {
			File filepath = new File("output/proofs");
			FileUtils.deleteDirectory(filepath);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void plotGoalNode(PrintWriter out, GoalNode g) {
		if (g.getOperator().equals("parallel")) {
			out.print("\t\"" + g.getGoalName()
					+ "\" [ style = \"filled\" fillcolor = \"white\" fontname = \"Courier New\" "
					+ "shape = \"diamond\" label = <<table border=\"0\" cellborder=\"0\">"
					+ "<tr><td align=\"center\"><font color=\"black\"><b>" + g.getGoalName()
					+ "</b></font></td></tr>");
		} else {
			out.print("\t\"" + g.getGoalName()
					+ "\" [ style = \"filled\" fillcolor = \"white\" fontname = \"Courier New\" "
					+ "shape = \"ellipse\" label = <<table border=\"0\" cellborder=\"0\">"
					+ "<tr><td align=\"center\"><b>" + g.getGoalName() + "</b></td></tr>");
		}

		for (Object s : g.getWorkloads())
			out.print("<tr><td align=\"left\"><sub><i>" + s + "</i></sub></td></tr>");

		out.println("</table>> ];");

		for (Inform s : g.getInforms())
			out.println("\t\"" + g.getGoalName() + "\"->\"" + s.getRecipient() + "\" [label=\""+ s.getId() + ":" + s.getValue() +"\"] ;");

		g.getDescendants().forEach(dg -> {
			plotGoalNode(out, dg);
			if (dg.getParent() != null)
				out.println("\t\"" + dg.getParent().getGoalName() + "\"->\"" + dg.getGoalName() + "\";");

		});
	}
}
