package js.lang;

import js.util.Params;

/**
 * Base looper implementation takes care of looper thread life cycle. Concrete looper extending this abstract class
 * should implement only the actual loop iteration logic. Both <code>continuous</code> and <code>periodic</code> modes
 * are supported; see {@link LooperThread} for details.
 * <p>
 * In sample code <code>Processor</code> uses a user defined iteration period. It is also possible to run into
 * continuous mode - PERIOD is explicitly set to zero, in which case {@link Looper#loop()} should wait for some IO
 * events or explicit sleep.
 * 
 * <pre>
 * public class Processor extends AbstractLooper
 * {
 *   public Processor()
 *   {
 *     super(PERIOD);
 *   }
 * 
 *   &#064;Override
 *   public void loop() throws IOException
 *   {
 *     // implements loop iteration logic
 *   }
 * }
 * </pre>
 * 
 * <p>
 * By default, when exception occurs on implementation logic, looper record the event to application logger and continue
 * running. If is preferable to stop running on exception uses the constructor {@link #AbstractLooper(int, boolean)}.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public abstract class AbstractLooper implements ManagedLifeCycle, Looper
{
  /** Looper thread created, started and stopped by this abstract looper. */
  protected final LooperThread thread;

  /**
   * Construct a looper instance with given <code>period</code> and no break on exception. Period argument can be zero,
   * in which case looper operates in continuous mode. In this mode is implementation responsibility to ensure thread is
   * not abused.
   * 
   * @param period loop iteration period, possible 0, in milliseconds.
   * @throws IllegalArgumentException if <code>period</code> argument is not strict positive.
   */
  protected AbstractLooper(int period)
  {
    this(period, false);
  }

  /**
   * Construct a looper instance with given <code>period</code> and <code>no break on exception</code> flag. Period
   * argument can be zero, in which case looper operates in continuous mode. In this mode is implementation
   * responsibility to ensure thread is not abused.
   * 
   * @param period loop iteration period, possible 0, in milliseconds,
   * @param breakOnException if true, looper breaks execution when its logic generates exception.
   * @throws IllegalArgumentException if <code>period</code> argument is not strict positive.
   */
  protected AbstractLooper(int period, boolean breakOnException)
  {
    Params.positive(period, "Looper period");
    this.thread = new LooperThread(this, period, breakOnException);
  }

  /**
   * Start looper thread. This post-construct hook just starts this looper thread. Subclass may need to add application
   * specific initialization and override this method but should explicitly invoke super. Otherwise looper thread is not
   * started.
   * 
   * @throws Exception exceptions from application defined post-construct logic are bubbled up.
   */
  @Override
  public void postConstruct() throws Exception
  {
    thread.start();
  }

  /**
   * Stop looper thread. This pre-destroy hook just stops this looper thread. Subclass may need to defined specific
   * clean-up logic. It can override this hook but should explicitly invoke super. Otherwise looper thread is not
   * stopped and runs indefinitely.
   * 
   * @throws Exception exceptions from application defined clean-up logic are bubbled up.
   */
  @Override
  public void preDestroy() throws Exception
  {
    thread.stop();
  }
}
