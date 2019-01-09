package js.log;

/**
 * Default log context does nothing.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public class DefaultLogContext implements LogContext
{
  @Override
  public void put(String name, Object value)
  {
  }

  @Override
  public void clear()
  {
  }
}
