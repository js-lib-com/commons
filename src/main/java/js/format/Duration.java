package js.format;

import java.lang.Number;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Format duration value, in milliseconds - 12.30 min. Duration format has a numeric part and units; units are selected
 * so that duration to be represented with smallest numeric part but greater or equals one. Numeric part is formatted
 * according locale settings and always has two decimals. For supported units take a look at {@link Duration.Units}
 * class.
 * 
 * @author Iulian Rotaru
 */
public final class Duration implements Format
{
  /** Formatter for duration numeric part. It is subject of locale adjustments. */
  private NumberFormat numberFormat;

  /** Construct duration formatter instance with default locale. */
  public Duration()
  {
    this(Locale.getDefault());
  }

  /**
   * Construct duration formatter instance for requested locale settings.
   * 
   * @param locale locale settings.
   */
  public Duration(Locale locale)
  {
    this.numberFormat = NumberFormat.getNumberInstance(locale);
    this.numberFormat.setGroupingUsed(true);
    this.numberFormat.setMinimumFractionDigits(2);
    this.numberFormat.setMaximumFractionDigits(2);
  }

  /**
   * Format a integer or long value as a time duration. Returns empty string if <code>value</code> argument is null.
   * 
   * @param value numeric value.
   * @return numeric value represented as duration, possible empty if <code>value</code> argument is null.
   * @throws IllegalArgumentException if <code>value</code> argument is not integer or long.
   */
  @Override
  public String format(Object value)
  {
    if(value == null) {
      return "";
    }

    double duration = 0;
    if(value instanceof Integer) {
      duration = (Integer)value;
    }
    else if(value instanceof Long) {
      duration = (Long)value;
    }
    else {
      throw new IllegalArgumentException(String.format("Invalid argument type |%s|.", value.getClass()));
    }

    if(duration == 0) {
      return format(0, Units.MILLISECONDS);
    }
    Units units = Units.MILLISECONDS;
    for(Units u : Units.values()) {
      if(duration < u.value) {
        break;
      }
      units = u;
    }
    return format(duration / units.value, units);
  }

  /**
   * Parse time duration from given string value and return it as {@link Long} instance. Given string <code>value</code>
   * should have a numeric part followed by units, separated by space. See {@link Duration.Units} for supported units.
   * <p>
   * Returns null if <code>value</code> argument is null or empty.
   * 
   * @param value formatted time duration value.
   * @return file size as {@link Long} instance, possible null if <code>value</code> argument is null or empty.
   * @throws ParseException if <code>value</code> is not formated as described above.
   */
  @Override
  public Object parse(String value) throws ParseException
  {
    if(value == null || value.isEmpty()) {
      return null;
    }

    String[] parts = value.split("\\s+");
    if(parts.length != 2) {
      throw new ParseException("Invalid bit rate value. Missing units.", 0);
    }

    Units units = Units.forName(parts[1]);
    if(units == null) {
      throw new ParseException(String.format("Invalid bit rate value. Not recognized units |%s|.", parts[1]), 0);
    }

    Number duration = numberFormat.parse(parts[0]);
    return Math.round(duration.doubleValue() * units.value);
  }

  /**
   * Build time duration representation for given numeric value and units.
   * 
   * @param duration duration numeric part value,
   * @param units bit rate units.
   * @return formated bit rate.
   */
  private String format(double duration, Units units)
  {
    StringBuilder builder = new StringBuilder();
    builder.append(numberFormat.format(duration));
    builder.append(' ');
    builder.append(units.display());
    return builder.toString();
  }

  /**
   * Duration measurement units.
   * 
   * @author Iulian Rotaru
   */
  private enum Units
  {
    /** Milliseconds, duration base units. */
    MILLISECONDS(1),

    /** Seconds = 1,000 milliseconds. */
    SECONDS(1000),

    /** Minutes = 60 seconds = 60,000 milliseconds. */
    MINUTES(60000),

    /** Hours = 60 minutes = 3,600,000 milliseconds. */
    HOURS(3600000),

    /** Days = 24 hours = 86,400,000 milliseconds. */
    DAYS(86400000),
    
    /** Month = 30 days = 2,592,000,000 milliseconds */
    MONTHS(2592000),
    
    /** Years = 365 days = 946,080,000,000 milliseconds */
    YEARS(946080000000L);

    /** Units value. */
    long value;

    /**
     * Construct units constant instance.
     * 
     * @param value units value.
     */
    Units(long value)
    {
      this.value = value;
    }

    public String display()
    {
      return DISPLAY.get(this);
    }

    public static Units forName(String name)
    {
      return UNITS.get(name.toUpperCase());
    }

    private static Map<Units, String> DISPLAY = new HashMap<>();
    static {
      DISPLAY.put(MILLISECONDS, "msec.");
      DISPLAY.put(SECONDS, "sec.");
      DISPLAY.put(MINUTES, "min.");
      DISPLAY.put(HOURS, "hr.");
      DISPLAY.put(DAYS, "days");
      DISPLAY.put(MONTHS, "months");
      DISPLAY.put(YEARS, "years");
    }

    private static Map<String, Units> UNITS = new HashMap<>();
    static {
      UNITS.put(DISPLAY.get(MILLISECONDS), MILLISECONDS);
      UNITS.put(DISPLAY.get(SECONDS), SECONDS);
      UNITS.put(DISPLAY.get(MINUTES), MINUTES);
      UNITS.put(DISPLAY.get(HOURS), HOURS);
      UNITS.put(DISPLAY.get(DAYS), DAYS);
      UNITS.put(DISPLAY.get(MONTHS), MONTHS);
      UNITS.put(DISPLAY.get(YEARS), YEARS);
    }
  }
}
