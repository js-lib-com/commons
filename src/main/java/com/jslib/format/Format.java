package com.jslib.format;

import java.text.ParseException;

/**
 * Format or parse object value as/from string suitable for user interfaces. Formatted string should be proper for
 * display on user interfaces and may be subject to locale and time zone adjustments. How this is accomplished is
 * entirely on implementation consideration. Note that not all format classes should be both locale and time zone
 * sensitive.
 * <p>
 * A formatter deals with object value. An object value is an instance of a class that wrap a single value susceptible
 * to be represented as a single string - a sort of data atom, e.g. java.io.File or java.net.URL.
 * 
 * @author Iulian Rotaru
 */
public interface Format
{
  /**
   * Return a string representation, suitable for user interface display, of the given object value. Returns empty
   * string if <code>object</code> argument is null.
   * 
   * @param object object value to format.
   * @return object user interface representation or empty string if given <code>object</code> argument is null.
   */
  String format(Object object);

  /**
   * Create object instance and initialize it from given string representation.
   * 
   * @param value object string representation.
   * @return object instance or null if <code>value</code> argument is null or empty.
   * @throws ParseException if given string attribute cannot be parsed.
   */
  Object parse(String value) throws ParseException;
}
