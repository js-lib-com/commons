package com.jslib.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

public class FilesIteratorUnitTest
{
  @Test
  public void testIterator()
  {
    Collection<File> files = new ArrayList<File>();
    File projectDir = new File("fixture/files-iterator");
    FilesIterator<File> it = FilesIterator.getAbsoluteIterator(projectDir);
    while(it.hasNext()) {
      files.add(it.next());
    }

    assertEquals(11, it.getProcessedFilesCount());
    assertEquals(11, files.size());
    assertTrue(files.contains(new File("fixture/files-iterator/file0")));
    assertTrue(files.contains(new File("fixture/files-iterator/dir1/file0")));
    assertTrue(files.contains(new File("fixture/files-iterator/dir1/file1")));
    assertTrue(files.contains(new File("fixture/files-iterator/dir1/dir11/file0")));
    assertTrue(files.contains(new File("fixture/files-iterator/dir1/dir11/file1")));
    assertTrue(files.contains(new File("fixture/files-iterator/dir1/dir11/file2")));
    assertTrue(files.contains(new File("fixture/files-iterator/dir2/file0")));
    assertTrue(files.contains(new File("fixture/files-iterator/dir2/file1")));
    assertTrue(files.contains(new File("fixture/files-iterator/dir2/dir22/file0")));
    assertTrue(files.contains(new File("fixture/files-iterator/dir2/dir22/file1")));
    assertTrue(files.contains(new File("fixture/files-iterator/dir2/dir22/file2")));
  }

  @Test
  public void testRelativeIterator()
  {
    Collection<File> files = new ArrayList<File>();
    File projectDir = new File("fixture/files-iterator");
    FilesIterator<File> it = FilesIterator.getRelativeIterator(projectDir);
    while(it.hasNext()) {
      files.add(it.next());
    }

    assertEquals(11, it.getProcessedFilesCount());
    assertEquals(11, files.size());
    assertTrue(files.contains(new File("file0")));
    assertTrue(files.contains(new File("dir1/file0")));
    assertTrue(files.contains(new File("dir1/file1")));
    assertTrue(files.contains(new File("dir1/dir11/file0")));
    assertTrue(files.contains(new File("dir1/dir11/file1")));
    assertTrue(files.contains(new File("dir1/dir11/file2")));
    assertTrue(files.contains(new File("dir2/file0")));
    assertTrue(files.contains(new File("dir2/file1")));
    assertTrue(files.contains(new File("dir2/dir22/file0")));
    assertTrue(files.contains(new File("dir2/dir22/file1")));
    assertTrue(files.contains(new File("dir2/dir22/file2")));
  }

  @Test
  public void testDirectoryWhithNoSubdirectories()
  {
    Collection<File> files = new ArrayList<File>();
    File projectDir = new File("fixture/files-iterator/dir1/dir11");
    FilesIterator<File> it = FilesIterator.getAbsoluteIterator(projectDir);
    while(it.hasNext()) {
      files.add(it.next());
    }

    assertEquals(3, it.getProcessedFilesCount());
    assertEquals(3, files.size());
    assertTrue(files.contains(new File("fixture/files-iterator/dir1/dir11/file0")));
    assertTrue(files.contains(new File("fixture/files-iterator/dir1/dir11/file1")));
    assertTrue(files.contains(new File("fixture/files-iterator/dir1/dir11/file2")));
  }

  @Test
  public void testRelativeDirectoryWhithNoSubdirectories()
  {
    Collection<File> files = new ArrayList<File>();
    File projectDir = new File("fixture/files-iterator/dir1/dir11");
    FilesIterator<File> it = FilesIterator.getRelativeIterator(projectDir);
    while(it.hasNext()) {
      files.add(it.next());
    }

    assertEquals(3, it.getProcessedFilesCount());
    assertEquals(3, files.size());
    assertTrue(files.contains(new File("file0")));
    assertTrue(files.contains(new File("file1")));
    assertTrue(files.contains(new File("file2")));
  }

  @Test
  public void testEmptyDirectory()
  {
    for(String baseDir : new String[]
    {
        "fixture/files-iterator/dir0", "fixture/files-iterator/file3"
    }) {
      Collection<File> files = new ArrayList<File>();
      File projectDir = new File(baseDir);
      projectDir.mkdir();
      FilesIterator<File> it = FilesIterator.getAbsoluteIterator(projectDir);
      while(it.hasNext()) {
        files.add(it.next());
      }
      assertEquals(0, it.getProcessedFilesCount());
      assertEquals(0, files.size());
    }
  }

  @Test
  public void testSuccessfulScanningLargeDirectoryHierarchy()
  {
    File baseDir = new File(".");
    FilesIterator<File> it = FilesIterator.getAbsoluteIterator(baseDir);
    while(it.hasNext()) {
      it.next();
    }
    // choose a reasonable value for the count of expected files in this project
    // at this test creation there was 5000+ but uses 2000 just to be sure
    assertTrue(it.getProcessedFilesCount() > 100);
    System.out.printf("Files iterator found %d files in %s\r\n", it.getProcessedFilesCount(), baseDir.getAbsolutePath());
  }
}
