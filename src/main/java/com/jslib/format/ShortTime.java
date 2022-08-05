package com.jslib.format;

import java.text.DateFormat;
import java.util.Locale;

/**
 * Short time format - 11:40 AM.
 * 
 * @author Iulian Rotaru
 */
public final class ShortTime extends DateTimeFormat {
	@Override
	protected DateFormat createDateFormat(Locale locale) {
		return DateFormat.getTimeInstance(DateFormat.SHORT, locale);
	}
}
