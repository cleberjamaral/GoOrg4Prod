package organisation;

import fit.FirstFit;
import organisation.resource.AgentSet;
import organisation.search.Organisation;
import simplelogger.SimpleLogger;

public class OrganisationBinder {
    private SimpleLogger LOG = SimpleLogger.getInstance();

	public void bindOrganisations(Organisation org, AgentSet agents) {
		
		LOG.info("Binding organisations and available agents...");
		LOG.info("Available agents: "+agents.getResources());
		
		OrganisationStatistics s = OrganisationStatistics.getInstance();
		s.prepareBindingStatisticsFile(org.getOrgName());
		
		double match = 0;
		if (org.getGoalList().isEmpty()) {
			LOG.info("Organisation: "+org.getOrgName());
			FirstFit fit = new FirstFit();
			match = fit.fitRequirements(org.getOrgName(), org.getRolesTree().getRequirements(), agents.getResources());
			s.saveBindingStatistics(org, agents, match);
		} else {
			LOG.info("Number of organisations to bind: "+org.getGoalList().size());
			for (int i = 0; i < org.getGoalList().size(); i++) {
				FirstFit fit = new FirstFit();
				match = fit.fitRequirements(
						org.getGoalList().get(i).getOrgName()+"_"+(i+1), 
						org.getGoalList().get(i).getRolesTree().getRequirements(), 
						agents.getResources()
				);
				s.saveBindingStatistics(org.getGoalList().get(i), agents, match);
			}
		}
		LOG.info("End of binding process");
	}
}
