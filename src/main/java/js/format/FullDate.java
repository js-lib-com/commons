package js.format;

import java.text.DateFormat;
import java.util.Locale;

/**
 * Full date format - Sunday, March 15, 1964.
 * 
 * @author Iulian Rotaru
 */
public final class FullDate extends DateTimeFormat {
	@Override
	protected DateFormat createDateFormat(Locale locale) {
		return DateFormat.getDateInstance(DateFormat.FULL, locale);
	}
}
