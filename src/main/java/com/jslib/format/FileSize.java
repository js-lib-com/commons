package com.jslib.format;

import java.lang.Number;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * File size format - 12.30 KB. A file size has a numeric part and units; units are selected so that file size to be
 * represented with smallest numeric part but greater or equals one. Numeric part is formatted according locale settings
 * and always has two decimals. For supported units take a look at {@link Units} class.
 * 
 * @author Iulian Rotaru
 */
public final class FileSize implements Format
{
  /** Formatter for file size numeric part. It is subject of locale adjustments. */
  private NumberFormat numberFormat;

  /** Construct file size formatter instance with default locale. */
  public FileSize()
  {
    this(Locale.getDefault());
  }

  /**
   * Construct file size formatter instance for requested locale settings.
   * 
   * @param locale locale settings.
   */
  public FileSize(Locale locale)
  {
    this.numberFormat = NumberFormat.getNumberInstance(locale);
    this.numberFormat.setGroupingUsed(true);
    this.numberFormat.setMinimumFractionDigits(2);
    this.numberFormat.setMaximumFractionDigits(2);
  }

  /**
   * Format a integer or long value as file size. Returns empty string if <code>value</code> argument is null.
   * 
   * @param value numeric value.
   * @return numeric value represented as file size, possible empty if <code>value</code> argument is null.
   * @throws IllegalArgumentException if <code>value</code> argument is not integer or long.
   */
  @Override
  public String format(Object value)
  {
    if(value == null) {
      return "";
    }

    double fileSize = 0;
    if(value instanceof Integer) {
      fileSize = (Integer)value;
    }
    else if(value instanceof Long) {
      fileSize = (Long)value;
    }
    else {
      throw new IllegalArgumentException(String.format("Invalid argument type |%s|.", value.getClass()));
    }

    if(fileSize == 0) {
      return format(0, Units.B);
    }
    Units units = Units.B;
    for(Units u : Units.values()) {
      if(fileSize < u.value) {
        break;
      }
      units = u;
    }
    return format(fileSize / units.value, units);
  }

  /**
   * Parse file size from given string value and return it as {@link Long} instance. Given string <code>value</code>
   * should have a numeric part followed by units, separated by space. See {@link Units} for supported units.
   * <p>
   * Returns null if <code>value</code> argument is null or empty.
   * 
   * @param value formatted file size value.
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
      throw new ParseException("Invalid file size value. Missing units.", 0);
    }

    Units units = Units.forName(parts[1]);
    if(units == null) {
      throw new ParseException(String.format("Invalid file size value. Not recognized units |%s|.", parts[1]), 0);
    }

    Number fileSize = numberFormat.parse(parts[0]);
    return Math.round(fileSize.doubleValue() * units.value);
  }

  /**
   * Build file size representation for given numeric value and units.
   * 
   * @param fileSize file size numeric part value,
   * @param units file size units.
   * @return formated file size.
   */
  private String format(double fileSize, Units units)
  {
    StringBuilder builder = new StringBuilder();
    builder.append(numberFormat.format(fileSize));
    builder.append(' ');
    builder.append(units.name());
    return builder.toString();
  }

  /**
   * File size measurement units.
   * 
   * @author Iulian Rotaru
   */
  private enum Units
  {
    /** Byte, standard unit. */
    B(1),

    /** Kilobytes, 2^10 = 1024 Bytes. */
    KB(1024),

    /** Megabytes, 2^20 = 1048576 Bytes. */
    MB(1048576),

    /** Gigabytes, 2^30 = 1073741824 Bytes. */
    GB(1073741824),

    /** Terabytes, 2^40 = 1099511627776 Bytes. */
    TB(1099511627776L);

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

    private static Map<String, Units> UNITS = new HashMap<>();
    static {
      UNITS.put("B", B);
      UNITS.put("KB", KB);
      UNITS.put("MB", MB);
      UNITS.put("GB", GB);
      UNITS.put("TB", TB);
    }

    public static Units forName(String name)
    {
      return UNITS.get(name.toUpperCase());
    }
  }
}
