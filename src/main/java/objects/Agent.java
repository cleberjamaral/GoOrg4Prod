package objects;

import java.util.ArrayList;
import java.util.List;

import properties.Workload;

public class Agent {
	
	private List<Object> properties = new ArrayList<>();
	
	public void addWorkload(Workload w) {
		properties.add(w);
	}

}
