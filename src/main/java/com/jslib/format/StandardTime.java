package com.jslib.format;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Standard time format - 14:30:00
 * 
 * @author Iulian Rotaru
 */
public final class StandardTime extends DateTimeFormat {
	/** Constant for standard time format. */
	private static final String STANDARD_FORMAT = "HH:mm:ss";

	@Override
	protected DateFormat createDateFormat(Locale locale) {
		return new SimpleDateFormat(STANDARD_FORMAT, locale);
	}
}
