package js.lang;

/**
 * Mark interface for enumerations that should be (de)serialized as numeric values, that is, enumeration ordinal.
 * <p>
 * Usually enumerations are represented to/from strings using enumeration constant name, see {@link Enum#name()}. When
 * an enumeration implements this interface uses {@link Enum#ordinal()} instead.
 * 
 * @author Iulian Rotaru
 */
public interface OrdinalEnum
{
}
