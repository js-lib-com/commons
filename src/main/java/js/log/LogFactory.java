package js.log;

import java.util.ServiceLoader;

import js.lang.Config;

/**
 * Logging system facade. To simplify integration log factory is a utility class; it provides static methods and is not
 * instantiable. Log factory has a static reference to logger provider that is initialized at this class loading. If no
 * implementation found on run-time, provider is initialized with {@link DefaultLogProvider}. Provider instance is delegated for
 * most of this factory tasks.
 * 
 * <p>
 * Before to be used, a logging provider may require configuration. It is highly probable but still configuration step is not
 * mandatory. For configuration, log factory has {@link #config(Config)} that just passes configuration object to provider.
 * Configuration object structure is provider implementation detail. Provider is encourage to initialize itself with sensible
 * default configuration in case user code fails to invoke {@link #config(Config)}.
 * 
 * <p>
 * Log factory main job is to create named logger instances and for that delegates {@link LogProvider#getLogger(String)}. There
 * is also a convenient factory method to create logger per class, {@link #getLog(Class)}. In fact this is most common usage
 * pattern: every class has its own logger instance, see sample code.
 * 
 * <pre>
 * public class Sample {
 * 	private static final Log log = LogFactory.getLog(Sample.class);
 * 	...
 * }
 * </pre>
 * 
 * @author Iulian Rotaru
 * @version final
 */
public final class LogFactory {
	/** Logger implementation provider. */
	private static LogProvider provider = provider();

    /**
     * Load log provider from Java services and return the first instance found. It is expected to have only one log service
     * provider deployed on runtime; it more found just blindly select the first found.
     * <p>
     * Returns {@link DefaultLogProvider} if no log provider service found.
     * 
     * @return log provider instance.
     */
    private static LogProvider provider() {
        Iterable<LogProvider> providers = ServiceLoader.load(LogProvider.class);
        for (LogProvider provider : providers) {
            // for now ignore multiple implementations and choose blindly the first one
            return provider;
        }
        return new DefaultLogProvider();
    }

	/** Prevent default constructor synthesis. */
	private LogFactory() {
	}

	/**
	 * Configure logging provider from configuration object. Configuration object format is entirely under provider control;
	 * this method just pass it as it is.
	 * <p>
	 * If provider configuration fails, most probably because of bad configuration, reset provider to default.
	 * 
	 * @param config configuration object.
	 */
	public static void config(Config config) {
		try {
			provider.config(config);
		} catch (Throwable t) {
			provider = new DefaultLogProvider();

			Log log = provider.getLogger(LogFactory.class.getName());
			log.error("Fail on logger provider configuration. Reset logging system to default provider.");
			log.dump("Logging configuration stack dump:", t);
		}
	}

	/**
	 * Create a named logger. By convention logger name is the name of the class for which it is created. This method is here
	 * for completeness and expected to be used in special cases. This method delegates {@link LogProvider#getLogger(String)}.
	 * 
	 * @param loggerName logger name.
	 * @return newly created logger.
	 */
	public static Log getLog(String loggerName) {
		return provider.getLogger(loggerName);
	}

	/**
	 * Create logger for given class. This method just delegates {@link LogProvider#getLogger(String)} with class name as
	 * argument.
	 * 
	 * @param targetClass class to create logger for.
	 * @return newly created class logger.
	 */
	public static Log getLog(Class<?> targetClass) {
		return provider.getLogger(targetClass.getName());
	}

	/**
	 * Get logger context usable to bind diagnostic data to current thread.
	 * 
	 * @return logger diagnostic context.
	 */
	public static LogContext getLogContext() {
		return provider.getLogContext();
	}
}
