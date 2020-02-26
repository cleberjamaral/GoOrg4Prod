package organisation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizEngine;
import guru.nidi.graphviz.engine.GraphvizJdkEngine;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import guru.nidi.graphviz.engine.GraphvizV8Engine;

import annotations.DataLoad;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.role.RoleNode;
import organisation.search.Organisation;

public class OrganisationPlot {
	
	DecimalFormat df = new DecimalFormat("#.##");

	public OrganisationPlot() {

	}

	public String plotOrganisation(final Organisation o, final String plotIndex) {
		final Set<String> links = new HashSet<>();

		createOutPutFolders();

		String index = "";
		if (!plotIndex.equals(""))
			index = "_" + plotIndex;

		try (PrintWriter pout = new PrintWriter(
				new BufferedWriter(new FileWriter("output/diagrams/" + o.getOrgName() + index + ".gv", false)))) {

			final StringWriter out = new StringWriter();
			out.write("digraph G {\n");

			for (final RoleNode or : o.getRolesTree().getTree()) {
				out.write("\t\"" + or.getRoleName() + "\" [ style = \"filled\" fillcolor = \"white\" "
						+ "shape = \"Mrecord\" label = <<table border=\"0\" cellborder=\"0\" bgcolor=\"white\">"
						+ "<tr><td bgcolor=\"black\" align=\"center\"><font color=\"white\">" + or.getRoleName()
						+ "</font></td></tr><tr><td align=\"center\">" + or.getAssignedGoals() + "</td></tr>");

				for (final Object s : or.getWorkloads())
					out.write("<tr><td align=\"center\">" + s.toString() + "</td></tr>");

				for (final Object s : or.getInforms())
					out.write("<tr><td align=\"center\">" + s.toString() + "</td></tr>");

				for (final Object s : or.getDataLoads())
					out.write("<tr><td align=\"center\"><sub><i>" + s.toString() + "</i></sub></td></tr>");

				out.write("</table>> ];\n");

				for (final DataLoad d : or.getDataLoads()) {
					for (final RoleNode rnn : o.getRolesTree().getTree()) {
						for (final GoalNode g : rnn.getAssignedGoals()) {
							if (g.getGoalName().equals(d.getSenderName())) {
								out.write("\t\"" + rnn.getRoleName() + "\"->\"" + or.getRoleName() + "\" [label=\""
										+ d.getId() + ":" + df.format(d.getValue())
										+ "\" style=dotted arrowhead=vee fontcolor=grey20 color=grey20];");
							}
						}
					}
				}

				if (or.getParent() != null)
					links.add("\"" + or.getParent().getRoleName() + "\"->\"" + or.getRoleName() + "\"");
			}

			for (final String l : links)
				out.write("\t" + l + ";\n");
			out.write("}\n");

			// save .gv file
			pout.print(out);

			return out.toString();

		} catch (final IOException e) {
			e.printStackTrace();
		}
		return null;
	}

    public void saveDotAsPNG(final String name, final String out) {
        // save .png file
        Graphviz.useEngine(new GraphvizV8Engine(), new GraphvizJdkEngine());
        List<GraphvizEngine> engines = new ArrayList<>();
        try {
            GraphvizEngine engine = new GraphvizV8Engine();
            engines.add(engine);
        } catch (java.lang.NoClassDefFoundError e) {
        }
        try {
            GraphvizEngine engine = new GraphvizJdkEngine();
            engines.add(engine);
        } catch (java.lang.NoClassDefFoundError e) {
        }
        Graphviz.useEngine(engines);
        try {
            final String filename = "output/graphs/" + name + ".png";
            final MutableGraph mg = new Parser().read(out);
            mg.setName(filename);
            Graphviz.fromGraph(mg).render(Format.PNG).toOutputStream(new FileOutputStream(filename, false));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public String plotGoalTree(final String plotName, final GoalTree gt) {
        createOutPutFolders();
        
		try (PrintWriter pout = new PrintWriter(new BufferedWriter(new FileWriter("output/diagrams/" + plotName + ".gv", false)))) {

            final StringWriter out = new StringWriter();

			out.write("digraph G {\n");
			plotGoalNode(out, gt.getRootNode());

            out.write("}\n");
 
            // save .gv file
            pout.print(out);

            return out.toString();
            
		} catch (final IOException e) {
			e.printStackTrace();
        }
        
        return null;
	}

    private void createOutPutFolders() {
        // create folders if doesnt exist
		File file = new File("output/diagrams/tmp");
		file.getParentFile().mkdirs();
        file = new File("output/graphs/tmp");
        file.getParentFile().mkdirs();
    }

	public void deleteExistingDiagrams() {
		try {
			final File filepath = new File("output/diagrams");
			FileUtils.deleteDirectory(filepath);

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

    public void deleteExistingGraphs() {
		try {
			final File filepath = new File("output/graphs");
			FileUtils.deleteDirectory(filepath);

		} catch (final IOException e) {
			e.printStackTrace();
		}
    }
    
	public void plotGoalNode(final StringWriter out, final GoalNode g) {
		if (g.getOperator().equals("parallel")) {
			out.write("\t\"" + g.getGoalName()
					+ "\" [ style = \"filled\" fillcolor = \"white\" "
					+ "shape = \"Mrecord\" label = <<table border=\"0\" cellborder=\"0\">"
					+ "<tr><td align=\"center\"><font color=\"black\"><b>" + g.getGoalName()
					+ "</b></font></td></tr>");
		} else {
			out.write("\t\"" + g.getGoalName()
					+ "\" [ style = \"filled\" fillcolor = \"white\" "
					+ "shape = \"Mrecord\" label = <<table border=\"0\" cellborder=\"0\">"
					+ "<tr><td align=\"center\"><b>" + g.getGoalName() + "</b></td></tr>");
		}

		for (final Object s : g.getWorkloads())
			out.write("<tr><td align=\"center\"><sub><i>" + s + "</i></sub></td></tr>");

		for (final Object s : g.getInforms())
			out.write("<tr><td align=\"center\">" + s + "</td></tr>");

		for (final Object s : g.getDataLoads())
			out.write("<tr><td align=\"center\"><sub><i>" + s + "</i></sub></td></tr>");

		out.write("</table>> ];\n");

		for (final DataLoad s : g.getDataLoads())
			out.write("\t\"" + s.getSender() + "\"->\"" + g.getGoalName() + "\" [label=\"" + s.getId() + ":"
					+ df.format(s.getValue()) + "\" style=dotted arrowhead=vee fontcolor=grey20 color=grey20];\n");

		g.getDescendants().forEach(dg -> {
			plotGoalNode(out, dg);
			if (dg.getParent() != null)
				out.write("\t\"" + dg.getParent().getGoalName() + "\"->\"" + dg.getGoalName() + "\";\n");

		});
	}
}
