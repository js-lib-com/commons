package js.lang.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import js.lang.AbstractLooper;
import js.lang.AsyncExceptionListener;
import js.lang.Looper;
import js.lang.LooperThread;
import js.util.Classes;

import org.junit.Test;

public class LooperThreadUnitTest
{
  @Test
  public void looperThreadConstructor()
  {
    LooperThread looper = new LooperThread(new Looper()
    {
      @Override
      @Test
      public void loop() throws Exception
      {
      }
    });

    assertNotNull(Classes.getFieldValue(looper, "looper"));
    assertNotNull(Classes.getFieldValue(looper, "thread"));
    assertFalse(((Thread)Classes.getFieldValue(looper, "thread")).isAlive());
    assertEquals(0, Classes.getFieldValue(looper, "loopPeriod"));
    assertFalse(((AtomicBoolean)Classes.getFieldValue(looper, "running")).get());
    assertFalse(((AtomicBoolean)Classes.getFieldValue(looper, "breakOnException")).get());
    assertNull(Classes.getFieldValue(looper, "exceptionListener"));
  }

  /**
   * If looper instance given to constructor implements {@link AsyncExceptionListener} exception listener field is
   * initialized.
   */
  @Test
  public void looperThreadContructorWithExceptionListener()
  {
    class MockLooper implements Looper, AsyncExceptionListener
    {
      @Override
      @Test
      public void onAsyncException(Throwable throwable)
      {
      }

      @Override
      @Test
      public void loop() throws Exception
      {
      }
    }

    LooperThread looper = new LooperThread(new MockLooper());
    assertNotNull(Classes.getFieldValue(looper, "exceptionListener"));
  }

  @Test
  public void looperThreadSetters()
  {
    LooperThread looper = new LooperThread(new Looper()
    {
      @Override
      @Test
      public void loop() throws Exception
      {
      }
    });
    assertFalse(((AtomicBoolean)Classes.getFieldValue(looper, "breakOnException")).get());
    assertNull(Classes.getFieldValue(looper, "exceptionListener"));

    looper.setBreakOnException(true);
    looper.setExceptionListener(new AsyncExceptionListener()
    {
      @Override
      @Test
      public void onAsyncException(Throwable throwable)
      {
      }
    });
    assertTrue(((AtomicBoolean)Classes.getFieldValue(looper, "breakOnException")).get());
    assertNotNull(Classes.getFieldValue(looper, "exceptionListener"));

    looper.setBreakOnException(false);
    looper.setExceptionListener(null);
    assertFalse(((AtomicBoolean)Classes.getFieldValue(looper, "breakOnException")).get());
    assertNull(Classes.getFieldValue(looper, "exceptionListener"));
  }

  @Test
  public void looperThreadStartLooperThread() throws InterruptedException
  {
    LooperThread looper = new LooperThread(new Looper()
    {
      @Override
      @Test
      public void loop() throws Exception
      {
        Thread.sleep(1000);
      }
    });
    long timestamp = System.currentTimeMillis();
    looper.start();
    Thread thread = Classes.getFieldValue(looper, "thread");

    assertNotNull(thread);
    assertTrue(thread.isDaemon());
    assertTrue(thread.isAlive());
    assertTrue("Looper thread start timeout suspiccion.", System.currentTimeMillis() - timestamp < 2000);
  }

  @Test
  public void looperThreadStopLooperThread() throws InterruptedException
  {
    long timestamp = System.currentTimeMillis();
    LooperThread looper = new LooperThread(new Looper()
    {
      @Override
      @Test
      public void loop() throws Exception
      {
        Thread.sleep(1000);
      }
    });
    looper.start();
    Thread.sleep(500);

    looper.stop();

    Thread thread = Classes.getFieldValue(looper, "thread");
    thread.join();
    assertTrue("Looper thread stop timeout suspiccion.", System.currentTimeMillis() - timestamp < 2000);
  }

  @Test
  public void looperThreadContinuousMode() throws InterruptedException
  {
    final AtomicInteger counter = new AtomicInteger();
    LooperThread thread = new LooperThread(new Looper()
    {
      @Override
      @Test
      public void loop() throws Exception
      {
        counter.incrementAndGet();
        Thread.sleep(1);
      }
    });
    thread.start();
    Thread.sleep(100);
    thread.stop();
    assertTrue(counter.get() > 0 && counter.get() <= 200);
  }

  @Test
  public void looperThreadPeriodicMode() throws InterruptedException
  {
    final AtomicInteger counter = new AtomicInteger();
    LooperThread thread = new LooperThread(new Looper()
    {
      @Override
      @Test
      public void loop() throws Exception
      {
        counter.incrementAndGet();
      }
    }, 1);
    thread.start();
    Thread.sleep(100);
    thread.stop();
    assertTrue(counter.get() > 0 && counter.get() <= 200);
  }

  @Test
  public void looperThreadLongPeriodicMode() throws InterruptedException
  {
    final AtomicInteger counter = new AtomicInteger();
    LooperThread thread = new LooperThread(new Looper()
    {
      @Override
      @Test
      public void loop() throws Exception
      {
        counter.incrementAndGet();
      }
    }, 10000);
    thread.start();
    Thread.sleep(100);
    thread.stop();
    assertTrue(counter.get() > 0 && counter.get() <= 200);
  }

  /**
   * Create a continuous looper thread and left break on exception to false. From loop iteration increments a counter
   * and throws exception. First exception set internal exception timestamp; the second comes too soon an triggers
   * exception thread timeout. As a consequence counter should remain at 2.
   */
  @Test
  public void looperThreadNotBreakOnException() throws InterruptedException
  {
    final AtomicInteger counter = new AtomicInteger();
    LooperThread looper = new LooperThread(new Looper()
    {
      @Override
      @Test
      public void loop() throws Exception
      {
        counter.incrementAndGet();
        throw new Exception();
      }
    });
    assertFalse(((AtomicBoolean)Classes.getFieldValue(looper, "breakOnException")).get());
    assertEquals(0, Classes.getFieldValue(looper, "loopPeriod"));
    looper.start();
    Thread.sleep(100);
    assertEquals(2, counter.get());
  }

  /**
   * Create a continuous looper thread and set break on exception to true. From loop iteration increments a counter and
   * throws exception. On first exception looper thread is interrupted. As a consequence counter should be 1.
   */
  @Test
  public void looperThreadBreakOnException() throws InterruptedException
  {
    final AtomicInteger counter = new AtomicInteger();
    LooperThread looper = new LooperThread(new Looper()
    {
      @Override
      @Test
      public void loop() throws Exception
      {
        counter.incrementAndGet();
        throw new Exception();
      }
    });
    assertEquals(0, Classes.getFieldValue(looper, "loopPeriod"));
    looper.setBreakOnException(true);
    looper.start();
    Thread.sleep(100);
    assertEquals(1, counter.get());
  }

  @Test
  public void looperThreadExceptionListener() throws InterruptedException
  {
    final AtomicInteger counter = new AtomicInteger();
    LooperThread looper = new LooperThread(new Looper()
    {
      @Override
      public void loop() throws Exception
      {
        throw new IOException();
      }
    }, 20);
    looper.setExceptionListener(new AsyncExceptionListener()
    {
      @Override
      public void onAsyncException(Throwable throwable)
      {
        assertTrue(throwable instanceof IOException);
        counter.incrementAndGet();
      }
    });
    looper.start();
    Thread.sleep(100);
    assertTrue(counter.get() >= 4 && counter.get() <= 6);
  }

  @Test
  public void createAbstractLooper() throws Exception
  {
    class MockLooper extends AbstractLooper
    {
      int loopProbe;

      public MockLooper()
      {
        super();
      }

      public MockLooper(int period)
      {
        super(period);
      }

      @Override
      public void loop() throws Exception
      {
        ++loopProbe;
        preDestroy();
      }

      public void join() throws InterruptedException
      {
        thread.join(0);
      }
    }

    MockLooper looper = new MockLooper();
    looper.postConstruct();
    looper.join();
    assertEquals(1, looper.loopProbe);

    looper = new MockLooper(10);
    looper.postConstruct();
    looper.join();
    assertEquals(1, looper.loopProbe);
  }
}
