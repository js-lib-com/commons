package com.jslib.converter;

import java.nio.charset.Charset;

public class CharsetConverter implements Converter
{
  /** Package default constructor. */
  CharsetConverter()
  {
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T asObject(String string, Class<T> valueType) throws IllegalArgumentException, ConverterException
  {
    return (T)Charset.forName(string);
  }

  @Override
  public String asString(Object object) throws ConverterException
  {
    return ((Charset)object).name();
  }
}
