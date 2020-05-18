package fit;

import java.util.Set;

public abstract class Fit {
	
	public Fit() {}
	
	public void fitRequirements(String orgName, Set<Requirement> requirements, Set<Resource> resources) {
		System.out.println("OrgN: "+orgName);
		System.out.println("Reqs: ");
		requirements.forEach(r -> {System.out.println(r.getFeatures());});
		System.out.println("Reso: ");
		resources.forEach(r -> {System.out.println(r.getFeatures());});
	}

}
