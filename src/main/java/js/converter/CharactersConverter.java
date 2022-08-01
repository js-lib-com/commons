package js.converter;

import js.lang.BugError;

/**
 * Character values converter.
 * 
 * @author Iulian Rotaru
 */
final class CharactersConverter implements Converter {
	/** Package default constructor. */
	CharactersConverter() {
	}

	/**
	 * Return the first character from given string.
	 * 
	 * @throws ConverterException if given string has more than one single character.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T asObject(String string, Class<T> valueType) throws BugError {
		// at this point value type is guaranteed to be char or Character
		if (string.length() > 1) {
			throw new ConverterException("Trying to convert a larger string into a single character.");
		}
		return (T) (Character) string.charAt(0);
	}

	/** Return a single character string. */
	@Override
	public String asString(Object object) {
		// at this point object is guaranteed to be instance of Character
		return object.toString();
	}
}
