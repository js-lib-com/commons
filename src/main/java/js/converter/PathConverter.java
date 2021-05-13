package js.converter;

import java.nio.file.Paths;

import js.util.Files;

/**
 * File converter.
 * 
 * @author Iulian Rotaru
 * @since 1.3.1
 */
@SuppressWarnings("unchecked")
final class PathConverter implements Converter
{
  /** Package default constructor. */
  PathConverter()
  {
  }

  /** Return file instance for the given path string. */
  @Override
  public <T> T asObject(String string, Class<T> valueType)
  {
    // at this point value type is guaranteed to be a Path
    return (T)Paths.get(string);
  }

  /** Return file object path. */
  @Override
  public String asString(Object object)
  {
    // at this point object is guaranteed to be a Path instance
    return Files.path2unix(object.toString());
  }
}
