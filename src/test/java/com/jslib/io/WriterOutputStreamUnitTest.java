package com.jslib.io;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.BufferOverflowException;

import org.junit.Before;
import org.junit.Test;

import com.jslib.util.Strings;

public class WriterOutputStreamUnitTest
{
  private static final byte[] bytes = "0123456789".getBytes();

  private OutputStream stream;
  private Writer writer;

  @Before
  public void beforeTest() throws Exception
  {
    writer = new StringWriter();
    stream = new WriterOutputStream(writer);
  }

  @Test
  public void testWriteByte() throws IOException
  {
    for(int i = 0; i < bytes.length; ++i) {
      stream.write(bytes[i]);
    }
    stream.close();
    assertEquals("0123456789", writer.toString());
  }

  @Test
  public void testWriteBytes() throws IOException
  {
    stream.write(bytes);
    stream.close();
    assertEquals("0123456789", writer.toString());
  }

  @Test
  public void testWriteBytesWithOffset() throws IOException
  {
    stream.write(bytes, 0, 4);
    stream.write(bytes, 4, 4);
    stream.write(bytes, 8, 2);
    stream.close();
    assertEquals("0123456789", writer.toString());
  }

  @Test
  public void testWriteUTF8() throws IOException
  {
    byte[] bytes = "ăîâșțĂÎÂȘȚשדגכעיחלך".getBytes();
    for(int i = 0; i < bytes.length; ++i) {
      stream.write(bytes[i]);
    }
    stream.close();
    assertEquals("ăîâșțĂÎÂȘȚשדגכעיחלך", writer.toString());
  }

  /** Write byte by byte followed by flush but without close does not fill writer buffer. */
  @Test
  public void testWriteByteRegression() throws IOException
  {
    for(int i = 0; i < bytes.length; ++i) {
      stream.write(bytes[i]);
    }
    stream.flush();
    assertEquals("0123456789", writer.toString());
  }

  @Test
  public void testClose() throws IOException
  {
    MockWriter writer = new MockWriter();
    OutputStream stream = new WriterOutputStream(writer);
    stream.close();
    assertEquals(0, writer.flushProbe);
    assertEquals(1, writer.closeProbe);
  }

  @Test
  public void testFlush() throws IOException
  {
    MockWriter writer = new MockWriter();
    OutputStream stream = new WriterOutputStream(writer);
    stream.flush();
    assertEquals(1, writer.flushProbe);
    assertEquals(0, writer.closeProbe);
    stream.close();
  }

  /** Test file copy byte by byte found to throw {@link BufferOverflowException} while used in the wild. */
  @Test
  public void testOverflowByteBuffer() throws IOException
  {
    InputStream inputStream = new FileInputStream("fixture/io/page.html");
    StringWriter writer = new StringWriter();
    WriterOutputStream outputStream = new WriterOutputStream(writer);

    int b;
    while((b = inputStream.read()) != -1) {
      outputStream.write(b);
    }
    inputStream.close();
    outputStream.close();

    assertEquals(Strings.load(new File("fixture/io/page.html")), writer.toString());
  }

  // ----------------------------------------------------------------------------------------------
  // FIXTURE

  private static class MockWriter extends Writer
  {
    private int flushProbe;
    private int closeProbe;

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException
    {
    }

    @Override
    public void flush() throws IOException
    {
      flushProbe++;
    }

    @Override
    public void close() throws IOException
    {
      closeProbe++;
    }
  }
}
