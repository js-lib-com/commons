/**
 * User interface formatters and parsers. Format package convert object values to/from string representation
 * usable to user interfaces. Formatted string should be proper for display on user interfaces and may be 
 * subject to locale and time zone adjustments. Note that not all format classes should be both locale 
 * and time zone sensitive.
 * <p>
 * A formatter deals with object value. An object value is an instance of a class that wrap a single value 
 * susceptible to be represented as a single string - a sort of data atom, e.g. java.io.File or java.net.URL. 
 * 
 * <h3>Stock Format Classes</h3>
 * Format package contains ready to use format classes for date/time, currency, percent, numbers and 
 * file size. Here is a list with sample output/input to help decide which best fit your needs.
 * <table>
 * <caption>Value Sample</caption>
 * <tr><th>Class</th><th>English Sample</th><th>Romanian Sample</th></tr>
 * <tr><td>FullDateTime</td><td>Sunday, March 15, 1964 11:40:00 AM UTC</td><td>15 martie 1964 11:40:00 UTC</td></tr>
 * <tr><td>FullDate</td><td>Sunday, March 15, 1964</td><td>15 martie 1964</td></tr>
 * <tr><td>FullTime</td><td>11:40:00 AM UTC</td><td>11:40:00 UTC</td></tr>
 * <tr><td>LongDateTime</td><td>March 15, 1964 11:40:00 AM UTC</td><td>15 martie 1964 11:40:00 UTC</td></tr>
 * <tr><td>LongDate</td><td>March 15, 1964</td><td>15 martie 1964</td></tr>
 * <tr><td>LongTime</td><td>11:40:00 AM UTC</td><td>11:40:00 UTC</td></tr>
 * <tr><td>MediumDateTime</td><td>Mar 15, 1964 11:40:00 AM</td><td>15.03.1964 11:40:00</td></tr>
 * <tr><td>MediumDate</td><td>Mar 15, 1964</td><td>15.03.1964</td></tr>
 * <tr><td>MediumTime</td><td>11:40:00 AM</td><td>11:40:00</td></tr>
 * <tr><td>ShortDateTime</td><td>3/15/64 11:40 AM</td><td>15.03.1964 11:40</td></tr>
 * <tr><td>ShortDate</td><td>3/15/64</td><td>15.03.1964</td></tr>
 * <tr><td>ShortTime</td><td>11:40 AM</td><td>11:40</td></tr>
 * <tr><td>StandardDateTime</td><td>1964-03-15 14:30:00</td><td>1964-03-15 14:30:00</td></tr>
 * <tr><td>StandardDate</td><td>1964-03-15</td><td>1964-03-15</td></tr>
 * <tr><td>StandardTime</td><td>14:30:00</td><td>14:30:00</td></tr>
 * <tr><td>Currency</td><td>$12.34</td><td>12,34 LEI</td></tr>
 * <tr><td>Percent</td><td>12.34%</td><td>12,34%</td></tr>
 * <tr><td>Number</td><td>12.34</td><td>12,34</td></tr>
 * <tr><td>FileSize</td><td>2.34KB</td><td>2,34KB</td></tr>
 * </table>
 * 
 * <h3>Usage</h3>
 * In order to invoke {@link js.format.Format#format(Object)} or {@link js.format.Format#parse(String)}
 * one needs a formatter instance. If a formatter is locale and or time zone sensitive its constructor 
 * takes care to initialize default values. Formatter implementation may provide setters for locale settings 
 * and time zone used to overwrite default values. If present, these setters should be called before 
 * executing formatting or parsing. Here is a sample usage on a date time formatter.
 * <pre>
 * LongDate formatter = new LongDate();
 * formatter.setLocale(new Locale("ro"));
 * formatter.setTimeZone(TimeZone.getTimeZone("EET"));
 * ...
 * System.out.println(formatter.format(new Date()));
 * </pre> 
 * 
 * <h3>User Defined Formatters</h3>
 * For formatters not related to date there is no much support. Custom formatter should only implement
 * {@link js.format.Format} interface.
 * <p>
 * For date time formatters, format package provides an abstract base class. This abstract base class 
 * implements almost entire date time formatting and parsing logic. User defined implementation should 
 * only implement the factory method {@link js.format.DateTimeFormat#createDateFormat(Locale)}.
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
 * @author Iulian Rotaru
 */
package js.format;

import java.util.Locale;

