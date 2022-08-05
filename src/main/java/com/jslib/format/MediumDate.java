package com.jslib.format;

import java.text.DateFormat;
import java.util.Locale;

/**
 * Medium date format - Mar 15, 1964.
 * 
 * @author Iulian Rotaru
 */
public final class MediumDate extends DateTimeFormat {
	@Override
	protected DateFormat createDateFormat(Locale locale) {
		return DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
	}
}
