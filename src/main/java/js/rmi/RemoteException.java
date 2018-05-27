package js.rmi;

import js.util.Strings;

/**
 * Data transport object used to convey information about exceptional condition at server level.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public final class RemoteException
{
  /** Remote exception cause. */
  private final String cause;

  /** Remote exception message. */
  private final String message;

  /** Test constructor. */
  public RemoteException()
  {
    this.cause = null;
    this.message = null;
  }

  /**
   * Construct immutable remote exception instance.
   * 
   * @param target exception root cause.
   */
  public RemoteException(Throwable target)
  {
    this.cause = target.getClass().getCanonicalName();
    this.message = target.getMessage();
  }

  /**
   * Get the class of the exception that causes this remote exception.
   * 
   * @return exception cause class.
   */
  public String getCause()
  {
    return cause;
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

  @Override
  public String toString()
  {
    return Strings.concat(cause, ": ", message);
  }
}
