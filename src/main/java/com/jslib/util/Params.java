package com.jslib.util;

import static java.lang.String.format;

import java.io.File;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * Invocation parameters (in)sanity tests. This utility class supplies convenient methods for invocation parameters
 * validation, see sample usage. All throws {@link IllegalArgumentException} if validation fails. Also all have a
 * <code>name</code> parameter used to format exception message; it is injected at message beginning exactly as
 * supplied.
 * <p>
 * In sample code throws illegal argument exception if <code>file</code> argument is null. Exception message is 'File
 * parameter is null.'
 * 
 * <pre>
 *  void method(File file . . . {
 *      Params.notNull(file, "File");
 *      . . .
 *  }
 * </pre>
 * <p>
 * This utility class allows for sub-classing. See {@link com.jslib.util} for utility sub-classing description.
 * 
 * @author Iulian Rotaru
 */
public class Params
{
  /** Prevent default constructor synthesis but allow sub-classing. */
  protected Params()
  {
  }

  /**
   * Throw exception if parameter is null. Name parameter can be formatted as accepted by
   * {@link String#format(String, Object...)}.
   * 
   * @param parameter invocation parameter to test,
   * @param name parameter name used on exception message,
   * @param args optional arguments if name is formatted.
   * @throws IllegalArgumentException if <code>parameter</code> is null.
   */
  public static void notNull(Object parameter, String name, Object... args) throws IllegalArgumentException
  {
    if(parameter == null) {
      throw new IllegalArgumentException(format(name, args) + " parameter is null.");
    }
  }

  /**
   * Throw exception if parameter is null character ('\0'). Name parameter can be formatted as accepted by
   * {@link String#format(String, Object...)}.
   * 
   * @param parameter invocation parameter to test,
   * @param name parameter name used on exception message,
   * @param args optional arguments if name is formatted.
   * @throws IllegalArgumentException if <code>parameter</code> is null character ('\0').
   */
  public static void notNull(Character parameter, String name, Object... args) throws IllegalArgumentException
  {
    if(parameter == null || parameter == '\0') {
      throw new IllegalArgumentException(format(name, args) + " parameter is null.");
    }
  }

  /**
   * Test if string parameter is strictly empty, that is, not null but empty.
   * 
   * @param parameter invocation string parameter,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> is null or not empty.
   */
  public static void empty(String parameter, String name)
  {
    if(parameter == null) {
      throw new IllegalArgumentException(name + " is null.");
    }
    if(!parameter.isEmpty()) {
      throw new IllegalArgumentException(name + " is not empty.");
    }
  }

  /**
   * Test if collection parameter is strictly empty, that is, not null but empty.
   * 
   * @param parameter invocation collection parameter,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> is null or not empty.
   */
  public static void empty(Collection<?> parameter, String name)
  {
    if(parameter == null) {
      throw new IllegalArgumentException(name + " is null empty.");
    }
    if(!parameter.isEmpty()) {
      throw new IllegalArgumentException(name + " is not empty.");
    }
  }

  /**
   * Test if string parameter is strict not empty. If parameter to test is null this test method does not rise
   * exception.
   * 
   * @param parameter invocation string parameter,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> is empty.
   */
  public static void notEmpty(String parameter, String name) throws IllegalArgumentException
  {
    if(parameter != null && parameter.isEmpty()) {
      throw new IllegalArgumentException(name + " is empty.");
    }
  }

  /**
   * Test if string parameter is not null or empty.
   * 
   * @param parameter invocation string parameter,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> is null or empty.
   */
  public static void notNullOrEmpty(String parameter, String name) throws IllegalArgumentException
  {
    if(parameter == null) {
      throw new IllegalArgumentException(name + " is null.");
    }
    if(parameter.isEmpty()) {
      throw new IllegalArgumentException(name + " is empty.");
    }
  }

  /**
   * Test if file parameter is not null and has not empty path.
   * 
   * @param parameter invocation file parameter,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> is null or its path is empty.
   */
  public static void notNullOrEmpty(File parameter, String name) throws IllegalArgumentException
  {
    if(parameter == null) {
      throw new IllegalArgumentException(name + " path is null.");
    }
    if(parameter.getPath().isEmpty()) {
      throw new IllegalArgumentException(name + " path is empty.");
    }
  }

  /**
   * Test if array parameter is not null or empty.
   * 
   * @param parameter invocation array parameter,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> array is null or empty.
   */
  public static void notNullOrEmpty(Object[] parameter, String name) throws IllegalArgumentException
  {
    if(parameter == null) {
      throw new IllegalArgumentException(name + " parameter is null.");
    }
    if(parameter.length == 0) {
      throw new IllegalArgumentException(name + " parameter is empty.");
    }
  }

  /**
   * Test if given collection size has a specified value.
   * 
   * @param parameter invocation collection parameters,
   * @param size requested size,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if collection has not requested size.
   */
  public static void size(Collection<?> parameter, int size, String name) throws IllegalArgumentException
  {
    if(parameter.size() != size) {
      throw new IllegalArgumentException(format("%s size is not %d.", name, size));
    }
  }

  /**
   * Test if given map size has a specified value.
   * 
   * @param parameter invocation map parameters,
   * @param size requested size,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if map has not requested size.
   */
  public static void size(Map<?, ?> parameter, int size, String name) throws IllegalArgumentException
  {
    if(parameter.size() != size) {
      throw new IllegalArgumentException(format("%s size is not %d.", name, size));
    }
  }

  /**
   * Check if type is not null and is an interface. Throws illegal argument is given type argument is not an interface.
   * 
   * @param type type to test,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if type is null or is not an interface.
   */
  public static void isInterface(Class<?> type, String name)
  {
    if(type == null) {
      throw new IllegalArgumentException(format("%s is null.", name));
    }
    if(!type.isInterface()) {
      throw new IllegalArgumentException(format("%s |%s| is not an interface.", name, type.getCanonicalName()));
    }
  }

  /**
   * Check if type is not null and is not an interface. Throws illegal argument is given type argument is an interface.
   * 
   * @param type type to test,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if type is null or is an interface.
   */
  public static void isNotInterface(Class<?> type, String name)
  {
    if(type == null) {
      throw new IllegalArgumentException(format("%s is null.", name));
    }
    if(type.isInterface()) {
      throw new IllegalArgumentException(format("%s |%s| is an interface.", name, type.getCanonicalName()));
    }
  }

  /**
   * Check if type is not null and it can be used for instances creation. Throws illegal argument is given type argument
   * is not usable for instances creation. This predicate consider a type instantiable if is not an interface or an
   * abstract class.
   * 
   * @param type type to test,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if type is null or is not usable for instances creation.
   */
  public static void isInstantiable(Class<?> type, String name)
  {
    if(type == null) {
      throw new IllegalArgumentException(format("%s is null.", name));
    }
    if(type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
      throw new IllegalArgumentException(format("%s |%s| is not instantiable.", name, type.getCanonicalName()));
    }
  }

  /**
   * Check if string parameter is numeric. This validator throws illegal argument if parameter value is not numeric. See
   * {@link Strings#isNumeric(String)} for <code>numeric</code> definition.
   * 
   * @param parameter invocation parameter value,
   * @param name parameter name.
   * @throws IllegalArgumentException if <code>parameter</code> is not numeric.
   */
  public static void isNumeric(String parameter, String name)
  {
    if(!Strings.isNumeric(parameter)) {
      throw new IllegalArgumentException(format("%s |%s| is not numeric.", name, parameter));
    }
  }

  /**
   * Check if file parameter is an existing ordinary file, not a directory. This validator throws illegal argument if
   * given file does not exist or is not an ordinary file.
   * 
   * @param parameter invocation file parameter,
   * @param name parameter name.
   * @throws IllegalArgumentException if <code>parameter</code> file is null or does not exist or is a directory.
   */
  public static void isFile(File parameter, String name) throws IllegalArgumentException
  {
    if(parameter == null) {
      throw new IllegalArgumentException(name + " is null.");
    }
    if(!parameter.exists() || parameter.isDirectory()) {
      throw new IllegalArgumentException(format("%s |%s| is missing or is a directory.", name, parameter.getAbsolutePath()));
    }
  }

  /**
   * Check if file parameter is an existing directory. This validator throws illegal argument if given file does not
   * exist or is not a directory.
   * 
   * @param parameter invocation file parameter,
   * @param name parameter name.
   * @throws IllegalArgumentException if <code>parameter</code> file is null or does not designate an existing
   *           directory.
   */
  public static void isDirectory(File parameter, String name) throws IllegalArgumentException
  {
    if(parameter == null) {
      throw new IllegalArgumentException(name + " is null.");
    }
    if(!parameter.isDirectory()) {
      throw new IllegalArgumentException(format("%s |%s| is missing or is not a directory.", name, parameter.getAbsolutePath()));
    }
  }

  /**
   * Check if file parameter belongs to requested directory. This predicate test if given <code>directory</code> is
   * ancestor of <code>parameter</code> file.
   * 
   * @param directory directory supposed to be ancestor of file parameter,
   * @param parameter invocation file parameter,
   * @param name parameter name.
   * @throws IllegalArgumentException if <code>parameter</code> file is outside <code>directory</code>.
   */
  public static void belongsTo(File directory, File parameter, String name) throws IllegalArgumentException
  {
    if(directory == null) {
      throw new IllegalArgumentException(name + " directory is null.");
    }
    if(parameter == null) {
      throw new IllegalArgumentException(name + " file is null.");
    }
    String dirPath = directory.getAbsolutePath();
    String filePath = parameter.getAbsolutePath();
    if(!filePath.startsWith(dirPath)) {
      throw new IllegalArgumentException(format("%s |%s| is outside directory |%s|.", name, filePath, dirPath));
    }
  }

  /**
   * Test if numeric parameter is in a given range. Parameter is considered in range if is greater or equal with lower
   * endpoint and smaller or equal higher endpoint.
   * 
   * @param parameter invocation numeric parameter,
   * @param lowEndpoint range lower endpoint,
   * @param highEndpoint range higher endpoint,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> is not in range.
   */
  public static void range(double parameter, double lowEndpoint, double highEndpoint, String name) throws IllegalArgumentException
  {
    if(lowEndpoint > parameter || parameter > highEndpoint) {
      throw new IllegalArgumentException(format("%s parameter is not in range [%d, %d].", name, lowEndpoint, highEndpoint));
    }
  }

  /**
   * Test if numeric parameter is zero.
   * 
   * @param parameter invocation numeric parameter,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> is not zero.
   */
  public static void zero(double parameter, String name) throws IllegalArgumentException
  {
    if(parameter != 0) {
      throw new IllegalArgumentException(name + " parameter is not zero.");
    }
  }

  /**
   * Test if numeric parameter is not zero.
   * 
   * @param parameter invocation numeric parameter,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> is zero.
   */
  public static void notZero(double parameter, String name) throws IllegalArgumentException
  {
    if(parameter == 0) {
      throw new IllegalArgumentException(name + " parameter is zero.");
    }
  }

  /**
   * Test if numeric parameter is positive or zero.
   * 
   * @param parameter invocation numeric parameter,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> is not positive or zero.
   */
  public static void positive(double parameter, String name) throws IllegalArgumentException
  {
    if(parameter < 0) {
      throw new IllegalArgumentException(name + " parameter is not positive.");
    }
  }

  /**
   * Test if numeric parameter is strict positive.
   * 
   * @param parameter invocation numeric parameter,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> is not strict positive.
   */
  public static void strictPositive(double parameter, String name) throws IllegalArgumentException
  {
    if(parameter <= 0) {
      throw new IllegalArgumentException(name + " parameter is not strict positive.");
    }
  }

  /**
   * Test if numeric parameter has expected value.
   * 
   * @param parameter invocation numeric parameter,
   * @param expected expected value,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> has not expected value.
   */
  public static void EQ(long parameter, long expected, String name) throws IllegalArgumentException
  {
    if(parameter != expected) {
      throw new IllegalArgumentException(format("%s is not %d.", name, expected));
    }
  }

  /**
   * Test if numeric parameter has expected value.
   * 
   * @param parameter invocation numeric parameter,
   * @param expected expected value,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> has not expected value.
   */
  public static void EQ(double parameter, double expected, String name) throws IllegalArgumentException
  {
    if(parameter != expected) {
      throw new IllegalArgumentException(format("%s is not %f.", name, expected));
    }
  }

  /**
   * Test if string parameter has expected value.
   * 
   * @param parameter invocation string parameter,
   * @param expected expected value,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> has not expected value.
   */
  public static void EQ(String parameter, String expected, String name) throws IllegalArgumentException
  {
    if(!parameter.equals(expected)) {
      throw new IllegalArgumentException(format("%s is not |%s|.", name, expected));
    }
  }

  /**
   * Test if character parameter has expected value.
   * 
   * @param parameter invocation character parameter,
   * @param expected expected value,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> has not expected value.
   */
  public static void EQ(char parameter, char expected, String name) throws IllegalArgumentException
  {
    if(parameter != expected) {
      throw new IllegalArgumentException(format("%s is not %c.", name, expected));
    }
  }

  /**
   * Test if numeric parameter is strictly greater than given threshold value.
   * 
   * @param parameter invocation numeric parameter,
   * @param value threshold value,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> is not greater than threshold value.
   */
  public static void GT(long parameter, long value, String name) throws IllegalArgumentException
  {
    if(parameter <= value) {
      throw new IllegalArgumentException(format("%s is not greater than %d.", name, value));
    }
  }

  /**
   * Test if numeric parameter is strictly greater than given threshold value.
   * 
   * @param parameter invocation numeric parameter,
   * @param value threshold value,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> is not greater than threshold value.
   */
  public static void GT(double parameter, double value, String name) throws IllegalArgumentException
  {
    if(parameter <= value) {
      throw new IllegalArgumentException(format("%s is not greater than %f.", name, value));
    }
  }

  /**
   * Test if character parameter is strictly greater than given character value.
   * 
   * @param parameter invocation character parameter,
   * @param expected expected character value,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> is not greater than threshold character.
   */
  public static void GT(char parameter, char expected, String name) throws IllegalArgumentException
  {
    if(parameter <= expected) {
      throw new IllegalArgumentException(format("%s is not greater than %c.", name, expected));
    }
  }

  /**
   * Test if numeric parameter is greater than or equal to given threshold value.
   * 
   * @param parameter invocation numeric parameter,
   * @param value threshold value,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> is not greater than or equal to threshold value.
   */
  public static void GTE(long parameter, long value, String name) throws IllegalArgumentException
  {
    if(parameter < value) {
      throw new IllegalArgumentException(format("%s is not greater than or equal %d.", name, value));
    }
  }

  /**
   * Test if numeric parameter is greater than or equal to given threshold value.
   * 
   * @param parameter invocation numeric parameter,
   * @param value threshold value,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> is not greater than or equal to threshold value.
   */
  public static void GTE(double parameter, double value, String name) throws IllegalArgumentException
  {
    if(parameter < value) {
      throw new IllegalArgumentException(format("%s is not greater than or equal %f.", name, value));
    }
  }

  /**
   * Test if character parameter is greater than or equal to given character value.
   * 
   * @param parameter invocation numeric parameter,
   * @param value character value,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> is not greater than or equal to character value.
   */
  public static void GTE(char parameter, char value, String name) throws IllegalArgumentException
  {
    if(parameter < value) {
      throw new IllegalArgumentException(format("%s is not greater than or equal %c.", name, value));
    }
  }

  /**
   * Test if numeric parameter is strictly less than given threshold value.
   * 
   * @param parameter invocation numeric parameter,
   * @param value threshold value,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> is not less than threshold value.
   */
  public static void LT(long parameter, long value, String name) throws IllegalArgumentException
  {
    if(parameter >= value) {
      throw new IllegalArgumentException(format("%s is not less than %d.", name, value));
    }
  }

  /**
   * Test if numeric parameter is strictly less than given threshold value.
   * 
   * @param parameter invocation numeric parameter,
   * @param value threshold value,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> is not less than threshold value.
   */
  public static void LT(double parameter, double value, String name) throws IllegalArgumentException
  {
    if(parameter >= value) {
      throw new IllegalArgumentException(format("%s is not less than %f.", name, value));
    }
  }

  /**
   * Test if character parameter is strictly less than given character value.
   * 
   * @param parameter invocation character parameter,
   * @param value threshold value,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> is not less than character value.
   */
  public static void LT(char parameter, char value, String name) throws IllegalArgumentException
  {
    if(parameter >= value) {
      throw new IllegalArgumentException(format("%s is not less than %c.", name, value));
    }
  }

  /**
   * Test if numeric parameter is less than or equal to given threshold value.
   * 
   * @param parameter invocation numeric parameter,
   * @param value threshold value,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> is not less than or equal to threshold value.
   */
  public static void LTE(long parameter, long value, String name) throws IllegalArgumentException
  {
    if(parameter > value) {
      throw new IllegalArgumentException(format("%s is not less than or equal %d.", name, value));
    }
  }

  /**
   * Test if numeric parameter is less than or equal to given threshold value.
   * 
   * @param parameter invocation numeric parameter,
   * @param value threshold value,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> is not less than or equal to threshold value.
   */
  public static void LTE(double parameter, double value, String name) throws IllegalArgumentException
  {
    if(parameter > value) {
      throw new IllegalArgumentException(format("%s is not less than or equal %f.", name, value));
    }
  }

  /**
   * Test if character parameter is less than or equal to given threshold value.
   * 
   * @param parameter invocation numeric parameter,
   * @param value character value,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if <code>parameter</code> is not less than or equal to character value.
   */
  public static void LTE(char parameter, char value, String name) throws IllegalArgumentException
  {
    if(parameter > value) {
      throw new IllegalArgumentException(format("%s is not less than or equal %c.", name, value));
    }
  }

  /**
   * Test if given boolean condition is true and throw exception if not. Exception message is that supplied by
   * <code>message</code> parameter. Message can be formatted as supported by {@link String#format(String, Object...)}.
   * 
   * @param condition boolean condition to test,
   * @param message exception message,
   * @param args optional arguments if message is formatted.
   * @throws IllegalArgumentException if given condition is false.
   */
  public static void isTrue(boolean condition, String message, Object... args) throws IllegalArgumentException
  {
    if(!condition) {
      throw new IllegalArgumentException(format(message, args));
    }
  }

  /**
   * Test if given boolean condition is false and throw exception if not. Exception message is that supplied by
   * <code>message</code> parameter. Message can be formatted as supported by {@link String#format(String, Object...)}.
   * 
   * @param condition boolean condition to test,
   * @param message exception message,
   * @param args optional arguments if message is formatted.
   * @throws IllegalArgumentException if given condition is true.
   */
  public static void isFalse(boolean condition, String message, Object... args) throws IllegalArgumentException
  {
    if(condition) {
      throw new IllegalArgumentException(format(message, args));
    }
  }

  /**
   * Test if parameter is of requested type and throw exception if not.
   * 
   * @param parameter invocation parameter,
   * @param typeToMatch type to match,
   * @param name the name of invocation parameter.
   * @throws IllegalArgumentException if parameter is not of requested type.
   */
  public static void isKindOf(Type parameter, Type typeToMatch, String name)
  {
    if(!Types.isKindOf(parameter, typeToMatch)) {
      throw new IllegalArgumentException(format("%s is not %s.", name, typeToMatch));
    }
  }
}
