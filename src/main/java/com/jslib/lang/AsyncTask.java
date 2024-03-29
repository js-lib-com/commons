package com.jslib.lang;

import java.util.function.Consumer;

import javax.security.auth.callback.Callback;

/**
 * Asynchronous tasks that may return a value. This class is the base class for tasks executed into a separated thread
 * and allowing for a return value. AsyncTask is abstract; concrete subclass should implement {@link #execute()}
 * abstract method.
 * <p>
 * There are basically two usage patterns for handling the returned value: first is to override
 * {@link #onPostExecute(Object)} like in sample code below. Please note that value handling hook is executed in the
 * same thread as task logic.
 * 
 * <pre>
 * AsyncTask&lt;Object&gt; task = new AsyncTask&lt;Object&gt;()
 * {
 *   protected Object execute() throws Throwable
 *   {
 *     // asynchronous task logic
 *   }
 * 
 *   protected void onPostExecute(Object value)
 *   {
 *     // take some actions on task returned value
 *     // executed in the same thread as execute()
 *   }
 * };
 * task.start();
 * </pre>
 * <p>
 * Second is supplying a {@link Callback} implementation to asynchronous task constructor. After {@link #execute()}
 * completes, {@link Callback#handle(Object)} is transparently invoked in the same thread on which task was executed.
 * 
 * <pre>
 * AsyncTask&lt;Object&gt; task = new AsyncTask&lt;Object&gt;(callback)
 * {
 *   protected Object execute() throws Throwable
 *   {
 *     // asynchronous task logic;
 *   }
 * };
 * task.start();
 * </pre>
 * <p>
 * Because of his nature, asynchronous logic exceptions are not directly accessible to caller thread. Once asynchronous
 * thread launched the caller thread has no chance to catch execution exceptions. To overcome this limitation this class
 * supply {@link #onThrowable(Throwable)} hook. If exception occur neither {@link #onPostExecute(Object)} nor
 * {@link Callback#handle(Object)} are invoked.
 * 
 * <pre>
 * AsyncTask&lt;Object&gt; task = new AsyncTask&lt;Object&gt;(callback)
 * {
 *   protected Object execute() throws Throwable
 *   {
 *     // asynchronous task logic;
 *   }
 * 
 *   protected void onThrowable(Throwable throwable)
 *   {
 *     // handle asynchronous execution exception
 *   }
 * };
 * task.start();
 * </pre>
 * 
 * @param <T> task returned value type.
 * @author Iulian Rotaru
 */
public abstract class AsyncTask<T>
{
  /**
   * Execute task logic and return a resulting value.
   * 
   * @return value resulting from task execution.
   * @throws Throwable any exception task logic may throw.
   */
  abstract protected T execute() throws Throwable;

  /** Asynchronous task listener. */
  protected Consumer<T> callback;

  /**
   * Constructor for tasks that may use {@link #onPostExecute(Object)} for returned value handling. See first usage
   * pattern from class description.
   */
  protected AsyncTask()
  {
  }

  /**
   * Constructor for task using callback for returning value handling. See the second usage pattern from class
   * description.
   * 
   * @param callback callback to be invoked on task completion.
   */
  protected AsyncTask(Consumer<T> callback)
  {
    this.callback = callback;
  }

  /** Start asynchronous task execution. */
  public void start()
  {
    Thread thread = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        try {
          onPreExecute();
          T value = execute();
          onPostExecute(value);
        }
        catch(Throwable throwable) {
          onThrowable(throwable);
        }
      }
    });
    thread.start();
  }

  /** Hook executed before task logic execution. This default implementation is just a placeholder, it does nothing. */
  protected void onPreExecute()
  {
  }

  /**
   * Task returned value handler. This hook is executed after task logic execution and its default action is to delegate
   * {@link #callback}, of course if one supplied to constructor.
   * 
   * @param value value returned by task logic.
   */
  protected void onPostExecute(T value)
  {
    if(callback != null) {
      callback.accept(value);
    }
  }

  /**
   * Hook executed when task logic fails. Concrete subclass may override this method and take recoverable actions. This
   * default implementation does nothing.
   * 
   * @param throwable exception generated by failed task logic.
   */
  protected void onThrowable(Throwable throwable)
  {
  }
}
