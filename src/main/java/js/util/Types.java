package js.util;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import js.lang.BugError;

/**
 * Duck typing predicates. This utility class supplies predicates for type discovery at runtime; it is inspired by
 * <code>JavaScript</code> paradigm. Although uncommon for utility classes this one allows for sub-classing, see sample
 * code.
 * 
 * <pre>
 * class Types extends js.util.Types {
 * 	public static boolean isWunderObject(Object o) {
 * 		return o instanceof WunderObject;
 * 	}
 * }
 * ...
 * if(Types.isInstanceOf(object, StandardObject.class)) { // predicate provided by base class
 * 	...
 * }
 * if(Types.isWunderObject(object)) { // predicate provided by extension
 * 	...		
 * }
 * </pre>
 * 
 * @author Iulian Rotaru
 * @version final
 */
public class Types
{
  /** Prevent default constructor synthesis but allow sub-classing. */
  protected Types()
  {
  }

  /**
   * Test if a requested type is identity equal with one from a given types list. If <code>type</code> is null return
   * false. If a type to match happened to be null is considered no match.
   * 
   * @param t type to search for, possible null,
   * @param typesToMatch types list to compare with.
   * @return true if requested type is one from given types list.
   * @throws IllegalArgumentException if <code>typesToMach</code> is empty.
   */
  public static boolean equalsAny(Type t, Type... typesToMatch) throws IllegalArgumentException
  {
    Params.notNullOrEmpty(typesToMatch, "Types to match");
    if(t == null) {
      return false;
    }
    for(Type typeToMatch : typesToMatch) {
      if(t.equals(typeToMatch)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Determine if a given type is a kind of one of the requested types to match. Traverses <code>typesToMatch</code> and
   * delegates {@link #isKindOf(Type, Type)} till first positive match and returns true. If no match found returns
   * false. If <code>type</code> is null returns false. If a type to match happened to be null is considered no match.
   * 
   * @param t type to test, possible null,
   * @param typesToMatch variable number of types to match.
   * @return true if <code>type</code> is a kind of one of <code>typesToMatch</code>.
   * @throws IllegalArgumentException if <code>typesToMach</code> is null or empty.
   */
  public static boolean isKindOf(Type t, Type... typesToMatch) throws IllegalArgumentException
  {
    Params.notNullOrEmpty(typesToMatch, "Types to match");
    for(Type typeToMatch : typesToMatch) {
      if(isKindOf(t, typeToMatch)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Determine if a given type is a kind of a requested type to match. Returns true if <code>type</code> is a subclass
   * or implements <code>typeToMatch</code> - not necessarily direct. Boxing classes for primitive values are
   * compatible. This depart from {@link Class#isAssignableFrom(Class)} that consider primitive and related boxing class
   * as different.
   * <p>
   * If either type or type to match are parameterized types uses the raw class. If either type or type to match are
   * null returns false.
   * 
   * @param t type to test,
   * @param typeToMatch desired type to match.
   * @return true if <code>type</code> is subclass of or implements <code>typeToMatch</code>.
   */
  private static boolean isKindOf(Type t, Type typeToMatch)
  {
    if(t == null || typeToMatch == null) {
      return false;
    }
    if(t.equals(typeToMatch)) {
      return true;
    }

    Class<?> clazz = typeToClass(t);
    Class<?> classToMatch = typeToClass(typeToMatch);

    if(clazz.isPrimitive()) {
      return BOXING_MAP.get(clazz) == classToMatch;
    }
    if(classToMatch.isPrimitive()) {
      return BOXING_MAP.get(classToMatch) == clazz;
    }

    return classToMatch.isAssignableFrom(clazz);
  }

  /** Java language primitive values boxing classes. */
  private static Map<Type, Type> BOXING_MAP = new HashMap<Type, Type>();
  static {
    BOXING_MAP.put(boolean.class, Boolean.class);
    BOXING_MAP.put(byte.class, Byte.class);
    BOXING_MAP.put(char.class, Character.class);
    BOXING_MAP.put(short.class, Short.class);
    BOXING_MAP.put(int.class, Integer.class);
    BOXING_MAP.put(long.class, Long.class);
    BOXING_MAP.put(float.class, Float.class);
    BOXING_MAP.put(double.class, Double.class);
  }

  /**
   * Get boxing class for requested type. If <code>type</code> is primitive returns related boxing class. If
   * <code>type</code> is already a boxing type returns it as it is. It is considered a bug if <code>type</code> is not
   * a primitive or a boxing type.
   * 
   * @param t primitive or boxing type.
   * @return boxing class representing requested type.
   * @throws BugError if <code>type</code> is not a primitive or boxing type.
   */
  public static Class<?> getBoxingClass(Type t) throws BugError
  {
    Type boxingClass = BOXING_MAP.get(t);
    if(boxingClass == null) {
      if(!BOXING_MAP.values().contains(t)) {
        throw new BugError("Trying to get boxing class from not boxed type.");
      }
      boxingClass = t;
    }
    return (Class<?>)boxingClass;
  }

  /**
   * Test if object instance is not null and extends or implements expected type. This predicate consider primitive and
   * related boxing types as equivalent, e.g. <code>1.23</code> is instance of {@link Double}.
   * 
   * @param o object instance to test, possible null,
   * @param t expected type.
   * @return true if instance is not null and extends or implements requested type.
   */
  public static boolean isInstanceOf(Object o, Type t)
  {
    if(o == null) {
      return false;
    }
    if(t instanceof Class) {
      Class<?> clazz = (Class<?>)t;
      if(clazz.isPrimitive()) {
        return BOXING_MAP.get(clazz) == o.getClass();
      }
      return clazz.isInstance(o);
    }
    return false;
  }

  /**
   * Test if type is concrete, that is, is not interface or abstract. If type to test is parameterized uses its raw
   * type. If type to test is null returns false.
   * 
   * @param t type to test, possible null.
   * @return true if type is concrete.
   */
  public static boolean isConcrete(Type t)
  {
    if(t instanceof Class) {
      final Class<?> c = (Class<?>)t;
      return !c.isInterface() && !Modifier.isAbstract(c.getModifiers());
    }

    if(t instanceof ParameterizedType) {
      final ParameterizedType p = (ParameterizedType)t;
      return isConcrete(p.getRawType());
    }

    return false;
  }

  /** Java standard classes used to represent numbers, including primitives. */
  private static Type[] NUMERICAL_TYPES = new Type[]
  {
      int.class, long.class, double.class, Integer.class, Long.class, Double.class, byte.class, short.class, float.class, Byte.class, Short.class, Float.class, Number.class, BigDecimal.class
  };

  /**
   * Test if object instance is primitive numeric value or related boxing class. Returns true if given instance is a
   * primitive numeric value or related boxing class. Returns false if instance is null.
   * 
   * @param o object instance, possible null.
   * @return true if instance is a number.
   */
  public static boolean isNumber(Object o)
  {
    return o != null && isNumber(o.getClass());
  }

  /**
   * Test if type is numeric. A type is considered numeric if is a Java standard class representing a number.
   * 
   * @param t type to test.
   * @return true if <code>type</code> is numeric.
   */
  public static boolean isNumber(Type t)
  {
    for(int i = 0; i < NUMERICAL_TYPES.length; i++) {
      if(NUMERICAL_TYPES[i] == t) {
        return true;
      }
    }
    return false;
  }

  /**
   * Test if instance is a character, primitive or boxing class. Not that null is not considered a valid character and
   * this predicate returns false.
   * 
   * @param o instance to test.
   * @return true if instance is a character.
   */
  public static boolean isCharacter(Object o)
  {
    return o != null && isCharacter(o.getClass());
  }

  /**
   * Test if type is a character, primitive or boxing.
   * 
   * @param t type to test.
   * @return true if type is character.
   */
  public static boolean isCharacter(Type t)
  {
    return equalsAny(t, char.class, Character.class);
  }

  /**
   * Test if object instance is a boolean, primitive or boxing class. Returns true if given object is primitive boolean
   * or {@link Boolean} instance or false otherwise. Return also false if object is null.
   * 
   * @param o object instance, possible null.
   * @return true if instance to test is boolean.
   */
  public static boolean isBoolean(Object o)
  {
    return o != null && isBoolean(o.getClass());
  }

  /**
   * Test if type is a boolean primitive or boxing class.
   * 
   * @param t type to test.
   * @return true if type is boolean.
   */
  public static boolean isBoolean(Type t)
  {
    return equalsAny(t, boolean.class, Boolean.class);
  }

  /**
   * Test if object instance is enumeration. Returns true if given object is enumeration instance or false otherwise.
   * Returns also false if object is null.
   * 
   * @param o object instance to test, possible null.
   * @return true if instance is enumeration.
   */
  public static boolean isEnum(Object o)
  {
    return o != null && isEnum(o.getClass());
  }

  /**
   * Test if type is enumeration. This predicate delegates {@link Class#isEnum()} if type is a class. If not, returns
   * false.
   * 
   * @param t type to test.
   * @return true if type is enumeration.
   */
  public static boolean isEnum(Type t)
  {
    if(t instanceof Class<?>) {
      return ((Class<?>)t).isEnum();
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public static boolean isValidEnum(Type t, String name)
  {
    if(!(t instanceof Class<?>)) {
      return false;
    }
    final Class<?> c = (Class<?>)t;
    if(!c.isEnum()) {
      return false;
    }
    for(final Enum<?> e : ((Class<? extends Enum<?>>)c).getEnumConstants()) {
      if(e.name().equals(name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Test if object instance is primitive. Returns true if given object is primitive instance, false otherwise. Return
   * also false if object is null.
   * 
   * @param o object instance to test.
   * @return true if object instance is primitive.
   */
  public static boolean isPrimitive(Object o)
  {
    return o != null && isPrimitive(o.getClass());
  }

  /**
   * Test if type is primitive. Primitive types are those considered so by {@link Class#isPrimitive()}.
   * 
   * @param t type to test.
   * @return true if type is primitive.
   */
  public static boolean isPrimitive(Type t)
  {
    if(t instanceof Class<?>) {
      return ((Class<?>)t).isPrimitive();
    }
    return false;
  }

  /**
   * Test if object instance is a primitive like. This predicates delegates {@link #isPrimitiveLike(Type)}. If instance
   * to test is null returns false.
   * 
   * @param o object instance, possible null.
   * @return true if object instance is primitive like.
   */
  public static boolean isPrimitiveLike(Object o)
  {
    return o != null && isPrimitiveLike(o.getClass());
  }

  /**
   * Test if type is like a primitive? Return true only if given type is a number, boolean, enumeration, character or
   * string.
   * 
   * @param t type to test.
   * @return true if this type is like a primitive.
   */
  public static boolean isPrimitiveLike(Type t)
  {
    if(isNumber(t)) {
      return true;
    }
    if(isBoolean(t)) {
      return true;
    }
    if(isEnum(t)) {
      return true;
    }
    if(isCharacter(t)) {
      return true;
    }
    if(isDate(t)) {
      return true;
    }
    if(t == String.class) {
      return true;
    }
    return false;
  }

  /**
   * Test if instance is an array. Delegates {@link #isArray(Type)} and returns false if object instance is null.
   * 
   * @param o instance to test.
   * @return true if instance is array.
   */
  public static boolean isArray(Object o)
  {
    return o != null && isArray(o.getClass());
  }

  /**
   * Test if type is array. If type is a class return {@link Class#isArray()} predicate value; otherwise test if type is
   * {@link GenericArrayType}.
   * 
   * @param t type to test.
   * @return true if type is array.
   */
  public static boolean isArray(Type t)
  {
    if(t instanceof Class<?>) {
      return ((Class<?>)t).isArray();
    }
    if(t instanceof GenericArrayType) {
      return true;
    }
    return false;
  }

  /**
   * Test instance if is array like. If instance to test is not null delegates {@link #isArrayLike(Type)}; otherwise
   * return false.
   * 
   * @param o instance to test, possible null in which case returns false.
   * @return true if instance is array like; returns false if instance to test is null.
   */
  public static boolean isArrayLike(Object o)
  {
    return o != null && isArrayLike(o.getClass());
  }

  /**
   * Test if type is array like, that is, array or collection. Uses {@link #isArray(Type)} and
   * {@link #isCollection(Type)}.
   * 
   * @param t type to test.
   * @return true if type is array like.
   */
  public static boolean isArrayLike(Type t)
  {
    return isArray(t) || isCollection(t);
  }

  /**
   * Test if instance is a collection. If instance to test is not null delegates {@link #isCollection(Type)}; otherwise
   * return false.
   * 
   * @param o instance to test, possible null in which case returns false.
   * @return true if instance is collection; returns false if instance to test is null.
   */
  public static boolean isCollection(Object o)
  {
    // TODO: what about o instanceof Collection ?
    return o != null && isCollection(o.getClass());
  }

  /**
   * Test if type is collection. Returns true if type implements, directly or through inheritance, {@link Collection}
   * interface.
   * 
   * @param t type to test.
   * @return true if type is collection.
   */
  public static boolean isCollection(Type t)
  {
    return Types.isKindOf(t, Collection.class);
  }

  /**
   * Test if instance is a map. If instance to test is not null delegates {@link #isMap(Type)}; otherwise returns false.
   * 
   * @param o instance to test, possible null.
   * @return true if instance is a map; returns false if instance to test is null.
   */
  public static boolean isMap(Object o)
  {
    return o != null && Types.isMap(o.getClass());
  }

  /**
   * Test if type is map. Returns true if type implements, directly or through inheritance, {@link Map} interface.
   * 
   * @param t type to test.
   * @return true if type is map.
   */
  public static boolean isMap(Type t)
  {
    return Types.isKindOf(t, Map.class);
  }

  /**
   * Test if object is instance of date. Returns true if given <code>object</code> is instance of {@link Date}; returns
   * false if instance to test is null.
   * 
   * @param o object instance, possible null.
   * @return true if instance to test is a date.
   */
  public static boolean isDate(Object o)
  {
    return o != null && o instanceof Date;
  }

  /**
   * Test if type is a calendar date.
   * 
   * @param t type to test.
   * @return true if type is a calendar date.
   */
  public static boolean isDate(Type t)
  {
    return isKindOf(t, Date.class);
  }

  /** Pattern constant used for Java class name validation. */
  private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("^[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]+)*\\.[A-Z][a-zA-Z0-9_]*(?:\\$[A-Z][a-zA-Z0-9_]*)*$");

  /**
   * Test if given name is a valid Java class name. This predicate returns false if name to test is null.
   * 
   * @param name name to test, possible null.
   * @return true if given name is a valid Java class name.
   */
  public static boolean isClass(String name)
  {
    if(name == null) {
      return false;
    }
    Matcher matcher = CLASS_NAME_PATTERN.matcher(name);
    return matcher.find();
  }

  /**
   * Returns a boolean representation of a given object instance. This predicate behavior resemble
   * <code>JavaScript</code> cast of object to boolean value. Here are tested conditions:
   * <ul>
   * <li>if {@link #isBoolean(Object)} returns boolean value,
   * <li>if {@link #isNumber(Object)} returns true if number is not zero,
   * <li>if instance is string returns true if string is not empty,
   * <li>if {@link #isArray(Object)}, {@link #isCollection(Object)} or {@link #isMap(Object)} returns true if is not
   * empty,
   * <li>if {@link #isCharacter(Object)} returns true if character is defined.
   * </ul>
   * <p>
   * If object instance is null returns false.
   * 
   * @param o object instance to interpret as boolean.
   * @return boolean representation of object instance.
   */
  public static boolean asBoolean(Object o)
  {
    if(o == null) {
      return false;
    }
    if(isBoolean(o)) {
      return (Boolean)o;
    }
    if(isNumber(o)) {
      return ((Number)o).byteValue() != 0;
    }
    if(o instanceof String) {
      return !((String)o).isEmpty();
    }
    if(isArray(o)) {
      return Array.getLength(o) > 0;
    }
    if(isCollection(o)) {
      return ((Collection<?>)o).size() > 0;
    }
    if(isMap(o)) {
      return ((Map<?, ?>)o).size() > 0;
    }
    if(isCharacter(o)) {
      return Character.isDefined((Character)o);
    }
    return true;
  }

  /**
   * Returns an empty value of requested type. This method returns <code>0</code>, <code>false</code>, empty string,
   * current date/time, empty collection. empty array or empty map if requested type is respectively a number, boolean,
   * string, date, collection, array or map. If none of previous returns null.
   * 
   * @param t desired type for empty value.
   * @return empty value.
   */
  public static Object getEmptyValue(Type t)
  {
    if(byte.class.equals(t) || Byte.class.equals(t)) {
      return (byte)0;
    }
    if(short.class.equals(t) || Short.class.equals(t)) {
      return (short)0;
    }
    if(int.class.equals(t) || Integer.class.equals(t)) {
      return (int)0;
    }
    if(long.class.equals(t) || Long.class.equals(t)) {
      return (long)0;
    }
    if(float.class.equals(t) || Float.class.equals(t)) {
      return (float)0;
    }
    if(double.class.equals(t) || Double.class.equals(t)) {
      return (double)0;
    }

    if(Types.isBoolean(t)) {
      return Boolean.valueOf(false);
    }
    if(Types.isCharacter(t)) {
      return '\0';
    }
    if(String.class.equals(t)) {
      return "";
    }
    if(Types.isDate(t)) {
      return new Date();
    }
    if(Types.isCollection(t)) {
      return Classes.newCollection(t);
    }
    if(Types.isArray(t)) {
      Array.newInstance(((Class<?>)t).getComponentType(), 0);
    }
    if(Types.isMap(t)) {
      return Classes.newMap(t);
    }
    return null;
  }

  /**
   * Cast Java reflective type to language class. If <code>type</code> is instance of {@link Class} just return it. If
   * is parameterized type returns the raw class.
   * 
   * @param t Java reflective type.
   * @return the class described by given <code>type</code>.
   */
  private static Class<?> typeToClass(Type t)
  {
    if(t instanceof Class<?>) {
      return (Class<?>)t;
    }
    if(t instanceof ParameterizedType) {
      return (Class<?>)((ParameterizedType)t).getRawType();
    }
    throw new BugError("Unknown type %s to convert to class.", t);
  }

  /**
   * Convert object instance to iterable. If object instance is an array or a collection returns an iterable instance
   * able to iterate array respective collection items. Otherwise return an empty iterable.
   * <p>
   * This utility method is designed to be used with <code>foreach</code> loop. Note that if object instance is not
   * iterable <code>foreach</code> loop is not executed.
   * 
   * <pre>
   * Object o = getObjectFromSomeSource();
   * for(Object item : Types.asIterable(o)) {
   *   // do something with item instance
   * }
   * </pre>
   * 
   * @param o object instance.
   * @return object iterable, possible empty.
   */
  public static Iterable<?> asIterable(final Object o)
  {
    if(isArray(o)) {
      return new Iterable<Object>()
      {
        private Object array = o;
        private int index;

        @Override
        public Iterator<Object> iterator()
        {
          return new Iterator<Object>()
          {
            @SuppressWarnings("unqualified-field-access")
            @Override
            public boolean hasNext()
            {
              return index < Array.getLength(array);
            }

            @SuppressWarnings("unqualified-field-access")
            @Override
            public Object next()
            {
              return Array.get(array, index++);
            }

            @Override
            public void remove()
            {
              throw new UnsupportedOperationException("Array iterator has no remove operation.");
            }
          };
        }
      };
    }

    if(isCollection(o)) {
      return (Iterable<?>)o;
    }

    return new Iterable<Object>()
    {
      @Override
      public Iterator<Object> iterator()
      {
        return new Iterator<Object>()
        {
          @Override
          public boolean hasNext()
          {
            return false;
          }

          @Override
          public Object next()
          {
            throw new UnsupportedOperationException("Empty iterator has no next operation.");
          }

          @Override
          public void remove()
          {
            throw new UnsupportedOperationException("Empty iterator has no remove operation.");
          }
        };
      }
    };
  }

  /**
   * Test if given type is void. Returns true if type is {@link Void#TYPE} or {@link Void} class, i.e. is
   * <code>void</code> keyword or <code>Void</code> class.
   * 
   * @param t type to test for void.
   * @return true if requested type is void.
   */
  public static boolean isVoid(Type t)
  {
    return Void.TYPE.equals(t) || Void.class.equals(t);
  }

  /**
   * Returns true if given object is empty. Object is considered empty if:
   * <ul>
   * <li>is null,
   * <li>is boolean false,
   * <li>is number zero,
   * <li>is empty string,
   * <li>is empty array, collection or map,
   * <li>is undefined character.
   * </ul>
   * 
   * @param o object to test if empty.
   * @return true if object is empty.
   */
  public static boolean isEmpty(Object o)
  {
    if(o == null) {
      return true;
    }
    if(isBoolean(o)) {
      return (Boolean)o == false;
    }
    if(isNumber(o)) {
      return ((Number)o).byteValue() == 0;
    }
    if(o instanceof String) {
      return !((String)o).isEmpty();
    }
    if(isArray(o)) {
      return Array.getLength(o) == 0;
    }
    if(isCollection(o)) {
      return ((Collection<?>)o).isEmpty();
    }
    if(isMap(o)) {
      return ((Map<?, ?>)o).isEmpty();
    }
    if(isCharacter(o)) {
      return !Character.isDefined((Character)o);
    }
    return false;
  }

  /**
   * Test two types for equality. This predicate takes care of parameterized types by comparing both raw type and actual
   * type arguments. If an actual type argument is parameterized on its own this method is executed recursively.
   * <p>
   * If both types are null returns true.
   * 
   * @param type1 first type, null accepted,
   * @param type2 second type, null accepted.
   * @return true if first type equals the second one.
   */
  public static boolean isEqual(Type type1, Type type2)
  {
    if(type1 == null) {
      return type2 == null;
    }
    if(type2 == null) {
      return type1 == null;
    }
    if(!(type1 instanceof ParameterizedType)) {
      return type1.equals(type2);
    }

    // handle parameterized types

    if(!(type2 instanceof ParameterizedType)) {
      return false;
    }
    ParameterizedType ptype1 = (ParameterizedType)type1;
    ParameterizedType ptype2 = (ParameterizedType)type2;
    if(!ptype1.getRawType().equals(ptype2.getRawType())) {
      return false;
    }
    Type[] atype1 = ptype1.getActualTypeArguments();
    Type[] atype2 = ptype2.getActualTypeArguments();
    if(atype1.length != atype2.length) {
      return false;
    }
    for(int i = 0; i < atype1.length; ++i) {
      if(!isEqual(atype1[i], atype2[i])) {
        return false;
      }
    }
    return true;
  }
}
