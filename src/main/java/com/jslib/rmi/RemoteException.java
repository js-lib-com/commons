package com.jslib.rmi;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.jslib.util.Strings;

/**
 * Data transport object used to convey information about exceptional condition at server level.
 * 
 * @author Iulian Rotaru
 */
public final class RemoteException
{
  private final String type;

  private final String message;

  private final Map<String, Object> properties;

  /** Remote exception message. */

  /** Test constructor. */
  public RemoteException()
  {
    this.type = null;
    this.message = null;
    this.properties = Collections.emptyMap();
  }

  /**
   * Construct immutable remote exception instance.
   * 
   * @param target exception root cause.
   */
  public RemoteException(Throwable target)
  {
    this.type = target.getClass().getCanonicalName();
    this.message = target.getMessage();

    Field[] fields = target.getClass().getDeclaredFields();
    if(fields.length == 0) {
      this.properties = Collections.emptyMap();
      return;
    }

    this.properties = new HashMap<>();
    for(Field field : fields) {
      field.setAccessible(true);
      if(Modifier.isStatic(field.getModifiers())) {
        continue;
      }
      try {
        this.properties.put(field.getName(), field.get(target));
      }
      catch(IllegalArgumentException | IllegalAccessException e) {}
    }
  }

  /**
   * Get the class of the exception that causes this remote exception.
   * 
   * @return exception cause class.
   */
  public String getType()
  {
    return type;
  }

  /**
   * Get exception message.
   * 
   * @return exception message.
   */
  public String getMessage()
  {
    return message;
  }

  public Map<String, Object> getProperties()
  {
    return properties;
  }

  @Override
  public String toString()
  {
    return Strings.concat(type, ": ", message);
  }
}
