package organisation.exception;

public class CircularReference extends Exception {

	private static final long serialVersionUID = 1L;
	
	public CircularReference(String message) {
		super(message);
	}

}
