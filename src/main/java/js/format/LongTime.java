package js.format;

import java.text.DateFormat;
import java.util.Locale;

/**
 * Long time format - 11:40:00 AM UTC.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public final class LongTime extends DateTimeFormat {
	@Override
	protected DateFormat createDateFormat(Locale locale) {
		return DateFormat.getTimeInstance(DateFormat.LONG, locale);
	}
}
