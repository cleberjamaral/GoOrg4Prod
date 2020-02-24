package organisation.goal;

import java.util.ArrayList;
import java.util.List;

import annotations.DataLoad;
import annotations.Inform;
import annotations.Workload;
import organisation.exception.CircularReference;

public class GoalNode {
	private String goalName;
	private GoalNode parent;
	private String operator;
	private List<GoalNode> descendants = new ArrayList<>();
	private List<Workload> workloads = new ArrayList<>();
	private List<Inform> informs = new ArrayList<>();
	private List<DataLoad> dataloads = new ArrayList<>();

	public GoalNode(GoalNode p, String name) {
		goalName = name;
		parent = p;
		operator = "sequence";
		if (parent != null) {
			parent.addDescendant(this);
		}
	}

	public void addWorkload(Workload workload) {
		Workload w = getWorkload(workload.getId());
		if (w != null) {
			w.setValue((double) w.getValue() + (double) workload.getValue());
		} else {
			workloads.add(workload);
		}
	}
	
	public Workload getWorkload(String id) {
		for (Workload w : workloads) 
			if (w.getId().equals(id)) return w;
		
		return null;
	}
	
	public List<Workload> getWorkloads() {
		return workloads;
	}
	
	public double getSumWorkload() {
		double sumEfforts = 0;
		for (Workload w : getWorkloads())
			sumEfforts += (double) w.getValue();
		return sumEfforts;
	}

	public double getSumDataLoad() {
		double sumData = 0;
		for (DataLoad w : getDataLoads())
			sumData += (double) w.getValue();
		return sumData;
	}

	public double getSumInform() {
		double sumData = 0;
		for (Inform w : getInforms())
			sumData += (double) w.getValue();
		return sumData;
	}

	public void addInform(Inform i) throws CircularReference {
        // prevent creating an inform to itself
        if (i.getRecipientName().equals(this.goalName))
            throw new CircularReference("Circular reference in inform "+i.getId()+" of goal "+this.goalName);
        
        informs.add(i);
	}

	public List<Inform> getInforms() {
		return informs;
	}

	public void addDataLoad(DataLoad t) {
		dataloads.add(t);
	}

	public List<DataLoad> getDataLoads() {
		return dataloads;
	}
	
	public void clearDataLoads() {
		dataloads.clear();
	}
	
	public void removeDataLoad(DataLoad t) {
		if (dataloads.contains(t)) dataloads.remove(t);
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

	public GoalNode cloneContent() throws CircularReference {
		GoalNode clone = new GoalNode(null, this.goalName);
		
		for (Workload w : getWorkloads()) 
			clone.addWorkload(w.clone());
		
		for (Inform i : getInforms()) 
			clone.addInform(i.clone());

		/* dataloads are not cloned, they are generated by informs
		for (DataLoad d : getDataLoads()) 
			clone.addDataLoad(d.clone());
		*/
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