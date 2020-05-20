package organisation.goal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import annotations.DataLoad;
import annotations.Inform;
import annotations.Workload;
import organisation.Parameters;
import organisation.exception.CircularReference;
import organisation.exception.GoalNotFound;

public class GoalTree {

	private static GoalTree instance = null;
    private GoalNode rootNode;
    private Set<GoalNode> tree = new HashSet<>();
    Set<Workload> allDiffWorkloads = new HashSet<>();

    private GoalTree() {}
    
	public static GoalTree getInstance() 
    { 
        if (instance == null) 
        	instance = new GoalTree();
  
        return instance; 
    }
	
    /**
     * Add a goal to this goals tree
     * 
     * @param rootNode the root node object
     */
    public void setRootNode(GoalNode rootNode) {
        this.rootNode = rootNode;
        if (!treeContains(this.rootNode))
            tree.add(this.rootNode);
    }

    /**
     * Add a goal to this goals tree
     * 
     * @param rootNode the root name
     */
    public void setRootNode(String rootNode) {
        this.rootNode = new GoalNode(null, rootNode);
        if (!treeContains(this.rootNode))
            tree.add(this.rootNode);
    }

    /**
     * get root node of this tree
     * 
     * @return the object root node
     */
    public GoalNode getRootNode() {
        return this.rootNode;
    }


    /**
     * get tree of goals
     * 
     * @return a set of GoalNode
     */
    public Set<GoalNode> getTree() {
        return this.tree;
    }
    
    /**
     * Add a goal to this tree
     * 
     * @param name    of the nonexistent goal
     * @param parent, an existing parent node for this goal
     * @return the created goal node
     */
    public GoalNode addGoal(String name, GoalNode parent) {
        GoalNode g = new GoalNode(parent, name);
        if (!treeContains(g))
            tree.add(g);
        return g;
    }
    
    public void addGoal(GoalNode goal) {
        if (!treeContains(goal))
            tree.add(goal);
    }

    /**
     * Add a goal to this tree
     * 
     * @param name    of the nonexistent goal
     * @param parent, the name of an existing goal
     */
    public void addGoal(String name, String parent) {
        GoalNode parentGoal = findAGoalByName(this.rootNode, parent);
        addGoal(name, parentGoal);
    }

    /**
     * Add a goal to this tree and a 'report' inform annotation on it
     * 
     * @param name          of the nonexistent goal
     * @param parent,       the name of an existing goal
     * @param reportAmount, the amount of data the descendant report
     * @throws CircularReference
     */
    public void addGoal(String name, String parent, double reportAmount) throws CircularReference {
        GoalNode parentGoal = findAGoalByName(this.rootNode, parent);
        GoalNode g = addGoal(name, parentGoal);
        addInform(name, "report", g.getParent().getGoalName(), reportAmount);
    }

    /**
     * check if this tree contains a given goal object
     * 
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
     * Built a tree adding the descendants of a root node This method is usually
     * used when the tree was created with linked nodes but not using this GoalTree
     * class which provide some extra facilities
     * 
     * @param root, a node that is linked to descendants
     */
    public void addAllDescendants(GoalNode root) {
        for (GoalNode g : root.getDescendants()) {
            if (!treeContains(g))
                tree.add(g);
            addAllDescendants(g);
        }
    }

	public void updateInformAndDataLoadReferences(GoalNode goal) throws GoalNotFound, CircularReference {
		int numberSlices = 0;
		List<Inform> toAdd = new ArrayList<>();
		List<DataLoad> toUpdate = new ArrayList<>();
		for (Inform i : goal.getInforms()) {
			GoalNode r = findAGoalByName(getRootNode(), i.getRecipientName());
			// check if exists an goal with the given recipient name
			if (r != null) {
				i.setRecipient(r);
				r.addDataLoad(new DataLoad(i.getId(), goal, (double) i.getValue()));
			} else {
				// check the recipient goal was split
				String recipientStem = i.getRecipientName();
				GoalNode rs = findAGoalByName(getRootNode(), recipientStem + "$0");
				if (rs == null) {
					throw new GoalNotFound("Goal " + recipientStem + "$0" + " not found!");
				} else {
					int j = 0;
					while (rs != null) {
						// adjust the first inform to refer to the first slice
						if (j == 0) {
							i.setRecipient(rs);
						} else {
							// add to a list since here the informs are under iteration
							toAdd.add(new Inform(i.getId(), rs, (double) i.getValue()));
						}
						DataLoad d = new DataLoad(i.getId(), goal, (double) i.getValue());
						toUpdate.add(d);
						rs.addDataLoad(d);
						j++;
						rs = findAGoalByName(getRootNode(), recipientStem + "$" + j);
					}
					numberSlices = j;
					i.setValue((double) i.getValue() / numberSlices);
				}
			}
		}
		// add informs to this split goal
		for (Inform i : toAdd) {
			i.setValue((double) i.getValue() / numberSlices);
			goal.addInform(i);
		}
		// adjust dataloads since slices was previously unknown
		for (DataLoad d : toUpdate) {
			d.setValue((double) d.getValue() / numberSlices);
		}
	}

	/**
	 * A recursive function to get unique workloads.
	 * Number of different workloads is used to calculate generalness
	 * and other rates. When the tree is build from goals node and later
	 * imported to the tree it should be calculated.
	 * 
	 * @param root goal
	 */
	public void updateNumberDiffWorkloads(GoalNode root) {
        for (Workload w : root.getWorkloads()) 
    		allDiffWorkloads.add(w);
        for (GoalNode g : root.getDescendants()) 
            updateNumberDiffWorkloads(g);
	}
	
    /**
     * Add a workload annotation to a given goal
     * 
     * @param goal,     the goal to add an annotation
     * @param workload, the id of the workload
     * @param effort,   a double the necessary effort
     * @throws GoalNotFound 
     */
    public void addWorkload(String goal, String workload, double effort) throws GoalNotFound {
        GoalNode g = findAGoalByName(getRootNode(), goal);
        
        if (g == null) 
        	throw new GoalNotFound("Goal '"+goal+"' not found!");
        
        Workload w = new Workload(workload, effort);
        g.addWorkload(w);
        allDiffWorkloads.add(w);
    }

    /**
     * Add an inform annotation to a given goal as well as data load annotation to
     * each recipient
     * 
     * @param goal,      the goal to add an annotation
     * @param inform,    the id of the inform
     * @param recipient, the recipient goal for this inform
     * @param amount,    a double representing amount of data
     * @throws CircularReference
     */
    public void addInform(String goal, String inform, String recipient, double amount) throws CircularReference {
		GoalNode g = findAGoalByName(this.rootNode, goal);
		GoalNode r = findAGoalByName(this.rootNode, recipient);
		g.addInform(new Inform(inform, r, amount));
		r.addDataLoad(new DataLoad(inform, g, amount));
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
	
	public void updateReferences() throws GoalNotFound, CircularReference {
		for (GoalNode d : getTree()) {
			updateInformAndDataLoadReferences(d);
		}
	}
	
	/**
	 * Brake the goal tree in smaller goals if any goal is exceeding the
	 * max allowed for the annotations
	 * @throws GoalNotFound 
	 */
	public synchronized void brakeGoalTree() throws GoalNotFound {
		try {
			// brake tree by workloads
			for (GoalNode d : getTree()) {
				d.clearDataLoads();
				brakeGoalNodeByWorkload(d);
			}
			
			addAllDescendants(getRootNode());
			updateReferences();

			// brake tree by dataloads
			for (GoalNode d : getTree()) {
				brakeGoalNodeByDataLoad(d);
				d.clearDataLoads();
			}
			
			addAllDescendants(getRootNode());
			updateReferences();

		} catch (CircularReference e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Brake the goal if the sum of workloads exceeds max workload
	 * 
	 * @param original, the currently examining goal
	 * @param parent, the parent of the examining goal
	 */
	private void brakeGoalNodeByWorkload(GoalNode goal) {
		try {
			double sumEfforts = 0;
			for (Workload w : goal.getWorkloads())
				sumEfforts += (double) w.getValue();

			// the number of slices is at least 1 being more according to properties
			int slices = (int) Math.max(Math.ceil(sumEfforts / Parameters.getWorkloadGrain()), 1.0);

			if (slices > 1) {
				String originalName = goal.getGoalName();
				goal.setGoalName(originalName + "$0");

				// workload is a primitive annotation which must be present
				for (Workload w : goal.getWorkloads())
					w.setValue((double) w.getValue() / slices);

				// inform is primitive, dataloads are later created from informs
				for (Inform j : goal.getInforms()) {
					j.setValue((double) j.getValue() / slices);
				}

				// clone other slices
				for (int i = 1; i < slices; i++) {
					GoalNode g = goal.cloneContent();

					// in case it is the root goal, put root as parent
	    			if (goal.getParent() == null)
	    				g.setParent(goal);
	    			else 
	    				g.setParent(goal.getParent());
					g.setGoalName(originalName + "$" + i);
				}
			}
		} catch (CircularReference e) {
			e.printStackTrace();
		}
	}

	/**
	 * Brake the goal if the sum of dataload exceeds max dataload
	 * 
	 * @param original, the currently examining goal
	 * @param parent, the parent of the examining goal
	 */
	private void brakeGoalNodeByDataLoad(GoalNode goal) {
		try {
			double sumDataAmount = 0;
			for (DataLoad t : goal.getDataLoads())
				sumDataAmount += (double) t.getValue();

			// the number of slices is at least 1 being more according to properties
			int slices = (int) Math.max(Math.ceil(sumDataAmount / Parameters.getDataLoadGrain()), 1.0);

			if (slices > 1) {
				String originalName = goal.getGoalName();
				goal.setGoalName(originalName + "$0");

				// workload is a primitive annotation which must be present
				for (Workload w : goal.getWorkloads())
					w.setValue((double) w.getValue() / slices);

				// inform is primitive, dataloads are later created from informs
				for (Inform j : goal.getInforms()) {
					j.setValue((double) j.getValue() / slices);
				}

				// clone other slices
				for (int i = 1; i < slices; i++) {
					GoalNode g;
					g = goal.cloneContent();

					// in case it is the root goal, put root as parent
					if (goal.getParent() == null)
						g.setParent(goal);
					else
						g.setParent(goal.getParent());
					g.setGoalName(originalName + "$" + i);
				}
			}
		} catch (CircularReference e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Give the sum of efforts of the whole tree
	 * 
	 * @return a double
	 */
	public double getSumEfforts() {
		double sumEfforts = 0;
		for (GoalNode g : this.getTree()) {
			for (Workload w : g.getWorkloads()) 
				sumEfforts += (double) w.getValue();
			
		}
		return sumEfforts;
	}
	
	public double getSumDataLoad() {
		double sumDataLoad = 0;
		for (GoalNode g : this.getTree()) {
			for (DataLoad w : g.getDataLoads()) 
				sumDataLoad += (double) w.getValue();
			
		}
		return sumDataLoad;
	}

	/**
	 * This is the lower bound of number of roles according to max workloads
	 * 
	 * @return integer representing bet number of roles (lower bound)
	 */
	public int getBestNumberOfRoles() {
		return Math.max(
				(int) Math.ceil(getSumEfforts() / Parameters.getMaxWorkload()),
				(int) Math.ceil(getSumDataLoad() / Parameters.getMaxDataLoad()));
	}
	
	public int getNumberDiffWorkloads() {
		return allDiffWorkloads.size();
	}

}
