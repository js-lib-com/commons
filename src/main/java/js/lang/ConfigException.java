package js.lang;

/**
 * Configuration object is not well formed or is not valid. This exception is thrown by configurable objects when
 * configuration is not well formed or does not suits.
 * 
 * @author Iulian Rotaru
 */
public class ConfigException extends Exception
{
  /** Java serialization version. */
  private static final long serialVersionUID = 8278811819295726845L;

  /**
   * Create configuration exception for specified cause.
   * 
   * @param cause exception cause.
   */
  public ConfigException(Throwable cause)
  {
    super(cause);
  }

  /**
   * Create configuration exception with formatted message. Message may be formatted as supported by Java String format
   * in which case adequate arguments should be provided. Plain text message is supported.
   * 
   * @param message formatted message, possible null,
   * @param args optional arguments when message contains formats.
   */
  public ConfigException(String message, Object... args)
  {
    super(message != null ? String.format(message, args) : message);
  }
}
