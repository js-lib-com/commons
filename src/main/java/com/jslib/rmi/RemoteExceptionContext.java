package com.jslib.rmi;

import com.jslib.util.Classes;
import com.jslib.util.Params;
import com.jslib.util.Strings;

/**
 * Data transport object used to convey information about exceptional condition at server level.
 * 
 * @author Iulian Rotaru
 */
public final class RemoteExceptionContext
{
  private String exceptionClassName;
  private String exceptionMessage;
  private String causeClassName;
  private String causeMessage;

  public RemoteExceptionContext()
  {

  }

  /**
   * Construct immutable remote exception instance.
   * 
   * @param target remote exception root cause.
   */
  public RemoteExceptionContext(Throwable target)
  {
    this.exceptionClassName = target.getClass().getCanonicalName();
    this.exceptionMessage = target.getMessage();
    if(target.getCause() != null) {
      this.causeClassName = target.getCause().getClass().getCanonicalName();
      this.causeMessage = target.getCause().getMessage();
    }
  }

  public String getExceptionClass()
  {
    return exceptionClassName;
  }

  public String getExceptionMessage()
  {
    return exceptionMessage;
  }

  public String getCauseClass()
  {
    return causeClassName;
  }

  public String getCauseMessage()
  {
    return causeMessage;
  }

  /**
   * Attempt to create an exception instance described by this remote exception context. Returns null if exception class
   * cannot be found by current class loader or constructor is missing. If this remote exception context has cause class
   * attempt to create and initialize the cause of returned exception instance.
   * 
   * @return exception instance or null.
   */
  public Throwable getException()
  {
    if(exceptionClassName == null) {
      return null;
    }
    Class<? extends Throwable> exceptionClass = Classes.forOptionalName(exceptionClassName);
    if(exceptionClass == null) {
      return null;
    }

    if(exceptionMessage != null) {
      Throwable cause = getCause();
      if(cause != null) {
        return newInstance(exceptionClass, exceptionMessage, cause);
      }
      return newInstance(exceptionClass, exceptionMessage);
    }

    return newInstance(exceptionClass);
  }

  private Throwable getCause()
  {
    if(causeClassName == null) {
      return null;
    }
    Class<? extends Throwable> causeClass = Classes.forOptionalName(causeClassName);
    if(causeClass == null) {
      return null;
    }

    if(causeMessage != null) {
      return newInstance(causeClass, causeMessage);
    }
    return newInstance(causeClass);
  }

  private static <T> T newInstance(Class<T> exceptionClass, Object... arguments)
  {
    Params.notNull(exceptionClass, "Exception class");

    if(arguments.length == 0) {
      try {
        return Classes.newInstance(exceptionClass);
      }
      catch(Throwable alsoUnused) {
        return null;
      }
    }

    try {
      return Classes.newInstance(exceptionClass, arguments);
    }
    catch(Throwable unused) {
      return newInstance(exceptionClass);
    }
  }

  @Override
  public String toString()
  {
    return Strings.toString(exceptionClassName, exceptionMessage);
  }
}
