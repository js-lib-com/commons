package js.rmi;

/**
 * Service provider for remote instance factories allows for {@link RemoteFactory} implementations as standard Java
 * service. Implementation of this service should provide a remote factory instance able to handle URL protocol declared
 * by {@link #getProtocol()}.
 * <p>
 * Being a Java service, implementation of this class should have a public default constructor and need to be declared
 * by distributed archive on <code>META-INF/services/js.rmi.RemoteFactoryProvider</code> file.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public interface RemoteFactoryProvider
{
  /**
   * Get URL protocol that {@link #getRemoteFactory()} is able to handle.
   * 
   * @return URL protocol handled by remote instance factory.
   */
  String getProtocol();

  /**
   * Get remote factory instance able to handle declared {@link #getProtocol()}.
   * 
   * @return remote factory for protocol.
   */
  RemoteFactory getRemoteFactory();
}
