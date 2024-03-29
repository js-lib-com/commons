package com.jslib.format;

import java.text.DateFormat;
import java.util.Locale;

/**
 * Long date format - March 15, 1964.
 * 
 * @author Iulian Rotaru
 */
public final class LongDate extends DateTimeFormat {
	@Override
	protected DateFormat createDateFormat(Locale locale) {
		return DateFormat.getDateInstance(DateFormat.LONG, locale);
	}
}
