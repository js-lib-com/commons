package com.jslib.lang;

import java.lang.reflect.InvocationTargetException;

/**
 * Unchecked alternative for Java invocation target exception.
 * 
 * @author Iulian Rotaru
 */
public class InvocationException extends RuntimeException
{
  /** Java serialization version. */
  private static final long serialVersionUID = -3068174098664182105L;

  /**
   * Create invocation exception with given target exception.
   * 
   * @param e source target exception.
   */
  public InvocationException(InvocationTargetException e)
  {
    super(e.getTargetException() != null ? e.getTargetException() : e.getCause());
  }

  /**
   * Create invocation exception with given cause.
   * 
   * @param cause exception root cause.
   */
  public InvocationException(Throwable cause)
  {
    super(cause);
  }
}
