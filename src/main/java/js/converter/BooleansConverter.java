package js.converter;

/**
 * Boolean values converter. Specialized converter used for boolean values.
 * 
 * @author Iulian Rotaru
 * @version final
 */
@SuppressWarnings("unchecked")
final class BooleansConverter implements Converter {
	/** Package default constructor. */
	BooleansConverter() {
	}

	/**
	 * Create a boolean value from given string. Returns boolean true only if string is one of next constants: <em>true</em>,
	 * <em>yes</em>, <em>1</em>, <em>on</em>; otherwise returns false. Note that comparison is not case sensitive.
	 */
	@Override
	public <T> T asObject(String string, Class<T> valueType) {
		// at this point value type is a boolean or a boxing boolean
		string = string.toLowerCase();
		if (string.equals("true")) {
			return (T) (Boolean) true;
		}
		if (string.equals("yes")) {
			return (T) (Boolean) true;
		}
		if (string.equals("1")) {
			return (T) (Boolean) true;
		}
		if (string.equals("on")) {
			return (T) (Boolean) true;
		}
		return (T) (Boolean) false;
	}

	/** Return <em>true</em> string if given object is a boolean true or <em>false</em> otherwise. */
	@Override
	public String asString(Object object) {
		assert object instanceof Boolean;
		return object.toString();
	}
}
