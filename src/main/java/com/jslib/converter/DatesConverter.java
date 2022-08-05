package com.jslib.converter;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.jslib.lang.BugError;

/**
 * Date/time values conversion to/from ISO8601.
 * 
 * @author Iulian Rotaru
 */
final class DatesConverter implements Converter {
	// current conversion format doesn't cope with milliseconds that are simply ignored on parse and don't included in formated
	// string; client MAY include milliseconds when sent dates as JSON string but MUST not expect them when receive
	// the rationale of this decision lies on browser difference when implementing milliseconds:
	// at the time this are writing WebKit and Presto doesn't use milliseconds while Trident and Gecko does

  
    private static final List<String> PATTERNS = new ArrayList<>();
    static {
      // ISO8601 date/time format
      PATTERNS.add("yyyy-MM-dd'T'HH:mm:ss");
      // LocalDateTime usual format
      PATTERNS.add("yyyy-MM-dd HH:mm:ss");
    }

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
	@SuppressWarnings("unchecked")
	public <T> T asObject(String string, Class<T> valueType) {
		if (string.isEmpty()) {
			return null;
		}

		Date date = null;
        ParsePosition parsePosition = null;
		for(String pattern: PATTERNS) {
		  DateFormat df = new SimpleDateFormat(pattern);
		  df.setTimeZone(TimeZone.getTimeZone("UTC"));

		  parsePosition = new ParsePosition(0);
		  date = df.parse(string, parsePosition);
		  if(date != null) {
		    break;
		  }
		}
		if (date == null) {
			throw new ConverterException("Cannot parse date from |%s| at position |%d|.", string, parsePosition.getErrorIndex());
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.MILLISECOND, 0);
		long time = calendar.getTimeInMillis();

        if (valueType == Date.class) {
          return (T) new Date(time);
        }
        
        if (valueType == LocalDate.class) {
          return (T) date.toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
        }
        if (valueType == LocalTime.class) {
          return (T) date.toInstant().atZone(ZoneId.of("UTC")).toLocalTime();
        }
        if (valueType == LocalDateTime.class) {
          return (T) date.toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime();
        }
        if (valueType == ZonedDateTime.class) {
          return (T) ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
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
