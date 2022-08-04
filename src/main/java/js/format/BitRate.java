package js.format;

import java.lang.Number;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Bit rate format - 12.30 Kbps. Bit rate format has a numeric part and units; units are selected so that bit rate to be
 * represented with smallest numeric part but greater or equals one. Numeric part is formatted according locale settings
 * and always has two decimals. For supported units take a look at {@link BitRate.Units} class.
 * 
 * @author Iulian Rotaru
 */
public final class BitRate implements Format
{
  /** Formatter for bit rate numeric part. It is subject of locale adjustments. */
  private NumberFormat numberFormat;

  /** Construct bit rate formatter instance with default locale. */
  public BitRate()
  {
    this(Locale.getDefault());
  }

  /**
   * Construct bit rate formatter instance for requested locale settings.
   * 
   * @param locale locale settings.
   */
  public BitRate(Locale locale)
  {
    this.numberFormat = NumberFormat.getNumberInstance(locale);
    this.numberFormat.setGroupingUsed(true);
    this.numberFormat.setMinimumFractionDigits(2);
    this.numberFormat.setMaximumFractionDigits(2);
  }

  /**
   * Format a integer or long value as bit rate. Returns empty string if <code>value</code> argument is null.
   * 
   * @param value numeric value.
   * @return numeric value represented as bit rate, possible empty if <code>value</code> argument is null.
   * @throws IllegalArgumentException if <code>value</code> argument is not integer or long.
   */
  @Override
  public String format(Object value)
  {
    if(value == null) {
      return "";
    }

    double bitRate = 0;
    if(value instanceof Integer) {
      bitRate = (Integer)value;
    }
    else if(value instanceof Long) {
      bitRate = (Long)value;
    }
    else {
      throw new IllegalArgumentException(String.format("Invalid argument type |%s|.", value.getClass()));
    }

    if(bitRate == 0) {
      return format(0, Units.BPS);
    }
    Units units = Units.BPS;
    for(Units u : Units.values()) {
      if(bitRate < u.value) {
        break;
      }
      units = u;
    }
    return format(bitRate / units.value, units);
  }

  /**
   * Parse bit rate from given string value and return it as {@link Long} instance. Given string <code>value</code>
   * should have a numeric part followed by units, separated by space. See {@link BitRate.Units} for supported units.
   * <p>
   * Returns null if <code>value</code> argument is null or empty.
   * 
   * @param value formatted bit rate value.
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

    Number bitRate = numberFormat.parse(parts[0]);
    return Math.round(bitRate.doubleValue() * units.value);
  }

  /**
   * Build bit rate representation for given numeric value and units.
   * 
   * @param bitRate bit rate numeric part value,
   * @param units bit rate units.
   * @return formated bit rate.
   */
  private String format(double bitRate, Units units)
  {
    StringBuilder builder = new StringBuilder();
    builder.append(numberFormat.format(bitRate));
    builder.append(' ');
    builder.append(units.display());
    return builder.toString();
  }

  /**
   * Bit rate measurement units.
   * 
   * @author Iulian Rotaru
   */
  private enum Units
  {
    /** Bits per second - bps, standard unit. */
    BPS(1),

    /** Kbps, 10^3 = 1,000 bps. */
    KBPS(1000),

    /** Mbps, 10^6 = 1,000,000 bps. */
    MBPS(1000000),

    /** Gbps, 10^9 = 1,000,000,000 bps. */
    GBPS(1000000000),

    /** Tbps, 10^12 = 1,000,000,000,000 bps. */
    TBPS(1000000000000L);

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
      DISPLAY.put(BPS, "bit/s");
      DISPLAY.put(KBPS, "Kbit/s");
      DISPLAY.put(MBPS, "Mbit/s");
      DISPLAY.put(GBPS, "Gbit/s");
      DISPLAY.put(TBPS, "Tbit/s");
    }

    private static Map<String, Units> UNITS = new HashMap<>();
    static {
      UNITS.put(DISPLAY.get(BPS), BPS);
      UNITS.put(DISPLAY.get(KBPS), KBPS);
      UNITS.put(DISPLAY.get(MBPS), MBPS);
      UNITS.put(DISPLAY.get(GBPS), GBPS);
      UNITS.put(DISPLAY.get(TBPS), TBPS);
    }
  }
}
