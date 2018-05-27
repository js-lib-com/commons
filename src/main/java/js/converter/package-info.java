/**
 * Convert value types to and from string representation. In this package scope a <b>value type</b> 
 * is a class that wrap a single value susceptible to be represented as a single string, e.g. java.io.File 
 * or java.net.URL. A <b>value type instance</b> is and instance of a value type. This package is not 
 * meant to format/parse objects for/from user interface; converters package is designed for string (de)serialization 
 * support usable by string based wire format like JSON, XML or SOAP. This package deals only with value 
 * types, not aggregated objects and collections; those structured data format is specific to every 
 * wire protocol and is not in scope of this package.
 * 
 * <p>
 * Using converter is trivial: get converter instance from registry and call conversion methods, like 
 * in sample code below. User code should be prepared to deal with {@link js.converter.ConverterException} 
 * thrown if requested value type has no converter registered or conversion fail due to value type string 
 * representation not well formed. This package does not deal with parsing exceptions, they are propagated 
 * to user code after converting parsing exceptions into {@link js.converter.ConverterException}.
 *  <pre>
 * Converter converter = ConverterRegistry.getConverter();
 * String string = converter.asString(object);
 * ...
 * Object object = converter.asObject(string, Object.class);
 *  </pre>
 *  
 * <h3>User Defined Converters</h3>
 * Although there are stock converters ready to use, this package is designed to be extensible. In order 
 * to create user defined converter one should implement {@link js.converter.Converter} and register it 
 * to using {@link js.converter.ConverterRegistry#registerConverter(Class, Class)}, see sample code below. 
 * For completeness here is a list of stock converters supplied by library. Boxed types apply also to 
 * related primitives.
 * <ul>
 * <li>{@link java.lang.Boolean}
 * <li>{@link java.lang.Character}
 * <li>{@link java.lang.Byte}
 * <li>{@link java.lang.Short}
 * <li>{@link java.lang.Integer}
 * <li>{@link java.lang.Long}
 * <li>{@link java.lang.Float}
 * <li>{@link java.lang.Double}
 * <li>{@link java.lang.Enum}
 * <li>{@link java.util.Date}
 * <li>{@link java.sql.Date}
 * <li>{@link java.sql.Time}
 * <li>{@link java.sql.Timestamp}
 * <li>{@link java.lang.Class}
 * <li>{@link java.io.File}
 * <li>{@link java.net.URL}
 * <li>{@link java.util.Locale}
 * <li>{@link java.util.TimeZone}
 * </ul>
 * <p>
 * Here a is sample code of an user defined converter for a hypothetical MessageID value object. Note 
 * that in the context {@link js.converter.Converter#asObject(String, Class)} is executed <code>string</code> 
 * argument is already tested for null value; the same is true for <code>object</code> argument from 
 * {@link js.converter.Converter#asString(Object)}.
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
 * 
 * <h3>Self-converting</h3>
 * Self-converting value types are objects that contains both data model and converting logic. To simplify
 * user code, there is no need to declare binding for self-converting value types; they are created on the
 * fly at first attempt to use it. Anyway, there is a catch and user code should be aware of it: binding 
 * logic is not executed in the same instance as data model. When bind is registered a different instance
 * is created for conversion logic. 
 * <pre>
 * public class PhoneNumber implements Converter {
 * 	private String value;
 * 
 * 	public String getValue() {
 * 		return value;
 * 	}
 * 
 * 	// converter implementation cannot access 'value' state
 * 	// because conversion is executed in different instance from model instance 
 *  
 * 	&#064;Override
 * 	public &lt;T&gt; T asObject(String string, Class&lt;T&gt; valueType) {
 * 		return (T)new PhoneNumber(string);
 * 	}
 *     
 * 	&#064;Override
 * 	public String asString(Object object) {
 * 		return ((PhoneNumber)object).getValue();
 * 	}
 * }
 * </pre>
 *  
 * <h3>Abstract classes and interfaces</h3>
 * There are cases where a value type desired to be converted is not a concrete class. For example 
 * time zone instances returned by JRE are not directly implementing TimeZone interface; there is an 
 * internal abstract base class that is private and cannot be bound to a converter instance.
 * <p>
 * For such cases converter package uses abstract converters map. Binding is declared as usual, see above
 * description. Internally, binding logic detects it has to do with an interface or abstract class and
 * store binding to an abstract converters map.
 * <p>
 * When a request for converter instance is made, lookup an entry into abstract converters map that is 
 * super-class for requested value type, that is, requested value type is a kind of a class from abstract
 * converters map; then uses that converter instance to create a concrete bound on the fly.
 * 
 * <h3>Developer note</h3>
 * As already stated this converter package is designed to be used on string serialization. This process 
 * implies some sort of IO operations like file writing/reading or networking communication, operations 
 * that are inherently slow. For this reason user defined converters speed is not really critical.   
 *  
 * @author Iulian Rotaru
 * @version draft
 */
package js.converter;

