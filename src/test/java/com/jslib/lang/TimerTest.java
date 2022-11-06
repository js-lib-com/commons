package com.jslib.lang;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TimerTest
{

  @Mock
  private AsyncExceptionListener container;

  private Timer timer;

  @Before
  public void beforeTest()
  {
    timer = new Timer(container);
  }

  @Test
  public void GivenTimeoutTaskClass_WhenTimeout_ThenInvoke() throws InterruptedException
  {
    // given
    final FinalInteger counter = new FinalInteger();
    final CountDownLatch latch = new CountDownLatch(1);

    TimeoutTask task = new TimeoutTask()
    {
      @Override
      public void onTimeout() throws Exception
      {
        counter.increment();
        latch.countDown();
      }
    };

    // when
    timer.timeout(10, task);

    // then
    latch.await();
    assertThat(counter.get(), equalTo(1));
  }

  @Test
  public void GivenTimeoutTaskFunction_WhenTimeout_ThenInvoke() throws InterruptedException
  {
    // given
    final FinalInteger counter = new FinalInteger();
    final CountDownLatch latch = new CountDownLatch(1);

    // when
    timer.timeout(10, () -> {
      counter.increment();
      latch.countDown();
    });

    // then
    latch.await();
    assertThat(counter.get(), equalTo(1));
  }

  @Test
  public void GivenPeriodicTaskClass_WhenPeriod_ThenInvoke() throws InterruptedException
  {
    // given
    final FinalInteger counter = new FinalInteger();
    final CountDownLatch latch = new CountDownLatch(2);

    PeriodicTask task = new PeriodicTask()
    {
      @Override
      public void onPeriod() throws Exception
      {
        counter.increment();
        latch.countDown();
      }
    };

    // when
    timer.period(10, task);

    // then
    latch.await();
    assertThat(counter.get(), equalTo(2));
  }

  @Test
  public void GivenPeriodicTaskFunction_WhenPeriod_ThenInvoke() throws InterruptedException
  {
    // given
    final FinalInteger counter = new FinalInteger();
    final CountDownLatch latch = new CountDownLatch(2);

    // when
    timer.period(10, () -> {
      counter.increment();
      latch.countDown();
    });

    // then
    latch.await();
    assertThat(counter.get(), equalTo(2));
  }
}
