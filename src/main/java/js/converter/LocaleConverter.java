package js.converter;

import java.util.Locale;

/**
 * Locale converter for ISO-639 language and ISO-3166 country code format. This converter uses the same string representation as
 * most browsers do for language: two lower case letters language code, dash and two upper case letters country code, e.g.
 * en-US.
 * 
 * @author Iulian Rotaru
 * @version final
 */
@SuppressWarnings("unchecked")
final class LocaleConverter implements Converter {
	/** Package default converter. */
	LocaleConverter() {
	}

	/**
	 * Create locale instance from ISO-639 language and ISO-3166 country code, for example en-US.
	 * 
	 * @throws IllegalArgumentException if given string argument is not well formatted.
	 */
	@Override
	public <T> T asObject(String string, Class<T> valueType) throws IllegalArgumentException {
		// at this point value type is guaranteed to be a Locale
		int dashIndex = string.indexOf('-');
		if (dashIndex == -1) {
			throw new IllegalArgumentException(String.format("Cannot convert |%s| to locale instance.", string));
		}
		return (T) new Locale(string.substring(0, dashIndex), string.substring(dashIndex + 1));
	}

	/** Return locale instance ISO-639 language and ISO-3166 country code, for example en-US. */
	@Override
	public String asString(Object object) {
		// at this point object is guaranteed to be a Locale instance
		Locale locale = (Locale) object;
		StringBuilder builder = new StringBuilder(5);
		builder.append(locale.getLanguage());
		builder.append('-');
		builder.append(locale.getCountry());
		return builder.toString();
	}
}
