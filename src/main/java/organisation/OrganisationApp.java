package organisation;

import organisation.exception.CircularReference;
import organisation.exception.GoalNotFound;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.search.Cost;
import organisation.search.Parameters;

public class OrganisationApp {

	public static void main(String[] args) {

		Cost c = Cost.GENERALIST;
		String search = "BFS";
		boolean oneSolution = true;

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
				System.out.println("Max workload is 8");
				System.out.println("workload grain is 4");
				Parameters.setMaxDataLoad(8.0);
				Parameters.setDataLoadGrain(2.0);
				System.out.println("Max dataload is 8");
				System.out.println("dataloadgrain is 2");

				GoalNode g0 = new GoalNode(null, "g0");
				GoalTree gTree = GoalTree.getInstance();
				gTree.setRootNode(g0);
				gTree.addGoal("g1", "g0");
				gTree.addWorkload("g1", "w1", 12);
				gTree.addGoal("g2", "g0");
				gTree.addWorkload("g2", "w2", 9);
				gTree.addInform("g1", "i1", "g2", 1);

				orgGen.generateOrganisationFromTree("sample", gTree, c, search, oneSolution);
			} catch (GoalNotFound | CircularReference e) {
				e.printStackTrace();
			}

		} else {
			// Expected input example:
			// ./gradlew run --args="examples/Full_Link_ultramegasimple.xml GENERALIST BFS"

			// parameters
			Parameters.getInstance();
			Parameters.setMaxWorkload(8.0);
			Parameters.setMaxDataLoad(8.0);
			Parameters.setWorkloadGrain(4.0);
			Parameters.setDataLoadGrain(4.0);

			OrganisationXMLParser parser = new OrganisationXMLParser();
			GoalTree gTree = parser.parseXMLFile(args[0]);

			String path[] = args[0].split("/");
			String name = path[path.length - 1];
			name = name.substring(0, name.length() - 4);

			if (args.length >= 2)
				c = Cost.valueOf(args[1]);

			if (args.length >= 3)
				search = args[2];

			orgGen.generateOrganisationFromTree(name, gTree, c, search, oneSolution);
		}
	}
}
