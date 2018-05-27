package js.io.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import js.io.FilesInputStream;
import js.io.FilesOutputStream;
import js.util.Files;

import org.junit.Test;

public class FilesStreamUnitTest
{
  @Test
  public void testGetBaseDir() throws IOException
  {
    final File baseDir = new File("fixture/files-stream");
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    FilesOutputStream outputFiles = new FilesOutputStream(bytes);
    outputFiles.putMeta("Base-Directory", baseDir);
    outputFiles.addFiles(new File("fixture/files-iterator"));
    outputFiles.close();

    FilesInputStream inputFiles = new FilesInputStream(new ByteArrayInputStream(bytes.toByteArray()));
    File extractedBaseDir = inputFiles.getMeta("Base-Directory", File.class);
    assertNotNull(extractedBaseDir);
    assertEquals(baseDir, extractedBaseDir);

    for(File file : inputFiles) {
      file = new File(baseDir, file.getPath());
      Files.mkdirs(file);
      // Files.copy(inputFiles, new FileOutputStream(file));
    }
    inputFiles.close();
  }

  @Test
  public void testWriteFilesArchiveToFile() throws IOException
  {
    File baseDir = new File("fixture/files-iterator");
    File archiveFile = new File("fixture/files-stream/archive-file.zip");

    FilesOutputStream outputFiles = new FilesOutputStream(new FileOutputStream(archiveFile));
    outputFiles.putMeta("Base-Directory", baseDir);
    outputFiles.addFiles(baseDir);
    outputFiles.close();

    FilesInputStream inputFiles = new FilesInputStream(new FileInputStream(archiveFile));
    assertEquals(baseDir, inputFiles.getMeta("Base-Directory", File.class));
    for(File file : inputFiles) {
      System.out.println(file);
    }
    inputFiles.close();
  }
}
