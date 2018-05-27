package js.converter;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import js.lang.BugError;

/**
 * Date/time values conversion to/from ISO8601.
 * 
 * @author Iulian Rotaru
 * @version final
 */
@SuppressWarnings("unchecked")
final class DatesConverter implements Converter {
	// current conversion format doesn't cope with milliseconds that are simply ignored on parse and don't included in formated
	// string; client MAY include milliseconds when sent dates as JSON string but MUST not expect them when receive
	// the rationale of this decision lies on browser difference when implementing milliseconds:
	// WebKit and Presto doesn't use milliseconds while Trident and Gecko does

	/**
	 * ISO8601 date/time formatter without time zone used for parsing. Uses thread local storage because Java date formatters
	 * are not thread safe.
	 */
	private static ThreadLocal<DateFormat> dateParser = new ThreadLocal<DateFormat>();

	/** Package default constructor. */
	DatesConverter() {
	}

	/**
	 * Convert ISO8601 date string representation into date instance. Supported date classes are:
	 * <ul>
	 * <li>java.util.Date
	 * <li>java.sql.Date
	 * <li>java.sql.Time
	 * <li>java.sql.Timestamp
	 * </ul>
	 * String argument should be a valid ISO8601 format otherwise parsing exception is thrown; if is empty returns null.
	 * 
	 * @throws ConverterException if given string is not a valid ISO8601 format.
	 * @throws BugError if given value type is not a supported date class.
	 */
	@Override
	public <T> T asObject(String string, Class<T> valueType) {
		if (string.isEmpty()) {
			return null;
		}

		// simple date format is not thread safe; on the other hand http request threads are reused
		// so seems like a good idea to avoid parser instance creation and reuse it on a per thread basis
		// excerpt from Java api-doc:
		// Date formats are not synchronized. It is recommended to create separate format instances for each thread.
		DateFormat df = dateParser.get();
		if (df == null) {
			df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
			dateParser.set(df);
		}

		ParsePosition parsePosition = new ParsePosition(0);
		Date date = df.parse(string, parsePosition);
		if (date == null) {
			throw new ConverterException("Cannot parse ISO8601 date from |%s| at position |%d|.", string, parsePosition.getErrorIndex());
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.MILLISECOND, 0);
		long time = calendar.getTimeInMillis();

		if (valueType == Date.class) {
			return (T) new Date(time);
		}
		if (valueType == java.sql.Date.class) {
			return (T) new java.sql.Date(time);
		}
		if (valueType == Time.class) {
			return (T) new Time(time);
		}
		if (valueType == Timestamp.class) {
			return (T) new Timestamp(time);
		}

		throw new BugError("Unsupported date type |%s|.", valueType);
	}

	/** Return ISO8601 string representation for given date instance. */
	@Override
	public String asString(Object object) {
		// at this point object is a Date

		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		calendar.setTime((Date) object);

		StringBuilder builder = new StringBuilder();

		int year = calendar.get(Calendar.YEAR);
		if (calendar.get(Calendar.ERA) == 0) {
			// https://en.wikipedia.org/wiki/ISO_8601
			// by convention 1 BC is labeled +0000, 2 BC is labeled −0001, ...

			if (year > 1) {
				builder.append('-');
			}
			--year;
		}

		builder.append(String.format("%04d", year));
		builder.append('-');
		builder.append(String.format("%02d", calendar.get(Calendar.MONTH) + 1));
		builder.append('-');
		builder.append(String.format("%02d", calendar.get(Calendar.DATE)));
		builder.append('T');
		builder.append(String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)));
		builder.append(':');
		builder.append(String.format("%02d", calendar.get(Calendar.MINUTE)));
		builder.append(':');
		builder.append(String.format("%02d", calendar.get(Calendar.SECOND)));
		builder.append('Z');

		return builder.toString();
	}
}
