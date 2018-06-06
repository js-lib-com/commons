package js.format;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import js.util.Params;

/**
 * Test utility class for dates parsing and formatting in standard date time.
 * 
 * @author Iulian Rotaru
 */
public class TestDateTime
{
  /** Constant for standard date time format. */
  private static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";

  /**
   * Create a new date instance form given standard date time value.
   * 
   * @param value standard date time value, null or empty not accepted.
   * @return newly created date instance.
   * @throws IllegalArgumentException if <code>value</code> argument is null or empty.
   * @throws ParseException if <code>value</code> argument is not a valid date time format.
   */
  public static Date parse(String value) throws ParseException
  {
    Params.notNullOrEmpty(value, "Date value");
    return new SimpleDateFormat(STANDARD_FORMAT).parse(value);
  }

  /**
   * Convenient way to invoke standard date time formatter.
   * 
   * @param date date value, null not accepted.
   * @return date value string into standard date time format.
   * @throws IllegalArgumentException if <code>date</code> argument is null.
   */
  public static String format(Date date)
  {
    Params.notNull(date, "Date");
    return new SimpleDateFormat(STANDARD_FORMAT).format(date);
  }
}
