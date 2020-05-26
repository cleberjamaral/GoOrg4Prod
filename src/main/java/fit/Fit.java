package fit;

import java.util.Map;
import java.util.Set;

/**
 * @author cleber
 *
 */
public abstract class Fit {
	
	public Fit() {}
	
	/**
	 * Method Stub - should be overwritten by implementations 
	 * @param orgName is the name of the organisation used to refer to the generated ones
	 * @param requirements are what the necessary things to fill
	 * @param resources are the available agents
	 * @return matches, i.e., Map<Requirement,Resource>
	 */
	public Map<Requirement,Resource> fitRequirements(String orgName, Set<Requirement> requirements, Set<Resource> resources) {
		System.out.println("OrgN: "+orgName);
		System.out.println("Reqs: ");
		requirements.forEach(r -> {System.out.println(r.getFeatures());});
		System.out.println("Reso: ");
		resources.forEach(r -> {System.out.println(r.getFeatures());});
		return null;
	}
	
}
