package organisation;

import organisation.exception.CircularReference;
import organisation.exception.GoalNotFound;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.search.cost.Cost;
import simplelogger.SimpleLogger;

public class OrganisationApp {

	private static SimpleLogger LOG = SimpleLogger.getInstance();

	public static void main(String[] args) {

		Cost c = Cost.GENERALIST;
		String search = "BFS";

		OrganisationGenerator orgGen = new OrganisationGenerator();

		// if a Moise XML file was not provided, use a sample organisation
		if ((args.length < 1) || (args[0].equals("0"))) {
			// if an argument to choose a cost function was given
			if (args.length == 2)
				c = Cost.valueOf(args[1]);

			// Sample organization
			try {
				Parameters.getInstance();
				Parameters.setMaxWorkload(8.0);
				Parameters.setWorkloadGrain(4.0);
				Parameters.setMaxDataLoad(8.0);
				Parameters.setDataLoadGrain(2.0);
				Parameters.setOneSolution(false);

				LOG.info("Max Workload  : "+ Parameters.getMaxWorkload());
				LOG.info("Workload grain: "+ Parameters.getWorkloadGrain());
				LOG.info("Max DataLoad  : "+ Parameters.getMaxDataLoad());
				LOG.info("DataLoad grain: "+ Parameters.getDataLoadGrain());

				GoalNode g0 = new GoalNode(null, "g0");
				GoalTree gTree = GoalTree.getInstance();
				gTree.setRootNode(g0);
				gTree.addGoal("g1", "g0");
				gTree.addWorkload("g1", "w1", 5);
				gTree.addWorkload("g1", "w2", 5);
				gTree.addInform("g1", "i1", "g0", 7);

				orgGen.generateOrganisationFromTree("sample", c, search, Parameters.isOneSolution());
			} catch (GoalNotFound | CircularReference e) {
				e.printStackTrace();
			}

		} else {
			// Expected input example:
			// ./gradlew run --args="examples/Full_Link_ultramegasimple.xml GENERALIST BFS"
			OrganisationXMLParser parser = new OrganisationXMLParser();
			parser.parseOrganisationSpecification(args[0]);
			parser.parseDesignParameters(args[0]);
			
			Parameters.getInstance();
			LOG.info("Max Workload  : "+ Parameters.getMaxWorkload());
			LOG.info("Workload grain: "+ Parameters.getWorkloadGrain());
			LOG.info("Max DataLoad  : "+ Parameters.getMaxDataLoad());
			LOG.info("DataLoad grain: "+ Parameters.getDataLoadGrain());

			String path[] = args[0].split("/");
			String name = path[path.length - 1];
			name = name.substring(0, name.length() - 4);

			if (args.length >= 2)
				c = Cost.valueOf(args[1]);

			if (args.length >= 3)
				search = args[2];

			orgGen.generateOrganisationFromTree(name, c, search, Parameters.isOneSolution());
		}
	}
}
