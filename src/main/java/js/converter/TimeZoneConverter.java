package js.converter;

import java.util.TimeZone;

/**
 * Time zone converter.
 * 
 * @author Iulian Rotaru
 */
final class TimeZoneConverter implements Converter {
	/** Package default converter. */
	TimeZoneConverter() {
	}

	/** Create time zone instance from time zone ID. If time zone ID is not recognized return UTC. */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T asObject(String string, Class<T> valueType) {
		// at this point value type is guaranteed to be a kind of TimeZone
		return (T) TimeZone.getTimeZone(string);
	}

	/** Get time zone ID for given time zone instance. */
	@Override
	public String asString(Object object) {
		// at this point object is guaranteed to be a TimeZone instance
		return ((TimeZone) object).getID();
	}
}
