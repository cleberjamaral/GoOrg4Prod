package organisation;

import properties.Workload;

public class GoalTree {

	GoalNode rootNode;
	
	GoalTree(String rootNode) {
		this.rootNode = new GoalNode(null, rootNode);
	}
	
	GoalTree(GoalNode rootNode) {
		this.rootNode = rootNode;
	}
	
	public void addGoalToTree(String name, GoalNode parent, String workload, double effort) {
		GoalNode g = new GoalNode(parent, name);
		if (workload != null) {
			Workload w = new Workload(workload, effort);
			g.addRequirement(w);
		}
	}
	
	public void addGoalToTree(String name, String parent, String workload) {
		GoalNode parentGoal = findAGoalByName(this.rootNode, parent);
		addGoalToTree(name,parentGoal,workload,0);
	}
	
	public void addGoalToTree(String name, String parent, String workload, double effort) {
		GoalNode parentGoal = findAGoalByName(this.rootNode, parent);
		addGoalToTree(name,parentGoal,workload,effort);
	}
	
	public void addWorkloadToGoal(String name, String workload, double effort) {
		GoalNode g = findAGoalByName(this.rootNode, name);
		Workload w = new Workload(workload, effort);
		g.addRequirement(w);
	}
	
	
	private GoalNode findAGoalByName(GoalNode root, String name) {
		if (root.getGoalName().equals(name)) return root;
		for (GoalNode goal : root.getDescendents()) {
			if (findAGoalByName(goal, name) != null) return goal;
		}
		return null;
	}
	
	public GoalNode getBrokenGoalTree(double maxEffort) {
		GoalNode newRoot = this.rootNode.cloneContent();
		brakeGoalTree(this.rootNode, newRoot, maxEffort);
		return newRoot;
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
				for (Object w : s.getRequirements()) {
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
					for (Object w : g.getRequirements()) {

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

}
