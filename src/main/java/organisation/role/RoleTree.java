package organisation.role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import annotations.DataLoad;
import annotations.Workload;
import organisation.Parameters;
import organisation.exception.RoleNotFound;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;

public class RoleTree {

	private int numberOfLevels = 0;
	private Set<RoleNode> tree = new HashSet<>();

	public RoleTree() {
	}

	public int getNumberOfLevels() {
		return numberOfLevels;
	}

	private void setNumberOfLevels(int numberOfLevels) {
		this.numberOfLevels = numberOfLevels;
	}

	public int size() {
		return tree.size();
	}

	public Set<RoleNode> getTree() {
		return tree;
	}

	public void addRoleToTree(RoleNode role) {
		updateNumberOfLevels(role);
		
		tree.add(role);
	}

	private void updateNumberOfLevels(RoleNode role) {
		int levels = 1;
		while (role.getParent() != null) {
			role = role.getParent();
			levels++;
		}

		if (levels > getNumberOfLevels())
			setNumberOfLevels(levels);
	}

	public RoleNode createRole(RoleNode parent, String name, GoalNode g) {
		RoleNode nr = new RoleNode(parent, name);

		assignGoalToRole(nr, g);
		addRoleToTree(nr);

		return nr;
	}

	public RoleNode findRoleByRoleName(String roleName) throws RoleNotFound {
		for (RoleNode or : this.tree) {
			if (or.getRoleName().equals(roleName))
				return or;
		}
		throw new RoleNotFound("There is no role with signature = '" + roleName + "'!");
	}

	public RoleTree cloneContent() throws RoleNotFound {
		RoleTree clonedTree = new RoleTree();

		// first clone all roles
		for (RoleNode or : this.tree) {
			RoleNode nnewS = or.cloneContent();
			// not using addRoleToTree because parent is still unknown
			clonedTree.getTree().add(nnewS);
		}

		// finding right parents in the new tree
		for (RoleNode or : clonedTree.getTree()) {

			// it is not the root role
			if (!or.getParentName().equals("")) {
				or.setParent(clonedTree.findRoleByRoleName(or.getParentName()));
			}
		}
		
		// update number of levels after knowning parents
		clonedTree.setNumberOfLevels(1);
		for (RoleNode or : clonedTree.getTree()) {
			clonedTree.updateNumberOfLevels(or);
		}
		
		return clonedTree;
	}

	public RoleNode assignGoalToRoleByRoleName(String roleName, GoalNode newGoal) throws RoleNotFound {
		RoleNode role = this.findRoleByRoleName(roleName);

		assignGoalToRole(role, newGoal);

		return role;
	}

	public void assignGoalToRole(RoleNode role, GoalNode newGoal) {
		role.assignGoal(newGoal);

		// Copy all workloads of the goal to this new role
		for (Workload w : newGoal.getWorkloads())
			role.addWorkload(w.clone());

		// Copy all "non-circular" dataloads to new role (informs are not used for roles)
		for (DataLoad d : newGoal.getDataLoads()) {
			boolean circularDataload = false;
			for (GoalNode g : role.getAssignedGoals()) {
				if (g.getGoalName().equals(d.getSenderName())) circularDataload = true;
			}
			if (!circularDataload) role.addDataLoad(d.clone());
		}
	}

	/**
	 * Give the sum of efforts of the whole tree
	 * 
	 * @return a double
	 */
	public double getSumWorkload() {
		double sumWorkload = 0;
		for (RoleNode r : this.tree) {
			for (Workload w : r.getWorkloads()) {
				sumWorkload += (double) w.getValue();
			}
		}
		return sumWorkload;
	}

	@Override
	public String toString() {
		List<String> signatureByRoles = new ArrayList<>();
		if ((getTree() != null) && (!getTree().isEmpty())) {
			Iterator<RoleNode> iterator = getTree().iterator();
			while (iterator.hasNext()) {
				RoleNode n = iterator.next();
				signatureByRoles.add(n.toString());
			}
			Collections.sort(signatureByRoles);
		}
		return signatureByRoles.toString();
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RoleTree other = (RoleTree) obj;
		if (tree == null) {
			if (other.tree != null)
				return false;
		} else if (!tree.toString().equals(other.tree.toString()))
			return false;
		return true;
	}
	
	/**
	 * Generalness is about how workloads are distributed across roles. When all
	 * roles have all given workloads it means all agents must have all those skills
	 * and can play any role in the organisation, i.e., maximum generalness.
	 * 
	 * @return generalness index from 0 to 1 (less to maximum generalness possible)
	 */
	public double getGeneralness() {
		int nRoles = this.tree.size();
		int nAllWorkloads = 0;
		
		for (RoleNode or : this.tree) {
			//RoleNode.getWorkloads() is a hashset returning only unique workloads
			nAllWorkloads += or.getWorkloads().size();
		}
		
		// the most generalist roles tree must have all workload on each role
		// sumOfDiffWL.size() represents all unique workloads quantity
		int nMaxWorkloads = nRoles * GoalTree.getInstance().getNumberDiffWorkloads();
		
		return (double) nAllWorkloads / (double) nMaxWorkloads;
	}
	
	public double getSpecificness() {
		int nAllWorkloads = 0;
		
		for (RoleNode or : this.tree) {
			//RoleNode.getWorkloads() is a hashset returning only unique workloads
			nAllWorkloads += or.getWorkloads().size();
		}
		
		// the most specialist roles tree must have all workloads distributed
		// without splitting them (if may be impossible if the sumofefforts if higher
		// than maxWorkload, but efficiency/idleness concerns should be taken into account
		// by other functions
		int nMinWorkloads = GoalTree.getInstance().getNumberDiffWorkloads();
		
		return (double) nMinWorkloads / (double) nAllWorkloads;
	}
}
