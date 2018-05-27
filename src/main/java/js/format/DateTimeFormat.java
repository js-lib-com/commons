package js.format;

import java.text.DateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Base class for all date/time formatters. This abstract base class implements almost entire date time formatting logic.
 * Concrete implementation should only implement the factory method {@link #createDateFormat(Locale)}.
 * <p>
 * For your convenience here is an example of a date time formatter from this package.
 * 
 * <pre>
 * public final class StandardDateTime extends DateTimeFormat {
 * 	private static final String STANDARD_FORMAT = &quot;yyyy-MM-dd HH:mm:ss&quot;;
 * 
 * 	&#064;Override
 * 	protected DateFormat createDateFormat(Locale locale) {
 * 		return new SimpleDateFormat(STANDARD_FORMAT, locale);
 * 	}
 * }
 * </pre>
 * 
 * <p>
 * In order to use date time formatter one should create formatter instance - with default locale and time zone, optionally set
 * new locale settings and / or time zone then execute format.
 * 
 * <pre>
 * DateTimeFormat formatter = new FullDateTime();
 * formatter.setLocale(new Locale(&quot;ro&quot;));
 * formatter.setTimeZone(TimeZone.getTimeZone(&quot;UTC&quot;));
 * 
 * String dateDisplay = formatter.format(new Date());
 * </pre>
 * <p>
 * All date time formatters uses Java {@link DateFormat} for actual format. Since Java DateFormat is not synchronized date time
 * formatter instances are not thread safe. Anyway, inside a thread, formatter instances can be reused.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public abstract class DateTimeFormat implements Format {
	/** Formatter locale settings. */
	private Locale locale;
	/** Formatter date time zone. */
	private TimeZone timeZone;
	/** Date format instance in charge with actual formatting. */
	private DateFormat dateFormat;

	/**
	 * Factory method for date time formatter instance.
	 * 
	 * @param locale locale settings.
	 * @return newly created date time formatter.
	 */
	protected abstract DateFormat createDateFormat(Locale locale);

	/** Construct date time formatter with default locale settings and time zone. */
	public DateTimeFormat() {
		this.locale = Locale.getDefault();
		this.timeZone = TimeZone.getDefault();
	}

	/**
	 * Set locale settings for this formatter instance. Given locale settings overwrites default value initialized by
	 * constructor. For this setter to have effect it should be called before invoking {@link #format(Object)}.
	 * 
	 * @param locale locale settings.
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * Set time zone for this date time formatter instance. Given time zone overwrites default time zone initialized by
	 * constructor. For this setter to have effect it should be called before invoking {@link #format(Object)}.
	 * 
	 * @param timeZone time zone.
	 */
	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	/**
	 * Format date object accordingly concrete date time formatter implementation. Formatting process occurs in three steps:
	 * <ol>
	 * <li>create date time formatter instance with current locale, see {@link #createDateFormat(Locale)},
	 * <li>set current time zone to newly created formatter instance,
	 * <li>invoke {@link Format#format(Object)} on formatter instance.
	 * </ol>
	 * 
	 * @param date date object.
	 * @return string format for <code>date</code> argument.
	 */
	@Override
	public String format(Object date) {
		if (date == null) {
			return "";
		}
		dateFormat = createDateFormat(locale);
		dateFormat.setTimeZone(timeZone);
		return dateFormat.format(date);
	}
}
