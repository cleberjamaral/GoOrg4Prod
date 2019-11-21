package organisation.goal;

import java.util.ArrayList;
import java.util.List;

import properties.Throughput;
import properties.Workload;

public class GoalNode {
	private List<Workload> workloads = new ArrayList<>();
	private List<Throughput> throughputs = new ArrayList<>();
	private List<GoalNode> descendants = new ArrayList<>();
	private String goalName;
	private GoalNode parent;
	private String operator;

	public GoalNode(GoalNode p, String name) {
		goalName = name;
		parent = p;
		operator = "sequence";
		if (parent != null) {
			parent.addDescendant(this);
		}
	}

	public void addWorkload(Workload newWorkload) {
		workloads.add(newWorkload);
	}

	public List<Workload> getWorkloads() {
		return workloads;
	}
	
	public void addThroughput(Throughput t) {
		throughputs.add(t);
	}

	public List<Throughput> getThroughputs() {
		return throughputs;
	}
	
	public void addDescendant(GoalNode newDescendent) {
		descendants.add(newDescendent);
	}

	public List<GoalNode> getDescendants() {
		return descendants;
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
			this.parent.addDescendant(this);
		}
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String op) {
		this.operator = op;
	}
	
	public boolean containsWorkload() {
		return (this.getWorkloads().size() > 0);
	}
	
	public String toString() {
		return goalName;
	}

	public GoalNode cloneContent() {
		GoalNode clone = new GoalNode(null, this.goalName);
		
		for (Workload s : this.workloads) 
			clone.workloads.add(s.clone());
		
		for (Throughput t : this.throughputs) 
			clone.throughputs.add(t.clone());

		clone.operator = this.operator;
		
	    return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((goalName == null) ? 0 : goalName.hashCode());
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
		GoalNode other = (GoalNode) obj;
		if (goalName == null) {
			if (other.goalName != null)
				return false;
		} else if (!goalName.equals(other.goalName))
			return false;
		return true;
	}
}