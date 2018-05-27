package js.util.test;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;

import js.util.Base64;

import org.junit.Test;

public class Base64UnitTest
{
  @Test
  public void testEncodeString() throws UnsupportedEncodingException
  {
    assertEquals("VGhpcyBpcyBhIHN0cmluZy4=", Base64.encode("This is a string.".getBytes("UTF-8")));
  }
}
