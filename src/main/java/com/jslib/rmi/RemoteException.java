package com.jslib.rmi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.jslib.converter.Converter;
import com.jslib.converter.ConverterRegistry;
import com.jslib.util.Classes;
import com.jslib.util.Strings;

/**
 * Data transport object used to convey information about exceptional condition at server level.
 * 
 * @author Iulian Rotaru
 */
public final class RemoteException
{
  private String exceptionClass;
  private Value[] constructorArguments;

  public RemoteException()
  {

  }

  /**
   * Construct immutable remote exception instance.
   * 
   * @param target exception root cause.
   */
  public RemoteException(Throwable target)
  {
    this.exceptionClass = target.getClass().getCanonicalName();

    List<Value> arguments = new ArrayList<>();

    for(Constructor<?> constructor : target.getClass().getConstructors()) {
      ExceptionParameters annotation = constructor.getAnnotation(ExceptionParameters.class);
      if(annotation == null) {
        continue;
      }
      String[] parameterNames = annotation.value().split(",\\s*");
      for(int i = 0; i < parameterNames.length; ++i) {
        String parameterName = parameterNames[i];
        Field field = Classes.getOptionalField(target.getClass(), parameterName);
        if(field == null) {
          if(!constructor.getParameterTypes()[i].equals(String.class)) {
            throw new IllegalStateException("Exception constructor parameter without bound field should be exception message string.");
          }
          arguments.add(new Value(target.getMessage()));
          continue;
        }
        arguments.add(new Value(Classes.getFieldValue(target, field)));
      }
      break;
    }

    if(arguments.isEmpty() && target.getMessage() != null) {
      try {
        target.getClass().getConstructor(new Class<?>[]
        {
            String.class
        });
        arguments.add(new Value(target.getMessage()));
      }
      catch(NoSuchMethodException | SecurityException e) {}
    }

    this.constructorArguments = arguments.toArray(new Value[0]);
  }

  public Exception getCause()
  {
    Object[] arguments = new Object[constructorArguments.length];
    for(int i = 0; i < arguments.length; ++i) {
      arguments[i] = constructorArguments[i].getValue();
    }
    return Classes.newInstance(exceptionClass, arguments);
  }

  @Override
  public String toString()
  {
    Exception cause = getCause();
    return Strings.concat(cause.getClass().getCanonicalName(), ": ", cause.getMessage());
  }

  private static class Value
  {
    private static final Converter converter = ConverterRegistry.getConverter();

    private String type;
    private String value;

    @SuppressWarnings("unused")
    public Value()
    {
    }

    public Value(Object value)
    {
      this.type = value != null ? value.getClass().getCanonicalName() : null;
      this.value = converter.asString(value);
    }

    public Object getValue()
    {
      return type != null ? converter.asObject(value, Classes.forName(type)) : null;
    }
  }
}
