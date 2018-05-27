package js.format;

/**
 * Format object value as string suitable for user interfaces. Since this interface is used only on server it deals with objects
 * formatting, strings parsing being exclusively implemented by client code. Formatted string should be proper for display on
 * user interfaces and may be subject to locale and time zone adjustments. How this is accomplished is entirely on
 * implementation consideration. Note that not all format classes should be both locale and time zone sensitive.
 * <p>
 * A formatter deals with object value. An object value is an instance of a class that wrap a single value susceptible to be
 * represented as a single string - a sort of data atom, e.g. java.io.File or java.net.URL.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public interface Format {
	/**
	 * Return a string representation, suitable for user interface display, of given object value.
	 * 
	 * @param object object value to format.
	 * @return object user interface representation.
	 */
	String format(Object object);
}
