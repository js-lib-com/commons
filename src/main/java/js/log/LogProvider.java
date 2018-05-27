package js.log;

import js.lang.Configurable;

/**
 * Log service provider interface. Service provider should only create logger instances. Since it is configurable provider
 * should implement {@link Configurable#config(js.lang.Config)}. Configuration object structure is implementation detail.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public interface LogProvider extends Configurable {
	/**
	 * Create named logger instance.
	 * 
	 * @param loggerName logger name.
	 * @return logger instance.
	 */
	Log getLogger(String loggerName);

	/**
	 * Get the instance of logger context usable to store diagnostic context data on current thread.
	 * 
	 * @return logger context instance.
	 */
	LogContext getLogContext();
}
