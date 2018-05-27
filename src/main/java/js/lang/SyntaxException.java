package js.lang;

/**
 * Thrown whenever a parser encounter illegal syntax. For example missing pair separator from {@link PairsList}.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public class SyntaxException extends RuntimeException {
	/** Java serialization version. */
	private static final long serialVersionUID = -1263488623409654782L;

	/**
	 * Construct syntax exception formatted message. See {@link String#format(String, Object...)} for format
	 * description.
	 * 
	 * @param message formatted message,
	 * @param args variable number of format arguments.
	 */
	public SyntaxException(String message, Object... args) {
		super(String.format(message, args));
	}
}
