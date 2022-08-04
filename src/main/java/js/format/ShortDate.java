package js.format;

import java.text.DateFormat;
import java.util.Locale;

/**
 * Short date format - 3/15/64.
 * 
 * @author Iulian Rotaru
 */
public final class ShortDate extends DateTimeFormat {
	@Override
	protected DateFormat createDateFormat(Locale locale) {
		return DateFormat.getDateInstance(DateFormat.SHORT, locale);
	}
}
