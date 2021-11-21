package js.converter;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TimeZone;

import js.lang.BugError;
import js.lang.NoSuchBeingException;
import js.log.Log;
import js.log.LogFactory;
import js.util.Classes;
import js.util.Params;
import js.util.Types;

/**
 * Converter registry global per JVM. This is the facade of converters package and together with {@link Converter}
 * interface are the only public entities. Converter registry is a sort of directory service: it binds value types with
 * converters. A value type is a class that wrap a single value susceptible to be represented as a single string and a
 * converter is used exactly for that: convert value type to / from string.
 * <p>
 * String representation supplied by converter package is not meant for user interfaces but merely for internal value
 * types serialization and storage. For this reason is critical to have the same converter - or at least compatible, for
 * distributed applications running on separated JVM. Also this is the reason converter registry is singleton.
 * <p>
 * Converter registry provides method to retrieve registry singleton, see {@link #getInstance()} and convenient ways to
 * get converter instance, {@link #getConverter()} and predicate to test for value type support, {@link #hasType(Type)}.
 * 
 * <pre>
 * if (ConverterRegistry.hasType(MessageID.class)) {
 * 	String messageIdValue = ConverterRegistry.getConverter().asString(messageID);
 * 	// store or send message ID
 * }
 * ...
 * MessageID messageID = ConverterRegistry.getConverter().asObject(messageIdValue);
 * // message ID instance recreated
 * </pre>
 * 
 * <p>
 * Converter registry comes with built-in converters for common Java value types but user defined converters are
 * supported. One can use {@link #registerConverter(Class, Class)} to bind a custom converter. Also registry searches
 * for {@link ConverterProvider} implemented by third party libraries and deployed as services on Java run-time. Here
 * are built-in converters, boxed types apply also to related primitives:
 * <ul>
 * <li>{@link Boolean}
 * <li>{@link Character}
 * <li>{@link Byte}
 * <li>{@link Short}
 * <li>{@link Integer}
 * <li>{@link Long}
 * <li>{@link Float}
 * <li>{@link Double}
 * <li>{@link Enum}
 * <li>{@link Date}
 * <li>{@link Date}
 * <li>{@link Time}
 * <li>{@link Timestamp}
 * <li>{@link Class}
 * <li>{@link File}
 * <li>{@link URL}
 * <li>{@link Locale}
 * <li>{@link TimeZone}
 * <li>{@link Charset}
 * </ul>
 * 
 * <p>
 * Here a is sample code of an user defined converter for a hypothetical MessageID value object. Note that in the
 * context {@link Converter#asObject(String, Class)} is executed <code>string</code> argument is already tested for null
 * value; the same is true for <code>object</code> argument from {@link Converter#asString(Object)}.
 * 
 * <pre>
 * public final class MessageIDConverter implements Converter {
 * 	&#064;Override
 * 	public &lt;T&gt; T asObject(String string, Class&lt;T&gt; valueType) {
 * 		if (string.isEmpty()) {
 * 			return null;
 * 		}
 * 		return (T) new MessageID(string);
 * 	}
 * 
 * 	&#064;Override
 * 	public String asString(Object object) {
 * 		return ((MessageID) object).getValue();
 * 	}
 * }
 * ...
 * ConverterRegistry.getInstance().registerConverter(MessageID.class, MessageIDConverter.class);
 * </pre>
 * <p>
 * It is also possible to combine value type with converter in a single class.
 * 
 * <pre>
 * public final class MessageID implements Converter {
 * 	// MessageID implementation
 * ...
 * 
 * 	&#064;Override
 * 	public &lt;T&gt; T asObject(String string, Class&lt;T&gt; valueType) {
 * 		...
 * 	}
 * 
 * 	&#064;Override
 * 	public String asString(Object object) {
 * 		...
 * 	}
 * }
 * </pre>
 * 
 * @author Iulian Rotaru
 * @version draft
 */
public final class ConverterRegistry implements Converter
{
  /** Class logger. */
  private static final Log log = LogFactory.getLog(ConverterRegistry.class);

  /** Converter singleton. */
  // TODO: solution is not sound; it is vulnerable to multiple class loaders
  private static final ConverterRegistry instance = new ConverterRegistry();

  /**
   * Get converter registry instance.
   * 
   * @return converter instance.
   */
  public static ConverterRegistry getInstance()
  {
    return instance;
  }

  /**
   * Convenient way to test for converter support, see {@link #hasClassConverter(Class)}. Note that this predicate
   * variant accepts {@link Type} as parameter to simplify integration with user code but always returns false if type
   * is not a {@link Class}.
   * 
   * @param valueType value type to check for conversion support.
   * @return true if value type has registered converter.
   */
  public static boolean hasType(Type valueType)
  {
    return valueType instanceof Class ? instance.hasClassConverter((Class<?>)valueType) : false;
  }

  /**
   * Convenient way to retrieve converter instance, see {@link #getConverterInstance()}.
   * 
   * @return converter instance.
   */
  public static Converter getConverter()
  {
    return instance.getConverterInstance();
  }

  /** Concrete converters map. */
  private Map<Class<?>, Converter> converters = new IdentityHashMap<>();

  /** Abstract converters map. This map is used when value type is not concrete, that is, is interface or abstract. */
  private Map<Class<?>, Converter> abstractConverters = new IdentityHashMap<>();

  /**
   * Converters used for enumerations. There is no limit on user defined enumeration types and cannot simply map them
   * into converters repository; use a specialized converter for all enumeration types.
   */
  private Converter enumsConverter = new EnumsConverter();

  /**
   * Construct converter instance. Takes care to initialize built-in converters. See class description for list of types
   * supported by built-in converters.
   */
  private ConverterRegistry()
  {
    Converter booleansConverter = new BooleansConverter();
    this.converters.put(Boolean.class, booleansConverter);
    this.converters.put(boolean.class, booleansConverter);

    Converter charactersConverter = new CharactersConverter();
    this.converters.put(Character.class, charactersConverter);
    this.converters.put(char.class, charactersConverter);

    Converter numbersConverter = new NumbersConverter();
    this.converters.put(Byte.class, numbersConverter);
    this.converters.put(Short.class, numbersConverter);
    this.converters.put(Integer.class, numbersConverter);
    this.converters.put(Long.class, numbersConverter);
    this.converters.put(Float.class, numbersConverter);
    this.converters.put(Double.class, numbersConverter);
    this.converters.put(byte.class, numbersConverter);
    this.converters.put(short.class, numbersConverter);
    this.converters.put(int.class, numbersConverter);
    this.converters.put(long.class, numbersConverter);
    this.converters.put(float.class, numbersConverter);
    this.converters.put(double.class, numbersConverter);

    Converter datesConverter = new DatesConverter();
    this.converters.put(Date.class, datesConverter);
    this.converters.put(java.sql.Date.class, datesConverter);
    this.converters.put(Time.class, datesConverter);
    this.converters.put(Timestamp.class, datesConverter);
    this.converters.put(LocalDate.class, datesConverter);
    this.converters.put(LocalTime.class, datesConverter);
    this.converters.put(LocalDateTime.class, datesConverter);
    this.converters.put(ZonedDateTime.class, datesConverter);

    this.converters.put(Class.class, new ClassConverter());
    this.converters.put(File.class, new FileConverter());
    this.converters.put(URI.class, new UriConverter());
    this.converters.put(URL.class, new UrlConverter());
    this.converters.put(Locale.class, new LocaleConverter());
    this.converters.put(Charset.class, new CharsetConverter());

    this.abstractConverters.put(Path.class, new PathConverter());
    this.abstractConverters.put(TimeZone.class, new TimeZoneConverter());

    // register all converter providers found on run-time
    for(ConverterProvider converterProvider : ServiceLoader.load(ConverterProvider.class)) {
      for(Map.Entry<Class<?>, Class<? extends Converter>> entry : converterProvider.getConverters().entrySet()) {
        registerConverter(entry.getKey(), entry.getValue());
      }
    }
  }

  /**
   * Register user defined converter associated to value type. Converter class should be instantiable, i.e. not
   * abstract, interface or void, must have no arguments constructor, even if private and its constructor should of
   * course execute without exception. Otherwise unchecked exception may arise as described on throws section. There are
   * no other constrains on value type class.
   * <p>
   * Note: it is caller responsibility to enforce proper bound, i.e. given converter class to properly handle requested
   * value type. Otherwise exception may throw when this particular converter is enacted.
   * 
   * @param valueType value type class,
   * @param converterClass specialized converter class.
   * @throws BugError if converter class is not instantiable.
   * @throws NoSuchBeingException if converter class has no default constructor.
   */
  public void registerConverter(Class<?> valueType, Class<? extends Converter> converterClass)
  {
    Converter converter = Classes.newInstance(converterClass);
    if(Types.isConcrete(valueType)) {
      registerConverterInstance(valueType, converter);
    }
    else {
      if(abstractConverters.put(valueType, converter) == null) {
        log.debug("Register abstract converter |%s| for value type |%s|.", converterClass, valueType);
      }
      else {
        log.warn("Override abstract converter |%s| for value type |%s|.", converterClass, valueType);
      }
    }
  }

  /**
   * Get converter instance.
   * 
   * @return converter instance.
   */
  public Converter getConverterInstance()
  {
    return this;
  }

  /**
   * Check if registry instance has registered converter for requested class.
   * 
   * @param classConverter class to check for conversion support.
   * @return true if this converter registry has support for requested class.
   */
  public boolean hasClassConverter(Class<?> classConverter)
  {
    return getConverter(classConverter) != null || String.class.equals(classConverter);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T asObject(String string, Class<T> valueType)
  {
    Params.notNull(valueType, "Value type");
    if(string == null) {
      return null;
    }
    if(valueType == Object.class) {
      return (T)string;
    }
    if(valueType == String.class) {
      return (T)string;
    }

    Converter converter = getConverter(valueType);
    if(converter == null) {
      throw new ConverterException("No registered converter for |%s|.", valueType);
    }
    try {
      return converter.asObject(string, valueType);
    }
    catch(ConverterException e) {
      throw e;
    }
    catch(Throwable t) {
      throw new ConverterException(t);
    }
  }

  @Override
  public String asString(Object object)
  {
    if(object == null) {
      return null;
    }
    if(object instanceof String) {
      return (String)object;
    }

    Converter converter = getConverter(object.getClass());
    if(converter == null) {
      throw new ConverterException("No registered converter for |%s|.", object.getClass());
    }
    try {
      return converter.asString(object);
    }
    catch(ConverterException e) {
      throw e;
    }
    catch(Throwable t) {
      throw new ConverterException(t);
    }
  }

  /**
   * Return converter instance declared for requested value type or null if none found. This method has side effects: it
   * lazily binds converters for self-converting value types and for not concrete, i.e. interface or abstract.
   * 
   * @param valueType concrete value type.
   * @return converter instance bound to requested value type or null.
   */
  @SuppressWarnings("unchecked")
  private Converter getConverter(Class<?> valueType)
  {
    Converter c = converters.get(valueType);
    if(c == null) {
      synchronized(converters) {
        if(c == null) {
          if(Types.isKindOf(valueType, Converter.class)) {
            // self-converting value type are instances that contains both data model and converting logic
            registerConverter(valueType, (Class<? extends Converter>)valueType);
          }
          else {
            // not concrete value types, i.e. interface or abstract class, are bound to abstract converters map
            // on the other hand this method got a concrete value type as argument

            // lookup an entry into abstract converters map that is super-class for requested value type, that is,
            // requested value type is a kind of a class from abstract converters map
            // then uses that converter instance to create a concrete bind

            // as a rational for abstract converters map think of Java time zone
            // time zone instances returned by JRE are not directly implementing TimeZone interface
            // there is an internal abstract base class that cannot be bound to a converter instance

            for(Map.Entry<Class<?>, Converter> entries : abstractConverters.entrySet()) {
              if(Types.isKindOf(valueType, entries.getKey())) {
                registerConverterInstance(valueType, entries.getValue());
              }
            }
          }
          c = converters.get(valueType);
        }
      }
    }
    // at this point converter can still be null
    if(c == null && valueType.isEnum()) {
      return enumsConverter;
    }
    return c;
  }

  /**
   * Utility method to bind converter instance to concrete value type.
   * 
   * @param valueType concrete value type,
   * @param converter converter instance able to handle value type.
   */
  private void registerConverterInstance(Class<?> valueType, Converter converter)
  {
    if(converters.put(valueType, converter) == null) {
      log.debug("Register converter |%s| for value type |%s|.", converter.getClass(), valueType);
    }
    else {
      log.warn("Override converter |%s| for value type |%s|.", converter.getClass(), valueType);
    }
  }
}
