package organisation.exception;

/**
 * A position by its signature was not found
 * 
 * @author cleber
 */
public class PositionNotFound extends Exception {

	private static final long serialVersionUID = 1L;
	
	public PositionNotFound(final String message) {
		super(message);
	}

}
