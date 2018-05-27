/**
 * Logging system. This package contains both service providers interface and client API. At a minimum
 * service provider should implement {@link js.log.LogProvider} and {@link js.log.AbstractLog}. 
 * <p>
 * Client usage is classic: uses {@link js.log.LogFactory} to get named logger instance the operates on
 * it.
 * <pre>
 * public class Sample {
 * 	private static final Log log = LogFactory.getLog(Sample.class);
 * 	...
 * 	public void method() {
 * 		log.trace("method()");
 * 		...
 * 	}
 * }
 * </pre>
 * 
 * <h3>Formatted Log</h3>
 * Is not uncommon to need adding variables to log records and often it is solved using string concatenation.
 * The problem with string concatenation is it is performed even if log level is disabled. Solution to this problem
 * is to wrap into log level enabled check resulting in more verbose code.
 * <pre>
 *	if(log.isEnabledFor(Level.DEBUG)) {
 *		log.debug("Class loaded " + class.getName());
 *	}
 * </pre>
 * instead of
 * <pre>
 *	log.debug("Class loaded %s.", class.getName());
 * </pre>
 * The second solution executes string formatting only if log level is enabled but check for level is performed into logger writer.
 *
 * @author Iulian Rotaru
 * @version final
 */
package js.log;

