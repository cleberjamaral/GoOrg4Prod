package organisation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RoleNode {
	private Set<Object> requirements = new HashSet<Object>();
	private Set<GoalNode> assignedGoals = new HashSet<GoalNode>();
	private List<RoleNode> descendents = new ArrayList<RoleNode>();
	private String roleName;
	private RoleNode parent;

	public RoleNode(RoleNode parent, String name) {
		this.parent = parent;
		roleName = name;
		if (this.parent != null) {
			this.parent.addDescendent(this);
		}
	}

	public void addRequirement(Object skill) {
		requirements.add(skill);
	}

	public Set<Object> getRequirements() {
		return requirements;
	}

	public boolean matchRequirements(List<Object> reqs) {
		if ((requirements.containsAll(reqs)) || (reqs.isEmpty())) {
			return true;
		}
		return false;
	}
	
	public void assignGoal(GoalNode g) {
		assignedGoals.add(g);
	}

	public Set<GoalNode> getAssignedGoals() {
		return assignedGoals;
	}

	private void addDescendent(RoleNode newDescendent) {
		descendents.add(newDescendent);
	}

	public List<RoleNode> getSuccessors() {
		return descendents;
	}

	public String getRoleName() {
		return roleName;
	}

	public RoleNode getParent() {
		return parent;
	}

	public void setParent(RoleNode p) {
		parent = p;
	}

	public String toString() {
		String r = "";

		List<String> assignedGoals = new ArrayList<>();
		if ((this.getAssignedGoals() != null) && (!this.getAssignedGoals().isEmpty())) {
			Iterator<GoalNode> iterator = this.getAssignedGoals().iterator(); 
			while (iterator.hasNext()) {
				GoalNode n = iterator.next(); 
				assignedGoals.add(n.toString());
			}
		}
		Collections.sort(assignedGoals);
		r += "G{" + assignedGoals + "}";

		List<Object> reqs = new ArrayList<>(this.getRequirements());
		r += "S{" + reqs + "}";
		
		if (this.getParent() != null) {
			r += "^";
			List<String> parentAssignedGoals = new ArrayList<>();
			if ((this.getParent().getAssignedGoals() != null) && (!this.getParent().getAssignedGoals().isEmpty())) {
				Iterator<GoalNode> iterator = this.getParent().getAssignedGoals().iterator(); 
				while (iterator.hasNext()) {
					GoalNode n = iterator.next(); 
					parentAssignedGoals.add(n.toString());
				}
			}
			Collections.sort(parentAssignedGoals);
			r += parentAssignedGoals;
			
			List<Object> parentReqs = new ArrayList<>(this.getParent().getRequirements());
			r += parentReqs;
		}
		
		return r;
	}
	
	public RoleNode clone() {
		RoleNode clone = new RoleNode(this.parent, this.roleName);
		
		for (Object s : this.requirements) clone.requirements.add(s);
		for (RoleNode gn : this.descendents) clone.descendents.add(gn);
		for (GoalNode goal : this.assignedGoals) {
			if (!clone.assignedGoals.contains(goal)) clone.assignedGoals.add(goal);
		}
		
	    return clone;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((assignedGoals == null) ? 0 : assignedGoals.hashCode());
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
		RoleNode other = (RoleNode) obj;
		if (assignedGoals == null) {
			if (other.assignedGoals != null)
				return false;
		} else if (!assignedGoals.equals(other.assignedGoals))
			return false;
		return true;
	}
}