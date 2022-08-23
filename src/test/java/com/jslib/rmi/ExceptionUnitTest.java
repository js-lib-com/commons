package com.jslib.rmi;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;

import org.junit.Test;

public class ExceptionUnitTest
{
  @Test
  public void testRemoteException()
  {
    RemoteException exception = new RemoteException(new TestException("test"));
    assertEquals("com.jslib.rmi.TestException: test", exception.getCause().toString());
  }

  @Test
  public void testRemoteException_customFields()
  {
    RemoteException exception = new RemoteException(new TestException("test", 1, 3));
    TestException cause = (TestException)exception.getCause();
    assertEquals("com.jslib.rmi.TestException: test", cause.toString());
    assertEquals(Integer.valueOf(1), cause.getStart());
    assertEquals(Integer.valueOf(3), cause.getEnd());
  }

  @Test
  public void testBusinessException()
  {
    BusinessException exception = new BusinessException(1234);
    assertEquals(1234, exception.getErrorCode());
    assertEquals("com.jslib.rmi.BusinessException: 0x000004D2", exception.toString());
  }

  @Test
  public void testNullBusinessException()
  {
    BusinessException exception = new BusinessException();
    assertEquals(0, exception.getErrorCode());
    assertEquals("com.jslib.rmi.BusinessException: 0x00000000", exception.toString());
  }

  @Test
  public void testRmiExceptionWithRootCause() throws MalformedURLException
  {
    RmiException exception = new RmiException("http://server/app/js/util/Classes/forName", new RemoteException(new TestException("test")));
    assertEquals("HTTP-RMI server execution error on |http://server/app/js/util/Classes/forName|: com.jslib.rmi.TestException: test", exception.getMessage());
  }

  @Test
  public void testRmiExceptionWithMessage() throws MalformedURLException
  {
    RmiException exception = new RmiException("http://server/app/js/util/Classes/forName", "fail message");
    assertEquals("HTTP-RMI server execution error on |http://server/app/js/util/Classes/forName|: fail message", exception.getMessage());
  }

  @Test
  public void testRmiExceptionWithFormattedMessage()
  {
    RmiException exception = new RmiException("Formatted %s message.", "test");
    assertEquals("HTTP-RMI server execution error on |Formatted %s message.|: test", exception.getMessage());
  }
}
