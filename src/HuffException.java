/**
 *	Custom made exception
 */

@SuppressWarnings("serial")
public class HuffException extends RuntimeException {

	public HuffException(String error) {
		super(error);
	}
}