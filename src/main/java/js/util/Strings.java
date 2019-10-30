package js.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import js.converter.Converter;
import js.converter.ConverterRegistry;
import js.io.VariablesWriter;
import js.lang.BugError;
import js.lang.Handler;
import js.lang.Pair;
import js.log.Log;
import js.log.LogFactory;

/**
 * Strings manipulation utility. This utility class allows for sub-classing. See {@link js.util} for utility
 * sub-classing description.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public class Strings
{
  /** Class logger. */
  private static final Log log = LogFactory.getLog(Strings.class);

  /** Prevent default constructor synthesis but allow sub-classing. */
  protected Strings()
  {
  }

  /**
   * Convert word to camel case. This method convert a single word to English convention title case, that is, first
   * letter to upper case and the rest to lower. Note that this method may have not reasonable results if more words,
   * perhaps space separated, are given; it simply convert all but the first to lower case. Use
   * {@link #toMemberName(String)} or {@link #toTitleCase(String)} if need to convert more words.
   * <p>
   * Returns null if word argument is null and empty if empty.
   * 
   * @param word word to convert.
   * @return given word as title case, null or empty string.
   */
  public static String toCamelCase(String word)
  {
    if(word == null) {
      return null;
    }
    if(word.isEmpty()) {
      return "";
    }
    return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
  }

  /**
   * Convert words separated by dash, underscore, space, slash and backslash to title case. Title case follows English
   * convention: uses space for separator and every word begin with upper case. Returns null or empty string if given
   * <code>string</code> parameter is null, respective empty.
   * 
   * @param string string containing words to convert.
   * @return <code>string</code> converted to title case, null or empty.
   */
  public static String toTitleCase(String string)
  {
    if(string == null) {
      return null;
    }
    if(string.isEmpty()) {
      return "";
    }
    List<String> words = Strings.split(string, '-', '_', ' ', '/', '\\');
    StringBuilder title = new StringBuilder(toCamelCase(words.get(0)));
    for(int i = 1; i < words.size(); ++i) {
      title.append(' ');
      title.append(toCamelCase(words.get(i)));
    }
    return title.toString();
  }

  /**
   * Convert dashed name to Java member name. A dashed name contains only lower case and words are separated by dash
   * ('-'). By convention dashed names are used by HTML and CSS.
   * <p>
   * Note that first character of returned member name is lower case, e.g. <code>this-is-a-string</code> is converted to
   * <code>thisIsAString</code>.
   * <p>
   * Returns null if words argument is null and empty if empty.
   * 
   * @param dashedName dashed name to convert.
   * @return camel case member name.
   */
  public static String dashedToMemberName(String dashedName)
  {
    if(dashedName == null) {
      return null;
    }
    if(dashedName.isEmpty()) {
      return "";
    }

    String[] words = dashedName.split("-");
    StringBuilder sb = new StringBuilder();

    boolean first = true;
    for(String word : words) {
      if(word.isEmpty()) {
        continue;
      }
      if(first) {
        first = false;
        sb.append(word);
        continue;
      }

      sb.append(Character.toUpperCase(word.charAt(0)));
      sb.append(word.substring(1));
    }
    return sb.toString();
  }

  @Deprecated
  public static String toMemberName(String dashedName)
  {
    return dashedToMemberName(dashedName);
  }

  /**
   * Convert enumeration constant name to Java member name. By convention enumeration constant names are all upper case
   * and separated by underscore ('_'). This method relies on this convention. Providing enumeration constant names not
   * obeying this convention will generate not predictable results.
   * <p>
   * Note that first character of returned member name is lower case, e.g. <code>POSTAL_ADDRESS</code> is converted to
   * <code>postalAddress</code>. If given enumeration constant name has a single word return it as lower case, e.g.
   * <code>NAME</code> is converted to <code>name</code>.
   * <p>
   * Returns null if words argument is null and empty if empty.
   * 
   * @param enumName enumeration name to convert to member name.
   * @return camel case member name.
   */
  public static String enumToMemberName(String enumName)
  {
    if(enumName == null) {
      return null;
    }

    String[] words = enumName.split("_");
    StringBuilder sb = new StringBuilder();

    boolean first = true;
    for(String word : words) {
      if(word.isEmpty()) {
        continue;
      }
      if(first) {
        first = false;
        sb.append(word.toLowerCase());
        continue;
      }

      sb.append(Character.toUpperCase(word.charAt(0)));
      sb.append(word.substring(1).toLowerCase());
    }
    return sb.toString();
  }

  /**
   * Typed variant of {@link #enumToMemberName(String)}.
   * 
   * @param enumName enumeration constant.
   * @return camel case member name.
   */
  public static String enumToMemberName(Enum<?> enumName)
  {
    if(enumName == null) {
      return null;
    }
    return enumToMemberName(enumName.name());
  }

  /**
   * Convert Java member name to list of lower case words separated by dash, that is, HTML and CSS name convention.
   * <p>
   * Returns null if member name argument is null and empty if empty.
   * 
   * @param memberName Java style, camel case member name.
   * @return dashed string.
   */
  public static String memberToDashCase(String memberName)
  {
    if(memberName == null) {
      return null;
    }
    final int length = memberName.length();
    StringBuilder builder = new StringBuilder(length);

    for(int i = 0; i < length; i++) {
      char c = memberName.charAt(i);
      if(Character.isLowerCase(c)) {
        builder.append(c);
        continue;
      }

      c = Character.toLowerCase(c);
      if(i > 0) {
        builder.append('-');
      }
      builder.append(c);
    }
    return builder.toString();
  }

  @Deprecated
  public static String toDashCase(String memberName)
  {
    return memberToDashCase(memberName);
  }

  /** ISO8601 date format for {@link #toISO(Date)}. */
  private static DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  static {
    ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  /**
   * Format date to ISO8601. Date is normalized to UTC.
   * 
   * @param date date to format.
   * @return date as ISO8601 string.
   */
  public static String toISO(Date date)
  {
    return ISO_DATE_FORMAT.format(date);
  }

  /**
   * Concatenates variable number of objects converted to string, separated by colon. Return empty string if this method
   * is invoked with no arguments at all. Note that this method uses private helper {@link #toString(Object)} to
   * actually convert every given object to string.
   * 
   * @param objects variable number of objects.
   * @return objects string representation.
   */
  public static String toString(Object... objects)
  {
    if(objects.length == 0) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    sb.append(toString(objects[0]));
    for(int i = 1; i < objects.length; i++) {
      sb.append(":");
      sb.append(toString(objects[i]));
    }
    return sb.toString();
  }

  /**
   * Convert object to string representation. This method applies next heuristic to convert given object to string
   * representation:
   * <ul>
   * <li>if <code>object</code> is null returns "null",
   * <li>if it is a {@link String} returns it as it is,
   * <li>if is an instance of {@link Class} returns {@link Class#getName()},
   * <li>if is an instance of {@link Throwable} returns causes trace - limited to 8, from cause to cause; if no cause at
   * all returns {@link Throwable#toString()},
   * <li>if none of above returns {@link Object#toString()}.
   * </ul>
   * 
   * @param object object to stringify.
   * @return object string representation.
   */
  private static String toString(Object object)
  {
    if(object == null) {
      return null;
    }

    if(object instanceof String) {
      return (String)object;
    }

    if(object instanceof Class) {
      return ((Class<?>)object).getName();
    }

    if(!(object instanceof Throwable)) {
      return object.toString();
    }

    Throwable t = (Throwable)object;
    if(t.getCause() == null) {
      return t.toString();
    }

    int level = 0;
    StringBuilder sb = new StringBuilder();
    for(;;) {
      sb.append(t.getClass().getName());
      sb.append(":");
      sb.append(" ");
      if(++level == 8) {
        sb.append("...");
        break;
      }
      if(t.getCause() == null) {
        String s = t.getMessage();
        if(s == null) {
          t.getClass().getName();
        }
        sb.append(s);
        break;
      }
      t = t.getCause();
    }
    return sb.toString();
  }

  /** States enumeration for plain text parser automata. */
  private static enum PlainTextState
  {
    TEXT, START_TAG, END_TAG
  }

  /**
   * Convenient variant for {@link #toPlainText(String, int, int)} when using entire source text.
   * 
   * @param text HTML formatted text.
   * @return newly created plain text.
   */
  @Deprecated
  public static String toPlainText(String text)
  {
    return toPlainText(text, 0, Integer.MAX_VALUE);
  }

  /**
   * Convert HTML formatted into plain text. This method parses source text and detect HTML tags. Current implementation
   * handle only &lt;p&gt; tags replacing them with line break.
   * <p>
   * <b>Implementation note</b>: experimental implementation. This implementation is work in progress and is marked as
   * deprecated to warn developer about logic evolution. Final parser should handle &lt;br&gt; to line break, &lt;p&gt;
   * to double line breaks, &lt;q&gt; to simple quotation mark, &lt;ul&gt; to new lines beginning with tab and dash,
   * &lt;ol&gt; to new lines beginning with tab and ordinal and all &lt;h&gt; tags to triple line breaks.
   * 
   * @param text HTML formatted source text,
   * @param offset source text offset,
   * @param capacity generated plain text maximum allowed length.
   * @return newly created plain text.
   */
  @Deprecated
  public static String toPlainText(String text, int offset, int capacity)
  {
    PlainTextState state = PlainTextState.TEXT;

    StringBuilder plainText = new StringBuilder();
    for(int i = offset; i < text.length() && plainText.length() <= capacity; ++i) {
      int c = text.charAt(i);

      switch(state) {
      case TEXT:
        if(c == '<') {
          state = PlainTextState.START_TAG;
          break;
        }
        plainText.append((char)c);
        break;

      case START_TAG:
        if(c == '/') {
          state = PlainTextState.END_TAG;
          break;
        }
        if(c == '>') {
          state = PlainTextState.TEXT;
        }
        break;

      case END_TAG:
        if(c == 'p') {
          plainText.append("\r\n");
        }
        if(c == '>') {
          state = PlainTextState.TEXT;
        }
        break;
      }
    }
    return plainText.toString();
  }

  /**
   * Get Java accessor for a given member name. Returns the given <code>memberName</code> prefixed by
   * <code>prefix</code>. If <code>memberName</code> is dashed case, that is, contains dash character convert it to
   * camel case. For example, getter for <em>email-addresses</em> is <em>getEmailAddresses</em> and for <em>picture</em>
   * is <em>getPicture</em>.
   * <p>
   * Accessor <code>prefix</code> is inserted before method name and for flexibility it can be anything. Anyway, ususal
   * values are <code>get</code>, <code>set</code> and <code>is</code>. It is caller responsibility to supply the right
   * prefix.
   * 
   * @param prefix accessor prefix,
   * @param memberName member name.
   * @return member accessor name.
   * @throws IllegalArgumentException if any given parameter is null or empty.
   */
  public static String getMethodAccessor(String prefix, String memberName) throws IllegalArgumentException
  {
    Params.notNullOrEmpty(prefix, "Prefix");
    Params.notNullOrEmpty(memberName, "Member name");

    StringBuilder builder = new StringBuilder();
    builder.append(prefix);

    String[] parts = memberName.split("-+");
    for(int i = 0; i < parts.length; i++) {
      if(parts.length > 0) {
        builder.append(Character.toUpperCase(parts[i].charAt(0)));
        builder.append(parts[i].substring(1));
      }
    }
    return builder.toString();
  }

  /**
   * Pattern for qualified class name. This pattern matches class names but without member; inner classes are accepted.
   * 
   * <pre>
   *    qualifiedClassName := packageName ("." className)+
   *    packageName := packageName "." packagePart
   *    packagePart := lowerChar+
   *    className := upperChar char*
   * </pre>
   */
  private static final Pattern QUALIFIED_CLASS_NAME = Pattern.compile("^([a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)*(?:\\.[A-Z][a-zA-Z0-9]*)+)$");

  /**
   * Predicate to test if name is qualified name.
   * 
   * @param name name to test.
   * @return true if given name is a qualified class name.
   */
  public static boolean isQualifiedClassName(String name)
  {
    return QUALIFIED_CLASS_NAME.matcher(name).matches();
  }

  /**
   * Pattern constant used for Java member name validation.
   */
  private static final Pattern MEMBER_NAME_PATTERN = Pattern.compile("^[a-z][a-z0-9_]*(?:[A-Z][a-z0-9_]*)*$");

  /**
   * Test if given name is Java member like name. If <code>name</code> is null return false.
   * 
   * @param name name to test.
   * @return true if given name is Java member like name.
   */
  public static boolean isMemberName(String name)
  {
    if(name == null) {
      return false;
    }
    Matcher matcher = MEMBER_NAME_PATTERN.matcher(name);
    return matcher.find();
  }

  /**
   * Concatenates a variable number of objects, as strings. For every given argument, convert it to string using
   * {@link Object#toString()} overload and append to concatenated result. If a given argument happens to be null, skip
   * it. Return empty string if this method is invoked with no arguments at all.
   * 
   * @param objects variable number of objects.
   * @return concatenated objects.
   */
  public static String concat(Object... objects)
  {
    StringBuilder sb = new StringBuilder();
    for(Object object : objects) {
      if(object != null) {
        sb.append(object);
      }
    }
    return sb.toString();
  }

  /**
   * Join array of objects, converted to string, using space as separator. Returns null if given objects array is null
   * and empty if empty. Null objects or empty strings from given <code>objects</code> parameter are ignored.
   * 
   * @param objects array of objects to join.
   * @return joined string.
   */
  public static String join(Object[] objects)
  {
    return join(objects, " ");
  }

  /**
   * Join array of objects, converted to string, using specified separator. Returns null if given objects array is null
   * and empty if empty. Null objects or empty strings from given <code>objects</code> parameter are ignored.
   * 
   * @param objects array of objects to join,
   * @param separator character used as separator.
   * @return joined string.
   */
  public static String join(Object[] objects, char separator)
  {
    return join(objects, Character.toString(separator));
  }

  /**
   * Join array of objects, converted to string, using specified specified separator. Returns null if given objects
   * array is null and empty if empty. If separator is null uses space string instead, like invoking
   * {@link Strings#join(Iterable)}. Null objects or empty strings from given <code>objects</code> parameter are
   * ignored.
   * 
   * @param objects array of objects to join,
   * @param separator string used as separator.
   * @return joined string.
   */
  public static String join(Object[] objects, String separator)
  {
    return objects != null ? join(Arrays.asList(objects), separator) : null;
  }

  /**
   * Join collection of objects, converted to string, using space as separator. Returns null if given objects array is
   * null and empty if empty.
   * 
   * @param objects collection of objects to join.
   * @return joined string.
   */
  public static String join(Iterable<?> objects)
  {
    return join(objects, " ");
  }

  /**
   * Join collection of objects, converted to string, using specified char separator. Returns null if given objects
   * array is null and empty if empty.
   * 
   * @param objects collection of objects to join,
   * @param separator character used as separator.
   * @return joined string.
   */
  public static String join(Iterable<?> objects, char separator)
  {
    return join(objects, Character.toString(separator));
  }

  /**
   * Join collection of objects, converted to string, using specified string separator.Concatenates strings from
   * collection converted to string but take care to avoid null items. Uses given separator between strings. Returns
   * null if given objects array is null and empty if empty. If separator is null uses space string instead, like
   * invoking {@link Strings#join(Iterable)}. Null objects or empty strings from given <code>objects</code> parameter
   * are ignored.
   * 
   * @param objects collection of objects to join,
   * @param separator string used as separator.
   * @return joined string.
   */
  public static String join(Iterable<?> objects, String separator)
  {
    if(objects == null) {
      return null;
    }
    if(separator == null) {
      separator = " ";
    }

    StringBuilder builder = new StringBuilder();
    boolean first = true;
    for(Object object : objects) {
      if(object == null) {
        continue;
      }
      String value = object instanceof String ? (String)object : object.toString();
      if(value.isEmpty()) {
        continue;
      }
      if(first) {
        first = false;
      }
      else {
        builder.append(separator);
      }
      builder.append(value);
    }
    return builder.toString();
  }

  /**
   * Splits string into not empty, trimmed items, using specified separator(s) or space if no separator provided. Please
   * note that returned list does not contain empty items and that all items are trimmed using standard
   * {@link String#trim()}.
   * <p>
   * Returns null if string argument is null and empty list if is empty. This method supports a variable number of
   * separator characters - as accepted by {@link #isSeparator(char, char...)} predicate; if none given uses space.
   * 
   * @param string source string,
   * @param separators variable number of characters used as separators.
   * @return strings list, possible empty.
   */
  public static List<String> split(String string, char... separators)
  {
    if(string == null) {
      return null;
    }

    class ItemsList
    {
      List<String> list = new ArrayList<String>();

      void add(StringBuilder wordBuilder)
      {
        String value = wordBuilder.toString().trim();
        if(!value.isEmpty()) {
          list.add(value);
        }
      }
    }

    ItemsList itemsList = new ItemsList();
    StringBuilder itemBuilder = new StringBuilder();

    for(int i = 0; i < string.length(); ++i) {
      char c = string.charAt(i);
      // append to on building item all characters that are not separators
      if(!isSeparator(c, separators)) {
        itemBuilder.append(c);
      }
      // if separator found add item to list and reset builder
      if(itemBuilder.length() > 0) {
        if(isSeparator(c, separators)) {
          itemsList.add(itemBuilder);
          itemBuilder.setLength(0);
        }
      }
    }

    itemsList.add(itemBuilder);
    return itemsList.list;
  }

  /** Default separators used when {@link #isSeparator(char, char...)} separators list is empty. */
  private static final char[] DEFAULT_SEPARATORS = new char[]
  {
      ' '
  };

  /**
   * Test if <code>character</code> is a separator as defined by given <code>separators</code> list. If separator list
   * is empty consider used {@link #DEFAULT_SEPARATORS}. This predicate is designed specifically for
   * {@link #split(String, char...)} operation.
   *
   * @param character character to test if usable as separator,
   * @param separators variable number of separator characters.
   * @return true if <code>character</code> is a separator, as defined by this method.
   */
  private static boolean isSeparator(char character, char... separators)
  {
    if(separators.length == 0) {
      separators = DEFAULT_SEPARATORS;
    }
    for(int i = 0; i < separators.length; ++i) {
      if(character == separators[i]) {
        return true;
      }
    }
    return false;
  }

  /**
   * Splits string using specified string separator and returns trimmed values. Returns null if any argument is null and
   * empty list if any argument is empty.
   * 
   * @param string source string,
   * @param separator string used as separator.
   * @return strings list, possible null or empty.
   */
  public static List<String> split(String string, String separator)
  {
    if(string == null || separator == null) {
      return null;
    }
    final int separatorLength = separator.length();
    final List<String> list = new ArrayList<String>();
    if(separator.isEmpty()) {
      return list;
    }

    int fromIndex = 0;
    for(;;) {
      int endIndex = string.indexOf(separator, fromIndex);
      if(endIndex == -1) {
        break;
      }
      if(fromIndex < endIndex) {
        list.add(string.substring(fromIndex, endIndex).trim());
      }
      fromIndex = endIndex + separatorLength;
    }
    if(fromIndex < string.length()) {
      list.add(string.substring(fromIndex).trim());
    }
    return list;
  }

  /**
   * Splits string using specified string separator and returns values converted to requested type. Returns null if any
   * argument is null and empty list if any argument is empty.
   * 
   * @param string source string,
   * @param separator string used as separator,
   * @param type requested list item type.
   * @param <T> generic type to convert string parts.
   * @return typed list, possible empty.
   */
  public static <T> List<T> split(String string, String separator, Class<T> type)
  {
    if(string == null || separator == null) {
      return null;
    }
    final Converter converter = ConverterRegistry.getConverter();
    final int separatorLength = separator.length();
    final List<T> list = new ArrayList<>();
    if(separator.isEmpty()) {
      return list;
    }

    int fromIndex = 0;
    for(;;) {
      int endIndex = string.indexOf(separator, fromIndex);
      if(endIndex == -1) {
        break;
      }
      if(fromIndex < endIndex) {
        list.add(converter.asObject(string.substring(fromIndex, endIndex).trim(), type));
      }
      fromIndex = endIndex + separatorLength;
    }
    if(fromIndex < string.length()) {
      list.add(converter.asObject(string.substring(fromIndex).trim(), type));
    }
    return list;
  }

  /**
   * Split string using given pairs separator and every pair using pair component separator. Return the list of pair
   * instances, possible empty if string value is empty. Returns null if string value is null.
   * <p>
   * Returned pair components are trimmed for spaces. This means that spaces around separators are eliminated; this is
   * true for both pairs and pair components separators. For example, " john : doe ; jane : doe ; " will return
   * Pair("john", "doe"), Pair("jane", "doe").
   * <p>
   * Trailing pairs separator is optional.
   * 
   * @param string string value,
   * @param pairsSeparator pairs separator,
   * @param componentsSeparator pair components separator.
   * @return list of pairs, possible empty.
   * @throws BugError if a pair is not valid, that is, pair components separator is missing.
   */
  public static List<Pair> splitPairs(String string, char pairsSeparator, char componentsSeparator)
  {
    if(string == null) {
      return null;
    }
    string = string.trim();

    final int length = string.length();
    final List<Pair> list = new ArrayList<Pair>();
    int beginIndex = 0;
    int endIndex = 0;

    while(endIndex < length) {
      if(string.charAt(endIndex) == pairsSeparator) {
        if(endIndex > beginIndex) {
          list.add(pair(string.substring(beginIndex, endIndex), componentsSeparator));
        }
        beginIndex = ++endIndex;
      }
      ++endIndex;
    }
    if(beginIndex < length) {
      list.add(pair(string.substring(beginIndex), componentsSeparator));
    }
    return list;
  }

  /**
   * Split string value using given separator and return initialized pair instance. Pair values are trimmed for spaces
   * so that this method eliminates spaces around separator and around string value. For example, " john : doe " will
   * return Pair("john", "doe").
   * 
   * @param string string value,
   * @param separator pair components separator.
   * @return newly create pair instance.
   * @throws BugError if separator not found.
   */
  private static Pair pair(String string, char separator)
  {
    int separatorIndex = string.indexOf(separator);
    if(separatorIndex == -1) {
      throw new BugError("Missing pair separator. Cannot initialize pair instance.");
    }
    final String first = string.substring(0, separatorIndex).trim();
    final String second = string.substring(separatorIndex + 1).trim();
    return new Pair(first, second);
  }

  /** Current locale decimal separator. */
  private static final char DECIMAL_SEPARATOR = DecimalFormatSymbols.getInstance().getDecimalSeparator();

  /** Current locale grouping separator. */
  private static final char GROUPING_SEPARATOR = DecimalFormatSymbols.getInstance().getGroupingSeparator();

  /**
   * Predicate to test if character is numeric value suffix. Numeric suffix are Java standard: L, D and F and lower
   * case.
   * 
   * @param c character to test.
   * @return true if character is numeric value suffix.
   */
  private static boolean isNumericSuffix(char c)
  {
    if(c == 'L') return true;
    if(c == 'F') return true;
    if(c == 'D') return true;
    if(c == 'l') return true;
    if(c == 'f') return true;
    if(c == 'd') return true;
    return false;
  }

  /**
   * State machine for numeric value parser.
   * 
   * @author Iulian Rotaru
   */
  private static enum State
  {
    NONE, SIGN, INTEGER, FRACTIONAL, EXPONENT_SIGN, EXPONENT, END
  };

  /**
   * Test if string is a numeric value. Returns false if string argument is null or empty.
   * 
   * @param string string to test.
   * @return true if given string denote a number.
   */
  public static boolean isNumeric(String string)
  {
    if(string == null || string.isEmpty()) return false;

    State state = State.SIGN;
    for(int i = 0; i < string.length(); ++i) {
      char c = string.charAt(i);

      switch(state) {
      case SIGN:
        if(c == '-' || c == '+') {
          state = State.INTEGER;
          break;
        }
        if(c == DECIMAL_SEPARATOR) {
          state = State.FRACTIONAL;
          break;
        }
        if(Character.isDigit(c)) {
          state = State.INTEGER;
          break;
        }
        return false;

      case INTEGER:
        if(Character.isDigit(c)) {
          break;
        }
        if(c == GROUPING_SEPARATOR) {
          break;
        }
        if(c == DECIMAL_SEPARATOR) {
          state = State.FRACTIONAL;
          break;
        }
        if(c == 'e' || c == 'E') {
          state = State.EXPONENT_SIGN;
          break;
        }
        if(isNumericSuffix(c)) {
          state = State.END;
          break;
        }
        return false;

      case FRACTIONAL:
        if(c == 'e' || c == 'E') {
          state = State.EXPONENT_SIGN;
          break;
        }
        if(isNumericSuffix(c)) {
          state = State.END;
          break;
        }
        if(Character.isDigit(c)) {
          break;
        }
        return false;

      case EXPONENT_SIGN:
        if(c == '-' || c == '+') {
          state = State.EXPONENT;
          break;
        }
        if(Character.isDigit(c)) {
          break;
        }
        return false;

      case EXPONENT:
        if(isNumericSuffix(c)) {
          state = State.END;
          break;
        }
        if(Character.isDigit(c)) {
          break;
        }
        return false;

      case END:
        return false;

      default:
        throw new IllegalStateException();
      }

    }
    return true;
  }

  /**
   * Test if string is an integer numeric value. Returns false if string is null or empty.
   * 
   * @param string string to test.
   * @return true if string is an integer.
   */
  public static boolean isInteger(String string)
  {
    if(string == null || string.isEmpty()) return false;
    int startIndex = string.charAt(0) == '-' ? 1 : string.charAt(0) == '+' ? 1 : 0;
    for(int i = startIndex, l = string.length(); i < l; i++) {
      if(string.charAt(i) == GROUPING_SEPARATOR) continue;
      if(!Character.isDigit(string.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Escape text for reserved XML characters. Replace quotes, apostrophe, ampersand, left and right angular brackets
   * with entities. Return the newly created, escaped string; if text argument is null or empty returns an empty string.
   * 
   * @param text string to escape.
   * @return the newly created string.
   */
  public static String escapeXML(String text)
  {
    StringWriter writer = new StringWriter();
    try {
      escapeXML(text, writer);
    }
    catch(IOException e) {
      // there is no reason for IO exception on a string writer
      throw new BugError("IO failure while attempting to write to string.");
    }
    return writer.toString();
  }

  /**
   * Escape text for reserved XML characters to a specified writer. This method has the same logic as
   * {@link #escapeXML(String)} but result is serialized on the given writer. If <code>text</code> parameters is null
   * this method does nothing.
   * 
   * @param text string to escape,
   * @param writer writer to serialize resulted escaped string.
   * @throws IOException if writer operation fails.
   */
  public static void escapeXML(String text, Writer writer) throws IOException
  {
    if(text == null) {
      return;
    }
    for(int i = 0, l = text.length(); i < l; ++i) {
      char c = text.charAt(i);
      switch(c) {
      case '"':
        writer.write("&quot;");
        break;
      case '\'':
        writer.write("&apos;");
        break;
      case '&':
        writer.write("&amp;");
        break;
      case '<':
        writer.write("&lt;");
        break;
      case '>':
        writer.write("&gt;");
        break;
      default:
        writer.write(c);
      }
    }
  }

  /**
   * Pattern for regular expression reserved characters.
   */
  private static final String REGEXP_PATTERN = "([\\/|\\.|\\*|\\?|\\||\\(|\\)|\\[|\\]|\\{|\\}|\\\\|\\^|\\$])";

  /**
   * First argument for regular expression string replacement.
   */
  private static final String REPLACE_ARG_REX = "\\\\$1";

  /**
   * Regular expression for dot.
   */
  private static final String DOT_REX = "\\.";

  /**
   * Regular expression for any character.
   */
  private static final String ANY_REX = ".+";

  /**
   * Escape string for regular expression reserved characters. Return null if string argument is null and empty if
   * empty.
   * 
   * @param string regular expression to escape.
   * @return newly created, escaped string.
   */
  public static String escapeRegExp(String string)
  {
    if(string == null) {
      return null;
    }
    return string.replaceAll(REGEXP_PATTERN, REPLACE_ARG_REX);
  }

  /**
   * Escape files pattern for usage with Java regular expressions. Replace dot with escaped dot and asterisk with match
   * at least one character so that <code>*.html</code> becomes <code>.+\.html</code>. Return null if files pattern
   * argument is null and empty if empty.
   * 
   * @param filesPattern files pattern to prepare.
   * @return files pattern ready to use by regular expression.
   */
  public static String escapeFilesPattern(String filesPattern)
  {
    if(filesPattern == null) {
      return null;
    }
    return filesPattern.replace(".", DOT_REX).replace("*", ANY_REX);
  }

  /**
   * Get last string sequence following given character. If separator character is missing from the source string
   * returns entire string. Return null if string argument is null and empty if empty.
   * 
   * @param string source string,
   * @param separator character used as separator.
   * @return last string sequence.
   */
  public static String last(String string, char separator)
  {
    if(string == null) {
      return null;
    }
    if(string.isEmpty()) {
      return "";
    }
    return string.substring(string.lastIndexOf(separator) + 1);
  }

  /**
   * Return the first sentence of a string, where a sentence ends with a sentence separator followed be white space.
   * Returns null if <code>text</code> parameter is null. Current version recognizes next sentence separators: dot (.),
   * question mark (?), exclamation mark (!) and semicolon (;).
   * 
   * @param text string to scan for first sentence.
   * @return first sentence from text.
   */
  public static String firstSentence(String text)
  {
    if(text == null) {
      return null;
    }
    int length = text.length();
    boolean sentenceSeparator = false;

    for(int i = 0; i < length; i++) {
      switch(text.charAt(i)) {
      case '.':
      case '!':
      case '?':
      case ';':
        sentenceSeparator = true;
        break;

      case ' ':
      case '\t':
      case '\n':
      case '\r':
      case '\f':
        if(sentenceSeparator) return text.substring(0, i);
        break;

      default:
        sentenceSeparator = false;
      }
    }
    return text;
  }

  /**
   * Escaped white spaces. This include standard white spaces, dot, comma, colon and semicolon.
   */
  private static final String WHITE_SPACES = " \t\r\n.,:;";

  /**
   * Standard word separator.
   */
  private static final String SEPARATORS = " \t\r\n,:;({[<";

  /**
   * Return first word from a sentence. Search for first word separator and return substring before it. As word
   * separator uses space, tab, carriage return, line feed, point, comma, colon, semicolon and open parenthesis, all
   * round, curly, square and angular. Note that point must be followed by space to be considered word separator and
   * ellipsis is part of the word, like in Object... .
   * <p>
   * This method returns null if <code>sentence</code> argument is null and empty if empty.
   * 
   * @param sentence sentence to be scanned for its first word
   * @return sentence first word, null or empty.
   */
  public static String firstWord(String sentence)
  {
    if(sentence == null) {
      return null;
    }
    if(sentence.isEmpty()) {
      return sentence;
    }

    int length = sentence.length();
    int separatorIndex = 0;
    char c = sentence.charAt(0);
    String separators;
    boolean includeSeparator;
    switch(c) {
    case '(':
      separators = ")";
      separatorIndex = 1;
      includeSeparator = true;
      break;
    case '{':
      separators = "}";
      separatorIndex = 1;
      includeSeparator = true;
      break;
    case '[':
      separators = "]";
      separatorIndex = 1;
      includeSeparator = true;
      break;
    case '<':
      separators = ">";
      separatorIndex = 1;
      includeSeparator = true;
      break;
    default:
      separators = SEPARATORS;
      includeSeparator = false;
    }

    outerloop: for(; separatorIndex < length; ++separatorIndex) {
      c = sentence.charAt(separatorIndex);
      if(c == '.') {
        int nextIndex = separatorIndex + 1;
        if(nextIndex == length) break;
        boolean ellipsis = false;
        while(sentence.charAt(nextIndex) == '.') { // ellipsis is part to the word, e.g. Object...
          if(++nextIndex == length) break outerloop;
          ellipsis = true;
          ++separatorIndex;
        }
        if(separators.indexOf(sentence.charAt(nextIndex)) != -1) {
          if(ellipsis) ++separatorIndex;
          break;
        }
        continue;
      }
      if(separators.indexOf(c) != -1) break;
    }
    if(includeSeparator) ++separatorIndex;
    return sentence.substring(0, separatorIndex);
  }

  /**
   * Remove first word from a sentence. First word is recognized using the heuristic from {@link #firstWord(String)}.
   * White spaces after first word are also trimmed. For the purpose of this function white spaces are: space, tab,
   * carriage return, line feed, point, comma, colon and semicolon.
   * <p>
   * This method returns null if <code>sentence</code> argument is null and empty if empty.
   * 
   * @param sentence sentence to remove first word from.
   * @return the sentence with first word removed.
   */
  public static String removeFirstWord(String sentence)
  {
    if(sentence == null) {
      return null;
    }
    if(sentence.isEmpty()) {
      return sentence;
    }

    String firstWord = firstWord(sentence);
    if(firstWord.length() == sentence.length()) return sentence;

    int i = firstWord.length();
    for(; i < sentence.length(); i++) {
      if(WHITE_SPACES.indexOf(sentence.charAt(i)) == -1) break;
    }
    return sentence.substring(i);
  }

  /**
   * Replace all pattern occurrences using match transformer. This method is the standard replace all but allows for
   * matched pattern transformation via caller code handler. Common usage is to supply match transformer as anonymous
   * instance of {@link Handler} interface. If {@link Handler#handle(Object)} method returns given <code>match</code>
   * value replace all degrade to standard behavior, and of course there is no need to use it.
   * 
   * <pre>
   * Pattern pattern = Pattern.compile(&quot;i&quot;);
   * String text = Strings.replaceAll(&quot;This is a text&quot;, pattern, new Handler&lt;String, String&gt;()
   * {
   *   public String handle(String match)
   *   {
   *     return &quot;&lt;em&gt;&quot; + match + &quot;&lt;/em&gt;&quot;;
   *   }
   * });
   * </pre>
   * 
   * In above sample code, after replace all, <code>text</code> will be <em>Th&lt;em&gt;i&lt;/em&gt;s
   * &lt;em&gt;i&lt;/em&gt;s a text</em>.
   * 
   * @param source source string,
   * @param pattern pattern to look for,
   * @param handler match transformer.
   * @return new string with pattern replaced.
   */
  public static String replaceAll(String source, Pattern pattern, Handler<String, String> handler)
  {
    Matcher matcher = pattern.matcher(source);
    StringBuilder builder = new StringBuilder();
    int start = 0;
    while(matcher.find(start)) {
      builder.append(source.substring(start, matcher.start()));
      if(matcher.groupCount() == 0) {
        builder.append(handler.handle(source.substring(matcher.start(), matcher.end())));
      }
      else {
        for(int i = 0; i < matcher.groupCount(); ++i) {
          builder.append(handler.handle(matcher.group(i + 1)));
        }
      }
      start = matcher.end();
    }
    builder.append(source.substring(start));
    return builder.toString();
  }

  /**
   * String variable pattern used to inject system properties.
   */
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");

  /**
   * Inject system properties into text with variables. Given text uses standard variable notation
   * <code>${variable-name}</code> , as known from, for example, Ant or Log4j configuration. This method replace found
   * variable with system property using <code>variable-name</code> as property key. It is a logic flaw if system
   * property is missing.
   * <p>
   * For example, injecting system properties into <code>${log}/app.log</code> will return <code>/var/log/app.log</code>
   * provided there is a system property named <code>log</code> with value <code>/var/log</code>. Note that there is no
   * limit on the number of system properties and/or variables to replace.
   * 
   * @param string text with variables.
   * @return new string with variables replaces.
   * @throws BugError if system property is missing.
   */
  public static String injectProperties(final String string) throws BugError
  {
    return Strings.replaceAll(string, VARIABLE_PATTERN, new Handler<String, String>()
    {
      @Override
      public String handle(String variableName)
      {
        String property = System.getProperty(variableName);
        if(property == null) {
          throw new BugError("Missing system property |%s|. String |%s| variable injection aborted.", variableName, string);
        }
        return property;
      }
    });
  }

  /**
   * Inject string values into variables from a given template string. Template uses de facto standard variable
   * notation, <code>${variable-name}</code>, where name is the key from given variables hash. It is legal for a
   * variable to be declared into template but be missing from variables hash, in which case variable from template is
   * left unchanged.
   * 
   * @param template source string template,
   * @param variables string variables hash.
   * @return resulting string with variables replaced.
   */
  public static String injectVariables(String template, Map<String, String> variables)
  {
    VariablesWriter writer = new VariablesWriter(variables);
    try {
      Files.copy(new StringReader(template), writer);
    }
    catch(IOException e) {
      log.error(e);
    }
    return writer.toString();
  }

  /**
   * Trim given string. Remove given garbage characters from around the string. This method is smart enough to pair open
   * parenthesis, that is, if given garbage is an open parenthesis eliminates it from beginning of string and closing
   * parenthesis from the end. This is true for all rounded, square, curly and angular parenthesis.
   * <p>
   * This method returns null if given source string is null and returns empty string if source string is empty or
   * contains only garbage.
   * 
   * @param string source string,
   * @param garbage no longer needed characters at around the string.
   * @return string with garbage trimmed or null if given string was null
   */
  public static String trim(String string, char garbage)
  {
    if(string == null) {
      return null;
    }
    int length = string.length();
    char prefix = garbage, suffix;
    switch(prefix) {
    case '(':
      suffix = ')';
      break;
    case '[':
      suffix = ']';
      break;
    case '{':
      suffix = '}';
      break;
    case '<':
      suffix = '>';
      break;
    default:
      suffix = prefix;
    }

    int beginIndex = 0;
    for(; beginIndex < length; ++beginIndex) {
      if(string.charAt(beginIndex) != prefix) break;
    }
    if(beginIndex == length) {
      return "";
    }
    int endIndex = length - 1;
    for(; endIndex >= 0; --endIndex) {
      if(string.charAt(endIndex) != suffix) break;
    }
    return string.substring(beginIndex, endIndex + 1);
  }

  /**
   * Trim white spaces around given string. White space characters are those recognized by
   * {@link java.lang.Character#isWhitespace(char)}.
   * 
   * @param string string value to trim.
   * @return string with white spaces trimmed or null if given string was null.
   */
  public static String trim(String string)
  {
    if(string == null) {
      return null;
    }
    int length = string.length();
    int beginIndex = 0;
    for(; beginIndex < length; ++beginIndex) {
      if(!Character.isWhitespace(string.charAt(beginIndex))) break;
    }
    int endIndex = length - 1;
    for(; endIndex >= 0; --endIndex) {
      if(!Character.isWhitespace(string.charAt(endIndex))) break;
    }
    return string.substring(beginIndex, endIndex + 1);
  }

  /**
   * Remove trailing character, if exists.
   * 
   * @param string source string,
   * @param c trailing character to eliminate.
   * @return source string guaranteed to not end in requested character.
   */
  public static String removeTrailing(String string, char c)
  {
    if(string == null) {
      return null;
    }
    final int lastCharIndex = string.length() - 1;
    return string.charAt(lastCharIndex) == c ? string.substring(0, lastCharIndex) : string;
  }

  /**
   * Process formatted string with arguments transform and no illegal format exception. Java string format throws
   * unchecked {@link IllegalFormatException} if given string is not well formatted. This method catches it and return
   * original, not formatted string if exception happened. Uses this method instead of Java String whenever source
   * string is not from safe source, that can guaranty its format correctness.
   * <p>
   * Return null if format string argument is null and empty if empty. If optional arguments is missing return original
   * string.
   * <p>
   * This method takes care to pre-process arguments as follow:
   * <ul>
   * <li>replace {@link Class} with its canonical name,
   * <li>replace {@link Throwable} with exception message or exception class canonical name if null message,
   * <li>replace {@link Thread} with concatenation of thread name and thread ID.
   * </ul>
   * All pre-processed arguments are replaced with string value and format specifier should be also string (%s).
   * 
   * @param format string to format,
   * @param args variable number of arguments related to format.
   * @return formatted string, possible empty or null.
   */
  public static String format(String format, Object... args)
  {
    if(format == null) {
      return null;
    }
    if(format.isEmpty()) {
      return "";
    }
    if(args.length == 0) {
      return format;
    }

    for(int i = 0; i < args.length; i++) {
      if(args[i] instanceof Class) {
        args[i] = ((Class<?>)args[i]).getCanonicalName();
      }
      else if(args[i] instanceof Throwable) {
        String s = ((Throwable)args[i]).getMessage();
        if(s == null) {
          s = args[i].getClass().getCanonicalName();
        }
        args[i] = s;
      }
      else if(args[i] instanceof Thread) {
        Thread thread = (Thread)args[i];
        StringBuilder sb = new StringBuilder();
        sb.append(thread.getName());
        sb.append(':');
        sb.append(thread.getId());
        args[i] = sb.toString();
      }
      else if(args[i] instanceof File) {
        args[i] = ((File)args[i]).getAbsolutePath();
      }
    }

    try {
      return String.format(format, args);
    }
    catch(IllegalFormatException e) {
      log.error("Format operation aborted due to error on string format. Returns original, not formated string. Root cause is: ", e);
    }
    return format;
  }

  /**
   * Search string index of the first character from a series. Traverse <code>string</code> and compare with character
   * from <code>chars</code> series one by one, in sequence. If found return string index; if reach the end of the
   * string return -1.
   * 
   * @param string string builder to search on,
   * @param chars characters series.
   * @return index of first character from series or -1.
   */
  public static int indexOneOf(CharSequence string, char... chars)
  {
    for(int i = 0; i < string.length(); ++i) {
      for(int j = 0; j < chars.length; ++j) {
        if(string.charAt(i) == chars[j]) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * Load string from UTF-8 bytes stream then closes it.
   * 
   * @param inputStream source input stream,
   * @param maxCount optional maximum number of characters to read, default to MAX_VALUE.
   * @return string from input stream.
   * @throws NullPointerException if input stream is null.
   * @throws IOException if input stream read operation fails.
   */
  public static String load(InputStream inputStream, Integer... maxCount) throws IOException
  {
    return load(new InputStreamReader(inputStream, "UTF-8"), maxCount);
  }

  /**
   * Load string from UTF-8 file content.
   * 
   * @param file source file,
   * @param maxCount optional maximum character count to load, default to entire file.
   * @return loaded string, possible empty but never null.
   * @throws IOException if file not found or file read operation fails.
   */
  public static String load(File file, Integer... maxCount) throws IOException
  {
    return load(new FileReader(file), maxCount);
  }

  /**
   * Load string from character stream then closes it.
   * 
   * @param reader source character stream.
   * @param maxCount optional maximum character count to load, default to entire file.
   * @return loaded string, possible empty but never null.
   * @throws IOException if read operation fails.
   */
  public static String load(Reader reader, Integer... maxCount) throws IOException
  {
    long maxCountValue = maxCount.length > 0 ? maxCount[0] : Long.MAX_VALUE;
    StringWriter writer = new StringWriter();

    try {
      char[] buffer = new char[1024];
      for(;;) {
        int readChars = reader.read(buffer, 0, (int)Math.min(buffer.length, maxCountValue));
        if(readChars <= 0) {
          break;
        }
        writer.write(buffer, 0, readChars);
        maxCountValue -= readChars;
      }
    }
    finally {
      Files.close(reader);
      Files.close(writer);
    }

    return writer.toString();
  }

  /**
   * Create target file and copy characters into. Copy destination should be a file and this method throws access denied
   * if attempt to write to a directory. This method creates target file if it does not already exist. If target file
   * does exist it will be overwritten and old content lost. If given <code>chars</code> parameter is null or empty this
   * method does nothing.
   * <p>
   * Note that this method takes care to create file parent directories.
   * 
   * @param chars source characters sequence,
   * @param file target file.
   * @throws FileNotFoundException if <code>target</code> file does not exist and cannot be created.
   * @throws IOException if copy operation fails, including if <code>target</code> is a directory.
   */
  public static void save(CharSequence chars, File file) throws IOException
  {
    save(chars, new FileWriter(Files.mkdirs(file)));
  }

  /**
   * Copy source characters to requested output characters stream. If given <code>chars</code> parameter is null or
   * empty this method does nothing.
   * 
   * @param chars source characters stream,
   * @param writer target writer.
   * @throws IOException if copy operation fails.
   */
  public static void save(CharSequence chars, Writer writer) throws IOException
  {
    if(chars != null) {
      StringReader reader = new StringReader(chars.toString());
      Files.copy(reader, writer);
    }
  }

  /**
   * Copy source characters to requested output bytes stream. If given <code>chars</code> parameter is null or empty
   * this method does nothing.
   * 
   * @param chars source characters stream,
   * @param outputStream target bytes stream.
   * @throws IOException if copy operation fails.
   */
  public static void save(CharSequence chars, OutputStream outputStream) throws IOException
  {
    if(chars != null) {
      ByteArrayInputStream inputStream = new ByteArrayInputStream(chars.toString().getBytes());
      Files.copy(inputStream, outputStream);
    }
  }

  /**
   * Get universal unique ID. Current implementation is based on {@link UUID#randomUUID()} but with dashes removed.
   * 
   * @return universal unique ID.
   */
  public static String UUID()
  {
    return UUID.randomUUID().toString().replaceAll("-", "");
  }

  /**
   * Ensure a given text start with requested prefix. If <code>text</code> argument is empty returns given prefix;
   * returns null if <code>text</code> argument is null.
   * 
   * @param text text to add prefix to, null accepted,
   * @param prefix prefix to force on text start.
   * @return prefixed text or null.
   */
  public static String setPrefix(String text, String prefix)
  {
    if(text == null) {
      return null;
    }
    if(!text.startsWith(prefix)) {
      text = prefix + text;
    }
    return text;
  }

  /** Password dictionary contains ASCII characters for lower case, upper case, numeric and punctuation. */
  private static String[] PASSWORD_DICTIONARY = new String[]
  { //
      "abcdefghijklmnopqrstuvwxyz", // lower case characters
      "ABCDEFGHIJKLMNOPQRSTUVWXYZ", // // upper case characters
      "0123456789", // numerical characters
      "!@#$%&*()_+-=[]|,./?><" // punctuation characters
  };

  /**
   * Generate random password of requested length. Generated password contains characters from
   * {@link #PASSWORD_DICTIONARY} and has exactly requested length.
   * 
   * @param length password length, strict positive.
   * @return random password of requested length.
   */
  public static String generatePassword(int length)
  {
    Params.strictPositive(length, "Password length");
    StringBuilder password = new StringBuilder(length);
    Random random = new Random();

    for(int i = 0; i < length; i++) {
      String charCategory = PASSWORD_DICTIONARY[random.nextInt(PASSWORD_DICTIONARY.length)];
      int position = random.nextInt(charCategory.length());
      password.append(charCategory.charAt(position));
    }
    return new String(password);
  }

  /** Hexadecimal characters, lower case. */
  private static final char[] HEXA = "0123456789abcdef".toCharArray();

  /**
   * Generate text MD5 hash into hexadecimal format. Returned string will be exactly 32 characters long. This function
   * is designed, but not limited, to store password into databases. Anyway, be aware that MD5 is not a strong enough
   * hash function to be used on public transfers.
   * 
   * @param text source text.
   * @return MD5 hash in hexadecimal format.
   */
  public static String md5(String text)
  {
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("MD5");
    }
    catch(NoSuchAlgorithmException e) {
      throw new BugError("Java runtime without MD5 support.");
    }
    md.update(text.getBytes());
    byte[] md5 = md.digest();

    char[] buffer = new char[32];
    for(int i = 0; i < 16; i++) {
      buffer[i * 2] = HEXA[(md5[i] & 0xf0) >> 4];
      buffer[i * 2 + 1] = HEXA[md5[i] & 0x0f];
    }
    return new String(buffer);
  }

  /**
   * Get URL protocol - lower case, or null if given URL does not contains one. Returns null if given URL argument is
   * null or empty.
   * 
   * @param url URL to retrieve the protocol.
   * @return lower case URL protocol, possible null.
   */
  public static String getProtocol(String url)
  {
    if(url == null) {
      return null;
    }
    int protocolSeparatorIndex = url.indexOf("://");
    if(protocolSeparatorIndex == -1) {
      return null;
    }
    return url.substring(0, protocolSeparatorIndex).toLowerCase();
  }
}
