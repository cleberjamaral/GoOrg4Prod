package organisation;

import organisation.goal.GoalTree;
import organisation.search.Cost;
import organisation.search.Parameters;

public class OrganisationApp {

	public static void main(String[] args) {
		// parameters
		Parameters.setMaxWorkload(8.0);
		Parameters.setMaxDataLoad(8.0);
		Cost c = Cost.SPECIALIST;
		String search = "BFS";
		
		OrganisationGenerator orgGen = new OrganisationGenerator();

		// if a Moise XML file was not provided, use a sample organisation
		if ((args.length < 1) || (args[0].equals("0"))) {
			// if an argument to choose a cost function was given
			if (args.length == 2) c = Cost.valueOf(args[1]);

			//Sample organization
			orgGen.createFullLinkAutomationGDT(c, search);

		} else {
			OrganisationXMLParser parser = new OrganisationXMLParser();
			GoalTree gTree = parser.parseXMLFile(args[0]);
			
			orgGen.generateOrganisationFromTree(gTree, c, search);
		}
	}
}
