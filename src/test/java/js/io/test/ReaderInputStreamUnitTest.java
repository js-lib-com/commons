package js.io.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;

import js.io.ReaderInputStream;
import js.util.Strings;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ReaderInputStreamUnitTest
{
  private static final String source = "0123456789";
  private InputStream stream;

  @Before
  public void beforeTest() throws Exception
  {
    Reader reader = new StringReader(source);
    stream = new ReaderInputStream(reader);
  }

  @After
  public void afterTest() throws Exception
  {
    stream.close();
  }

  @Test
  public void testReadByte() throws IOException
  {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    for(int b = 0;;) {
      b = stream.read();
      if(b == -1) {
        break;
      }
      bytes.write((byte)b);
    }

    assertEquals(source, bytes.toString("UTF-8"));
  }

  @Test
  public void testUndeflowCharacterBuffer() throws IOException
  {
    InputStream inputStream = new ReaderInputStream(new FileReader("fixture/io/page.html"));
    OutputStream outputStream = new ByteArrayOutputStream();

    int b;
    while((b = inputStream.read()) != -1) {
      outputStream.write(b);
    }
    inputStream.close();
    outputStream.close();

    assertEquals(Strings.load(new File("fixture/io/page.html")), outputStream.toString());
  }

  @Test
  public void testReadBytes() throws IOException
  {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    byte[] buffer = new byte[4];
    for(;;) {
      int count = stream.read(buffer);
      if(count == -1) {
        break;
      }
      bytes.write(buffer, 0, count);
    }

    assertEquals(source, bytes.toString("UTF-8"));
  }

  @Test
  public void testReadBytesWithOffset() throws IOException
  {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    byte[] buffer = new byte[4];
    for(;;) {
      int count = stream.read(buffer, 0, 4);
      if(count == -1) {
        break;
      }
      bytes.write(buffer, 0, count);
    }

    assertEquals(source, bytes.toString("UTF-8"));
  }

  @Test
  public void testClose() throws IOException
  {
    class MockReader extends Reader
    {
      int closeProbe = 0;

      @Override
      public int read(char[] cbuf, int off, int len) throws IOException
      {
        return -1;
      }

      @Override
      public void close() throws IOException
      {
        closeProbe++;
      }
    }

    MockReader reader = new MockReader();
    InputStream stream = new ReaderInputStream(reader);

    stream.close();
    assertEquals(1, reader.closeProbe);
  }
}
