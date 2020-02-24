package organisation;

import organisation.goal.GoalTree;
import organisation.search.Cost;
import organisation.search.Parameters;

public class OrganisationApp {

	public static void main(String[] args) {
		// parameters
		Parameters.setMaxWorkload(8.0);
		Parameters.setMaxDataLoad(8.0);
		Parameters.setWorkloadGrain(2.0);
		Parameters.setDataLoadGrain(2.0);
		
		Cost c = Cost.GENERALIST;
		String search = "BFS";
		
		OrganisationGenerator orgGen = new OrganisationGenerator();

		// if a Moise XML file was not provided, use a sample organisation
		if ((args.length < 1) || (args[0].equals("0"))) {
			// if an argument to choose a cost function was given
			if (args.length == 2) c = Cost.valueOf(args[1]);

			//Sample organization
			orgGen.sampleOrganisation(c, search);

		} else {
			// Expected input example:
			// ./gradlew run --args="examples/Full_Link_ultramegasimple.xml GENERALIST BFS"
			
			OrganisationXMLParser parser = new OrganisationXMLParser();
			GoalTree gTree = parser.parseXMLFile(args[0]);
			
			String path[] = args[0].split("/");
			String name = path[path.length-1];
			name = name.substring(0, name.length()-4);
			
			if (args.length >= 2) c = Cost.valueOf(args[1]);
			
			if (args.length >= 3) search = args[2];
			
			orgGen.generateOrganisationFromTree(name, gTree, c, search);
		}
	}
}
