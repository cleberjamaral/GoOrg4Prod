package fit;

import java.util.Set;

public interface Requirement {

	/**
	 * Return strings representing items that must be fulfilled
	 * @return Set of strings
	 */
	public Set<String> getFeatures();

}
