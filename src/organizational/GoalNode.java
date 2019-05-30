package organizational;

import java.util.ArrayList;
import java.util.List;

public class GoalNode {
	private List<String> skills = new ArrayList<String>();
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

	public void addSkill(String newSkill) {
		skills.add(newSkill);
	}

	public List<String> getSkills() {
		return skills;
	}

	private void addDescendent(GoalNode newDescendent) {
		descendents.add(newDescendent);
		if (parent != null)
			parent.addDescendent(newDescendent);
	}

	public List<GoalNode> getSuccessors() {
		return descendents;
	}

	public String getGoalName() {
		return goalName;
	}

	public GoalNode getParent() {
		return parent;
	}

	public void setOperator(String op) {
		this.operator = op;
	}

	public String getOperator() {
		return operator;
	}

	public String toString() {
		return goalName;
	}
}