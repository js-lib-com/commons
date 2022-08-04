package js.lang;

/**
 * A configurable instance can be configured with a {@link Config} object. Implementation of this interface gets a configuration
 * object and configure itself accordingly.
 * <p>
 * It is not specified if configuration is performed at object creation or warm reconfiguration. Implementation of this
 * interface may choose to support warm configuration but is not mandatory.
 * 
 * @author Iulian Rotaru
 */
public interface Configurable {
	/**
	 * Configure this configurable using a given configuration object. Configuration object is clearly dependent on this
	 * configurable needs; it is configurable responsibility to ensure configuration object is valid, accordingly its internal
	 * rules. If validation fails implementation should throw {@link ConfigException}.
	 * <p>
	 * Any exception that may occur into configuration process are bubbled up.
	 * 
	 * @param config configuration object.
	 * @throws ConfigException if given configuration object is not valid.
	 * @throws Exception if anything goes wrong on configuration process.
	 */
	void config(Config config) throws Exception;
}
