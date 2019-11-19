package organisation.exception;

public class MoreThanOneRootRoleFound extends Exception {

	private static final long serialVersionUID = 1L;
	
	public MoreThanOneRootRoleFound(String message) {
		super(message);
	}

}
