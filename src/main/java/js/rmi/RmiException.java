package js.rmi;

import java.net.URL;

/**
 * HTTP-RMI runtime exception.
 * 
 * @author Iulian Rotaru
 */
public class RmiException extends RuntimeException
{
  /** Java serialization version. */
  private static final long serialVersionUID = 9037909480686795160L;

  /**
   * Create remote method exception with exception root cause.
   * 
   * @param remoteMethodURL remote method URL,
   * @param remoteException exception root cause.
   */
  public RmiException(URL remoteMethodURL, RemoteException remoteException)
  {
    super(String.format("HTTP-RMI server execution error on |%s|: %s", remoteMethodURL.toExternalForm(), remoteException));
  }

  /**
   * Create remote method exception with exception root cause.
   * 
   * @param remoteMethodURL remote method URL,
   * @param remoteException exception root cause.
   */
  public RmiException(String remoteMethodURL, RemoteException remoteException)
  {
    super(String.format("HTTP-RMI server execution error on |%s|: %s", remoteMethodURL, remoteException));
  }

  /**
   * Create remote method exception with message.
   * 
   * @param remoteMethodURL remote method URL,
   * @param message exception message.
   */
  public RmiException(String remoteMethodURL, String message)
  {
    super(String.format("HTTP-RMI server execution error on |%s|: %s", remoteMethodURL, message));
  }

  /**
   * Create HTTP-RMI exception with formatted message.
   * 
   * @param message formatted message, as supported by {@link String#format(String, Object...)},
   * @param args optional arguments if message has formatted tags.
   */
  public RmiException(String message, Object... args)
  {
    super(String.format(message, args));
  }
}
