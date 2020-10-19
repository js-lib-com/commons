package js.log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Default logger used when logger implementation is missing. It sends logging messages to standard output.
 * 
 * @author Iulian Rotaru
 */
final class DefaultLog extends AbstractLog
{
  /** Log message date format. */
  private static final DateFormat DATE_FMT = new SimpleDateFormat("DDD HH:mm:ss.SSS");

  /** Logger name, usually the qualified name of the target class. */
  private String loggerName;

  /**
   * Create default logger with given logger name.
   * 
   * @param loggerName logger name.
   */
  public DefaultLog(String loggerName)
  {
    this.loggerName = loggerName;
  }

  @Override
  protected boolean isLoggable(LogLevel level)
  {
    return true;
  }

  @Override
  protected void log(LogLevel level, String message)
  {
    if(message != null) {
      System.out.printf("%s [%s] %s - %s\r\n", DATE_FMT.format(new Date()), level, loggerName, message);
    }
  }

  @Override
  public void dump(Object message, Throwable throwable)
  {
    if(isLoggable(LogLevel.FATAL)) {
      System.out.printf("%s [%s] %s - %s\r\n", DATE_FMT.format(new Date()), LogLevel.FATAL, loggerName, message);
      throwable.printStackTrace(System.out);
    }
  }

  @Override
  public void print(LogLevel level, String message)
  {
    if(isLoggable(level)) {
      System.out.printf("%s [%s] %s - %s\r\n", DATE_FMT.format(new Date()), level, loggerName, message);
    }
  }
}
