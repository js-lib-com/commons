package js.converter;

import java.math.BigDecimal;

import js.lang.BugError;
import js.util.Types;

/**
 * Numerical values converter. Convert numbers to string and vice versa. Number string representation is that described by JSON
 * specifications as it is generic enough to cover all practical needs.
 * 
 * @author Iulian Rotaru
 */
final class NumbersConverter implements Converter {
	/** Package default converter. */
	NumbersConverter() {
	}

	/**
	 * Convert a string to a number of given type. If given string can't be stored into required type silent rounding is
	 * applied. If given string is empty return 0 since it is considered as an optional numeric input.
	 * 
	 * @throws BugError if value type is not supported.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T asObject(String string, Class<T> valueType) throws BugError {
		if ("null".equals(string)) {
			return null;
		}
		Number number = string.isEmpty() ? 0 : parseNumber(string);

		// hopefully int and double are most used numeric types and test them first
		if (Types.equalsAny(valueType, int.class, Integer.class)) {
			return (T) (Integer) number.intValue();
		}
		if (Types.equalsAny(valueType, double.class, Double.class)) {
			return (T) (Double) number.doubleValue();
		}
		if (Types.equalsAny(valueType, byte.class, Byte.class)) {
			return (T) (Byte) number.byteValue();
		}
		if (Types.equalsAny(valueType, short.class, Short.class)) {
			return (T) (Short) number.shortValue();
		}
		if (Types.equalsAny(valueType, long.class, Long.class)) {
			// because converting between doubles and longs may result in loss of precision we need
			// special treatment for longs. @see ConverterUnitTest.testConversionPrecision
			if (string.length() > 0 && string.indexOf('.') == -1) {
				// handle hexadecimal notation
				if (string.length() > 1 && string.charAt(0) == '0' && string.charAt(1) == 'x') {
					return (T) (Long) Long.parseLong(string.substring(2), 16);
				}
				return (T) (Long) Long.parseLong(string);
			}
			return (T) (Long) number.longValue();
		}
		if (Types.equalsAny(valueType, float.class, Float.class)) {
			return (T) (Float) number.floatValue();
		}
		// if(Classes.equalsAny(t,BigInteger.class) {
		// return (T)new BigInteger(number.doubleValue());
		// }
		if (Types.equalsAny(valueType, BigDecimal.class)) {
			return (T) new BigDecimal(number.doubleValue());
		}
		throw new BugError("Unsupported numeric value |%s|.", valueType);
	}

	/**
	 * Parse numeric string value to a number.
	 * 
	 * @param string numeric string value.
	 * @return number instance.
	 */
	private Number parseNumber(String string) {
		if (string.length() > 2 && string.charAt(0) == '0' && string.charAt(1) == 'x') {
			return Long.parseLong(string.substring(2), 16);
		}
		return Double.parseDouble(string);
	}

	/** Get number string representation. */
	@Override
	public String asString(Object object) {
		// at this point object is guaranteed to be a Number instance
		return object.toString();
	}
}
