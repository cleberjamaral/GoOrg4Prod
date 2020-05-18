package organisation;

import fit.FirstFit;
import organisation.resource.AgentSet;
import organisation.search.Organisation;
import simplelogger.SimpleLogger;

public class OrganisationBinder {
    private SimpleLogger LOG = SimpleLogger.getInstance();

	public void bindOrganisations(Organisation org, AgentSet agents) {
		//TODO: pass list of organisations to binder
		LOG.info("Here the code to bind organiations with available agents");
		OrganisationStatistics s = OrganisationStatistics.getInstance();
		s.prepareBindingStatisticsFile(org.getOrgName());
		
		LOG.info("List of generated organisations: ");
		if (org.getGoalList().isEmpty()) {
			LOG.info("Org: "+org.getRolesTree());
			FirstFit fit = new FirstFit();
			fit.fitRequirements(org.getOrgName(), org.getRolesTree().getRequirements(), agents.getResources());
			s.saveBindingStatistics(org);
		} else {
			for (int i = 0; i < org.getGoalList().size(); i++) {
				LOG.info("Org: "+org.getGoalList().get(i));
				FirstFit fit = new FirstFit();
				fit.fitRequirements(
						org.getGoalList().get(i).getOrgName()+"_"+(i+1), 
						org.getGoalList().get(i).getRolesTree().getRequirements(), 
						agents.getResources()
				);
				s.saveBindingStatistics(org.getGoalList().get(i));
			}
		}
		LOG.info("List of available agents: "+agents.getAvailableAgents());
	}
}
