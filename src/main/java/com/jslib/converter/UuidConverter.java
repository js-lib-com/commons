package com.jslib.converter;

import java.util.UUID;

public class UuidConverter implements Converter
{
  /** Package default converter. */
  UuidConverter()
  {
  }

  /**
   * Convert URL string representation into URL instance.
   * 
   * @throws ConverterException if given string is not a valid URL.
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T> T asObject(String string, Class<T> valueType) throws ConverterException
  {
    // at this point value type is guaranteed to be URI
    try {
      return (T)UUID.fromString(string);
    }
    catch(IllegalArgumentException e) {
      throw new ConverterException("Fail to convert invalid UUID |%s|. Root cause: %s", string, e.getMessage());
    }
  }

  /** Get string representation of given URL instance. */
  @Override
  public String asString(Object object)
  {
    // at this point object is guaranteed to be an URI instance
    return ((UUID)object).toString();
  }
}
