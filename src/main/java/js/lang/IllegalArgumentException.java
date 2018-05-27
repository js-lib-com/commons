package js.lang;

import js.util.Strings;

/**
 * Extends Java illegal argument exception with constructors for formatted messages.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public class IllegalArgumentException extends java.lang.IllegalArgumentException {
	/** Java serialization version. */
	private static final long serialVersionUID = 8867100427225705248L;

	/**
	 * Create new illegal argument exception with formatted message.
	 * 
	 * @param format formatted message as supported by {@link String#format(String, Object...)},
	 * @param args optional arguments if message contains formatting tags.
	 */
	public IllegalArgumentException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * Create new illegal argument exception with joined list of arguments.
	 * 
	 * @param args arguments are joined and used as exception message.
	 */
	public IllegalArgumentException(Object[] args) {
		super(Strings.join(args));
	}
}
