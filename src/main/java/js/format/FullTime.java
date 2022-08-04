package js.format;

import java.text.DateFormat;
import java.util.Locale;

/**
 * Full time format - 11:40:00 AM UTC.
 * 
 * @author Iulian Rotaru
 */
public final class FullTime extends DateTimeFormat {
	@Override
	protected DateFormat createDateFormat(Locale locale) {
		return DateFormat.getTimeInstance(DateFormat.FULL, locale);
	}
}
