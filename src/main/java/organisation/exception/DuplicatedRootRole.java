package organisation.exception;

/**
 * A tree should have only one root
 */
public class DuplicatedRootRole extends Exception {

	private static final long serialVersionUID = 1L;
	
	public DuplicatedRootRole(final String message) {
		super(message);
	}

}
