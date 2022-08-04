package js.format;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Standard date format - 1964-03-15
 * 
 * @author Iulian Rotaru
 */
public final class StandardDate extends DateTimeFormat {
	/** Constant for standard date format. */
	private static final String STANDARD_FORMAT = "yyyy-MM-dd";

	@Override
	protected DateFormat createDateFormat(Locale locale) {
		return new SimpleDateFormat(STANDARD_FORMAT, locale);
	}
}
