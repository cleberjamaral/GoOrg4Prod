package organisation;

import java.util.ArrayList;
import java.util.List;

import organisation.exception.CircularReference;
import organisation.exception.GoalNotFound;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.resource.AgentSet;
import organisation.search.Organisation;
import organisation.search.cost.Cost;
import simplelogger.SimpleLogger;

/**
 * @author cleber
 *
 */
public class OrganisationApp {

	private static SimpleLogger LOG = SimpleLogger.getInstance();

	public static void main(String[] args) {

		List<Cost> preferences = new ArrayList<>();
		String search = "BFS";

		OrganisationGenerator orgGen = new OrganisationGenerator();
		OrganisationBinder orgBin = new OrganisationBinder();

		// if an argument to choose a cost function was given
		for (int i = 1; i < args.length; i++)
			preferences.add(Cost.valueOf(args[i]));

		if (preferences.size() == 0)
			preferences.add(Cost.GENERALIST);

		// if a Moise XML file was not provided, use a sample organisation
		if ((args.length < 1) || (args[0].equals("0"))) {
		
			// Sample organization
			try {
				Parameters.getInstance();
				Parameters.setMaxWorkload(8.0);
				Parameters.setWorkloadGrain(2.0);
				Parameters.setMaxDataLoad(8.0);
				Parameters.setDataLoadGrain(2.0);
				Parameters.setOneSolution(false);

				LOG.info("Search algorit: "+ search);

				GoalNode g0 = new GoalNode(null, "g0");
				GoalTree gTree = GoalTree.getInstance();
				gTree.setRootNode(g0);
				gTree.addGoal("g1", "g0");
				gTree.addWorkload("g0", "w0", 5);
				gTree.addWorkload("g1", "w1", 5);
				gTree.addInform("g1", "i1", "g0", 1);

				// perform organisation generation (free design)
				Organisation org = orgGen.generateOrganisationFromTree("sample", preferences, search, Parameters.isOneSolution());

				// set available agents for this example
				AgentSet agents = AgentSet.getInstance();
				agents.addAgent("bob", new String[]{"w0"});
				agents.addAgent("alice", new String[]{"w1"});
				agents.addAgent("tom", new String[]{"w1"});

				// bind agents and positions
				orgBin.bindOrganisations(org, agents);
				
			} catch (GoalNotFound | CircularReference e) {
				e.printStackTrace();
			}

		} else {
			// Expected input example:
			// ./gradlew run --args="examples/Full_Link_ultramegasimple.xml GENERALIST BFS"
			OrganisationXMLParser parser = new OrganisationXMLParser();
			parser.parseOrganisationSpecification(args[0]);
			parser.parseDesignParameters(args[0]);
			parser.parseAvailableAgents(args[0]);
			
			String path[] = args[0].split("/");
			String name = path[path.length - 1];
			name = name.substring(0, name.length() - 4);

			LOG.info("Search algorit: "+ search);

			// generate organisations
			Organisation org = orgGen.generateOrganisationFromTree(name, preferences, search, Parameters.isOneSolution());
			
			// get parsed agents set
			AgentSet agents = AgentSet.getInstance();
			
			// perform organisation generation (free design)
			orgBin.bindOrganisations(org, agents);
		}
	}
}
