package com.jslib.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.jslib.io.ReaderInputStream;
import com.jslib.lang.BugError;
import com.jslib.lang.Predicate;

/**
 * Functions for files, byte and character streams copy and file path manipulation. This class supplies methods for byte
 * and character streams transfer and files copy. If not otherwise specified, in order to simplify caller logic, streams
 * methods close streams before returning, including on error. Also for files copy, target file is created if does not
 * exist; if target file creation fails, perhaps because of missing rights or target exists and is a directory, file
 * methods throw {@link FileNotFoundException}. Please note that in all cases target content is overwritten.
 * <p>
 * Finally, there are method working with temporary files as target. These methods return newly created temporary file
 * and is caller responsibility to remove it when is not longer necessary. This library does not keep record of created
 * temporary file and there is no attempt to remove then, not even at virtual machine exit.
 * <p>
 * This utility class allows for sub-classing. See {@link com.jslib.util} for utility sub-classing description.
 * 
 * @author Iulian Rotaru
 */
public class Files
{
  /** Current directory file name. */
  public static final String CURRENT_DIR = ".";

  /** System dependent line separator used to separate text lines into character streams. */
  public static final String LINE_SEPARATOR = System.getProperty("line.separator");

  /** The size of buffer used by copy operations. */
  private static final int BUFFER_SIZE = 4 * 1024;

  /** Prevent default constructor synthesis but allow sub-classing. */
  protected Files()
  {
  }

  /**
   * Create buffered reader from bytes stream using UTF-8 charset. This library always uses UTF-8 encoding and this
   * method does not rely on JVM default since there is no standard way to enforce it. JRE specification states that
   * <code>file.encoding</code> is not the standard way to set default charset and to use host settings.
   * 
   * @param stream input stream.
   * @return newly created buffered reader.
   */
  public static BufferedReader createBufferedReader(InputStream stream)
  {
    try {
      return new BufferedReader(new InputStreamReader(stream, "UTF-8"));
    }
    catch(UnsupportedEncodingException unused) {
      throw new BugError("Unsupported UTF-8 ecoding.");
    }
  }

  /**
   * Create buffered writer from bytes stream using UTF-8 charset. This library always uses UTF-8 encoding and this
   * method does not rely on JVM default since there is no standard way to enforce it. JRE specification states that
   * <code>file.encoding</code> is not the standard way to set default charset and to use host settings.
   * 
   * @param stream input stream.
   * @return newly created buffered reader.
   */
  public static BufferedWriter createBufferedWriter(OutputStream stream)
  {
    try {
      return new BufferedWriter(new OutputStreamWriter(stream, "UTF-8"));
    }
    catch(UnsupportedEncodingException unused) {
      throw new BugError("Unsupported UTF-8 ecoding.");
    }
  }

  /**
   * Binary copy from URL to local temporary file.
   * 
   * @param url source file URL.
   * @return temporarily created file.
   * @throws IOException if copy operation fails.
   * @throws IllegalArgumentException if <code>url</code> parameter is null.
   */
  public static File copy(URL url) throws IOException, IllegalArgumentException
  {
    Params.notNull(url, "Source URL");
    URLConnection connection = url.openConnection();
    connection.setDoInput(true);
    // copy method takes care to close input stream so there is no need to handle URL connection close here
    return copy(connection.getInputStream());
  }

  /**
   * Binary copy to temporary file. Be aware that temporary file is removed at JVM exit.
   * 
   * @param stream source stream.
   * @return temporarily created file.
   * @throws IOException if copy operation fails.
   */
  public static File copy(InputStream stream) throws IOException
  {
    File file = createTempFile();
    copy(stream, new FileOutputStream(file));
    return file;
  }

  /**
   * Copy text from URL using specified encoding to local temporary file then close both URL input stream and temporary
   * file.
   * 
   * @param url source file URL,
   * @param encoding character encoding to use.
   * @return temporarily created file.
   * @throws IOException if copy operation fails.
   * @throws IllegalArgumentException if <code>url</code> parameter is null.
   */
  public static File copy(URL url, String encoding) throws IOException, IllegalArgumentException
  {
    Params.notNull(url, "Source URL");
    URLConnection connection = url.openConnection();
    connection.setDoInput(true);
    return copy(connection.getInputStream(), encoding);
  }

  /**
   * Copy text from byte stream using specified encoding to temporary file then close both input stream and temporary
   * file. Be aware that temporary file is removed at JVM exit.
   * 
   * @param stream input stream to read from,
   * @param encoding used character encoding.
   * @return temporarily created file.
   * @throws IOException if copy operation fails.
   */
  public static File copy(InputStream stream, String encoding) throws IOException
  {
    File file = createTempFile();
    copy(new InputStreamReader(stream, encoding), new OutputStreamWriter(new FileOutputStream(file), encoding));
    return file;
  }

  /**
   * Copy text from character stream to temporary file then close both the source reader and temporary file. Be aware
   * that temporary file is removed at JVM exit.
   * 
   * @param reader source character reader.
   * @return the newly created temporarily file.
   * @throws IOException if copy operation fails.
   */
  public static File copy(Reader reader) throws IOException
  {
    File file = createTempFile();
    copy(reader, new FileWriter(file));
    return file;
  }

  /**
   * Copy source file to target. Copy destination should be a file and this method throws access denied if attempt to
   * write to a directory. Source file should exist but target is created by this method, but if not already exist.
   * 
   * @param source file to read from, should exist,
   * @param target file to write to.
   * @return the number of bytes transfered.
   * @throws FileNotFoundException if source file does not exist or target file does not exist and cannot be created.
   * @throws IOException if copy operation fails, including if <code>target</code> is a directory.
   */
  public static long copy(File source, File target) throws FileNotFoundException, IOException
  {
    return copy(new FileInputStream(source), new FileOutputStream(target));
  }

  /**
   * Copy characters from a reader to a given writer then close both character streams.
   * 
   * @param reader character stream to read from,
   * @param writer character stream to write to.
   * @return the number of characters processed.
   * @throws IOException if read or write operation fails.
   * @throws IllegalArgumentException if reader or writer is null.
   */
  public static int copy(Reader reader, Writer writer) throws IOException
  {
    Params.notNull(reader, "Reader");
    Params.notNull(writer, "Writer");

    if(!(reader instanceof BufferedReader)) {
      reader = new BufferedReader(reader);
    }
    if(!(writer instanceof BufferedWriter)) {
      writer = new BufferedWriter(writer);
    }

    int charsCount = 0;
    try {
      char[] buffer = new char[BUFFER_SIZE];
      for(;;) {
        int readChars = reader.read(buffer);
        if(readChars == -1) {
          break;
        }
        charsCount += readChars;
        writer.write(buffer, 0, readChars);
      }
    }
    finally {
      close(reader);
      close(writer);
    }
    return charsCount;
  }

  /**
   * Copy remote binary file denoted by requested URL to local file identified by given base directory and file name.
   * Local file is created by {@link File#File(File, String)}. This method creates local file if it does already exist.
   * 
   * @param url remote file URL,
   * @param baseDir local file base directory,
   * @param fileName local file name.
   * @return the number of bytes transfered.
   * @throws FileNotFoundException if given local file does not exist and cannot be created.
   * @throws IOException if transfer fails.
   * @throws IllegalArgumentException if <code>url</code> is null.
   */
  public static long copy(URL url, File baseDir, String fileName) throws FileNotFoundException, IOException, IllegalArgumentException
  {
    Params.notNull(url, "Source URL");
    URLConnection connection = url.openConnection();
    connection.setDoInput(true);
    // copy method takes care to close both streams so there is no need to handle here URL connection close
    return copy(connection.getInputStream(), new FileOutputStream(new File(baseDir, fileName)));
  }

  /**
   * Copy bytes from given input stream to file denoted by requested base directory and file name. Destination file is
   * create by {@link File#File(File, String)}. This method tries to create destination file and throws exception if
   * fails.
   * 
   * @param inputStream source bytes stream,
   * @param baseDir destination base directory,
   * @param fileName destination file name.
   * @return the number of bytes processed.
   * @throws FileNotFoundException if given <code>baseDir</code> and <code>fileName</code> does not denote and existing
   *           file and cannot be created.
   * @throws IOException if bytes transfer fails.
   */
  public static long copy(InputStream inputStream, File baseDir, String fileName) throws FileNotFoundException, IOException
  {
    return copy(inputStream, new FileOutputStream(new File(baseDir, fileName)));
  }

  /**
   * Copy bytes from requested input stream to given target file. This method creates the target file if does not
   * already exist.
   * 
   * @param inputStream source input stream,
   * @param file target file.
   * @return the number of bytes processed.
   * @throws FileNotFoundException if given <code>file</code> does not exist and cannot be created.
   * @throws IOException if bytes transfer fails.
   */
  public static long copy(InputStream inputStream, File file) throws FileNotFoundException, IOException
  {
    return copy(inputStream, new FileOutputStream(file));
  }

  /**
   * Copy bytes from remote file denoted by given URL to specified local file. This method creates local file if not
   * already exists.
   * 
   * @param url source remote file URL,
   * @param file target local file.
   * @return the number of bytes transfered.
   * @throws FileNotFoundException if given <code>file</code> does not exist and cannot be created.
   * @throws IOException if bytes transfer fails.
   */
  public static long copy(URL url, File file) throws FileNotFoundException, IOException
  {
    return copy(url, new FileOutputStream(file));
  }

  /**
   * Copy bytes from remote file denoted by given URL to requested output stream. Destination stream is closed before
   * this method returning.
   * 
   * @param url URL of the source remote file,
   * @param outputStream destination output stream.
   * @return the number of bytes transfered.
   * @throws IOException the number of bytes transfered.
   * @throws IllegalArgumentException if <code>url</code> is null.
   */
  public static long copy(URL url, OutputStream outputStream) throws IOException, IllegalArgumentException
  {
    Params.notNull(url, "Source URL");
    URLConnection connection = url.openConnection();
    connection.setDoInput(true);
    // copy method takes care to close both streams so there is no need to handle here URL connection close
    return copy(connection.getInputStream(), outputStream);
  }

  /**
   * Copy source file bytes to requested output stream. Note that output stream is closed after transfer completes,
   * including on error.
   * 
   * @param file source file,
   * @param outputStream destination output stream.
   * @return the number of bytes processed.
   * @throws FileNotFoundException if <code>file</code> does not exist.
   * @throws IOException bytes processing fails.
   * @throws IllegalArgumentException if input file or output stream is null.
   */
  public static long copy(File file, OutputStream outputStream) throws IOException
  {
    Params.notNull(file, "Input file");
    return copy(new FileInputStream(file), outputStream);
  }

  /**
   * Copy bytes from input to given output stream then close both byte streams. Please be aware this method closes both
   * input and output streams. This is especially important if work with ZIP streams; trying to get/put next ZIP entry
   * after this method completes will fail with <em>stream closed</em> exception.
   * 
   * @param inputStream bytes input stream,
   * @param outputStream bytes output stream.
   * @return the number of bytes processed.
   * @throws IOException if reading or writing fails.
   * @throws IllegalArgumentException if input or output stream is null or ZIP stream.
   */
  public static long copy(InputStream inputStream, OutputStream outputStream) throws IOException, IllegalArgumentException
  {
    Params.notNull(inputStream, "Input stream");
    Params.notNull(outputStream, "Output stream");
    Params.isFalse(inputStream instanceof ZipInputStream, "Input stream is ZIP.");
    Params.isFalse(outputStream instanceof ZipOutputStream, "Output stream is ZIP.");

    if(!(inputStream instanceof BufferedInputStream)) {
      inputStream = new BufferedInputStream(inputStream);
    }
    if(!(outputStream instanceof BufferedOutputStream)) {
      outputStream = new BufferedOutputStream(outputStream);
    }

    long bytes = 0;
    try {
      byte[] buffer = new byte[BUFFER_SIZE];
      int length;
      while((length = inputStream.read(buffer)) != -1) {
        bytes += length;
        outputStream.write(buffer, 0, length);
      }
    }
    finally {
      close(inputStream);
      close(outputStream);
    }
    return bytes;
  }

  /** Partially transfered file name for {@link #download(URL, File)}. */
  private static final String PARTIAL_FILE_SUFFIX = ".part";

  /**
   * Atomic download file from specified URL to local target file. This method guarantee atomic operation: file is
   * actually created only if transfer completes with success. For this purpose download is performed to a temporary
   * file that is renamed after transfer complete. Of course, if transfer fails rename does not occur.
   * 
   * @param url source file URL,
   * @param file local target file.
   * @throws IOException if transfer or local write fails.
   */
  public static void download(URL url, File file) throws IOException
  {
    final File partialFile = new File(file.getAbsolutePath() + PARTIAL_FILE_SUFFIX);
    try {
      Files.copy(url, partialFile);
      renameTo(partialFile, file);
    }
    finally {
      // last resort to ensure partially file does not hang around on error
      // do not test for delete execution fail since, at this point, the partial file state is not certain
      partialFile.delete();
    }
  }

  /**
   * Dump byte input stream content to standard output till end of input stream. Input stream is closed.
   * 
   * @param inputStream source byte input stream.
   * @throws IOException if reading from input stream fails.
   */
  public static void dump(InputStream inputStream) throws IOException
  {
    if(!(inputStream instanceof BufferedInputStream)) {
      inputStream = new BufferedInputStream(inputStream);
    }
    try {
      byte[] buffer = new byte[BUFFER_SIZE];
      int length;
      while((length = inputStream.read(buffer)) != -1) {
        System.out.write(buffer, 0, length);
      }
    }
    finally {
      inputStream.close();
    }
  }

  /**
   * Dump UTF-8 character stream content to standard out.
   * 
   * @param reader source UTF-8 character stream.
   * @throws IOException if reading from character stream fails.
   */
  public static void dump(Reader reader) throws IOException
  {
    copy(new ReaderInputStream(reader), System.out);
  }

  /**
   * Close given <code>closeable</code> if not null but ignoring IO exception generated by failing close operation.
   * 
   * @param closeable closeable to close.
   */
  public static void close(Closeable closeable)
  {
    if(closeable == null) {
      return;
    }
    try {
      closeable.close();
    }
    catch(IOException e) {
    }
    catch(IncompatibleClassChangeError error) {
      // Closeable interface not implemented; invoke close method directly on instance
      try {
        Classes.invoke(closeable, "close");
      }
      catch(Throwable throwable) {
      }
    }
  }

  /**
   * Thin wrapper for {@link File#renameTo(File)} throwing exception on fail. This method ensures destination file
   * parent directories exist. If destination file already exist its content is silently overwritten.
   * <p>
   * Warning: if destination file exists, it is overwritten and its old content lost.
   * 
   * @param source source file,
   * @param destination destination file.
   * @throws IOException if rename operation fails.
   */
  public static void renameTo(File source, File destination) throws IOException
  {
    mkdirs(destination);

    // excerpt from File.renameTo API:
    // ... and it (n.b. File.renameTo) might not succeed if a file with the destination abstract pathname already exists
    //
    // so ensure destination does not exist
    destination.delete();

    if(!source.renameTo(destination)) {
      throw new IOException(String.format("Fail to rename file |%s| to |%s|.", source, destination));
    }
  }

  /**
   * Move source file to requested destination but do not overwrite. This method takes care to not overwrite destination
   * file and returns false if it already exists. Note that this method does not throw exceptions if move fails and
   * caller should always test returned boolean to determine if operation completes.
   * 
   * @param sourcePath source file path,
   * @param targetPath target file path.
   * @return true if move completes, false if source does not exist, destination already exists or move fails.
   * @throws IllegalArgumentException if source or target path parameter is null.
   */
  public static boolean move(String sourcePath, String targetPath) throws IllegalArgumentException
  {
    Params.notNull(sourcePath, "Source path");
    Params.notNull(targetPath, "Target path");

    File sourceFile = new File(sourcePath);
    if(!sourceFile.exists()) {
      return false;
    }
    File targetFile = new File(targetPath);
    if(targetFile.exists()) {
      return false;
    }
    return sourceFile.renameTo(targetFile);
  }

  /**
   * Get file access path relative to a base directory. This is the relative path that allows to access given file
   * starting from the base directory. But be warned: both base directory and file should reside on the same root. Also,
   * file argument can be a directory too but base directory must be a directory indeed.
   * <p>
   * Resulting relative path uses system separator unless optional <em>forceURLPath</em> is supplied and is false; in
   * this case always use <em>/</em>, no matter platform running JVM.
   * <p>
   * Known limitations: this method always assume that both base directory and file are on the same root; failing to
   * satisfy this condition render not predictable results.
   * 
   * @param baseDir base directory,
   * @param file file or directory to compute relative path,
   * @param forceURLPath flag to force resulting path as URL path, i.e. always uses '/' for path components separator.
   * @return file access path regarding base directory.
   * @throws IllegalArgumentException if any argument is null or empty.
   */
  public static String getRelativePath(File baseDir, File file, boolean... forceURLPath)
  {
    Params.notNull(baseDir, "Base directory");
    Params.notNull(file, "File");
    Params.notNullOrEmpty(baseDir, "Base directory");
    Params.notNullOrEmpty(file, "File");

    List<String> baseDirPathComponents = getPathComponents(baseDir.getAbsoluteFile());
    if(baseDirPathComponents.size() > 0) {
      int lastIndex = baseDirPathComponents.size() - 1;
      if(baseDirPathComponents.get(lastIndex).equals(".")) {
        baseDirPathComponents.remove(lastIndex);
      }
      else if(baseDirPathComponents.get(lastIndex).equals("..")) {
        if(baseDirPathComponents.size() < 2) {
          throw new BugError("Invalid base directory for relative path. It ends with '..' but has no parent directory.");
        }
        baseDirPathComponents.remove(baseDirPathComponents.size() - 1);
        baseDirPathComponents.remove(baseDirPathComponents.size() - 1);
      }
    }
    List<String> filePathComponenets = getPathComponents(file.getAbsoluteFile());
    List<String> relativePath = new ArrayList<String>();

    int i = 0;
    for(; i < baseDirPathComponents.size(); i++) {
      if(i == filePathComponenets.size()) break;
      if(!baseDirPathComponents.get(i).equals(filePathComponenets.get(i))) break;
    }
    for(int j = i; j < baseDirPathComponents.size(); j++) {
      relativePath.add("..");
    }
    for(; i < filePathComponenets.size(); i++) {
      relativePath.add(filePathComponenets.get(i));
    }

    return Strings.join(relativePath, forceURLPath.length > 0 && forceURLPath[0] ? '/' : File.separatorChar);
  }

  /**
   * Get file path components. Return a list of path components in their natural order. List first item is path root
   * stored as an empty string; if file argument is empty returned list contains only root. If file argument is null
   * returns empty list.
   * 
   * @param file file to retrieve path components.
   * @return file path components.
   */
  public static List<String> getPathComponents(File file)
  {
    if(file == null) {
      return Collections.emptyList();
    }
    List<String> pathComponents = new ArrayList<String>();
    do {
      pathComponents.add(0, file.getName());
      file = file.getParentFile();
    }
    while(file != null);
    return pathComponents;
  }

  /**
   * Remove extension from given file path and return resulting path.
   * 
   * @param path file path to remove extension from.
   * @return newly created path without extension.
   * @throws IllegalArgumentException if <code>path</code> parameter is null.
   */
  public static String removeExtension(String path) throws IllegalArgumentException
  {
    Params.notNull(path, "Path");
    int extensionIndex = path.lastIndexOf('.');
    return extensionIndex != -1 ? path.substring(0, extensionIndex) : path;
  }

  /**
   * Remove extension from given file and create a new one with resulting path.
   * 
   * @param file file to remove extension from.
   * @return newly created file without extension.
   * @throws IllegalArgumentException if <code>file</code> parameter is null.
   */
  public static File removeExtension(File file) throws IllegalArgumentException
  {
    Params.notNull(file, "File");
    return new File(removeExtension(file.getPath()));
  }

  /**
   * Replace extension on given file path and return resulting path. Is legal for new extension parameter to start with
   * dot extension separator, but is not mandatory.
   * 
   * @param path file path to replace extension,
   * @param newExtension newly extension, with optional dot separator prefix.
   * @return newly created file path.
   * @throws IllegalArgumentException if path or new extension parameter is null.
   */
  public static String replaceExtension(String path, String newExtension) throws IllegalArgumentException
  {
    Params.notNull(path, "Path");
    Params.notNull(newExtension, "New extension");

    if(newExtension.charAt(0) == '.') {
      newExtension = newExtension.substring(1);
    }

    int extensionDotIndex = path.lastIndexOf('.') + 1;
    if(extensionDotIndex == 0) {
      extensionDotIndex = path.length();
    }
    StringBuilder sb = new StringBuilder(path.length());
    sb.append(path.substring(0, extensionDotIndex));
    sb.append(newExtension);
    return sb.toString();
  }

  /**
   * Replace extension on given file and return resulting file.
   * 
   * @param file file to replace extension,
   * @param newExtension newly extension.
   * @return newly created file.
   * @throws IllegalArgumentException if file parameter is null.
   */
  public static File replaceExtension(File file, String newExtension) throws IllegalArgumentException
  {
    Params.notNull(file, "File");
    return new File(replaceExtension(file.getPath(), newExtension));
  }

  /** Regular expression for path separator. */
  private static final String SEPARATOR_CHAR_REX = File.separatorChar == '\\' ? "\\\\" : "/";

  /**
   * Convert a file path to a dot separated list of words. This utility is handy, for example, on converting file paths
   * to Java packages, provided file path contains Java valid characters. Returns null if <code>path</code> parameter is
   * null.
   * 
   * @param path file path to convert.
   * @return dot separated list of words or null.
   */
  public static String path2dot(String path)
  {
    return path != null ? path.replaceAll(SEPARATOR_CHAR_REX, ".") : null;
  }

  /**
   * Convert a file to an Unix like path string. This method just delegates {@link #path2unix(String)}. Returns null if
   * <code>file</code> parameter is null.
   * 
   * @param file file to convert.
   * @return Unix like path string.
   */
  public static String path2unix(File file)
  {
    return file != null ? path2unix(file.getPath()) : null;
  }

  /**
   * Convert file path to an Unix like path string. If given <code>path</code> is Windows like replaces drive letter
   * with Unix path root and all Windows path separators to Unix counterpart.
   * 
   * <pre>
   *    D:\\temp\file.txt -&gt; /temp/file.txt
   * </pre>
   * 
   * If <code>path</code> is already Unix like this method leave it as it is but remove trailing path separator, if any.
   * Returns null if <code>path</code> parameter is null.
   * 
   * @param path path to convert, trailing path separator ignored.
   * @return Unix like path string or null.
   */
  public static String path2unix(String path)
  {
    if(path == null) {
      return null;
    }
    if(path.endsWith("/") || path.endsWith("\\")) {
      path = path.substring(0, path.length() - 1);
    }
    return path.replaceAll("(^[a-zA-Z]\\:\\\\?)?\\\\", "/");
  }

  /** Regular expression pattern for dot (.) character. */
  private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

  /**
   * Replace all dots from given qualified name with platform specific path separator.
   * 
   * <pre>
   *    js.net.Transaction -&gt; js/net/Transaction or js\net\Transaction
   * </pre>
   * 
   * Returns null if <code>qualifiedName</code> parameter is null.
   * 
   * @param qualifiedName qualified name.
   * @return resulting path or null.
   */
  public static String dot2path(String qualifiedName)
  {
    return qualifiedName != null ? DOT_PATTERN.matcher(qualifiedName).replaceAll(Matcher.quoteReplacement(File.separator)) : null;
  }

  /** URL path separator. */
  private static final String URL_PATH_SEPARATOR = "/";

  /**
   * Same as {@link #dot2path(String)} but always uses forward slash as path separator, as used by URLs. Returns null if
   * <code>qualifiedName</code> parameter is null.
   * 
   * @param qualifiedName qualified name.
   * @return resulting URL path or null.
   */
  public static String dot2urlpath(String qualifiedName)
  {
    return qualifiedName != null ? DOT_PATTERN.matcher(qualifiedName).replaceAll(Matcher.quoteReplacement(URL_PATH_SEPARATOR)) : null;
  }

  /**
   * Convert qualified name to platform specific path and add given extension. Uses {@link #dot2path(String)} to convert
   * <code>qualifiedName</code> to file path then add give <code>fileExtension</code>. Is legal for
   * <code>fileExtension</code> to start with dot.
   * 
   * <pre>
   *    js.net.Transaction java -&gt; js/net/Transaction.java or js\net\Transaction.java
   *    js.net.Transaction .java -&gt; js/net/Transaction.java or js\net\Transaction.java
   * </pre>
   * 
   * Returns null if <code>qualifiedName</code> parameter is null. If <code>fileExtension</code> parameter is null
   * resulting path has no extension.
   * 
   * @param qualifiedName qualified name,
   * @param fileExtension requested file extension, leading dot accepted.
   * @return resulting file path or null.
   */
  public static String dot2path(String qualifiedName, String fileExtension)
  {
    if(qualifiedName == null) {
      return null;
    }
    StringBuilder path = new StringBuilder();
    path.append(dot2path(qualifiedName));
    if(fileExtension != null) {
      if(fileExtension.charAt(0) != '.') {
        path.append('.');
      }
      path.append(fileExtension);
    }
    return path.toString();
  }

  /** The extension separator character. */
  public static final char EXTENSION_SEPARATOR = '.';
  /** The Unix separator character. */
  private static final char UNIX_SEPARATOR = '/';
  /** The Windows separator character. */
  private static final char WINDOWS_SEPARATOR = '\\';

  /**
   * Get extension of the file denoted by given URL or empty string if not extension. Returned extension does not
   * contain dot separator, that is, <code>htm</code> not <code>.htm</code>. Returns null if <code>url</code> parameter
   * is null.
   * 
   * @param url the URL of file to return extension.
   * @return file extension or empty string or null if <code>url</code> parameter is null.
   */
  public static String getExtension(URL url)
  {
    return url != null ? getExtension(url.getPath()) : null;
  }

  /**
   * Get file extension as lower case or empty string. Returned extension does not contain dot separator, that is,
   * <code>htm</code> not <code>.htm</code>. Returns null if <code>file</code> parameter is null.
   * 
   * @param file file to return extension of.
   * @return file extension or empty string or null if <code>file</code> parameter is null.
   */
  public static String getExtension(File file)
  {
    return file != null ? getExtension(file.getAbsolutePath()) : null;
  }

  /**
   * Get the lower case extension of the file denoted by given path or empty string if not extension. Returned extension
   * does not contain dot separator, that is, <code>htm</code> not <code>.htm</code>. Returns null if given
   * <code>path</code> parameter is null.
   * 
   * @param path the path of the file to return extension of.
   * @return file extension, as lower case, or empty string if no extension.
   */
  public static String getExtension(String path)
  {
    if(path == null) {
      return null;
    }

    // search for both Unix and Windows path separators because this logic is common for files and URLs

    int extensionPos = path.lastIndexOf(EXTENSION_SEPARATOR);
    int lastUnixPos = path.lastIndexOf(UNIX_SEPARATOR);
    int lastWindowsPos = path.lastIndexOf(WINDOWS_SEPARATOR);
    int lastSeparatorPos = Math.max(lastUnixPos, lastWindowsPos);

    // do not consider extension separator before last path separator, e.g. /etc/rc.d/file
    int i = (lastSeparatorPos > extensionPos ? -1 : extensionPos);
    return i == -1 ? "" : path.substring(i + 1).toLowerCase();
  }

  /**
   * Return file name without extension. Returns null if given <code>file</code> parameter is null.
   * 
   * @param file file to return base name for.
   * @return file name, without extension, or null.
   */
  public static String basename(File file)
  {
    if(file == null) {
      return null;
    }
    String fileName = file.getName();
    int i = fileName.lastIndexOf('.');
    return i != -1 ? fileName.substring(0, i) : fileName;
  }

  /**
   * Return base name for given file path. Returns null if given path parameter is null.
   * 
   * @param path file path string.
   * @return file name without extension or null.
   */
  public static String basename(String path)
  {
    return path != null ? basename(new File(path)) : null;
  }

  /** Prefix for temporary files. */
  private static final String TMP_FILE_PREFIX = "jslib";
  /** Extension for temporary files. */
  private static final String TMP_FILE_EXTENSION = ".tmp";

  /**
   * Create temporary file using library specific prefix and extension. Created file is removed at JVM exit.
   * 
   * @return newly create temporary file.
   * @throws IOException if file creation fails.
   */
  private static File createTempFile() throws IOException
  {
    File file = File.createTempFile(TMP_FILE_PREFIX, TMP_FILE_EXTENSION);
    file.deleteOnExit();
    return file;
  }

  /**
   * Detect if a file is XML with a given root element. Returns true if file exists and has <code>xml</code> extension.
   * If optional roots parameter is provided check also the file root element and return false if no match.
   * 
   * @param file file to test,
   * @param roots optional roots elements to match.
   * @return true if file is XML and optional roots match.
   * @throws IOException if roots parameter is provided and file read fails.
   * @since 1.3
   */
  public static boolean isXML(File file, String... roots) throws IOException
  {
    if(!file.exists() || !file.getName().endsWith(".xml")) {
      return false;
    }
    if(roots.length == 0) {
      return true;
    }
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line = reader.readLine();
      if(line.startsWith("<?")) {
        line = reader.readLine();
      }
      for(String root : roots) {
        if(line.startsWith(Strings.concat('<', root, '>'))) {
          return true;
        }
      }
    }
    return false;
  }

  /** Standard extensions for image files. Used by {@link #isImage(File)}. */
  private static final List<String> IMAGE_FILE_EXTENSIONS = new ArrayList<String>();
  static {
    IMAGE_FILE_EXTENSIONS.add("png");
    IMAGE_FILE_EXTENSIONS.add("gif");
    IMAGE_FILE_EXTENSIONS.add("jpg");
    IMAGE_FILE_EXTENSIONS.add("jpeg");
    IMAGE_FILE_EXTENSIONS.add("tiff");
    IMAGE_FILE_EXTENSIONS.add("bmp");
  }

  /**
   * Guess if file is an image file based on file extension. This is a very trivial test relying on file extension and
   * obviously cannot guarantee correct results.
   * 
   * @param file file to test.
   * @return true if given file is an image file.
   */
  public static boolean isImage(File file)
  {
    return file != null ? IMAGE_FILE_EXTENSIONS.contains(Files.getExtension(file)) : false;
  }

  /**
   * Convenient alternative for {@link #isImage(File)} method.
   * 
   * @param extension file extension to test.
   * @return true if given file extension denotes an image file.
   */
  public static boolean isImage(String extension)
  {
    return extension != null ? IMAGE_FILE_EXTENSIONS.contains(extension.toLowerCase()) : false;
  }

  /**
   * Create MD5 message digest for requested file content. This method returns a 16-bytes array with computed MD5
   * message digest value.
   * 
   * @param file file to create message digest for.
   * @return 16-bytes array of message digest.
   * @throws FileNotFoundException if <code>file</code> does not exist.
   * @throws IOException if file read operation fails.
   */
  public static byte[] getFileDigest(File file) throws FileNotFoundException, IOException
  {
    return getFileDigest(new FileInputStream(file));
  }

  /**
   * Create MD5 message digest for bytes produced by given input stream till its end. This method returns a 16-bytes
   * array with computed MD5 message digest value. Note that input stream is read till its end and is closed before this
   * method returning, including on stream read error.
   * 
   * @param inputStream input stream for source bytes.
   * @return 16-bytes array of message digest.
   * @throws IOException if file read operation fails.
   */
  public static byte[] getFileDigest(InputStream inputStream) throws IOException
  {
    if(!(inputStream instanceof BufferedInputStream)) {
      inputStream = new BufferedInputStream(inputStream);
    }

    MessageDigest messageDigest = null;
    try {
      byte[] buffer = new byte[1024];
      messageDigest = MessageDigest.getInstance("MD5");
      for(;;) {
        int bytesRead = inputStream.read(buffer);
        if(bytesRead <= 0) {
          break;
        }
        messageDigest.update(buffer, 0, bytesRead);
      }
    }
    catch(NoSuchAlgorithmException e) {
      throw new BugError("JVM with missing MD5 algorithm for message digest.");
    }
    finally {
      inputStream.close();
    }

    return messageDigest.digest();
  }

  /**
   * Create all ancestor directories for a requested file path. Given <code>file</code> is assumed to be a file path,
   * not a directory; this method retrieve file parent and enact {@link File#mkdirs()}. If given file reside into file
   * system root this method does nothing.
   * <p>
   * For caller convenience this method returns <code>file</code> argument, see sample usage.
   * 
   * <pre>
   * save(text, new FileWriter(Files.mkdirs(file)));
   * </pre>
   * 
   * @param file file path.
   * @return given file argument with guaranteed parent directories.
   * @throws IOException if directories creation fails.
   * @throws IllegalArgumentException if <code>file</code> parameter is null.
   */
  public static File mkdirs(File file) throws IOException, IllegalArgumentException
  {
    Params.notNull(file, "File");
    File parentFile = file.getParentFile();
    if(parentFile != null && !parentFile.exists() && !file.getParentFile().mkdirs()) {
      throw new IOException(String.format("Fail to create target file |%s| directories.", file.getAbsolutePath()));
    }
    return file;
  }

  /**
   * Delete file if exists throwing exception if delete fails. Note that this method does nothing if file does not
   * exist; anyway, null parameter sanity check is still performed.
   * 
   * @param file file path.
   * @throws IOException if delete operation fails.
   * @throws IllegalArgumentException if <code>file</code> parameter is null.
   */
  public static void delete(File file) throws IOException
  {
    Params.notNull(file, "File");
    if(file.exists() && !file.delete()) {
      throw new IOException(Strings.format("Fail to delete file |%s|.", file));
    }
  }

  /**
   * Remove ALL files and directories from a given base directory. This method remove ALL files and directory tree,
   * child of given <code>baseDir</code> but directory itself is not removed. As a result <code>baseDir</code> becomes
   * empty, that is, no children. If exception occur base directory state is not defined, that is, some files may be
   * removed and other may still be there.
   * <p>
   * For caller convenience this method returns given base directory. This allows to chain this method with
   * {@link File#delete()} method, like <code>Files.removeFilesHierarchy(dir).delete();</code>.
   * 
   * @param baseDir existing, not null, base directory to clean-up.
   * @return base directory argument for method chaining, mainly for {@link File#delete()}.
   * @throws IllegalArgumentException if base directory argument is null or is not an existing directory.
   * @throws IOException if remove operation fails.
   */
  public static File removeFilesHierarchy(File baseDir) throws IOException
  {
    Params.notNull(baseDir, "Base directory");
    Params.isDirectory(baseDir, "Base directory");
    removeDirectory(baseDir);
    return baseDir;
  }

  /**
   * Utility method invoked recursively to remove directory files. This method traverses <code>directory</code> files
   * and remove them, one by one. If a child file is happen to be a directory this method invoked itself with child
   * directory as parameter. After child directory is clean-up iteration continue removing child directory itself. Note
   * that given <code>directory</code> is not removed.
   * <p>
   * On remove exception <code>directory</code> state is not defined, that is, some files may be removed while others
   * may not.
   * 
   * @param directory directory to remove files from.
   * @throws IOException if remove operation fails.
   */
  private static void removeDirectory(File directory) throws IOException
  {
    // File.listFiles() may return null is file is not a directory 
    // condition already tested before entering this method
    for(File file : directory.listFiles()) {
      if(file.isDirectory()) {
        removeDirectory(file);
      }
      if(!file.delete()) {
        throw new IOException(String.format("Fail to delete %s |%s|.", file.isDirectory() ? "empty directory" : "file", file));
      }
    }
  }

  /** Predefined predicates used by {@link #inotify(File, FileNotify, int)}. */
  private static final Map<FileNotify, Predicate> INOTIFY_PREDICATES = new HashMap<FileNotify, Predicate>();
  static {
    INOTIFY_PREDICATES.put(FileNotify.CREATE, new Predicate()
    {
      @Override
      public boolean test(Object value)
      {
        assert value instanceof File;
        return ((File)value).exists();
      }
    });
    INOTIFY_PREDICATES.put(FileNotify.DELETE, new Predicate()
    {
      @Override
      public boolean test(Object value)
      {
        assert value instanceof File;
        return !((File)value).exists();
      }
    });
  }

  /**
   * Wait for requested action to happen on given file. If <code>timeout</code> is zero or negative this method returns
   * false immediately.
   * <p>
   * This implementation is a fallback solution for JVM without <code>java.nio.file.WatchService</code>. Please consider
   * using watch service when available.
   * 
   * @param file file to wait for its state change,
   * @param notify notify type,
   * @param timeout timeout value.
   * @return true if file state change occurred before timeout or false otherwise.
   * @throws IllegalArgumentException if <code>file</code> is null.
   */
  public static boolean inotify(File file, FileNotify notify, int timeout) throws IllegalArgumentException
  {
    Params.notNull(file, "File");

    Predicate predicate = INOTIFY_PREDICATES.get(notify);
    if(predicate == null) {
      throw new BugError("Unsupported file notification |%s|. Missing predicate.", notify);
    }

    long timestamp = System.currentTimeMillis() + timeout;
    while(!predicate.test(file)) {
      if(timestamp <= System.currentTimeMillis()) {
        return false;
      }
      try {
        Thread.sleep(500);
      }
      catch(InterruptedException unused) {
        Thread.currentThread().interrupt();
      }
    }
    return true;
  }

  /**
   * File notification types used by {@link Files#inotify(java.io.File, FileNotify, int)} utility method.
   * 
   * @author Iulian Rotaru
   * @version draft
   */
  public static enum FileNotify
  {
    /** Neutral value. */
    NONE,
    /** File was deleted. */
    DELETE,
    /** File was created. */
    CREATE
  }

  /**
   * Ensure that path separators are compatible with current operating system.
   * 
   * @param path path to normalize.
   * @return normalized path.
   */
  public static String normalizePath(String path)
  {
    if(File.separatorChar == '/') {
      return path.replace('\\', '/');
    }
    return path.replace('/', '\\');
  }
}
