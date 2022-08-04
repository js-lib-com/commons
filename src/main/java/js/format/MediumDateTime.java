package js.format;

import java.text.DateFormat;
import java.util.Locale;

/**
 * Medium date/time format - Mar 15, 1964 11:40:00 AM.
 * 
 * @author Iulian Rotaru
 */
public final class MediumDateTime extends DateTimeFormat {
	@Override
	protected DateFormat createDateFormat(Locale locale) {
		return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
	}
}
