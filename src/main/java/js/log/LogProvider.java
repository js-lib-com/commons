package js.log;

import js.lang.Configurable;

/**
 * Log service provider interface. Service provider should only create logger instances. Since it is configurable
 * provider should implement {@link Configurable#config(js.lang.Config)}. Configuration object structure is
 * implementation detail. Also, implementation is free to ignore configuration object and to configure itself from
 * external sources.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public interface LogProvider extends Configurable
{
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

  /**
   * Force all appenders to immediate flush all written messages to target media. This method is only one way; there is
   * no option to undo its effect. It is intended to be called on container / application destruction to ensure there
   * are no messages loss.
   */
  void forceImmediateFlush();
}
