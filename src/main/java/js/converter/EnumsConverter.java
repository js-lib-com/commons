package js.converter;

/**
 * Enumeration values converter.
 * 
 * @author Iulian Rotaru
 * @version final
 */
@SuppressWarnings("unchecked")
final class EnumsConverter implements Converter {
	/** Package default constructor. */
	EnumsConverter() {
	}

	/**
	 * Create enumeration constant for given string and enumeration type.
	 * 
	 * @throws IllegalArgumentException string argument is not a valid constant for given enumeration type.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public <T> T asObject(String string, Class<T> valueType) throws IllegalArgumentException {
		if (string.isEmpty()) {
			return null;
		}
		// at this point value type is guaranteed to be enumeration
		return (T) Enum.valueOf((Class) valueType, string);
	}

	/** Get enumeration constant name. */
	@Override
	public String asString(Object object) {
		// at this point object is guaranteed to be enumeration
		return ((Enum<?>) object).name();
	}
}
