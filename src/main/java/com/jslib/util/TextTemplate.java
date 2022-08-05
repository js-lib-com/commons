package com.jslib.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import com.jslib.converter.Converter;
import com.jslib.converter.ConverterException;
import com.jslib.converter.ConverterRegistry;
import com.jslib.io.VariablesWriter;

/**
 * Text template with variables. Text template with standard <code>${...}</code> variables. This class uses
 * {@link VariablesWriter} for actual variables injection.
 * <p>
 * Using this class is trivial: create template instance, wrapping a given template string with variables, and uses
 * {@link #put(String, Object)} to replace variables with values. When complete uses {@link #toString()} to get string
 * with variables replaced.
 * 
 * <pre>
 * TextTemplate parameters = new TextTemplate(template);
 * parameters.put("fax-number", faxNumber);
 * parameters.put("email-address", emailAddress);
 * ...
 * // do something with parameters.toString()
 * </pre>
 * 
 * @author Iulian Rotaru
 */
public class TextTemplate
{
  /** Source template string with variables. */
  private String template;

  /** Variables map. Variables value objects are converted to string before storing in this map. */
  private Map<String, String> variables = new HashMap<String, String>();

  /**
   * Construct text template instance for given string with <code>${...}</code> variable place holders.
   * 
   * @param template string with variable place holders.
   */
  public TextTemplate(String template)
  {
    this.template = template;
  }

  /**
   * Set variables value. If variable name already exists its value is overridden. Convert variable value to string
   * before storing to variables map. Null is not accepted for either variable name or its value.
   * <p>
   * This method uses {@link Converter} to convert variable value to string. If there is no converter able to handle
   * given variable value type this method rise exception.
   * 
   * @param name variable name,
   * @param value variable value.
   * @throws IllegalArgumentException if variable name is null or empty or value is null.
   * @throws ConverterException if there is no converter able to handle given value type.
   */
  public void put(String name, Object value)
  {
    Params.notNullOrEmpty(name, "Variable name");
    Params.notNull(value, "Variable %s value", name);
    variables.put(name, ConverterRegistry.getConverter().asString(value));
  }

  /**
   * Inject variables into template and return resulting string. This method uses current status of the variables map.
   * If variables map is not completely initialized resulting string will still have <code>${...}</code> variable marks.
   */
  @Override
  public String toString()
  {
    VariablesWriter writer = new VariablesWriter(variables);
    try {
      Files.copy(new StringReader(template), writer);
    }
    catch(IOException e) {
      // ignore IO exception on strings
    }
    return writer.toString();
  }
}
