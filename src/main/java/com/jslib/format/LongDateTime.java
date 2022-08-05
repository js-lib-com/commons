package com.jslib.format;

import java.text.DateFormat;
import java.util.Locale;

/**
 * Long date/time format - March 15, 1964 11:40:00 AM UTC.
 * 
 * @author Iulian Rotaru
 */
public final class LongDateTime extends DateTimeFormat {
	@Override
	protected DateFormat createDateFormat(Locale locale) {
		return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
	}
}
