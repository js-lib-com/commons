package js.format;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Standard date time format - 1964-03-15 14:30:00
 * 
 * @author Iulian Rotaru
 */
public final class StandardDateTime extends DateTimeFormat
{
  /** Constant for standard date time format. */
  private static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";

  @Override
  protected DateFormat createDateFormat(Locale locale)
  {
    return new SimpleDateFormat(STANDARD_FORMAT, locale);
  }
}
