package com.jslib.rmi;

/**
 * Exception thrown by {@link RemoteFactory} if URL protocol is not supported.
 * 
 * @author Iulian Rotaru
 */
public class UnsupportedProtocolException extends RuntimeException
{
  /** Java serialization version. */
  private static final long serialVersionUID = -8593611031628063965L;

  /**
   * Create exception with, optionally formatted, message. See {@link String#format(String, Object...)} for supported
   * format designators.
   * 
   * @param message formatted message,
   * @param args optional arguments, if message is formatted.
   */
  public UnsupportedProtocolException(String message, Object... args)
  {
    super(String.format(message, args));
  }

  /**
   * Create unsupported protocol exception with given root cause. Message can be formatted, see
   * {@link String#format(String, Object...)} for supported format designators.
   * 
   * @param cause exception root cause,
   * @param message formatted message,
   * @param args optional arguments, if message is formatted.
   */
  public UnsupportedProtocolException(Throwable cause, String message, Object... args)
  {
    super(String.format(message, args), cause);
  }

  /**
   * Create unsupported protocol exception with given root cause.
   * 
   * @param cause exception root cause.
   */
  public UnsupportedProtocolException(Throwable cause)
  {
    super(cause);
  }
}
