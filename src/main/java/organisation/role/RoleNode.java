package organisation.role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import organisation.goal.GoalNode;
import properties.Throughput;
import properties.Workload;

public class RoleNode {
	private Set<Workload> workloads = new HashSet<>();
	private Set<Throughput> throughputs = new HashSet<>();
	private Set<GoalNode> assignedGoals = new HashSet<>();
	private List<RoleNode> descendants = new ArrayList<>();
	private String roleName;
	private RoleNode parent;
	private String parentSignature; // used to find the original parent after cloning

	public RoleNode(RoleNode parent, String name) {
		setParent(parent);
		roleName = name;
	}
	
	public void addWorkload(Workload workload) {
		Workload w = getWorkload(workload.getId());
		if (w != null) {
			w.setEffort(w.getEffort() + workload.getEffort());
		} else {
			this.workloads.add(workload);
		}
	}
	
	public Workload getWorkload(String id) {
		for (Workload w : this.workloads) 
			if (w.getId().equals(id)) return w;
		
		return null;
	}
	
	public Set<Workload> getWorkloads() {
		return workloads;
	}

	public double getSumWorkload() {
		double sumEfforts = 0;
		for (Workload w : this.getWorkloads())
			sumEfforts += w.getEffort();
		return sumEfforts;
	}

	public void addThroughput(Throughput throughput) {
		Throughput t = getThroughput(throughput.getId());
		if (t != null) {
			t.setAmount(t.getAmount() + throughput.getAmount());
		} else {
			this.throughputs.add(throughput);
		}
	}

	private Throughput getThroughput(String id) {
		for (Throughput w : this.throughputs) 
			if (w.getId().equals(id)) return w;
		
		return null;
	}
	
	public Set<Throughput> getThroughputs() {
		return this.throughputs;
	}

	public void assignGoal(GoalNode g) {
		assignedGoals.add(g);
	}

	public Set<GoalNode> getAssignedGoals() {
		return assignedGoals;
	}

	private void addDescendent(RoleNode newDescendent) {
		descendants.add(newDescendent);
	}

	public List<RoleNode> getDescendants() {
		return descendants;
	}

	public String getRoleName() {
		return roleName;
	}

	public RoleNode getParent() {
		return parent;
	}

	public String getParentSignature() {
		return this.parentSignature;
	}
	
	public void setParentSignature(String parentSignature) {
		this.parentSignature = parentSignature;
	}

	public void setParent(RoleNode parent) {
		this.parent = parent;
		if (this.parent != null) {
			this.parentSignature = parent.signature();
			this.parent.addDescendent(this);
		} else {
			this.parentSignature = "";
		}
	}

	/**
	 * Check if this role has a goal which is sibling of the given goal
	 * @param g
	 * @return
	 */
	public boolean hasSiblingGoal(GoalNode g) {
		if (this.getParent() == null)
			return false;
		return this.getParent().getAssignedGoals().contains(g.getParent());
	}

	/**
	 * Check if this role has a goal which is sibling of the given goal
	 * @param g
	 * @return
	 */
	public boolean hasParentGoal(GoalNode g) {
		return this.getAssignedGoals().contains(g.getParent());
	}
	
	/**
	 * Signature makes this role unique in a tree
	 * It differs from toString because signature has no reference to other nodes
	 * 
	 * @return an unique string
	 */
	public String signature() {
		String r = "";

		List<String> assignedGoals = new ArrayList<>();
		if ((this.getAssignedGoals() != null) && (!this.getAssignedGoals().isEmpty())) {
			Iterator<GoalNode> iterator = this.getAssignedGoals().iterator(); 
			while (iterator.hasNext()) {
				GoalNode n = iterator.next(); 
				assignedGoals.add(n.getGoalName());
			}
		}
		Collections.sort(assignedGoals);
		r += "G{" + assignedGoals + "}";

		r += "W{" + this.getWorkloads() + "}";
		
		r += "T{" + this.getThroughputs() + "}";

		return r;
	}
	
	public String toString() {
		String r = signature();

		if (this.getParent() != null) {
			r += "^";
			r += this.getParent().toString();
		}
		
		return r;
	}
	
	public RoleNode cloneContent() {
		// parent is not cloned it must be resolved by the tree
		RoleNode clone = new RoleNode(null, this.roleName);
		
		clone.setParentSignature(this.parentSignature);

		// descendants are not cloned, it must be resolved by the tree
		
		for (Workload s : this.workloads) 
			clone.workloads.add(s.clone());

		for (Throughput t : this.throughputs) 
			clone.throughputs.add(t.clone());

		for (GoalNode goal : this.assignedGoals) 
			if (!clone.assignedGoals.contains(goal)) clone.assignedGoals.add(goal);
		
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
		} else if (!signature().equals(other.signature()))
			return false;
		return true;
	}
}