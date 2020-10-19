package js.log;

import js.lang.Config;
import js.lang.ConfigException;

/**
 * Default log provider creates {@link DefaultLog} instances.
 * 
 * @author Iulian Rotaru
 */
final class DefaultLogProvider implements LogProvider
{
  private final LogContext logContext = new DefaultLogContext();

  @Override
  public void config(Config config) throws ConfigException
  {
  }

  @Override
  public Log getLogger(String loggerName)
  {
    return new DefaultLog(loggerName);
  }

  @Override
  public LogContext getLogContext()
  {
    return logContext;
  }

  @Override
  public void forceImmediateFlush()
  {
    // default logger is already synchronous; nothing to do here
  }
}