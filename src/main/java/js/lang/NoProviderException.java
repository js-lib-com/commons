package js.lang;

/**
 * No run-time service provider found for requested service interface.
 * 
 * @author Iulian Rotaru
 */
public class NoProviderException extends RuntimeException {
	/** Java serialization version. */
	private static final long serialVersionUID = 4620095129653112725L;

	/**
	 * Create missing provider exception for given service interface.
	 * 
	 * @param serviceInterface service interface with no implementation found on run-time.
	 */
	public NoProviderException(Class<?> serviceInterface) {
		super(String.format("No service provider found for |%s|.", serviceInterface));
	}
}
