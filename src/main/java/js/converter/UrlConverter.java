package js.converter;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * URL converter.
 * 
 * @author Iulian Rotaru
 * @version final
 */
@SuppressWarnings("unchecked")
final class UrlConverter implements Converter {
	/** Package default converter. */
	UrlConverter() {
	}

	/**
	 * Convert URL string representation into URL instance.
	 * 
	 * @throws ConverterException if given string is not a valid URL.
	 */
	@Override
	public <T> T asObject(String string, Class<T> valueType) throws ConverterException {
		// at this point value type is guaranteed to be URL
		try {
			return (T) new URL(string);
		} catch (MalformedURLException e) {
			throw new ConverterException(e.getMessage());
		}
	}

	/** Get string representation of given URL instance. */
	@Override
	public String asString(Object object) {
		// at this point object is guaranteed to be an URL instance
		return ((URL) object).toExternalForm();
	}
}
