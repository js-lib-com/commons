package js.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 * Convert characters writer to output bytes stream using UTF-8 encoding. While operates on output stream, bytes are
 * written to input buffer then {@link #processBytesBuffer(boolean)} is invoked. This takes care to decode bytes into
 * characters using UTF-8 decoder and write them to output buffer. On output stream explicit flush or close or on input
 * buffer overflow, output buffer is transfered to underlying target writer.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public class WriterOutputStream extends OutputStream
{
  /** Size of internal bytes buffer. See {@link #bytesBuffer}. */
  private static final int BYTES_BUFFER_SIZE = 128;

  /** Size of internal characters buffer. See {@link #charactersBuffer}. */
  private static final int CHARACTERS_BUFFER_SIZE = 1024;

  /**
   * Decode characters from UTF-8 bytes. Charset decoder transforms a sequence of UTF-8 bytes into a sequence of 16-bit
   * Unicode characters. Basically decoder reads from {@link #bytesBuffer} and writes characters to
   * {@link #charactersBuffer}.
   */
  private final CharsetDecoder decoder;

  /** Bytes buffer used as input for the decoder. All output stream write operations fill in this bytes buffer. */
  private final ByteBuffer bytesBuffer;

  /**
   * Characters buffer used as output for the decoder. Stores decoded characters before writing to underlying target
   * writer. This output buffer will only be flushed when it overflows or when {@link #flush()} or {@link #close()} is
   * called.
   */
  private final CharBuffer charactersBuffer;

  /** Target writer. */
  private final Writer writer;

  /**
   * Constructs output stream for target writer, using UTF-8 decoder.
   * 
   * @param writer target writer.
   */
  public WriterOutputStream(Writer writer)
  {
    this.writer = writer;

    this.decoder = Charset.forName("UTF-8").newDecoder();
    this.decoder.onMalformedInput(CodingErrorAction.REPLACE);
    this.decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
    this.decoder.replaceWith("?");

    this.bytesBuffer = ByteBuffer.allocate(BYTES_BUFFER_SIZE);
    this.charactersBuffer = CharBuffer.allocate(CHARACTERS_BUFFER_SIZE);
  }

  /**
   * Write bytes from the specified byte array to the stream.
   * 
   * @param b the byte array containing the bytes to write,
   * @param off the start offset in the byte array,
   * @param len the number of bytes to write.
   * @throws IOException if writing operation to underlying target writer fails.
   */
  @Override
  public void write(byte[] b, int off, int len) throws IOException
  {
    while(len > 0) {
      int c = Math.min(len, bytesBuffer.remaining());
      bytesBuffer.put(b, off, c);
      processBytesBuffer(false);
      len -= c;
      off += c;
    }
  }

  /**
   * Write bytes from the specified byte array to the stream. This method just delegates
   * {@link #write(byte[], int, int)} using entire buffer.
   * 
   * @param b the byte array containing the bytes to write.
   * @throws IOException if writing operation to underlying target writer fails.
   */
  @Override
  public void write(byte[] b) throws IOException
  {
    write(b, 0, b.length);
  }

  /**
   * Write a single byte to the stream. If bytes buffer is full invokes {@link #processBytesBuffer(boolean)} first.
   * 
   * @param b the byte to write.
   * @throws IOException if writing operation to underlying target writer fails.
   */
  @Override
  public void write(int b) throws IOException
  {
    if(!bytesBuffer.hasRemaining()) {
      processBytesBuffer(false);
    }
    bytesBuffer.put((byte)b);
  }

  /**
   * Flush output stream. Any remaining content accumulated in the characters buffer will be written to the underlying
   * {@link #writer} and writer flushed.
   * 
   * @throws IOException if writing operation to underlying target writer fails.
   */
  @Override
  public void flush() throws IOException
  {
    processBytesBuffer(false);
    flushCharactersBuffer();
    writer.flush();
  }

  /**
   * Close output stream. Any remaining content accumulated into bytes buffer will be written to the underlying
   * {@link #writer} via characters buffer. After that {@link Writer#close()} will be called.
   * 
   * @throws IOException if writing or close operation on underlying target writer fails.
   */
  @Override
  public void close() throws IOException
  {
    processBytesBuffer(true);
    flushCharactersBuffer();
    writer.close();
  }

  /**
   * Decode the contents of input bytes buffer into the characters buffer. If characters buffer overflows invoke
   * {@link #flushCharactersBuffer()} to commit buffer to underlying writer.
   * 
   * @param endOfInput signal that end of input was reached.
   * @throws IOException if writing operation to underlying target writer fails.
   */
  private void processBytesBuffer(boolean endOfInput) throws IOException
  {
    // prepare bytes buffer for reading
    bytesBuffer.flip();

    // decode bytes from input buffer into decoder characters buffer till bytes buffer underflow
    while(true) {
      CoderResult coderResult = decoder.decode(bytesBuffer, charactersBuffer, endOfInput);
      if(coderResult.isOverflow()) {
        flushCharactersBuffer();
      }
      else if(coderResult.isUnderflow()) {
        break;
      }
      else {
        // isMalformed() - decoder is configured to replace malformed input with '?'
        // isUnmappable() - decoder is configured to replace with '?' characters that cannot be mapped
        // so there is no reason to be here
        throw new IOException("Unexpected coder result.");
      }
    }

    // discard the bytes that have been read
    bytesBuffer.compact();
  }

  /**
   * Flush characters buffer to underlying writer.
   * 
   * @throws IOException if write operation on underlying writer fails.
   */
  private void flushCharactersBuffer() throws IOException
  {
    if(charactersBuffer.position() > 0) {
      writer.write(charactersBuffer.array(), 0, charactersBuffer.position());
      charactersBuffer.rewind();
    }
  }
}
