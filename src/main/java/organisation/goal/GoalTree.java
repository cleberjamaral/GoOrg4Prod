package organisation.goal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import annotations.DataLoad;
import annotations.Inform;
import annotations.Workload;
import organisation.exception.GoalNotFound;
import organisation.search.Parameters;

public class GoalTree {

	GoalNode rootNode;
	Set<GoalNode> tree = new HashSet<>();
	
	/**
	 * Add a goal to this goals tree
	 * @param rootNode the root node object
	 */
	public GoalTree(GoalNode rootNode) {
		this.rootNode = rootNode;
		if (!treeContains(this.rootNode)) tree.add(this.rootNode);
	}

	/**
	 * Add a goal to this goals tree
	 * @param rootNode the root name
	 */
	public GoalTree(String rootNode) {
		this.rootNode = new GoalNode(null, rootNode);
		if (!treeContains(this.rootNode)) tree.add(this.rootNode);
	}
	
	/**
	 * get root node of this tree
	 * @return the object root node
	 */
	public GoalNode getRootNode() {
		return this.rootNode;
	}
	
	/**
	 * Add a goal to this tree
	 * @param name of the nonexistent goal
	 * @param parent, an existing parent node for this goal
	 * @return the created goal node
	 */
	public GoalNode addGoal(String name, GoalNode parent) {
		GoalNode g = new GoalNode(parent, name);
		if (!treeContains(g)) tree.add(g);
		return g;
	}
	
	/**
	 * Add a goal to this tree
	 * @param name of the nonexistent goal
	 * @param parent, the name of an existing goal
	 */
	public void addGoal(String name, String parent) {
		GoalNode parentGoal = findAGoalByName(this.rootNode, parent);
		addGoal(name,parentGoal);
	}

	/**
	 * Add a goal to this tree and a 'report' inform annotation on it
	 * @param name of the nonexistent goal
	 * @param parent, the name of an existing goal
	 * @param reportAmount, the amount of data the descendant report
	 */
	public void addGoal(String name, String parent, double reportAmount) {
		GoalNode parentGoal = findAGoalByName(this.rootNode, parent);
		GoalNode g = addGoal(name,parentGoal);
		addInform(name, "report", g.getParent().getGoalName(), reportAmount);
	}
	
	/**
	 * check if this tree contains a given goal object
	 * @param g, a goal object
	 * @return true when the goal was found
	 */
	public boolean treeContains(GoalNode g) {
		for (GoalNode gn : tree) 
			if (gn.getGoalName().equals(g.getGoalName())) 
				return true;
			
		return false;
	}

	/**
	 * Built a tree adding the descendants of a root node
	 * This method is usually used when the tree was created with
	 * linked nodes but not using this GoalTree class which provide
	 * some extra facilities
	 * @param root, a node that is linked to descendants
	 */
	public void addAllDescendants(GoalNode root) {
		for (GoalNode g : root.getDescendants()) {
			if (!treeContains(g)) tree.add(g);
			addAllDescendants(g);
		}
	}
	
	public void updateRecipientGoalsAndDataLoad(GoalNode root) throws GoalNotFound {
		for (GoalNode g : root.getDescendants()) {
			for (Inform i : g.getInforms()) {
				GoalNode r = findAGoalByName(getRootNode(), i.getRecipientName());
				if (r == null) throw new GoalNotFound("Goal "+i.getRecipientName()+" not found!");
                i.setRecipient(r);
                r.addDataLoad(new DataLoad(i.getId(), g, (double) i.getValue()));
			}
			updateRecipientGoalsAndDataLoad(g);
		}
	}
	
	/**
	 * Add a workload annotation to a given goal
	 * 
	 * @param goal, the goal to add an annotation
	 * @param workload, the id of the workload
	 * @param effort, a double the necessary effort
	 */
	public void addWorkload(String goal, String workload, double effort) {
		GoalNode g = findAGoalByName(getRootNode(), goal);
		g.addWorkload(new Workload(workload, effort));
	}
	
	/**
	 * Add an inform annotation to a given goal as well as data load annotation to
	 * each recipient
	 * 
	 * @param goal, the goal to add an annotation
	 * @param inform, the id of the inform
	 * @param recipient, the recipient goal for this inform
	 * @param amount, a double representing amount of data
	 */
	public void addInform(String goal, String inform, String recipient, double amount) {
		GoalNode g = findAGoalByName(this.rootNode, goal);
		GoalNode r = findAGoalByName(this.rootNode, recipient);
		g.addInform(new Inform(inform, r, amount));
		r.addDataLoad(new DataLoad(inform, g, amount));
	}

	/**
	 * Remove data load annotations that refers to null goals, it may occur after
	 * braking goals
	 * 
	 * @param g, the current goal that is being examined
	 * @param root, the root node of the tree
	 */
	public void removeBrokenDataLoads(GoalNode g, GoalNode root) {
		// Erase current dataloads of split goal
		g.getDescendants().forEach(s -> {
			List<DataLoad> tobeRemoved = new ArrayList<>();
			for (DataLoad d : s.getDataLoads()) {
				if (findAGoalByName(root, d.getSender().getGoalName()) == null) {
					tobeRemoved.add(d);
				}
			}
			// avoid remove an annotation during the loop
			tobeRemoved.forEach(t -> {
				s.removeDataLoad(t);
			});
			removeBrokenDataLoads(s, root);
		});
	}
	
	/**
	 * Return the goal object descendant of a given goal
	 * @param root, the higher kinship of the node
	 * @param name
	 * @return
	 */
	public GoalNode findAGoalByName(GoalNode root, String name) {
		if (root.getGoalName().equals(name)) {
			return root;
		} 
		for (GoalNode goal : root.getDescendants()) {
			GoalNode d = findAGoalByName(goal, name);
			if (d != null) return d;
		}
		return null;
	}
	
	/**
	 * Add to the given successors list all descendants of the given goal
	 * 
	 * @param successors, a list to receive the descendants
	 * @param gn, the parent goal of the successors
	 */
	public void addSuccessorsToList(List<GoalNode> successors, GoalNode gn) {
		for (GoalNode goal : gn.getDescendants()) {
			successors.add(goal);
			addSuccessorsToList(successors, goal);
		}
	}
	
	/**
	 * Brake the goal tree in smaller goals if any goal is exceeding the
	 * max allowed for the annotations
	 */
	public void brakeGoalTree() {
		GoalNode newRootByWorkload = this.rootNode.cloneContent();
		brakeGoalNodeByWorkload(this.rootNode, newRootByWorkload);
		removeBrokenDataLoads(newRootByWorkload,newRootByWorkload);
		GoalNode newRootByDataLoad = newRootByWorkload.cloneContent();
		brakeGoalNodeByDataLoad(newRootByWorkload, newRootByDataLoad);
		this.rootNode = newRootByDataLoad;
	}
	
	/**
	 * Brake the goal if the sum of workloads exceeds max workload
	 * 
	 * @param original, the currently examining goal
	 * @param parent, the parent of the examining goal
	 */
	private void brakeGoalNodeByWorkload(GoalNode original, GoalNode parent) {
		original.getDescendants().forEach(s -> {
			double sumEfforts = 0;
			for (Workload w : s.getWorkloads())
				sumEfforts += (double) w.getValue();

			// the number of slices is at least 1 being more according to properties
			int slices = (int) Math.max(Math.ceil(sumEfforts / Parameters.getWorkloadGrain()), 1.0);

			GoalNode g = null;
			for (int i = 0; i < slices; i++) {
				g = s.cloneContent();
				g.setParent(parent);
				// it will be sliced only if slices > 1
				if (slices > 1) {
					g.setGoalName(g.getGoalName() + "$" + i);
					for (Workload w : g.getWorkloads())
						w.setValue((double) w.getValue() / slices);
					for (Inform j : g.getInforms()) {
						GoalNode r = j.getRecipient();
						// Create dataloads using the created fragmented goals
						r.addDataLoad(new DataLoad(j.getId(), g, (double) j.getValue() / slices));
					}
				}
			}
			// when reaching the last slice, go to the next node
			brakeGoalNodeByWorkload(s, g);
		});
	}

	/**
	 * Brake the goal if the sum of dataload exceeds max dataload
	 * 
	 * @param original, the currently examining goal
	 * @param parent, the parent of the examining goal
	 */
	private void brakeGoalNodeByDataLoad(GoalNode original, GoalNode parent) {
		original.getDescendants().forEach(s -> {
			double sumDataAmount = 0;
			for (DataLoad t : s.getDataLoads())	sumDataAmount += (double) t.getValue();

			// the number of slices is at least 1 being more according to properties
			int slices = (int) Math.max(Math.ceil(sumDataAmount / Parameters.getDataLoadGrain()), 1.0);
			
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
