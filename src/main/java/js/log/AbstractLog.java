package js.log;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

/**
 * Basic implementation of logger interface. This base logger class implements almost entire logger functionality.
 * Logger provider should extend from this base class, rather that directly implementing {@link Log} interface, and
 * supply next methods:
 * <ul>
 * <li>{@link #isLoggable(LogLevel)}: to test if log level is loggable,
 * <li>{@link #log(LogLevel, String)}: to write log message if requested level is loggable,
 * <li>{@link #dump(Object, Throwable)}: to dump throwable stack trace to this logger using <code>fatal</code> level,
 * <li>{@link #print(LogLevel, String)}: to write message directly to logger with specified logging level.
 * </ul>
 * <p>
 * This class also provides utility methods for message normalization and ellipsis.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public abstract class AbstractLog implements Log
{
  /**
   * Test if log level is loggable.
   * 
   * @param level log level.
   * @return true if log level is loggable.
   */
  protected abstract boolean isLoggable(LogLevel level);

  /**
   * Write log message if requested level is loggable. If <code>message</code> argument is null this method is NOP.
   * 
   * @param level log level,
   * @param message log message, possible null.
   */
  protected abstract void log(LogLevel level, String message);

  @Override
  public void trace(Object message)
  {
    if(isLoggable(LogLevel.TRACE)) {
      log(LogLevel.TRACE, message(message));
    }
  }

  @Override
  public void trace(String message, Object... args)
  {
    if(isLoggable(LogLevel.TRACE)) {
      log(LogLevel.TRACE, format(message, args));
    }
  }

  @Override
  public void debug(Object message)
  {
    if(isLoggable(LogLevel.DEBUG)) {
      log(LogLevel.DEBUG, message(message));
    }
  }

  @Override
  public void debug(String message, Object... args)
  {
    if(isLoggable(LogLevel.DEBUG)) {
      log(LogLevel.DEBUG, format(message, args));
    }
  }

  @Override
  public void info(Object message)
  {
    if(isLoggable(LogLevel.INFO)) {
      log(LogLevel.INFO, message(message));
    }
  }

  @Override
  public void info(String message, Object... args)
  {
    if(isLoggable(LogLevel.INFO)) {
      log(LogLevel.INFO, format(message, args));
    }
  }

  @Override
  public void warn(Object message)
  {
    if(isLoggable(LogLevel.WARN)) {
      log(LogLevel.WARN, message(message));
    }
  }

  @Override
  public void warn(String message, Object... args)
  {
    if(isLoggable(LogLevel.WARN)) {
      log(LogLevel.WARN, format(message, args));
    }
  }

  @Override
  public void error(Object message)
  {
    if(isLoggable(LogLevel.ERROR)) {
      log(LogLevel.ERROR, message(message));
    }
  }

  @Override
  public void error(String message, Object... args)
  {
    if(isLoggable(LogLevel.ERROR)) {
      log(LogLevel.ERROR, format(message, args));
    }
  }

  @Override
  public void fatal(Object message)
  {
    if(isLoggable(LogLevel.FATAL)) {
      log(LogLevel.FATAL, message(message));
    }
  }

  @Override
  public void fatal(String message, Object... args)
  {
    if(isLoggable(LogLevel.FATAL)) {
      log(LogLevel.FATAL, format(message, args));
    }
  }

  @Override
  public void bug(String message, Object... args)
  {
    if(isLoggable(LogLevel.BUG)) {
      log(LogLevel.BUG, format(message, args));
    }
  }

  @Override
  public void bug(boolean expectedCondition, String message, Object... args)
  {
    if(isLoggable(LogLevel.BUG) && !expectedCondition) {
      log(LogLevel.BUG, format(message, args));
    }
  }

  /**
   * Normalize log message. This method returns message to string; if message is a Throwable and has not null cause
   * returns cause hierarchy formed from cause class name.
   * 
   * @param message log message.
   * @return normalized log message or null.
   */
  protected static String message(Object message)
  {
    if(message == null) {
      return null;
    }
    if(!(message instanceof Throwable)) {
      return message.toString();
    }

    Throwable t = (Throwable)message;
    if(t.getCause() == null) {
      return t.toString();
    }

    int nestingLevel = 0;
    StringBuilder sb = new StringBuilder();
    for(;;) {
      sb.append(t.getClass().getName());
      sb.append(":");
      sb.append(" ");
      if(++nestingLevel == 8) {
        sb.append("...");
        break;
      }
      if(t.getCause() == null) {
        String s = t.getMessage();
        if(s == null) {
          t.getClass().getCanonicalName();
        }
        sb.append(s);
        break;
      }
      t = t.getCause();
    }
    return sb.toString();
  }

  /**
   * Return formatted string with arguments injected or original message if format or arguments are invalid. This method
   * does not throw exception on bad format; it simply returns original message.
   * <p>
   * This method takes care to pre-process arguments as follow:
   * <ul>
   * <li>replace {@link Class} with its canonical name,
   * <li>replace {@link Throwable} with exception message or exception class canonical name if null message,
   * <li>replace {@link Thread} with concatenation of thread name and thread ID,
   * <li>replace {@link File} with file absolute path,
   * <li>dump first 3 items from arrays like argument.
   * </ul>
   * All pre-processed arguments are replaced with string value and format specifier should be also string (%s).
   * 
   * @param message formatted message,
   * @param args variable number of formatting arguments.
   * @return built string or original message if format or arguments are not valid.
   */
  private static String format(String message, Object... args)
  {
    if(message == null) {
      return null;
    }
    if(message.isEmpty()) {
      return "";
    }
    if(args.length == 0) {
      return message;
    }

    for(int i = 0; i < args.length; i++) {
      // at this point args[i] could be null, condition silently ignored by next branching logic
      // and this is because all branch tests are null safe
      // anyway, if add new branch test takes care to be also null safe or test explicitly for args[i] == null

      if(args[i] instanceof Class) {
        args[i] = ((Class<?>)args[i]).getCanonicalName();
      }
      else if(args[i] instanceof Throwable) {
        String s = ((Throwable)args[i]).getMessage();
        if(s == null) {
          s = args[i].getClass().getCanonicalName();
        }
        args[i] = s;
      }
      else if(args[i] instanceof Thread) {
        Thread thread = (Thread)args[i];
        StringBuilder sb = new StringBuilder();
        sb.append(thread.getName());
        sb.append(':');
        sb.append(thread.getId());
        args[i] = sb.toString();
      }
      else if(args[i] instanceof File) {
        args[i] = ((File)args[i]).getAbsolutePath();
      }
      else if(isArrayLike(args[i])) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        int index = 0;
        for(Object object : iterable(args[i])) {
          if(object == null) {
            continue;
          }
          String value = object instanceof String ? (String)object : object.toString();
          if(value.isEmpty()) {
            continue;
          }
          if(index++ > 0) {
            sb.append(',');
          }
          if(index == 4) {
            sb.append("...");
            break;
          }
          sb.append(object);
        }
        sb.append(']');
        args[i] = sb.toString();
      }
    }

    try {
      return String.format(message, args);
    }
    catch(Throwable unused) {
      // return unformatted message if format fails
      return message;
    }
  }

  /** Ellipsis constant. */
  private static final String ELLIPSIS = "...";

  /**
   * Ensure message is not larger than requested maximum length. If message length is larger than allowed size shorten
   * it and append ellipsis. This method guarantee maximum length is not exceed also when ellipsis is appended.
   * <p>
   * This method returns given <code>message</code> argument if smaller that requested maximum length or a new created
   * string with trailing ellipsis if larger.
   * 
   * @param message message string, possible null,
   * @param maxLength maximum allowed space.
   * @return given <code>message</code> argument if smaller that requested maximum length or new created string with
   *         trailing ellipsis.
   */
  protected static String ellipsis(String message, int maxLength)
  {
    if(message == null) {
      return "null";
    }
    return message.length() < maxLength ? message : message.substring(0, maxLength - ELLIPSIS.length()) + ELLIPSIS;
  }

  /**
   * An object is array like if is an actual array or a collection.
   * 
   * @param object object to test if array like.
   * @return true if <code>object</code> argument is array like.
   */
  private static boolean isArrayLike(Object object)
  {
    return object != null && (object.getClass().isArray() || object instanceof Collection);
  }

  /**
   * Create an iterator supplied via Iterable interface. If <code>object</code> argument is a collection just returns it
   * since collection is already iterbale. If is array, create an iterator able to traverse generic arrays.
   * 
   * @param object collection or array.
   * @return Iterable instance.
   */
  private static Iterable<?> iterable(final Object object)
  {
    // at this point object cannot be null and is array or collection

    if(object instanceof Iterable) {
      return (Iterable<?>)object;
    }

    // at this point object is an array
    // create a iterator able to traverse generic arrays

    return new Iterable<Object>()
    {
      private Object array = object;
      private int index;

      @Override
      public Iterator<Object> iterator()
      {
        return new Iterator<Object>()
        {
          @SuppressWarnings("unqualified-field-access")
          @Override
          public boolean hasNext()
          {
            return index < Array.getLength(array);
          }

          @SuppressWarnings("unqualified-field-access")
          @Override
          public Object next()
          {
            return Array.get(array, index++);
          }

          @Override
          public void remove()
          {
            throw new UnsupportedOperationException("Array iterator has no remove operation.");
          }
        };
      }
    };
  }
}
