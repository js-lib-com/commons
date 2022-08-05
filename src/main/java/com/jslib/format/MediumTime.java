package com.jslib.format;

import java.text.DateFormat;
import java.util.Locale;

/**
 * Medium time format - 11:40:00 AM.
 * 
 * @author Iulian Rotaru
 */
public final class MediumTime extends DateTimeFormat {
	@Override
	protected DateFormat createDateFormat(Locale locale) {
		return DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
	}
}
