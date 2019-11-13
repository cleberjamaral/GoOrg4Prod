package organisation;

import java.util.ArrayList;
import java.util.List;

import properties.Workload;

public class GoalNode {
	private List<Object> requirements = new ArrayList<Object>();
	private List<GoalNode> descendents = new ArrayList<GoalNode>();
	private String goalName;
	private GoalNode parent;
	private String operator;

	public GoalNode(GoalNode p, String name) {
		goalName = name;
		parent = p;
		operator = "sequence";
		if (parent != null) {
			parent.addDescendent(this);
		}
	}

	public void addRequirement(Object newRequirement) {
		requirements.add(newRequirement);
	}

	public List<Object> getRequirements() {
		return requirements;
	}

	public void addDescendent(GoalNode newDescendent) {
		descendents.add(newDescendent);
	}

	public List<GoalNode> getDescendents() {
		return descendents;
	}

	public String getGoalName() {
		return goalName;
	}

	public void setGoalName(String goalName) {
		this.goalName = goalName;
	} 
	
	public GoalNode getParent() {
		return parent;
	}

	public void setParent(GoalNode parent) {
		this.parent = parent;
		if (this.parent != null) {
			this.parent.addDescendent(this);
		}
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String op) {
		this.operator = op;
	}
	
	public boolean containsWorkload() {
		for (Object s : this.requirements) 
			if (s instanceof Workload) return true;
		return false;
	}
	
	public String toString() {
		return goalName;
	}

	public GoalNode cloneContent() {
		GoalNode clone = new GoalNode(null, this.goalName);
		
		for (Object s : this.requirements) clone.requirements.add(((Workload)s).clone());
		clone.operator = this.operator;
		
	    return clone;
	}
	
	public GoalNode clone() {
		GoalNode clone = new GoalNode(this.parent, this.goalName);
		
		for (Object s : this.requirements) clone.requirements.add(((Workload)s).clone());
		for (GoalNode gn : this.descendents) clone.descendents.add(gn);
		clone.operator = this.operator;
		
	    return clone;
	}
}