package fit;

import java.util.Set;

/**
 * @author cleber
 *
 */
public interface Requirement {

	/**
	 * An identification to the requirement
	 * @return a string
	 */
	public String getRequirement();
	
	/**
	 * Return strings representing items that must be fulfilled
	 * @return Set of strings
	 */
	public Set<String> getFeatures();

}
