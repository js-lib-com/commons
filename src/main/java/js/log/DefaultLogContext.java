package js.log;

/**
 * Default log context does nothing.
 * 
 * @author Iulian Rotaru
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
