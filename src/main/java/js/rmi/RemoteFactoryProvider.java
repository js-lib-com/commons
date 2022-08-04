package js.rmi;

/**
 * Service provider for remote instance factories allows for {@link RemoteFactory} implementations as standard Java
 * service. Implementation of this service should provide a remote factory instance able to handle URL protocol declared
 * by {@link #getProtocols()}. Protocol syntax is implementation detail but is part of <code>implementationURL</code>
 * supplied to {@link RemoteFactory#getRemoteInstance(Class, String)}.
 * <p>
 * Being a Java service, implementation of this class should have a public default constructor and need to be declared
 * by distributed archive on <code>META-INF/services/js.rmi.RemoteFactoryProvider</code> file.
 * 
 * @author Iulian Rotaru
 */
public interface RemoteFactoryProvider
{
  /**
   * Get URL protocols that {@link #getRemoteFactory()} is able to handle.
   * 
   * @return URL protocols list handled by remote instance factory.
   */
  String[] getProtocols();

  /**
   * Get remote factory instance able to handle declared {@link #getProtocols()}.
   * 
   * @return remote factory for protocol.
   */
  RemoteFactory getRemoteFactory();
}
