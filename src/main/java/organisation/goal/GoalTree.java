package organisation.goal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import annotations.AccountableFor;
import annotations.Throughput;
import annotations.Workload;
import organisation.search.Parameters;

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
		for (GoalNode g : root.getDescendants()) {
			if (!treeContains(g)) tree.add(g);
			addAllDescendants(g);
		}
	}
	
	public void addWorkload(String goal, String workload, double effort) {
		GoalNode g = findAGoalByName(this.rootNode, goal);
		g.addWorkload(new Workload(workload, effort));
	}
	
	public void addThroughput(String goal, String thoughput, double amount) {
		GoalNode g = findAGoalByName(this.rootNode, goal);
		g.addThroughput(new Throughput(thoughput, amount));
	}
	
	public void addAccountableFor(String goal, String accountableFor) {
		GoalNode hostGoal = findAGoalByName(this.rootNode, goal);
		GoalNode accoGoal = findAGoalByName(this.rootNode, accountableFor);
		hostGoal.addAccountableFor(new AccountableFor(accoGoal));
	}

	private GoalNode findAGoalByName(GoalNode root, String name) {
		if (root.getGoalName().equals(name)) {
			return root;
		} 
		for (GoalNode goal : root.getDescendants()) {
			GoalNode d = findAGoalByName(goal, name);
			if (d != null) return d;
		}
		return null;
	}
	
	public void addSuccessorsToList(List<GoalNode> successors, GoalNode gn) {
		for (GoalNode goal : gn.getDescendants()) {
			successors.add(goal);
			addSuccessorsToList(successors, goal);
		}
	}
	
	public void brakeGoalTree() {
		GoalNode newRoot = this.rootNode.cloneContent();
		brakeGoalNode(this.rootNode, newRoot);
		this.rootNode = newRoot;
	}
	
	private void brakeGoalNode(GoalNode original, GoalNode parent) {
		original.getDescendants().forEach(s -> {
			double sumEfforts = 0;
			double sumThroughput = 0;
			for (Workload w : s.getWorkloads())
				sumEfforts += w.getEffort();
			for (Throughput t : s.getThroughputs())
				sumThroughput += t.getAmount();

			// the number of slices is at least 1 being more according to properties
			int slices = (int) Math.max(Math.max(Math.ceil(sumEfforts / Parameters.getMaxWorkload()),
					Math.ceil(sumThroughput / Parameters.getMaxThroughput())), 1.0);
			
			GoalNode g = null;
			for (int i = 0; i < slices; i++) {
				g = s.cloneContent();
				g.setParent(parent);
				// it will be sliced only if slices > 1
				if (slices > 1) {
					g.setGoalName(g.getGoalName() + "$" + i);
					for (Workload w : g.getWorkloads())
						w.setEffort(w.getEffort() / slices);
					for (Throughput t : g.getThroughputs())
						t.setAmount(t.getAmount() / slices);
				}
			}
			// when reaching the last slice, go to the next node
			brakeGoalNode(s, g);
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
			for (Workload w : g.getWorkloads()) 
				sumEfforts += w.getEffort();
			
		}
		return sumEfforts;
	}
}
