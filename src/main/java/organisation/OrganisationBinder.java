package organisation;

import organisation.binder.Binding;
import organisation.resource.AgentSet;
import organisation.search.Organisation;
import simplelogger.SimpleLogger;
import organisation.OrganisationJacamoExport;

/**
 * @author cleber
 *
 */
public class OrganisationBinder {
    private SimpleLogger LOG = SimpleLogger.getInstance();

	public void bindOrganisations(Organisation org, AgentSet agents) {
		
		LOG.info("Binding organisations and available agents...");
		LOG.info("Available agents: "+agents.getAvailableAgents());
		
		OrganisationStatistics s = OrganisationStatistics.getInstance();
		s.prepareBindingStatisticsFile(org.getOrgName());
		OrganisationJacamoExport j = new OrganisationJacamoExport();
		j.deleteExistingJcms();
		
		if (org.getGoalList().isEmpty()) {
			LOG.info("Organisation: "+org.getOrgName());
			Binding binding = new Binding(org, agents);
			binding.FirstFit();
			
			s.saveBindingStatistics(org, binding);
            j.exportBindingAsJCM(org, "", binding);
		} else {
			LOG.info("Number of organisations to bind: "+org.getGoalList().size());
			for (int i = 0; i < org.getGoalList().size(); i++) {
				Binding binding = new Binding(org.getGoalList().get(i), agents);
				binding.FirstFit();
				
				s.saveBindingStatistics(org.getGoalList().get(i), binding);
                j.exportBindingAsJCM(org.getGoalList().get(i), Integer.toString(i + 1), binding);
			}
		}
		LOG.info("End of binding process");
	}
}
