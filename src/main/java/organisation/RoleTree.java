package organisation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import properties.Throughput;
import properties.Workload;

public class RoleTree {

	RoleNode rootNode;
	Set<RoleNode> tree = new HashSet<>();
	
	RoleTree() {}
	
	public int size() {
		return tree.size();
	}

	public Set<RoleNode> tree() {
		return tree;	
	}
	
	public void add(RoleNode role) {
		tree.add(role);
	}
	
	public String getSignature() {
		List<String> signature = new ArrayList<>();

		if ((this.tree != null) && (!this.tree.isEmpty())) {
			Iterator<RoleNode> iterator = this.tree.iterator();
			while (iterator.hasNext()) {
				RoleNode n = iterator.next();
				signature.add(n.toString());
			}
		}

		Collections.sort(signature);
		return signature.toString();
	}
	
	//TODO: create a clone function cloning each role of the tree because each state has its own roles tree
	
	@Override
	public String toString() {
		return tree.toString();
	}
}
