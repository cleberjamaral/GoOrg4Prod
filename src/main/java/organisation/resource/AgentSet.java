package organisation.resource;

import java.util.HashSet;
import java.util.Set;

public class AgentSet {

	private static AgentSet instance = null;
	private Set<Agent> availableAgents = new HashSet<>();

    private AgentSet() {}
    
	public static AgentSet getInstance() 
    { 
        if (instance == null) 
        	instance = new AgentSet();
  
        return instance; 
    }
	
	public void addAgent(Agent agent) {
		availableAgents.add(agent);
	}
	
	public Set<Agent> getAvailableAgents() {
		return availableAgents;
	}
}
