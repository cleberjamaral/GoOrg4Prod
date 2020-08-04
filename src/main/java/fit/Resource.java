package fit;

import java.util.Set;

/**
 * @author cleber
 * @param <T>
 *
 */
public interface Resource<T> {

	/**
	 * An identification to the resource
	 * @return a string
	 */
	public T getResource();

	
	/**
	 * Return strings representing items that it is able to do
	 * @return Set of strings
	 */
	public Set<T> getFeatures();
}
