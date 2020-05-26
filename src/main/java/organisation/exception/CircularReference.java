package organisation.exception;

/**
 * Circular reference occurs for instance when a goal
 * has an inform to itself which is inconsistent since
 * inform to itself about something is unnecessary
 * 
 * @author cleber
 */
public class CircularReference extends Exception {

	private static final long serialVersionUID = 1L;
	
	public CircularReference(final String message) {
		super(message);
	}

}
