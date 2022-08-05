package com.jslib.format;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Numeric value format - 12.34. This class is a thin wrapper for Java {@link NumberFormat} with grouping.
 * 
 * @author Iulian Rotaru
 */
public final class Number implements Format
{
  /** Number formatter. */
  private NumberFormat numberFormat;

  /** Create number formatter with default locale settings. */
  public Number()
  {
    this(Locale.getDefault());
  }

  /**
   * Create number formatter with given locale settings.
   * 
   * @param locale locale settings.
   */
  public Number(Locale locale)
  {
    this.numberFormat = NumberFormat.getNumberInstance(locale);
    this.numberFormat.setGroupingUsed(true);
  }

  /**
   * Format numeric value using this formatter locale settings. Returns empty string if <code>value</code> argument is
   * null.
   * 
   * @param value numeric value.
   * @return numeric value represented as locale string, possible empty if <code>value</code> argument is null.
   * @throws IllegalArgumentException if <code>value</code> argument is not an instance of Number.
   */
  @Override
  public String format(Object value)
  {
    if(value == null) {
      return "";
    }
    return numberFormat.format(value);
  }

  /**
   * Parse string numeric value accordingly this formatter locale. Given numeric value argument should be represented
   * accordingly this formatter locale. This method returns a {@link Number} instance. Returns null if
   * <code>value</code> argument is null or empty.
   * 
   * @param value locale numeric value.
   * @return {@link Number} instance, possible null.
   * @throws ParseException if <code>value</code> argument does not represent a number accordingly this formatter locale.
   */
  @Override
  public Object parse(String value) throws ParseException
  {
    if(value == null || value.isEmpty()) {
      return null;
    }
    return numberFormat.parse(value);
  }
}
