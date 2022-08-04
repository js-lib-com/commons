package js.lang;

import js.util.Params;

/**
 * Thread that executes {@link Looper#loop()} with configurable iteration period. Looper thread has convenient methods
 * to start and stop thread execution and supports two iteration modes: <code>continuous</code> and
 * <code>periodic</code>.
 * <p>
 * In <code>continuous</code> mode looper thread executes loop iterations with processor speed. Looper instance is
 * usually waiting for some IO, takes care itself to sleep for some time or really has much computation to do. It is
 * looper instance responsibility to ensure processor is not abused.
 * <p>
 * To enable <code>periodic</code> mode one should use a constructor that supplies a period value. Looper thread
 * executes internal sleep and invoke iteration with requested period. Implementation takes care to compensate for
 * iteration processing time but if it takes more that requested period, looper thread works again in continuous mode.
 * <p>
 * In any case, looper implementation should consider that thread checks for running state between loops - for this
 * reason blocking not interruptible operations are not allowed.
 * <p>
 * For your convenience here is an usage sample code.
 * 
 * <pre>
 * class DemoLooper implements Looper, AsyncExceptionListener
 * {
 *   private LooperThread looper;
 * 
 *   DemoLooper()
 *   {
 *     looper = new LooperThread(this);
 *     looper.start();
 *   }
 * 
 *   void stop()
 *   {
 *     looper.stop();
 *   }
 * 
 *   void loop() throws Exception
 *   {
 *     // execute iteration logic
 *   }
 * 
 *   void onAsyncException(Throwable throwable)
 *   {
 *     // handle loop iteration exception
 *   }
 * }
 * </pre>
 * <p>
 * Since looper is running in a separated thread is not possible to catch exceptions. If interested in handling loop
 * iteration exception one should make its looper implementing {@link AsyncExceptionListener}. It is not possible to set
 * exception listener after looper thread start.
 * <p>
 * Looper thread runs till explicit {@link #stop()}. This is true even if a loop execution throws exception; default
 * behavior is to invoke {@link AsyncExceptionListener#onAsyncException(Throwable)} and continue loop iterations.
 * Anyway, if desirable to break the loop on exception one can use {@link #LooperThread(Looper, int, boolean)}
 * constructor to set {@link #breakOnException} flag to true.
 * 
 * @author Iulian Rotaru
 */
public class LooperThread implements Runnable
{
  /** Looper thread start-up timeout. */
  private static final int STARTUP_TIMEOUT = 10000;

  /**
   * Looper thread stop timeout. If this timeout exceeds {@link #stop()} method returns without ensuring thread graceful
   * close. Note that {@link Looper#loop()} processing duration should not exceed this timeout.
   */
  private static final int STOP_TIMEOUT = 6000;

  /**
   * If looper thread works in <code>continuous</code> mode add a timeout on exception loop in order to avoid hogging
   * the thread.
   */
  private static final int EXCEPTION_TIMEOUT = 4000;

  /**
   * Looper tests {@link #running} flag with this sampling period, expressed in milliseconds. Anyway, if loop period is
   * smaller uses it instead.
   */
  private static final int SAMPLING_PERIOD = 1000;

  /** Underlying Java Thread. */
  private final Thread thread;

  /** Looper instance to invoke repetitively. */
  private final Looper looper;

  /** break thread looper on exception, default to false. */
  private final boolean breakOnException;

  /** Asynchronous exception listener. */
  private final AsyncExceptionListener exceptionListener;

  /**
   * Thread running state used by looper start and stop logic. Since both {@link #start()} and {@link #stop()} methods
   * are synchronized there is no need to use volatile on this flag.
   */
  private boolean running;

  /** Loop iteration period in milliseconds. */
  private int loopPeriod;

  /**
   * Keep track of the previous exception in order to prevent exceptions to occur to often. It is used only in
   * continuous mode, that is, period is zero. Also, if {@link #breakOnException} flag is true this value is not
   * relevant.
   */
  private long exceptionTimestamp;

  /**
   * Create looper thread with iteration period. Iteration <code>period</code> is expressed in milliseconds and should
   * be positive; if zero, looper thread is configured in continuous mode. If looper instance implements
   * {@link AsyncExceptionListener} it is also registered as exception listener.
   * 
   * @param looper looper instance,
   * @param period loop iteration period, in milliseconds, possible zero.
   * @throws IllegalArgumentException if looper instance is null or period is not positive or zero.
   */
  public LooperThread(Looper looper, int period)
  {
    this(looper, period, false);
  }

  public LooperThread(Looper looper, int period, boolean breakOnException)
  {
    Params.notNull(looper, "Looper reference");
    Params.positive(period, "Loop period");
    this.looper = looper;
    this.loopPeriod = period;

    this.thread = new Thread(this, looper.getClass().getName());
    this.thread.setDaemon(true);

    if(looper instanceof AsyncExceptionListener) {
      this.exceptionListener = (AsyncExceptionListener)looper;
    }
    else {
      this.exceptionListener = null;
    }
    this.breakOnException = breakOnException;
  }

  public int getLoopPeriod()
  {
    return loopPeriod;
  }

  public void setLoopPeriod(int loopPeriod)
  {
    this.loopPeriod = loopPeriod;
  }

  /**
   * Start the thread and wait for its actual startup. This method can be called only once per looper instance.
   * 
   * @throws IllegalStateException if attempt to start and already running looper.
   */
  public synchronized void start()
  {
    if(running) {
      throw new IllegalStateException("Cannot alter a running looper state.");
    }
    running = true;
    thread.start();
    wait(this, STARTUP_TIMEOUT);
  }

  /**
   * Stop thread and waits for its graceful close. Please note that if {@link #STOP_TIMEOUT} exceeds this method does
   * not guarantee graceful thread close.
   * <p>
   * If thread is not alive, see {@link Thread#isAlive()}, this method does nothing.
   */
  public synchronized void stop()
  {
    running = false;
    if(thread.isAlive()) {
      thread.interrupt();
      wait(this, STOP_TIMEOUT);
    }
  }

  /**
   * Return true if this looper thread is running.
   * 
   * @return true if looper thread is running.
   */
  public boolean isRunning()
  {
    return running;
  }

  @Override
  public void run()
  {
    // notify parent thread so that #start() method can finish
    synchronized(this) {
      notify();
    }
    // next iteration timestamp, used only if period is not zero
    long nextIterationTimestamp = 0;
    // millisecond for looper thread to sleep till next iteration, in milliseconds
    long sleepingPeriod = 0;

    while(running) {
      if(loopPeriod > 0) {
        nextIterationTimestamp = System.currentTimeMillis() + loopPeriod;
      }

      try {
        looper.loop();
      }
      catch(final Throwable throwable) {
        if(throwable instanceof InterruptedException) {
          continue;
        }

        if(exceptionListener != null) {
          exceptionListener.onAsyncException(throwable);
        }
        if(breakOnException) {
          break;
        }

        if(loopPeriod == 0) {
          // if period is zero, looper thread is working in continuous mode and is possible that loop exception to
          // occur at every iteration
          // to prevent wasting resources add timeout after exception but only if previous was too recent
          if(System.currentTimeMillis() - exceptionTimestamp < EXCEPTION_TIMEOUT) {
            sleep(EXCEPTION_TIMEOUT);
          }
          exceptionTimestamp = System.currentTimeMillis();
        }
      }

      if(loopPeriod > 0) {
        for(; running;) {
          sleepingPeriod = nextIterationTimestamp - System.currentTimeMillis();
          if(sleepingPeriod <= 0) {
            break;
          }
          sleep(Math.min(sleepingPeriod, SAMPLING_PERIOD));
        }
      }
    }

    // notify caller thread so that #stop() method can finish but first ensure log trace is written
    synchronized(this) {
      notify();
    }
  }

  /**
   * Waits at most millis milliseconds for this thread to die. A timeout of 0 means to wait forever.
   * 
   * @param millis the time to wait in milliseconds.
   * @throws InterruptedException if any thread has interrupted the current thread. The interrupted status of the
   *           current thread is cleared when this exception is thrown.
   */
  public void join(long millis) throws InterruptedException
  {
    thread.join(millis);
  }

  // --------------------------------------------------------------------------------------------
  // UTILITY METHODS

  /**
   * Wait notification on synchronized object for specified period of time. If timeout is zero this method blocks till
   * notification arrive.
   * 
   * @param lock synchronized object to wait on, possible zero,
   * @param timeout timeout in milliseconds.
   */
  private static void wait(Object lock, int timeout)
  {
    try {
      lock.wait(timeout);
    }
    catch(InterruptedException unused) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Interruptible thread sleep.
   * 
   * @param period thread sleep period.
   */
  private static void sleep(long period)
  {
    try {
      Thread.sleep(period);
    }
    catch(InterruptedException unused) {}
  }
}
