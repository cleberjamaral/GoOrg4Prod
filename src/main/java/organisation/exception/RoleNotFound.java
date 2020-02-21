package organisation.exception;

/**
 * A role by its signature was not found
 */
public class RoleNotFound extends Exception {

	private static final long serialVersionUID = 1L;
	
	public RoleNotFound(final String message) {
		super(message);
	}

}
