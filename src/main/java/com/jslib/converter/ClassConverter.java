package com.jslib.converter;

import com.jslib.lang.NoSuchBeingException;
import com.jslib.util.Classes;

/**
 * Java class converter. This converter supplies (de)serialization services for {@link Class} instances.
 * 
 * @author Iulian Rotaru
 */
final class ClassConverter implements Converter
{
  /** Package default converter. */
  ClassConverter()
  {
  }

  /**
   * Return the Java class instance for given canonical (qualified) name. If given string is empty returns null. Also
   * returns null if class not found.
   * 
   * @param string class canonical (qualified) name,
   * @param valueType class type.
   * @return class singleton, possible null if class not found.
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T> T asObject(String string, Class<T> valueType)
  {
    // at this point string cannot be null and value type is a class
    if(string.isEmpty()) return null;

    // uses this library Classes#forName instead of Java standard Class#forName
    // first uses current thread context loader whereas the second uses library loader
    // as a consequence classes defined by web app could not be found if use Java standard Class#forName

    try {
      return (T)Classes.forName(string);
    }
    catch(NoSuchBeingException e) {
      return null;
    }
  }

  /** Get string representation for given Java class instance. Return class canonical name. */
  @Override
  public String asString(Object object)
  {
    assert object instanceof Class;
    return ((Class<?>)object).getCanonicalName();
  }
}
