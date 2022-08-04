package js.format;

import java.text.DateFormat;
import java.util.Locale;

/**
 * Full date/time format - Sunday, March 15, 1964 11:40:00 AM UTC.
 * 
 * @author Iulian Rotaru
 */
public final class FullDateTime extends DateTimeFormat {
	@Override
	protected DateFormat createDateFormat(Locale locale) {
		return DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, locale);
	}
}
