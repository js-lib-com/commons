package js.converter;

import java.lang.reflect.Type;

/**
 * Convert value type instances to and from strings representation. This interface is the core abstraction of string converter
 * package supplying methods for value type conversion. A value type is a class that wrap a single value susceptible to be
 * represented as a single string - a sort of data atom, e.g. java.io.File or java.net.URL.
 * <p>
 * In order to be able to convert a given value type implementation must have a converter defined, see {@link ConverterRegistry}
 * . One can check for implementation support using {@link ConverterRegistry#hasType(Type)} predicate.
 * 
 * <p>
 * As stated, this interface supplies two complementary conversion methods for string (de)serialization:
 * {@link #asString(Object)} and {@link #asObject(String, Class)}. The contract is that applying these methods in sequence we
 * need to obtain original object, that is, sequence is idempotent:
 * 
 * <pre>
 * string.equals(converter.asString(converter.asObject(string, ValueType.class)))
 * </pre>
 * 
 * <p>
 * Using converter is straightforward: get instance from converter registry and call conversion methods. Converter is reusable
 * and thread safe.
 * 
 * <pre>
 *      ConverterRegistry registry = ConverterRegistry.getInstance();
 *      Converter converter = registry.getConverterInstance();
 *      String string = converter.asString(object);
 *      ...
 *      Object object = converter.asObject(string, Object.class);
 * </pre>
 * 
 * <h3>Custom Converter</h3>
 * <p>
 * Converter registry comes with stock converters for Java common value types but user defined converters are supported.
 * Implement this interface and register with {@link ConverterRegistry#registerConverter(Class, Class)}. It is user
 * responsibility to ensure user defined converter is able to actually handle value type(s) for which is registered. Usually a
 * converter is registered for a single value type but there is no formal restriction on that.
 * <p>
 * Custom converters could be registered also from third party libraries via {@link ConverterProvider} service.
 * 
 * <h3>Thread Safe</h3>
 * <p>
 * Converter implementation should be reusable and thread safe. For this reason it should not hold state, maybe besides thread
 * local. Since converter registry is global per JVM also converter instances are.
 * 
 * @author Iulian Rotaru
 * @see js.converter package description
 * @version final
 */
public interface Converter {
	/**
	 * Create a new instance of given value type and initialize it from string. Instance string representation should be
	 * compatible with expected value type. It is user code responsibility to ensure that. Returns null if <code>string</code>
	 * argument is null; if <code>string</code> is empty implementation should return some specific empty instance, like false
	 * or 0 but is not recommended to return null.
	 * 
	 * <pre>
	 * String string = &quot;Iulian Rotaru&lt;iuli@bbnet.ro&gt;&quot;;
	 * Mailbox mailbox = converter.asObject(string, Mailbox.class);
	 * </pre>
	 * <p>
	 * Requested value type should have converter registered via {@link ConverterRegistry#registerConverter(Class, Class)}.
	 * 
	 * @param string instance string representation, possible null or empty,
	 * @param valueType expected value type, null not accepted.
	 * @param <T> instance type.
	 * @return newly created instance of requested type, null if <code>string</code> argument is null.
	 * @throws IllegalArgumentException if <code>valueType</code> is null.
	 * @throws ConverterException if value type has no converter registered or string parsing fails.
	 */
	default <T> T asObject(String string, Class<T> valueType) throws IllegalArgumentException, ConverterException {
	  throw new UnsupportedOperationException();
	}

	/**
	 * Create a string representation for given value type instance. Returns serialized instance or null if <code>object</code>
	 * argument is null.
	 * 
	 * <pre>
	 * Mailbox mailbox = new Mailbox();
	 * String string = converter.asString(mailbox);
	 * </pre>
	 * <p>
	 * The type of requested value object should have converter registered via
	 * {@link ConverterRegistry#registerConverter(Class, Class)}.
	 * 
	 * @param object value type instance, possible null.
	 * @return string representation of given value type instance or null.
	 * @throws ConverterException if value object has not converter registered or if anything goes wrong on serialization
	 *             process.
	 */
	default String asString(Object object) throws ConverterException {
	  throw new UnsupportedOperationException();
	}
}
