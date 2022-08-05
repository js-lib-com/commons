package com.jslib.converter;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

import com.jslib.util.Files;

/**
 * File converter.
 * 
 * @author Iulian Rotaru
 * @since 1.3.1
 */
final class PathConverter implements Converter
{
  /** Package default constructor. */
  PathConverter()
  {
  }

  /** Return file instance for the given path string. */
  @Override
  @SuppressWarnings("unchecked")
  public <T> T asObject(String string, Class<T> valueType)
  {
    // at this point value type is guaranteed to be a Path
    try {
      return (T)Paths.get(string);
    }
    catch(InvalidPathException e) {
      throw new ConverterException("Fail to convert invalid path |%s|. Root cause: %s", string, e.getMessage());
    }
  }

  /** Return file object path. */
  @Override
  public String asString(Object object)
  {
    // at this point object is guaranteed to be a Path instance
    return Files.path2unix(object.toString());
  }
}
