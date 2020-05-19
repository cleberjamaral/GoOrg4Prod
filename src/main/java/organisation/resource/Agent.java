package organisation.resource;

import java.util.HashSet;
import java.util.Set;

import annotations.Skill;
import fit.Resource;

public class Agent implements Resource {
	private String name;
	private Set<Skill> skills = new HashSet<>();

	public Agent(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Skill> getSkills() {
		return skills;
	}

	public void addSkill(Skill skill) {
		skills.add(skill);
	}
	
	public String toString() {
		return this.name;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Agent other = (Agent) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public Set<String> getFeatures() {
		Set<String> features = new HashSet<>();
		skills.forEach(s -> {features.add(s.getId());});
		return features;
	}

	@Override
	public String getResource() {
		return getName();
	}

}
