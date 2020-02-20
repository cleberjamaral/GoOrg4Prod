package organisation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.parse.Parser;

import annotations.DataLoad;
import annotations.Inform;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.role.RoleNode;
import organisation.search.Organisation;

public class OrganisationPlot {
	
	DecimalFormat df = new DecimalFormat("#.##");

	public OrganisationPlot() {

	}

	public void plotOrganisation(Organisation o, String plotIndex) {
		Set<String> links = new HashSet<>();

		createOutPutFolders();
		
		String index = "";
		if (!plotIndex.equals("")) index = "_" + plotIndex;

		try (PrintWriter pout = new PrintWriter(new BufferedWriter(new FileWriter("output/diagrams/" + o.getOrgName() + index + ".gv", false)))) 
		{

            StringWriter out = new StringWriter();
			out.write("digraph G {\n");

			for (RoleNode or : o.getRolesTree().getTree()) {
				out.write("\t\"" + or.getRoleName()
						+ "\" [ style = \"filled\" fillcolor = \"white\" fontname = \"Courier New\" "
						+ "shape = \"Mrecord\" label = <<table border=\"0\" cellborder=\"0\" bgcolor=\"white\">"
						+ "<tr><td bgcolor=\"black\" align=\"center\"><font color=\"white\">" + or.getRoleName()
						+ "</font></td></tr><tr><td align=\"center\">" + or.getAssignedGoals() + "</td></tr>");

				for (Object s : or.getWorkloads())
					out.write("<tr><td align=\"left\">" + s.toString() + "</td></tr>");

				for (Object s : or.getInforms())
					out.write("<tr><td align=\"left\">" + s.toString() + "</td></tr>");

				out.write("</table>> ];\n");

				Set<String> uniqueInformArrows = new HashSet<>();
				Iterator<GoalNode> iterator = or.getAssignedGoals().iterator();
				while (iterator.hasNext()) {
					iterator.next();
					for (Inform s : or.getInforms()) {
						for (RoleNode rnn : o.getRolesTree().getTree()) {
							if (rnn.getAssignedGoals().contains(s.getRecipient()) && (rnn != or)) {
								uniqueInformArrows
										.add("\t\"" + or.getRoleName() + "\"->\"" + rnn.getRoleName() + "\" [label=\""
												+ s.getId() + ":" + df.format(s.getValue()) + "\" style=dotted arrowhead=vee fontcolor=grey20 color=grey20];");
							}
						}
					}
				}
                uniqueInformArrows.forEach(i -> {out.write(i+"\n");});
                if (or.getParent() != null)	
					links.add("\"" + or.getParent().getRoleName() + "\"->\"" + or.getRoleName() + "\"");
			}

			for (String l : links)
				out.write("\t" + l + ";\n");
            out.write("}\n");

            // save .gv file
            pout.print(out);

            // save .png file
            FileOutputStream pdf = new FileOutputStream("output/graphs/" + o.getOrgName() + index + ".png", false);
            Graphviz.fromGraph(Parser.read(out.toString())).render(Format.PNG).toOutputStream(pdf);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void generateProof(Organisation o, String plotName) {
		createOutPutFolders();

		try (FileWriter fw = new FileWriter("output/proofs/" + plotName + ".txt", false);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {

			out.println(o.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void plotGoalTree(String plotName, GoalTree gt) {
        createOutPutFolders();
        
		try (PrintWriter pout = new PrintWriter(new BufferedWriter(new FileWriter("output/diagrams/" + plotName + "_gdt.gv", false)))) {

            StringWriter out = new StringWriter();

			out.write("digraph G {\n");
			plotGoalNode(out, gt.getRootNode());

            out.write("}\n");
 
            // save .gv file
            pout.print(out);

            // save .png file
            FileOutputStream pdf = new FileOutputStream("output/graphs/" + plotName + "_gdt.png", false);
            Graphviz.fromGraph(Parser.read(out.toString())).render(Format.PNG).toOutputStream(pdf);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    private void createOutPutFolders() {
        // create folders if doesnt exist
		File file = new File("output/diagrams/tmp");
		file.getParentFile().mkdirs();
        file = new File("output/graphs/tmp");
        file.getParentFile().mkdirs();
        file = new File("output/proofs/tmp");
        file.getParentFile().mkdirs();
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

    public void deleteExistingGraphs() {
		try {
			File filepath = new File("output/graphs");
			FileUtils.deleteDirectory(filepath);

		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
	public void plotGoalNode(StringWriter out, GoalNode g) {
		if (g.getOperator().equals("parallel")) {
			out.write("\t\"" + g.getGoalName()
					+ "\" [ style = \"filled\" fillcolor = \"white\" fontname = \"Courier New\" "
					+ "shape = \"diamond\" label = <<table border=\"0\" cellborder=\"0\">"
					+ "<tr><td align=\"center\"><font color=\"black\"><b>" + g.getGoalName()
					+ "</b></font></td></tr>");
		} else {
			out.write("\t\"" + g.getGoalName()
					+ "\" [ style = \"filled\" fillcolor = \"white\" fontname = \"Courier New\" "
					+ "shape = \"ellipse\" label = <<table border=\"0\" cellborder=\"0\">"
					+ "<tr><td align=\"center\"><b>" + g.getGoalName() + "</b></td></tr>");
		}

		for (Object s : g.getWorkloads())
			out.write("<tr><td align=\"left\"><sub><i>" + s + "</i></sub></td></tr>");

		for (Object s : g.getDataLoads())
			out.write("<tr><td align=\"left\"><sub><i>" + s + "</i></sub></td></tr>");

		out.write("</table>> ];\n");

		for (DataLoad s : g.getDataLoads())
			out.write("\t\"" + s.getSender() + "\"->\"" + g.getGoalName() + "\" [label=\"" + s.getId() + ":"
					+ df.format(s.getValue()) + "\" style=dotted arrowhead=vee fontcolor=grey20 color=grey20];\n");

		g.getDescendants().forEach(dg -> {
			plotGoalNode(out, dg);
			if (dg.getParent() != null)
				out.write("\t\"" + dg.getParent().getGoalName() + "\"->\"" + dg.getGoalName() + "\";\n");

		});
	}
}
