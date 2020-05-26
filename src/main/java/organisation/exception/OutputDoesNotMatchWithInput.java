package organisation.exception;

/**
 * Output may not match with input for instance when the sum
 * of workloads given by the input is not the same as the output
 * whowing that the output is inconsistent
 * 
 * @author cleber
*/
public class OutputDoesNotMatchWithInput extends Exception {

	private static final long serialVersionUID = 1L;
    
	public OutputDoesNotMatchWithInput(final String message) {
		super(message);
	}

}
