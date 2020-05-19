package organisation.binder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import fit.FirstFit;
import fit.Requirement;
import fit.Resource;
import organisation.resource.AgentSet;
import organisation.search.Organisation;

public class Binding {
	private Organisation organisation;
	private AgentSet agents;
	Map<Requirement,Resource> match = new HashMap<>();
	
	public Binding(Organisation organisation, AgentSet agents){
		this.organisation = organisation;
		this.agents = agents;
	}
	
	public void FirstFit() {
		FirstFit fit = new FirstFit();
		match = fit.fitRequirements(organisation.getOrgName(), organisation.getRolesTree().getRequirements(),
				agents.getResources());
	}

	public String getAgents() {
		return agents.getResources().toString();
	}

	public String getMatches() {
		String r = "";
		
		Iterator<Entry<Requirement,Resource>> iterator = match.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Requirement,Resource> entry = iterator.next();
			r += entry.getKey().getRequirement() + "=" + entry.getValue().getResource();
			if (iterator.hasNext()) r += ", ";
		}
		
		return r;
	}
	
	public double getFeasibily() {
		return (double) match.keySet().size() / (double) organisation.getRolesTree().getRequirements().size();
	}
}
