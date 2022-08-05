package com.jslib.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class FilesUnitTest
{
  @Test
  public void testBasename()
  {
    assertEquals("file", Files.basename("/tmp/file.txt"));
    assertEquals("file", Files.basename("/tmp/file"));
    assertEquals("file", Files.basename(new File("/tmp/file.txt")));
    assertEquals("file", Files.basename(new File("/tmp/file")));
    assertEquals("", Files.basename(""));
    assertNull(Files.basename((String)null));
    assertNull(Files.basename((File)null));
  }

  @Test
  public void testPathComponenets()
  {
    if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
      File file = new File("c://a/b/c/");
      List<String> pathComponenets = Files.getPathComponents(file);
      assertEquals(4, pathComponenets.size());
      assertEquals("", pathComponenets.get(0));
      assertEquals("a", pathComponenets.get(1));
      assertEquals("b", pathComponenets.get(2));
      assertEquals("c", pathComponenets.get(3));

      file = new File("d://a/b/c");
      pathComponenets = Files.getPathComponents(file);
      assertEquals(4, pathComponenets.size());
      assertEquals("", pathComponenets.get(0));
      assertEquals("a", pathComponenets.get(1));
      assertEquals("b", pathComponenets.get(2));
      assertEquals("c", pathComponenets.get(3));
    }

    File file = new File("/a/b/c/");
    List<String> pathComponenets = Files.getPathComponents(file);
    assertEquals(4, pathComponenets.size());
    assertEquals("", pathComponenets.get(0));
    assertEquals("a", pathComponenets.get(1));
    assertEquals("b", pathComponenets.get(2));
    assertEquals("c", pathComponenets.get(3));

    file = new File("/a/b/c");
    pathComponenets = Files.getPathComponents(file);
    assertEquals(4, pathComponenets.size());
    assertEquals("", pathComponenets.get(0));
    assertEquals("a", pathComponenets.get(1));
    assertEquals("b", pathComponenets.get(2));
    assertEquals("c", pathComponenets.get(3));

    file = new File("");
    pathComponenets = Files.getPathComponents(file);
    assertEquals(1, pathComponenets.size());
    assertEquals("", pathComponenets.get(0));

    pathComponenets = Files.getPathComponents(null);
    assertEquals(0, pathComponenets.size());
  }

  @Test
  public void testRelativePath()
  {
    File baseDir = new File("/a/b/c");
    assertEquals("d", exerciseRelativePath(baseDir, "/a/b/c/d/"));
    assertEquals("d", exerciseRelativePath(baseDir, "/a/b/c/d"));
    assertEquals("d/e", exerciseRelativePath(baseDir, "/a/b/c/d/e"));
    assertEquals("d/e/f", exerciseRelativePath(baseDir, "/a/b/c/d/e/f/"));
    assertEquals("../d", exerciseRelativePath(baseDir, "/a/b/d"));
    assertEquals("../d/e", exerciseRelativePath(baseDir, "/a/b/d/e/"));
    assertEquals("../../d", exerciseRelativePath(baseDir, "/a/d"));
    assertEquals("../../d/e", exerciseRelativePath(baseDir, "/a/d/e/"));
    assertEquals("../../../d", exerciseRelativePath(baseDir, "/d"));
    assertEquals("../../../d/e", exerciseRelativePath(baseDir, "/d/e/"));
  }

  @Test
  public void testRelativePathCurrentBaseDirectory()
  {
    File baseDir = new File("fixture/complex-page/context/.").getAbsoluteFile();
    File file = new File("fixture/complex-page/context/images/header-bg.jpg").getAbsoluteFile();
    assertEquals("images/header-bg.jpg", Files.getRelativePath(baseDir, file, true));
  }

  @Test
  public void testRelativePathParentBaseDirectory()
  {
    File baseDir = new File("fixture/complex-page/context/..").getAbsoluteFile();
    File file = new File("fixture/complex-page/context/images/header-bg.jpg").getAbsoluteFile();
    assertEquals("context/images/header-bg.jpg", Files.getRelativePath(baseDir, file, true));
  }

  private static String exerciseRelativePath(File baseDir, String file)
  {
    return Files.getRelativePath(baseDir, new File(file)).replace(File.separatorChar, '/');
  }

  @Test
  public void testRelativePathBadArguments()
  {
    try {
      Files.getRelativePath(null, new File("a"));
      fail("Null base directory should rise exception");
    }
    catch(IllegalArgumentException e) {}

    try {
      Files.getRelativePath(new File("a"), null);
      fail("Null file should rise exception");
    }
    catch(IllegalArgumentException e) {}

    try {
      Files.getRelativePath(new File(""), new File("a"));
      fail("Empty base directory should rise exception");
    }
    catch(IllegalArgumentException e) {}

    try {
      Files.getRelativePath(new File("a"), new File(""));
      fail("Empty file should rise exception");
    }
    catch(IllegalArgumentException e) {}
  }

  @Test
  public void testGetExtension()
  {
    assertEquals("jpg", Files.getExtension(new File("image.jpg")));
    assertEquals("jpg", Files.getExtension(new File("image.JPG")));
    assertEquals("jpg", Files.getExtension(new File("relative/path/image.jpg")));
    assertEquals("jpg", Files.getExtension(new File("/absolute/path/image.JPG")));
    assertEquals("", Files.getExtension(new File("image")));
    assertEquals("", Files.getExtension(new File("image.")));
    assertEquals("", Files.getExtension(new File("relative/path/image")));
    assertEquals("", Files.getExtension(new File("/absolute/pathimage")));
    assertEquals("", Files.getExtension(new File("/etc/rc.d/file")));
    assertEquals("txt", Files.getExtension(new File("/etc/rc.d/readme.txt")));
    assertEquals("", Files.getExtension(new File("../readme")));

    assertNull(Files.getExtension((String)null));
    assertNull(Files.getExtension((File)null));
  }

  private static final byte[] EXPECTED_FILE_DIGEST = new byte[]
  {
      73, 89, 53, 124, -16, 127, -82, -125, -124, -101, 59, -98, 14, -106, 60, 84
  };

  @Test
  public void testGetFileDigest() throws Exception
  {
    byte[] fileDigest = Files.getFileDigest(new File("fixture/util/image.png"));
    assertEquals(16, fileDigest.length);
    assertTrue(Arrays.equals(EXPECTED_FILE_DIGEST, fileDigest));
  }

  @Test
  public void testDot2path()
  {
    if(File.separatorChar == '/') {
      assertEquals("com/jslib/util/FilesUnitTest", Files.dot2path(FilesUnitTest.class.getName()));
    }
    else {
      assertEquals("com\\jslib\\util\\FilesUnitTest", Files.dot2path(FilesUnitTest.class.getName()));
    }
    assertNull(Files.dot2path(null));
  }

  @Test
  public void testDot2pathWithExtension()
  {
    if(File.separatorChar == '/') {
      assertEquals("com/jslib/util/FilesUnitTest.java", Files.dot2path(FilesUnitTest.class.getName(), ".java"));
      assertEquals("com/jslib/util/FilesUnitTest", Files.dot2path(FilesUnitTest.class.getName(), null));
    }
    else {
      assertEquals("com\\jslib\\util\\FilesUnitTest.java", Files.dot2path(FilesUnitTest.class.getName(), ".java"));
      assertEquals("com\\jslib\\util\\FilesUnitTest", Files.dot2path(FilesUnitTest.class.getName(), null));
    }
    assertNull(Files.dot2path(null, ".java"));
    assertNull(Files.dot2path(null, null));
  }

  @Test
  public void testDot2urlpath()
  {
    if(File.separatorChar == '/') {
      assertEquals("com/jslib/util/FilesUnitTest", Files.dot2urlpath(FilesUnitTest.class.getName()));
    }
    else {
      assertEquals("com/jslib/util/FilesUnitTest", Files.dot2urlpath(FilesUnitTest.class.getName()));
    }
    assertNull(Files.dot2urlpath(null));
  }

  @Test
  public void testPath2Unix()
  {
    assertEquals("/temp", Files.path2unix("C:\\temp\\"));
    assertEquals("/temp/file.txt", Files.path2unix("C:\\temp\\file.txt"));
    assertEquals("/temp", Files.path2unix("C:\\\\temp"));
    assertEquals("/temp/C:", Files.path2unix("\\temp\\C:\\"));

    assertEquals("/temp", Files.path2unix(new File("C:\\temp\\")));
    assertEquals("/temp/file.txt", Files.path2unix(new File("C:\\temp\\file.txt")));
    assertEquals("/temp", Files.path2unix(new File("C:\\\\temp")));
    assertEquals("/temp/C:", Files.path2unix(new File("\\temp\\C:\\")));

    assertNull(Files.path2unix((String)null));
    assertNull(Files.path2unix((File)null));
  }

  @Test
  public void testClose()
  {
    final AtomicInteger probe = new AtomicInteger();
    Files.close(new Closeable()
    {
      @Override
      @Test
      public void close() throws IOException
      {
        probe.incrementAndGet();
      }
    });
    assertEquals(1, probe.get());

    Files.close(new Closeable()
    {
      @Override
      @Test
      public void close() throws IOException
      {
        probe.incrementAndGet();
        throw new IncompatibleClassChangeError();
      }
    });
    assertEquals(3, probe.get());

    // close on null should not throw exception
    try {
      Files.close(null);
    }
    catch(Throwable t) {
      fail(t.getMessage());
    }
  }

  @Test
  public void testRemoveFilesHierarchy() throws IOException
  {
    File baseDir = new File("fixture/files-hierarchy");
    baseDir.mkdir(); // ensure directory is create
    assertEquals(0, baseDir.list().length);

    ByteArrayInputStream inputStream = new ByteArrayInputStream("1234567890".getBytes("UTF-8"));

    File dir1 = new File(baseDir, "dir1");
    dir1.mkdir();
    File dir11 = new File(dir1, "dir11");
    dir11.mkdir();
    File dir2 = new File(baseDir, "dir2");
    dir2.mkdir();
    File dir22 = new File(dir2, "dir22");
    dir22.mkdir();
    File dir3 = new File(baseDir, "dir3");
    dir3.mkdir();

    Files.copy(inputStream, new File(baseDir, "file0"));
    Files.copy(inputStream, new File(dir1, "file1"));
    Files.copy(inputStream, new File(dir11, "file11"));
    Files.copy(inputStream, new File(dir2, "file2"));
    Files.copy(inputStream, new File(dir22, "file22"));

    Files.removeFilesHierarchy(baseDir);
    assertEquals(0, baseDir.list().length);
  }

  @Test
  public void testRemoveFilesHierarchy_NotWrittenFile() throws IOException {
    File baseDir = new File("fixture/files-hierarchy");
    baseDir.mkdir(); // ensure directory is create
    assertEquals(0, baseDir.list().length);

    File file = new File(baseDir, "file");
    Writer writer = new FileWriter(file);
    writer.close();

    Files.removeFilesHierarchy(baseDir);
    assertEquals(0, baseDir.list().length);
  }
  
  @Test
  public void testCopyReaderWriter() throws IOException
  {
    File tmp = createTempFile();
    Files.copy(new FileReader("fixture/util/image.png"), new FileWriter(tmp));
    assertFile(tmp);

    try {
      Files.copy((FileReader)null, new FileWriter(tmp));
      fail("Null reader should rise illegal argument.");
    }
    catch(IllegalArgumentException e) {}
    try {
      Files.copy(new FileReader("fixture/util/image.png"), (FileWriter)null);
      fail("Null writer should rise illegal argument.");
    }
    catch(IllegalArgumentException e) {}
  }

  @Test
  public void testCopyInputStreamOutputStream() throws IOException
  {
    File tmp = createTempFile();
    Files.copy(new FileInputStream("fixture/util/image.png"), new FileOutputStream(tmp));
    assertFile(tmp);

    try {
      Files.copy((FileInputStream)null, new FileOutputStream(tmp));
      fail("Null input stream should rise illegal argument.");
    }
    catch(IllegalArgumentException e) {}
    try {
      Files.copy(new FileInputStream("fixture/util/image.png"), (FileOutputStream)null);
      fail("Null output stream should rise illegal argument.");
    }
    catch(IllegalArgumentException e) {}
  }

  @Test
  public void testCopyFileOutputStream() throws IOException
  {
    File tmp = createTempFile();
    Files.copy(new File("fixture/util/image.png"), new FileOutputStream(tmp));
    assertFile(tmp);

    try {
      Files.copy((File)null, new FileOutputStream(tmp));
      fail("Null input file should rise illegal argument.");
    }
    catch(IllegalArgumentException e) {}
    try {
      Files.copy(new File("fixture/util/image.png"), (FileOutputStream)null);
      fail("Null output stream should rise illegal argument.");
    }
    catch(IllegalArgumentException e) {}
  }

  private static File createTempFile() throws IOException
  {
    File tmp = File.createTempFile("test", null);
    tmp.deleteOnExit();
    return tmp;
  }

  private void assertFile(File tmp) throws IOException
  {
    String expected = Strings.load(new FileReader("fixture/util/image.png"));
    String concrete = Strings.load(tmp);
    assertEquals(expected, concrete);
  }
}
