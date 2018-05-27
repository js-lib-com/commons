package js.converter;

/**
 * Generic converters exception thrown when a converter implementation fails to do its job.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public class ConverterException extends RuntimeException
{
  /** Java serialization version. */
  private static final long serialVersionUID = 3767217155714395093L;

  /**
   * Create converter exception with formatted message. See {@link String#format(String, Object...)} for supported
   * formatting tags.
   * 
   * @param message formatted exception message,
   * @param args optional arguments if message contains formatting tags.
   */
  public ConverterException(String message, Object... args)
  {
    super(String.format(message, args));
  }

  /**
   * Create converter exception of given cause.
   * 
   * @param cause exception root cause.
   */
  public ConverterException(Throwable cause)
  {
    super(cause);
  }
}
