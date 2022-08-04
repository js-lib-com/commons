package js.format;

import java.text.DateFormat;
import java.util.Locale;

/**
 * Short date/time format - 3/15/64 11:40 AM.
 * 
 * @author Iulian Rotaru
 */
public final class ShortDateTime extends DateTimeFormat {
	@Override
	protected DateFormat createDateFormat(Locale locale) {
		return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
	}
}
