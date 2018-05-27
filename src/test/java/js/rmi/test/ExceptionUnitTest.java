package js.rmi.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;

import js.rmi.BusinessException;
import js.rmi.RemoteException;
import js.rmi.RmiException;

import org.junit.Test;

public class ExceptionUnitTest
{
  @Test
  public void testRemoteException()
  {
    RemoteException exception = new RemoteException(new IOException("test"));
    assertEquals(IOException.class.getName(), exception.getCause());
    assertEquals("test", exception.getMessage());
    assertEquals("java.io.IOException: test", exception.toString());
  }

  @Test
  public void testBusinessException()
  {
    BusinessException exception = new BusinessException(1234);
    assertEquals(1234, exception.getErrorCode());
    assertEquals("js.rmi.BusinessException: 0x000004D2", exception.toString());
  }

  @Test
  public void testNullBusinessException()
  {
    BusinessException exception = new BusinessException();
    assertEquals(0, exception.getErrorCode());
    assertEquals("js.rmi.BusinessException: 0x00000000", exception.toString());
  }

  @Test
  public void testRmiExceptionWithRootCause() throws MalformedURLException
  {
    RmiException exception = new RmiException("http://server/app/js/util/Classes/forName", new RemoteException(new IOException("test")));
    assertEquals("HTTP-RMI server execution error on |http://server/app/js/util/Classes/forName|: java.io.IOException: test", exception.getMessage());
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
