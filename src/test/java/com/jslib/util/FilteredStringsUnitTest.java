package com.jslib.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FilteredStringsUnitTest
{
  private static String[] files =
  {
      ".classpath", ".project", "tomcat.log", "index.html", "index.css", "index.js", "MainClass.java", "Logger.java"
  };

  @Test
  public void testStartsWithPredicate()
  {
    FilteredStrings strings = new FilteredStrings(".*");
    strings.addAll(files);
    assertEquals(2, strings.size());
    assertEquals(".classpath", strings.get(0));
    assertEquals(".project", strings.get(1));
  }

  @Test
  public void testStartsWithPredicateWithPrefix()
  {
    FilteredStrings strings = new FilteredStrings("index.*");
    strings.addAll("/var/www/", files);
    assertEquals(3, strings.size());
    assertEquals("/var/www/index.html", strings.get(0));
    assertEquals("/var/www/index.css", strings.get(1));
    assertEquals("/var/www/index.js", strings.get(2));
  }

  @Test
  public void testContainsPredicate()
  {
    FilteredStrings strings = new FilteredStrings("*dex*");
    strings.addAll(files);
    assertEquals(3, strings.size());
    assertEquals("index.html", strings.get(0));
    assertEquals("index.css", strings.get(1));
    assertEquals("index.js", strings.get(2));
  }

  @Test
  public void testEndsWithPredicate()
  {
    FilteredStrings strings = new FilteredStrings("*.java");
    strings.addAll(files);
    assertEquals(2, strings.size());
    assertEquals("MainClass.java", strings.get(0));
    assertEquals("Logger.java", strings.get(1));
  }
}
