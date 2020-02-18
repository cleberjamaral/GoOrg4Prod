package organisation.goal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import annotations.DataLoad;
import annotations.Inform;
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
	
	public GoalNode addGoal(String name, GoalNode parent) {
		GoalNode g = new GoalNode(parent, name);
		if (!treeContains(g)) tree.add(g);
		return g;
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

	public void addGoal(String name, String parent, double reportAmount) {
		GoalNode parentGoal = findAGoalByName(this.rootNode, parent);
		GoalNode g = addGoal(name,parentGoal);
		addInform(name, "report", g.getParent().getGoalName(), reportAmount);
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
	
	public void addInform(String goal, String inform, String recipient, double amount) {
		GoalNode g = findAGoalByName(this.rootNode, goal);
		GoalNode r = findAGoalByName(this.rootNode, recipient);
		g.addInform(new Inform(inform, r, amount));
		r.addDataLoad(new DataLoad(inform, g, amount));
	}

	public void removeBrokenDataLoads(GoalNode original, GoalNode root) {
		// Erase current dataloads of split goal
		original.getDescendants().forEach(s -> {
			List<DataLoad> tobeRemoved = new ArrayList<>();
			for (DataLoad d : s.getDataLoads()) {
				if (findAGoalByName(root, d.getSender().getGoalName()) == null) {
					//s.removeDataLoad(d);
					tobeRemoved.add(d);
				}
			}
			tobeRemoved.forEach(t -> {s.removeDataLoad(t);});
			// when reaching the last slice, go to the next node
			removeBrokenDataLoads(s,root);
		});

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
		GoalNode newRootByWorkload = this.rootNode.cloneContent();
		brakeGoalNodeByWorkload(this.rootNode, newRootByWorkload);
		removeBrokenDataLoads(newRootByWorkload,newRootByWorkload);
		GoalNode newRootByDataLoad = newRootByWorkload.cloneContent();
		brakeGoalNodeByDataLoad(newRootByWorkload, newRootByDataLoad);
		this.rootNode = newRootByDataLoad;
	}
	
	private void brakeGoalNodeByWorkload(GoalNode original, GoalNode parent) {
		original.getDescendants().forEach(s -> {
			double sumEfforts = 0;
			for (Workload w : s.getWorkloads())	sumEfforts += (double) w.getValue();

			// the number of slices is at least 1 being more according to properties
			int slices = (int) Math.max(Math.ceil(sumEfforts / Parameters.getMaxWorkload()), 1.0);
			
			GoalNode g = null;
			for (int i = 0; i < slices; i++) {
				g = s.cloneContent();
				g.setParent(parent);
				// it will be sliced only if slices > 1
				if (slices > 1) {
					g.setGoalName(g.getGoalName() + "$" + i);
					for (Workload w : g.getWorkloads())	w.setValue((double) w.getValue() / slices);
					//
					for (Inform j : g.getInforms())	{
						GoalNode r = j.getRecipient();
						// Create dataloads using the created fragmented goals, later broken dataloads must be removed
						r.addDataLoad(new DataLoad(j.getId(), g, (double) j.getValue() / slices)); 
					}
				}
			}
			// when reaching the last slice, go to the next node
			brakeGoalNodeByWorkload(s, g);
		});
	}

	private void brakeGoalNodeByDataLoad(GoalNode original, GoalNode parent) {
		original.getDescendants().forEach(s -> {
			double sumDataAmount = 0;
			for (DataLoad t : s.getDataLoads())	sumDataAmount += (double) t.getValue();

			// the number of slices is at least 1 being more according to properties
			int slices = (int) Math.max(Math.ceil(sumDataAmount / Parameters.getMaxDataAmount()), 1.0);
			
			GoalNode g = null;
			for (int i = 0; i < slices; i++) {
				g = s.cloneContent();
				g.setParent(parent);
				// it will be sliced only if slices > 1
				if (slices > 1) {
					g.setGoalName(g.getGoalName() + "$" + i);
					for (DataLoad w : g.getDataLoads())	w.setValue((double) w.getValue() / slices);
				}
			}
			// when reaching the last slice, go to the next node
			brakeGoalNodeByDataLoad(s, g);
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
				sumEfforts += (double) w.getValue();
			
		}
		return sumEfforts;
	}
}
