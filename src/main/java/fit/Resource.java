package fit;

import java.util.Set;

/**
 * @author cleber
 *
 */
public interface Resource {

	/**
	 * An identification to the resource
	 * @return a string
	 */
	public String getResource();

	
	/**
	 * Return strings representing items that it is able to do
	 * @return Set of strings
	 */
	public Set<String> getFeatures();
}
