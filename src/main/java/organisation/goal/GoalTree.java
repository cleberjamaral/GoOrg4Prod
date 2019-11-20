package organisation.goal;

import java.util.HashSet;
import java.util.Set;

import properties.Workload;

public class GoalTree {

	GoalNode rootNode;
	Set<GoalNode> tree = new HashSet<>();
	
	public GoalTree(GoalNode rootNode) {
		this.rootNode = rootNode;
		if (!treeContains(this.rootNode)) tree.add(this.rootNode);
	}

	public GoalTree(String rootNode) {
		this.rootNode = new GoalNode(null, rootNode);
		if (!treeContains(this.rootNode)) tree.add(this.rootNode);
	}
	
	public GoalNode getRootNode() {
		return this.rootNode;
	}
	
	public void addGoal(String name, GoalNode parent) {
		GoalNode g = new GoalNode(parent, name);
		if (!treeContains(g)) tree.add(g);
	}
	
	private boolean treeContains(GoalNode g) {
		for (GoalNode gn : tree) 
			if (gn.getGoalName().equals(g.getGoalName())) 
				return true;
			
		return false;
	}
	
	public void addGoal(String name, String parent) {
		GoalNode parentGoal = findAGoalByName(this.rootNode, parent);
		addGoal(name,parentGoal);
	}

	public void addAllDescendants(GoalNode root) {
		for (GoalNode g : root.getDescendents()) {
			if (!treeContains(g)) tree.add(g);
			addAllDescendants(g);
		}
	}
	
	public void addWorkload(String name, String workload, double effort) {
		GoalNode g = findAGoalByName(this.rootNode, name);
		Workload w = new Workload(workload, effort);
		g.addWorkload(w);
	}
	
	public void addThroughput(String name, String thoughput, double amount) {
		//GoalNode g = findAGoalByName(this.rootNode, name);
		//Throughput t = new Throughput(thoughput, amount);
		//g.addWorkload(t);
	}
	
	private GoalNode findAGoalByName(GoalNode root, String name) {
		if (root.getGoalName().equals(name)) {
			return root;
		} 
		for (GoalNode goal : root.getDescendents()) {
			GoalNode d = findAGoalByName(goal, name);
			if (d != null) return d;
		}
		return null;
	}
	
	public void getBrokenGoalTree(double maxEffort) {
		GoalNode newRoot = this.rootNode.cloneContent();
		brakeGoalTree(this.rootNode, newRoot, maxEffort);
		this.rootNode = newRoot;
	}
	
	private void brakeGoalTree(GoalNode original, GoalNode parent, double maxEffort) {
		original.getDescendents().forEach(s -> {
			if (!s.containsWorkload()) {
				GoalNode g = s.cloneContent();
				g.setParent(parent);
				brakeGoalTree(s, g, maxEffort);
			} else {
				// get the biggest effort and divide all workloads by the limit
				double sumEfforts = 0;
				for (Object w : s.getWorkloads()) {
					if (w instanceof Workload) {
						sumEfforts += ((Workload) w).getEffort();
					}
				}

				int slices = (int) Math.ceil(sumEfforts / maxEffort);
				if (slices == 0)
					slices = 1;
				for (int i = 1; i <= slices; i++) {
					GoalNode g = s.cloneContent();
					g.setParent(parent);
					if (slices > 1)
						g.setGoalName(g.getGoalName() + "$" + i);
					for (Object w : g.getWorkloads()) {

						if ((w instanceof Workload) && (slices > 1)) {
							((Workload) w).setEffort(((Workload) w).getEffort() / slices);
						}
					}
					if (i == slices)
						brakeGoalTree(s, g, maxEffort);
				}

			}
		});
	}

	/**
	 * Give the sum of efforts of the whole tree
	 * 
	 * @return a double
	 */
	public double sumEfforts() {
		double sumEfforts = 0;
		for (GoalNode g : this.tree) {
			for (Workload w : g.getWorkloads()) {
				sumEfforts += w.getEffort();
			}
		}
		return sumEfforts;
	}
}
