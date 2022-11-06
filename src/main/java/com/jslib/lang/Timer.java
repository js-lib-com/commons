package com.jslib.lang;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

/**
 * Scheduler for periodic and timeout tasks. If task execution throws exception this scheduler takes care to invoke
 * {@link AsyncExceptionListener}, of course if application registers one.
 * <p>
 * This class supports two kinds of tasks: executed periodic with a given <code>period</code> and execute once, after a
 * certain <code>timeout</code> expires. Any pending task can be canceled, see <code>purge</code> methods.
 * 
 * <pre>
 * Timer timer = new Timer();
 * timer.period(this, period);
 * . . .
 * timer.purge(this);
 * </pre>
 * 
 * @author Iulian Rotaru
 */
public final class Timer
{
  private final AsyncExceptionListener exceptionListener;

  /** Java {@link java.util.Timer} instance. */
  private final java.util.Timer timer;

  /** Pending user defined tasks mapped to Java {@link TimerTask}. */
  private final Map<Object, TimerTask> tasks;

  public Timer()
  {
    this(null);
  }

  public Timer(AsyncExceptionListener exceptionListener)
  {
    this.exceptionListener = exceptionListener;
    this.timer = new java.util.Timer();
    this.tasks = new HashMap<>();
  }

  public synchronized void destroy()
  {
    tasks.values().forEach(task -> task.cancel());
    tasks.clear();
    timer.cancel();
  }

  /**
   * Schedule periodic task execution.
   * 
   * @param periodicTask periodic task instance,
   * @param period requested execution period, milliseconds.
   */
  public synchronized void period(long period, PeriodicTask periodicTask)
  {
    TimerTask task = new PeriodicTaskImpl(periodicTask);
    tasks.put(periodicTask, task);
    timer.schedule(task, 0L, period);
  }

  /**
   * Schedule timeout task to be executed once, after requested timeout value expired.
   * 
   * @param timeout timeout value, milliseconds,
   * @param timeoutTask timeout task.
   */
  public synchronized void timeout(long timeout, TimeoutTask timeoutTask)
  {
    TimerTask task = new TimeoutTaskImpl(timeoutTask);
    tasks.put(timeoutTask, task);
    timer.schedule(task, timeout);
  }

  /**
   * Purge periodic task. If given periodic task is not scheduled this method is NOP.
   * 
   * @param periodicTask periodic task instance.
   */
  public synchronized void purge(PeriodicTask periodicTask)
  {
    purgeTask(periodicTask);
  }

  /**
   * Purge timeout task. If timeout task is not scheduled this method is NOP.
   * 
   * @param timeoutTask timeout task instance.
   */
  public synchronized void purge(TimeoutTask timeoutTask)
  {
    purgeTask(timeoutTask);
  }

  /**
   * Purge task helper method. If given <code>task</code> is not scheduled this method does nothing.
   * 
   * @param task pending user defined task.
   */
  private void purgeTask(Object task)
  {
    TimerTask timerTask = tasks.get(task);
    if(timerTask != null) {
      timerTask.cancel();
      tasks.values().remove(timerTask);
    }
  }

  // ----------------------------------------------------
  // TASK CLASS IMPLEMENTATIONS

  /**
   * Periodic task implementation.
   * 
   * @author Iulian Rotaru
   */
  private class PeriodicTaskImpl extends TimerTask
  {
    private PeriodicTask periodicTask;

    public PeriodicTaskImpl(PeriodicTask periodicTask)
    {
      super();
      this.periodicTask = periodicTask;
    }

    @Override
    public void run()
    {
      try {
        this.periodicTask.onPeriod();
      }
      catch(Throwable throwable) {
        if(exceptionListener != null) {
          exceptionListener.onAsyncException(throwable);
        }
      }
    }
  }

  /**
   * Timeout task implementation.
   * 
   * @author Iulian Rotaru
   */
  private class TimeoutTaskImpl extends TimerTask
  {
    private TimeoutTask timeoutTask;

    public TimeoutTaskImpl(TimeoutTask timeoutTask)
    {
      super();
      this.timeoutTask = timeoutTask;
    }

    @Override
    public void run()
    {
      try {
        this.timeoutTask.onTimeout();
      }
      catch(Throwable throwable) {
        if(exceptionListener != null) {
          exceptionListener.onAsyncException(throwable);
        }
      }
    }
  }
}
