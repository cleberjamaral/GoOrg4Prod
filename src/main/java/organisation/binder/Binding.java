package organisation.binder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fit.FirstFit;
import fit.Requirement;
import fit.Resource;
import organisation.resource.Agent;
import organisation.resource.AgentSet;
import organisation.search.Organisation;

/**
 * @author cleber
 *
 */
public class Binding {
	private Organisation organisation;
	private AgentSet agents;
	Map<Requirement,Resource> matches = new HashMap<>();
	
	public Binding(Organisation organisation, AgentSet agents){
		this.organisation = organisation;
		this.agents = agents;
	}
	
	public void FirstFit() {
		FirstFit fit = new FirstFit();
		matches = fit.fitRequirements(organisation.getOrgName(), organisation.getPositionsTree().getRequirements(),
				agents.getResources());
	}

	public Set<String> getAgents() {
		Set<String> ags = new HashSet<>();
		for (Agent a : agents.getAvailableAgents()) 
			ags.add(a.getName());
		
		return ags;
	}

	public Map<Requirement,Resource> getMatches() {
		return matches;
	}
	
	public String getMatchesAsString() {
		String r = "";
		
		Iterator<Entry<Requirement,Resource>> iterator = matches.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Requirement,Resource> entry = iterator.next();
			r += entry.getKey().getRequirement() + "=" + entry.getValue().getResource();
			if (iterator.hasNext()) r += ", ";
		}
		
		return r;
	}

	public double getFeasibily() {
		return (double) matches.keySet().size() / (double) organisation.getPositionsTree().getRequirements().size();
	}
}
