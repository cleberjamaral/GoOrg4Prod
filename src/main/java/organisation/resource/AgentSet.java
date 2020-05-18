package organisation.resource;

import java.util.HashSet;
import java.util.Set;

import annotations.Skill;

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
	
	public void addAgent(String name, String[] skills) {
		Agent agent = new Agent(name);
		for (int i = 0; i < skills.length; i++) {
			Skill skill = new Skill(skills[i]);
			agent.addSkill(skill);
		}
		
		availableAgents.add(agent);
	}
	
	public Set<Agent> getAvailableAgents() {
		return availableAgents;
	}
}
