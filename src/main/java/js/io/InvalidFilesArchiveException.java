package js.io;

/**
 * Invalid files archive thrown by {@link FilesInputStream} when given files archive is not well formed or fail to read.
 * 
 * @author Iulian Rotaru
 */
public class InvalidFilesArchiveException extends RuntimeException
{
  /** Java serialization version. */
  private static final long serialVersionUID = 402563267973590157L;

  /**
   * Construct exception with formatted message. See {@link String#format(String, Object...)} for message format
   * description.
   * 
   * @param message message with optional formatting tags,
   * @param args optional formatting arguments.
   */
  public InvalidFilesArchiveException(String message, Object... args)
  {
    super(String.format(message, args));
  }
}
