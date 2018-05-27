package js.lang;

/**
 * Wrapper for variable arguments used by reflexive method invocation.
 * 
 * @param <T> all variable arguments have the same type.
 * @author Iulian Rotaru
 * @version final
 */
public class VarArgs<T> {
	/** Predefined NULL variable arguments instance. */
	public static VarArgs<Object> NULL = new VarArgs<Object>();

	/** Variable arguments storage. */
	private T[] arguments;

	/**
	 * Construct variable arguments instance.
	 * 
	 * @param arguments variable number of arguments.
	 */
	@SafeVarargs
	public VarArgs(T... arguments) {
		this.arguments = arguments;
	}

	/**
	 * Get variable arguments type.
	 * 
	 * @return arguments type.
	 */
	@SuppressWarnings("unchecked")
	public Class<T> getType() {
		return (Class<T>) arguments.getClass();
	}

	/**
	 * Get arguments array.
	 * 
	 * @return arguments array.
	 */
	public T[] getArguments() {
		return arguments;
	}
}