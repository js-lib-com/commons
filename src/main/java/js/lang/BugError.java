package js.lang;

/**
 * Thrown for conditions that are clearly out of normal code logic, that is, very likely to be a bug. For example, not
 * handled cause in a switch statement, bad format for hard coded URLs, missing UTF-8 support from JVM, illegal
 * reflexive access on field with accessibility set, etc.
 * <p>
 * This error is for developer only. It is not expected ever to happen on production server. For this reason it is not
 * normally caught.
 * 
 * @author Iulian Rotaru
 */
public class BugError extends Error
{
  /** Java serialization version. */
  private static final long serialVersionUID = 2638028558574291957L;

  /**
   * Constructor from formatted message. See {@link String#format(String, Object...)} for format description.
   * 
   * @param message exception formatted message,
   * @param args variable number of arguments for formatted message.
   */
  public BugError(String message, Object... args)
  {
    super(String.format(message, args));
  }

  /**
   * Constructor from root cause.
   * 
   * @param cause root cause.
   */
  public BugError(Throwable cause)
  {
    super(cause);
  }

  /**
   * Constructor with message and root cause.
   * 
   * @param message exception context messages,
   * @param cause root cause.
   */
  public BugError(String message, Throwable cause)
  {
    super(message, cause);
  }
}
