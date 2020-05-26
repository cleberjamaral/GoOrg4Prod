package organisation.exception;

/**
 * A goal with the given name was not found
 * 
 * @author cleber
 */
public class GoalNotFound extends Exception {

	private static final long serialVersionUID = 1L;
	
	public GoalNotFound(final String message) {
		super(message);
	}

}
