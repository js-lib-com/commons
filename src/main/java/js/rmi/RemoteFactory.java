package js.rmi;

/**
 * This factory creates a Java proxy instance for a remotely deployed class. In order to to create remote instance one
 * needs to know implementation context URL and remote interface. Created Java proxy should be able to process given URL
 * protocol; implementation should throw exception if protocol is not supported. At a minimum implementation is required
 * to support <code>HTTP</code> protocol, both plain text and secure variants.
 * <p>
 * In sample code, creates a Java proxy for weather service deployed on <code>http://weather.com/</code>. When invoke a
 * method, proxy takes care to create HTTP-RMI transaction that deals with arguments and value marshaling. This way a
 * remote instance is used as a local one.
 * 
 * <pre>
 * interface WeatherService {
 *    Weather getCurrentWeather(double latitude, double longitude);
 * }
 * ...
 * String implementationURL = &quot;http://weather.com/&quot;;
 * WeatherService service = remoteFactory.getInstance(implementationURL, WeatherService.class);
 * Weather weather = service.getCurrentWeather(47.1569, 27.5903);
 * </pre>
 * 
 * Implementation will return a Java proxy instance able to send class and method names and invocation arguments to
 * remote server and to retrieve returned value, using the protocol requested by given URL. It is not required to
 * perform URL validity and is caller responsibility to ensure given implementation context URL points to and existing
 * remote class and that remote class actually implements given interface.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public interface RemoteFactory
{
  /**
   * Create a Java proxy for a remote deployed class located at specified URL. Remote class should implement requested
   * interface. Returned proxy should be able to handle given URL protocol and throws exception if protocol is not
   * supported.
   * <p>
   * Implementation is not required to perform URL validity tests and is caller responsibility to ensure given URL value
   * points to and existing remote class. This factory method just create local proxy instance and does not throw
   * exceptions. If given arguments does not match an existing remote class there will be exception on actual remote
   * method invocation.
   * 
   * @param interfaceClass interface implemented by remote class,
   * @param implementationURL remote class context URL.
   * @param <T> instance type.
   * @return Java Proxy instance for given interface.
   * @throws UnsupportedProtocolException if URL protocol is not supported or arguments are otherwise not valid or null
   *           - in which case root cause has details about erroneous argument.
   */
  <T> T getRemoteInstance(Class<? super T> interfaceClass, String implementationURL) throws UnsupportedProtocolException;
}
