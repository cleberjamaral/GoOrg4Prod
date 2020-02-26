package organisation.role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import annotations.DataLoad;
import annotations.Inform;
import annotations.Workload;
import organisation.goal.GoalNode;

/**
 * @author cleber
 *
 */
public class RoleNode {
	// roleName and parentName are unique names for this role and its parent in this tree (ex: r0, r1...)
	private String roleName;
	private String parentName;

	private RoleNode parent;
	private List<RoleNode> descendants = new ArrayList<>();
	private Set<Workload> workloads = new HashSet<>();
	private Set<Inform> informs = new HashSet<>();
	private Set<DataLoad> dataloads = new HashSet<>();
	private Set<GoalNode> assignedGoals = new HashSet<>();


	public RoleNode(RoleNode parent, String roleName) {
		setParent(parent);
		this.roleName = roleName;
	}
	
	public void addWorkload(Workload workload) {
		Workload w = getWorkload(workload.getId());
		if (w != null) {
			w.setValue((double) w.getValue() + (double) workload.getValue());
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
		return this.workloads;
	}

	public double getSumWorkload() {
		double sumEfforts = 0;
		for (Workload w : getWorkloads())
			sumEfforts += (double) w.getValue();
		return sumEfforts;
	}

	public void addInform(Inform inform) {
		if (this.informs.contains(inform)) {
			Inform t = getInform(inform);
			t.setValue((double) t.getValue() + (double) inform.getValue());
		} else {
			informs.add(inform);
		}
	}

	/**
	 * Prevent to create same inform for same recipient
	 * 
	 * @param id of the inform
	 * @return the inform that can be a new one or an existing with same id/recipient
	 */
	private Inform getInform(Inform inform) {
		for (Inform i : this.informs) 
			if (i.equals(inform)) {
				return i;
			}
		
		return null;
	}
	
	public Set<Inform> getInforms() {
		return this.informs;
	}

	private DataLoad getDataLoad(DataLoad dataload) {
		for (DataLoad d : this.dataloads) 
			if (d.equals(dataload)) {
				return d;
			}
		
		return null;
	}
	
	public void addDataLoad(DataLoad dataload) {
		if (this.dataloads.contains(dataload)) {
			DataLoad d = getDataLoad(dataload);
			d.setValue((double) d.getValue() + (double) dataload.getValue());
		} else {
			this.dataloads.add(dataload);
		}
	}
	
	public Set<DataLoad> getDataLoads() {
		return dataloads;
	}

	/**
	 * sum amount of data generated by all descendants 
	 * of its parent role
	 * @return double sum of descendants of its parent
	 */
	public double getParentSumDataAmount() {
		double sumDataAmount = 0;
		if (getParent() != null) {
			for (RoleNode d : getParent().getDescendants()) {
				for (Inform t : d.getInforms())
					sumDataAmount += (double) t.getValue();
			}
		}
		return sumDataAmount;
	}
	
	/**
	 * sum amount of data generated by all descendants 
	 * @return double sum of descendants data amount
	 */
	public double getSumDataLoad() {
		double sumDataLoad = 0;
		for (DataLoad d : getDataLoads())
			sumDataLoad += (double) d.getValue();
		return sumDataLoad;
	}
	
	public void assignGoal(GoalNode g) {
		this.assignedGoals.add(g);
	}

	public Set<GoalNode> getAssignedGoals() {
		return this.assignedGoals;
	}

	private void addDescendant(RoleNode newDescendant) {
		this.descendants.add(newDescendant);
	}

	public List<RoleNode> getDescendants() {
		return this.descendants;
	}

	public String getRoleName() {
		return this.roleName;
	}

	public RoleNode getParent() {
		return this.parent;
	}

	public String getParentName() {
		return this.parentName;
	}
	
	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public void setParent(RoleNode parent) {
		this.parent = parent;
		if (getParent() != null) {
			setParentName(parent.getRoleName());
			getParent().addDescendant(this);
		} else {
			setParentName("");
		}
	}

	/**
	 * Check if this role has a goal which is sibling of the given goal
	 * @param g
	 * @return
	 */
	public boolean hasSiblingGoal(GoalNode g) {
		if (getParent() == null)
			return false;
		return getParent().getAssignedGoals().contains(g.getParent());
	}

	/**
	 * Check if this role has a goal which is sibling of the given goal
	 * @param g
	 * @return
	 */
	public boolean hasParentGoal(GoalNode g) {
		return getAssignedGoals().contains(g.getParent());
	}

	/**
	 * Generate a signature of this role which will be used to make a signature of
	 * the tree which makes a search state unique
	 */
	public String toString() {
		String r = "";

		List<String> signatureByGoals = new ArrayList<>();
		if ((getAssignedGoals() != null) && (!getAssignedGoals().isEmpty())) {
			Iterator<GoalNode> iterator = getAssignedGoals().iterator(); 
			while (iterator.hasNext()) {
				GoalNode n = iterator.next(); 
				//signatureByGoals.add(n.getGoalName());
				if (n.getGoalName().indexOf('$') > 0)
					signatureByGoals.add(n.getGoalName().substring(0, n.getGoalName().lastIndexOf('$')));
			}
		}
		Collections.sort(signatureByGoals);
		r += "G{" + signatureByGoals + "}";

		//r += "W{" + getWorkloads() + "}";
		
		//Example of signatures: g0g0g0g1, g0g1, g0g0g0
		//r += "G{" + signatureByGoals + "}";
		
		// dataloads cannot be part of signature because assigning more goals may
		// make this role with circular dataloads which will be removed
		//r += "T{" + getDataLoads() + "}";

		if (getParent() != null) {
			r += "^";
			r += getParent().toString();
		}
		
		return r;
	}
	
	public RoleNode cloneContent() {
		// parent is not cloned it must be resolved by the tree
		RoleNode clone = new RoleNode(null, getRoleName());
		// parent is resolved by its cloned source parent's name
		clone.setParentName(getParentName());

		for (Workload w : this.workloads) 
			clone.addWorkload(w.clone());

		for (GoalNode goal : this.assignedGoals) 
			if (!clone.assignedGoals.contains(goal)) 
				clone.assignedGoals.add(goal);

		// Copy all "non-circular" dataloads to new role (informs are not used for roles)
		for (DataLoad d : this.getDataLoads()) {
			boolean circularDataload = false;
			for (GoalNode g : clone.getAssignedGoals()) {
				if (g.getGoalName().equals(d.getSenderName())) {
					circularDataload = true;
				} else {
					
				}
			}
			if (!circularDataload) clone.addDataLoad(d.clone());
		}
		
	    return clone;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.assignedGoals == null) ? 0 : this.assignedGoals.hashCode());
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
		if (this.assignedGoals == null) {
			if (other.assignedGoals != null)
				return false;
		} else if (!assignedGoals.equals(other.assignedGoals))
			return false;
		return true;
	}
}