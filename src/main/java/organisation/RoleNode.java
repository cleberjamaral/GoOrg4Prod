package main.java.organisation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RoleNode {
	private Set<String> skills = new HashSet<String>();
	private Set<GoalNode> assignedGoals = new HashSet<GoalNode>();
	private List<RoleNode> descendents = new ArrayList<RoleNode>();
	private String roleName;
	private RoleNode parent;

	public RoleNode(RoleNode p, String name) {
		roleName = name;
		parent = p;
		if (parent != null) {
			parent.addDescendent(this);
		}
	}

	public void addSkill(String newSkill) {
		skills.add(newSkill);
	}

	public Set<String> getSkills() {
		return skills;
	}

	public void assignGoal(GoalNode g) {
		assignedGoals.add(g);
	}

	public Set<GoalNode> getAssignedGoals() {
		return assignedGoals;
	}

	private void addDescendent(RoleNode newDescendent) {
		descendents.add(newDescendent);
		if (parent != null)
			parent.addDescendent(newDescendent);
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

		List<String> skills = new ArrayList<>(this.getSkills());
		Collections.sort(skills);
		r += "S{" + skills + "}";
		
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
			r += assignedGoals;
			
			List<String> parentSkills = new ArrayList<>(this.getParent().getSkills());
			Collections.sort(parentSkills);
			r += skills;
		}
		
		return r;
	}
	
	public RoleNode clone() {
		RoleNode clone = new RoleNode(this.parent, this.roleName);
		
		for (String s : this.skills) clone.skills.add(s);
		for (RoleNode gn : this.descendents) clone.descendents.add(gn);
		for (GoalNode goal : this.assignedGoals) {
			if (!clone.assignedGoals.contains(goal)) clone.assignedGoals.add(goal);
		}
		
	    return clone;

	    
	}
}