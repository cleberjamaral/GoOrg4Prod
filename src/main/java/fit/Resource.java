package fit;

import java.util.Set;

public interface Resource {

	/**
	 * Return strings representing items that it is able to do
	 * @return Set of strings
	 */
	public Set<String> getFeatures();
}
