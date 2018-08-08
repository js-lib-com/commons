package js.converter;

import js.lang.OrdinalEnum;
import js.util.Types;

/**
 * Enumeration values converter. This converter uses {@link Enum#name()} and {@link Enum#valueOf(Class, String)} to
 * (de)serialize enumeration values. String representation is upper case enumeration name.
 * <p>
 * Anyway, if enumeration type implements {@link OrdinalEnum} this converted uses {@link Enum#ordinal()} and string
 * representation is a numeric value of enumeration constant position. This may be useful when communicating with
 * languages where enumerations are numeric values.
 * 
 * @author Iulian Rotaru
 * @version final
 */
@SuppressWarnings("unchecked")
final class EnumsConverter implements Converter
{
  /** Package default constructor. */
  EnumsConverter()
  {
  }

  /**
   * Create enumeration constant for given string and enumeration type.
   * 
   * @throws IllegalArgumentException string argument is not a valid constant for given enumeration type.
   */
  @SuppressWarnings("rawtypes")
  @Override
  public <T> T asObject(String string, Class<T> valueType) throws IllegalArgumentException
  {
    if(string.isEmpty()) {
      return null;
    }
    // at this point value type is guaranteed to be enumeration
    if(Types.isKindOf(valueType, OrdinalEnum.class)) {
      return valueType.getEnumConstants()[Integer.parseInt(string)];
    }
    return (T)Enum.valueOf((Class)valueType, string);
  }

  /** Get enumeration constant name. */
  @Override
  public String asString(Object object)
  {
    // at this point object is guaranteed to be enumeration
    Enum<?> e = (Enum<?>)object;
    if(object instanceof OrdinalEnum) {
      return Integer.toString(e.ordinal());
    }
    return e.name();
  }
}
