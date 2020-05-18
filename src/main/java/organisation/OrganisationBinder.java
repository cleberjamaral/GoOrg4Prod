package organisation;

import organisation.resource.AgentSet;
import organisation.search.Organisation;
import simplelogger.SimpleLogger;

public class OrganisationBinder {
    private SimpleLogger LOG = SimpleLogger.getInstance();

	public void bindOrganisations(Organisation org, AgentSet agents) {
		//TODO: pass list of organisations to binder
		LOG.info("Here the code to bind organiations with available agents");
		
		LOG.info("List of generated organisations: ");
		org.getGoalList().forEach(o -> {LOG.info("*** "+o.toString());});
		LOG.info("List of available agents: "+agents.getAvailableAgents());
	}
}
