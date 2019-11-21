package organisation.role;

import java.util.HashSet;
import java.util.Set;

import organisation.exception.DuplicatedRootRole;
import organisation.exception.RoleNotFound;
import organisation.goal.GoalNode;
import properties.Throughput;
import properties.Workload;

public class RoleTree {

	RoleNode rootNode;
	Set<RoleNode> tree = new HashSet<>();

	public RoleTree() {
	}

	public int size() {
		return tree.size();
	}

	public Set<RoleNode> getTree() {
		return tree;
	}

	public void add(RoleNode role) {
		tree.add(role);
	}

	public RoleNode createRole(RoleNode parent, String name, GoalNode g) {
		RoleNode nr = new RoleNode(parent, name);

		assignGoalToRole(nr, g);
		add(nr);

		return nr;
	}

	public RoleNode findRoleBySignature(String roleSignature) throws RoleNotFound {
		for (RoleNode or : this.tree) {
			if (or.signature().equals(roleSignature))
				return or;
		}
		throw new RoleNotFound(
				"There is no role with signature = '" + roleSignature + "'! Tree signature: " + this.tree.toString());
	}

	public RoleTree cloneContent() throws DuplicatedRootRole, RoleNotFound {
		RoleTree clonedTree = new RoleTree();

		// first clone all roles
		for (RoleNode or : this.tree) {
			RoleNode nnewS = or.cloneContent();
			clonedTree.add(nnewS);
		}

		int rootNodesFound = 0;
		// finding right parents in the new tree
		for (RoleNode or : clonedTree.getTree()) {

			// it is not the root role
			if (!or.getParentSignature().equals("")) {
				or.setParent(clonedTree.findRoleBySignature(or.getParentSignature()));
			} else {
				rootNodesFound++;
			}
			if (rootNodesFound > 1)
				throw new DuplicatedRootRole(
						"More than one root role found in this tree! Tree signature: " + clonedTree.getTree());
		}
		return clonedTree;
	}

	public RoleNode assignGoalToRoleBySignature(String signature, GoalNode newGoal) throws RoleNotFound {
		RoleNode role = this.findRoleBySignature(signature);

		assignGoalToRole(role, newGoal);

		return role;
	}

	public void assignGoalToRole(RoleNode role, GoalNode newGoal) {
		role.assignGoal(newGoal);

		// Copy all workloads of the goal to this new role
		for (Workload w : newGoal.getWorkloads())
			role.addWorkload(w.clone());

		// Copy all throughput of the goal to this new role
		for (Throughput t : newGoal.getThroughputs())
			role.addThroughput(t.clone());

		// changes on content may change role signature, its children must be updated
		for (RoleNode child : role.getDescendants())
			child.setParentSignature(role.signature());
	}

	/**
	 * Give the sum of efforts of the whole tree
	 * 
	 * @return a double
	 */
	public double sumEfforts() {
		double sumEfforts = 0;
		for (RoleNode r : this.tree) {
			for (Workload w : r.getWorkloads()) {
				sumEfforts += w.getEffort();
			}
		}
		return sumEfforts;
	}

	@Override
	public String toString() {
		return tree.toString();
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
}
