package js.lang;

import js.util.Params;

/**
 * Base looper implementation takes care of looper thread life cycle. Concrete looper extending this abstract class
 * should implement only the actual loop iteration logic. Both <code>continuous</code> and <code>periodic</code> modes
 * are supported; see {@link LooperThread} for details.
 * <p>
 * In sample code <code>Processor</code> uses a user defined iteration period. It is also possible to run into
 * continuous mode in which case {@link Looper#loop()} should wait for some IO events or explicit sleep.
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
 * @author Iulian Rotaru
 * @version final
 */
public abstract class AbstractLooper implements ManagedLifeCycle, Looper
{
  /** Looper thread created, started and stopped by this abstract looper. */
  protected final LooperThread thread;

  /**
   * Construct a looper instance operating in <code>continuous</code> modes. {@link Looper#loop()} implementation should
   * wait for some IO events or explicit sleep to avoid abusing the thread.
   */
  protected AbstractLooper()
  {
    this.thread = new LooperThread(this);
  }

  /**
   * Construct a looper instance operation in <code>periodic</code> mode.
   * 
   * @param period loop iteration period, in milliseconds.
   * @throws IllegalArgumentException if <code>period</code> argument is not strict positive.
   */
  protected AbstractLooper(int period)
  {
    Params.strictPositive(period, "Looper period");
    this.thread = new LooperThread(this, period);
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
