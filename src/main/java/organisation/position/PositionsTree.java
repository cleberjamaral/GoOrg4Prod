package organisation.position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import annotations.DataLoad;
import annotations.Workload;
import fit.Requirement;
import fit.RequirementSet;
import organisation.Parameters;
import organisation.exception.PositionNotFound;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;

/**
 * @author cleber
 *
 */
public class PositionsTree implements RequirementSet {

	private int numberOfLevels = 0;
	private Set<PositionNode> tree = new HashSet<>();

	public PositionsTree() {
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

	public Set<PositionNode> getTree() {
		return tree;
	}

	public void addPositionToTree(PositionNode position) {
		updateNumberOfLevels(position);
		
		tree.add(position);
	}

	private void updateNumberOfLevels(PositionNode position) {
		int levels = 1;
		while (position.getParent() != null) {
			position = position.getParent();
			levels++;
		}

		if (levels > getNumberOfLevels())
			setNumberOfLevels(levels);
	}

	public PositionNode createPosition(PositionNode parent, String name, GoalNode g) {
		PositionNode nr = new PositionNode(parent, name);

		assignGoalToPosition(nr, g);
		addPositionToTree(nr);

		return nr;
	}

	public PositionNode findPositionByName(String positionName) throws PositionNotFound {
		for (PositionNode or : this.tree) {
			if (or.getPositionName().equals(positionName))
				return or;
		}
		throw new PositionNotFound("There is no position with signature = '" + positionName + "'!");
	}

	public PositionsTree cloneContent() throws PositionNotFound {
		PositionsTree clonedTree = new PositionsTree();

		// first clone all positions
		for (PositionNode or : this.tree) {
			PositionNode nnewS = or.cloneContent();
			// not using addPositionToTree because parent is still unknown
			clonedTree.getTree().add(nnewS);
		}

		// finding right parents in the new tree
		for (PositionNode or : clonedTree.getTree()) {

			// it is not the root position
			if (!or.getParentName().equals("")) {
				or.setParent(clonedTree.findPositionByName(or.getParentName()));
			}
		}
		
		// update number of levels after knowning parents
		clonedTree.setNumberOfLevels(1);
		for (PositionNode or : clonedTree.getTree()) {
			clonedTree.updateNumberOfLevels(or);
		}
		
		return clonedTree;
	}

	public PositionNode assignGoalToPositionByPositionName(String positionName, GoalNode newGoal) throws PositionNotFound {
		PositionNode position = this.findPositionByName(positionName);

		assignGoalToPosition(position, newGoal);

		return position;
	}

	public void assignGoalToPosition(PositionNode position, GoalNode newGoal) {
		position.assignGoal(newGoal);

		// Copy all workloads of the goal to this new position
		for (Workload w : newGoal.getWorkloads())
			position.addWorkload(w.clone());

		// Copy all "non-circular" dataloads to new position (informs are not used for positions)
		for (DataLoad d : newGoal.getDataLoads()) {
			boolean circularDataload = false;
			for (GoalNode g : position.getAssignedGoals()) {
				if (g.getGoalName().equals(d.getSenderName())) circularDataload = true;
			}
			if (!circularDataload) position.addDataLoad(d.clone());
		}
	}

	/**
	 * Give the sum of efforts of the whole tree
	 * 
	 * @return a double
	 */
	public double getSumWorkload() {
		double sumWorkload = 0;
		for (PositionNode r : this.tree) {
			for (Workload w : r.getWorkloads()) {
				sumWorkload += (double) w.getValue();
			}
		}
		return sumWorkload;
	}

	@Override
	public String toString() {
		List<String> signatureByPositions = new ArrayList<>();
		if ((getTree() != null) && (!getTree().isEmpty())) {
			Iterator<PositionNode> iterator = getTree().iterator();
			while (iterator.hasNext()) {
				PositionNode n = iterator.next();
				signatureByPositions.add(n.toString());
			}
			Collections.sort(signatureByPositions);
		}
		return signatureByPositions.toString();
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
		PositionsTree other = (PositionsTree) obj;
		if (tree == null) {
			if (other.tree != null)
				return false;
		} else if (!tree.toString().equals(other.tree.toString()))
			return false;
		return true;
	}
	
	/**
	 * Generalness is about how goals are distributed across positions. When all
	 * positions have all given goals it means all agents are responsible for all
	 * tasks and they can play any position, i.e., maximum generalness.
	 * 
	 * For instance, for a GDT with a set of goals that together have 3 different
	 * goals ideally these goals should be broken into 9 goals and the PositionsTree 
	 * should prefer structures in which all positions have one part of each of 
	 * those 3 goals. So, the 3 agents that will plays those positions can be
	 * actually allocated in any one of those positions
	 * 
	 * @return generalness index from 0 to 1 (less to maximum generalness possible)
	 * @throws Exception 
	 */
	public double getGeneralness() {
		int nAllOriginalGoalsAssigned = 0;
		int nGoalsAssigned = 0;

		// An original goal refers to the whole or a part of a given (non-broken) goal
		for (PositionNode or : this.tree) {
			Set<String> allOriginalGoalsOfPosition = new HashSet<>();
			for (GoalNode g : or.getAssignedGoals()) {
				allOriginalGoalsOfPosition.add(g.getOriginalName());
				// Accumulates all goals (all broken goals) 
				nGoalsAssigned++;
			}
			// Accumulates only different goals, sum all we have on each position 
			nAllOriginalGoalsAssigned += allOriginalGoalsOfPosition.size();
		}

		// ideal situation: each original goal has a broken part that can be distributed
		// equally across all positions
		int nMaxOriginalGoalsSpread = GoalTree.getInstance().getNumberOriginalGoals() * this.tree.size();

		// the actual generalness of the current positions tree
		double generalness = (double) nAllOriginalGoalsAssigned / (double) nMaxOriginalGoalsSpread;

		// if it is a partial generalness, add a penalty according to the number of goals to assign
		int nGoalsToAssing = GoalTree.getInstance().getTree().size() - nGoalsAssigned;

		// a penalty for partial generalness
		if (nGoalsToAssing > 0)
			generalness /= nGoalsToAssing * GoalTree.getInstance().getTree().size() * 10;

		return generalness;
	}
	
	/**
	 * Specificness like generalness is about how workloads are distributed across
	 * positions. In case of specificness, it would be ideal to do not split workloads,
	 * since splitting means sharing this workload what makes necessary more skills
	 * to the other agent.
	 * 
	 * For instance, for a GDT with a set of goals that together have 3 different
	 * workloads the PositionsTree should prefer structure in which each position has only
	 * 1 workload ideally In this case, it would need less skills from each agent.
	 * 
	 * Efficiency/idleness is not being taken into account here
	 * 
	 * @return specificness index from 0 to 1 (less to maximum specificness
	 *         possible)
	 */
	public double getSpecificness() {
		int nAllWorkloads = 0;
		
		for (PositionNode or : this.tree) {
			// PositionNode.getWorkloads() is a hashset returning only unique workloads
			nAllWorkloads += or.getWorkloads().size();
		}
		
		// the most specialist positions tree must have all workloads distributed
		// without splitting them (if may be impossible if the sumofefforts if higher
		// than maxWorkload, but efficiency/idleness should not be taken into account
		int nMinWorkloads = Math.max(GoalTree.getInstance().getNumberDiffWorkloads(), this.tree.size());
		
		return (double) nMinWorkloads / (double) nAllWorkloads;
	}
	
	/**
	 * Idleness is about total workload split into positions. It considers the ideal
	 * number of positions according to maximum workload per positions. In case of relative
	 * idleness, the idleness is ZERO if the goals tree has the minimum idleness that
	 * the GDT and max workload allows.
	 * 
	 * @return idleness relative to the minimum idleness which is possibly achieved.
	 *         Varies from 0 to 1.
	 */
	public double getRelativeIdleness() {
		double minIdleness = Parameters.getMaxWorkload()
				- (GoalTree.getInstance().getSumEfforts() % Parameters.getMaxWorkload());

		double idleness = this.tree.size() * Parameters.getMaxWorkload() - GoalTree.getInstance().getSumEfforts();

		return (idleness - minIdleness) / minIdleness;
	}
	
	/**
	 * Idleness is about total workload split into positions. It considers the ideal
	 * number of positions according to maximum workload per position. In case of absolute
	 * idleness, the idleness is the real one, no matter if it is possible to do a 
	 * perfect split or not, i.e., idleness is ZERO only if the goals tree has reached 
	 * no idle at all.
	 * 
	 * @return absolute idleness achieved, varies from 0 to 1.
	 */
	public double getAbsoluteIdleness() {
		double capacity = this.tree.size() * Parameters.getMaxWorkload();
		double occupancy = capacity - GoalTree.getInstance().getSumEfforts();

		return occupancy / capacity;
	}

	@Override
	public Set<Requirement> getRequirements() {
		//TODO: (Set<Requirement>) tree should work!!!
		Set<Requirement> requirements = new HashSet<>();
		tree.forEach(r -> {requirements.add(r);});
		return requirements;
	}
}
