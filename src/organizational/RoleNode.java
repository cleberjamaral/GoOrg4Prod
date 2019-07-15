package organizational;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoleNode {
	private Set<String> skills = new HashSet<String>();
	private List<GoalNode> assignedGoals = new ArrayList<GoalNode>();
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

	public List<GoalNode> getAssignedGoals() {
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

	public String toString() {
		return roleName;
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