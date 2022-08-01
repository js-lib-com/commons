package js.converter;

import js.lang.NoSuchBeingException;
import js.log.Log;
import js.log.LogFactory;
import js.util.Classes;

/**
 * Java class converter. This converter supplies (de)serialization services for {@link Class} instances.
 * 
 * @author Iulian Rotaru
 */
final class ClassConverter implements Converter {
	/** Class logger. */
	private static final Log log = LogFactory.getLog(ClassConverter.class);

	/** Package default converter. */
	ClassConverter() {
	}

	/**
	 * Return the Java class instance for given canonical name. If given string is empty returns null. If class not found warn
	 * to logger and also returns null.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T asObject(String string, Class<T> valueType) {
		// at this point value type is a class
		if (string.isEmpty())
			return null;

		// uses this library Classes#forName instead of Java standard Class#forName
		// first uses current thread context loader whereas the second uses library loader
		// as a consequence classes defined by web app could not be found if use Java standard Class#forName

		try {
			return (T) Classes.forName(string);
		} catch (NoSuchBeingException e) {
			log.warn("Class |%s| not found. Class converter force return value to null.", string);
			return null;
		}
	}

	/** Get string representation for given Java class instance. Return class canonical name. */
	@Override
	public String asString(Object object) {
		assert object instanceof Class;
		return ((Class<?>) object).getCanonicalName();
	}
}
