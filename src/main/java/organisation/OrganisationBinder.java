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
		
		LOG.info("List of generated organisations: ");
		if (org.getGoalList().isEmpty()) {
			LOG.info("Org: "+org.getRolesTree());
			FirstFit fit = new FirstFit();
			fit.fitRequirements(org.getRolesTree().getRequirements(), agents.getResources());
		} else {
			org.getGoalList().forEach(o -> {
				LOG.info("Org: "+o);
				FirstFit fit = new FirstFit();
				fit.fitRequirements(o.getRolesTree().getRequirements(), agents.getResources());
			});
		}
		LOG.info("List of available agents: "+agents.getAvailableAgents());
	}
}
