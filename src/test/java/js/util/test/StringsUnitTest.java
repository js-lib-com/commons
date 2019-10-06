package js.util.test;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import js.lang.Handler;
import js.lang.Pair;
import js.util.Strings;

public class StringsUnitTest
{
  @Test
  public void toTitleCase()
  {
    assertEquals("Iulian", Strings.toTitleCase("iulian"));
    assertEquals("Iulian", Strings.toTitleCase("iULIAN"));
    assertEquals("Iulian", Strings.toTitleCase("IULIAN"));
    assertEquals("Iulian Rotaru", Strings.toTitleCase("IULIAN rotaru"));
    assertTrue(Strings.toTitleCase("").isEmpty());
    assertNull(Strings.toTitleCase(null));
  }

  @Test
  public void dashedToMemberName()
  {
    assertEquals("thisIsAString", Strings.dashedToMemberName("this-is-a-string"));
    assertEquals("thisIsAString", Strings.dashedToMemberName("this-is--a-string"));
    assertEquals("thisIsAString", Strings.dashedToMemberName("thisIsAString"));
    assertEquals("upload", Strings.dashedToMemberName("upload"));
    assertTrue(Strings.dashedToMemberName("").isEmpty());
    assertNull(Strings.dashedToMemberName(null));
  }

  @Test
  public void memberToDashCase()
  {
    assertEquals("this-is-a-string", Strings.memberToDashCase("thisIsAString"));
    assertEquals("a-b-c", Strings.memberToDashCase("ABC"));
    assertTrue(Strings.memberToDashCase("").isEmpty());
    assertNull(Strings.memberToDashCase(null));
  }

  @Test
  public void enumToMemberName()
  {
    assertEquals("none", Strings.enumToMemberName("NONE"));
    assertEquals("postalAddress", Strings.enumToMemberName("POSTAL_ADDRESS"));
    assertEquals("postalAddress", Strings.enumToMemberName("_POSTAL__ADDRESS_"));
    assertEquals("none", Strings.enumToMemberName("none"));
    assertEquals("postaladdress", Strings.enumToMemberName("postalAddress"));
    assertNull(Strings.enumToMemberName((String)null));
  }

  @Test
  public void enumToMemberName_EnumerationConstant()
  {
    assertEquals("none", Strings.enumToMemberName(EnumNames.NONE));
    assertEquals("postalAddress", Strings.enumToMemberName(EnumNames.POSTAL_ADDRESS));
    assertEquals("postalAddress", Strings.enumToMemberName(EnumNames._POSTAL__ADDRESS_));
    assertEquals("none", Strings.enumToMemberName(EnumNames.none));
    assertEquals("postaladdress", Strings.enumToMemberName(EnumNames.postalAddress));
    assertNull(Strings.enumToMemberName((Enum<?>)null));
  }

  @Test
  public void isMemberName()
  {
    assertTrue(Strings.isMemberName("thisIsAString"));
    assertTrue(Strings.isMemberName("upload"));
    assertFalse(Strings.isMemberName(""));
    assertFalse(Strings.isMemberName(null));
  }

  @Test
  public void testToString()
  {
    assertEquals("true:null:string:12.34", Strings.toString(true, null, "string", 12.34));
  }

  @Test
  public void splitStringByChar()
  {
    assertEqualsList(Strings.split("one two three four"));
    assertEqualsList(Strings.split("one,two,three,four", ','));
    assertEqualsList(Strings.split(",one,two,three,four", ','));
    assertEqualsList(Strings.split("one,two,three,four,", ','));
    assertEqualsList(Strings.split(",one,two,three,four,", ','));
  }

  @Test
  public void splitStringByString()
  {
    assertEqualsList(Strings.split("one two three four"));
    assertEqualsList(Strings.split("one,two,three,four", ","));
    assertEqualsList(Strings.split(",one,two,three,four", ","));
    assertEqualsList(Strings.split("one,two,three,four,", ","));
    assertEqualsList(Strings.split(",one,two,three,four,", ","));
  }

  @Test
  public void splitStringByString_InvalidSeparator()
  {
    assertThat(Strings.split(null, ","), nullValue());
    assertThat(Strings.split("one two three four", (String)null), nullValue());
    assertThat(Strings.split("", ","), empty());
    assertThat(Strings.split("one,two,three,four", ""), empty());
    assertThat(Strings.split(null, ""), nullValue());
    assertThat(Strings.split("", (String)null), nullValue());
  }

  @Test
  public void typedSplitStringByString_Integers()
  {
    List<Integer> integers = Strings.split("10,11,12,13", ",", Integer.class);
    assertThat(integers, notNullValue());
    assertThat(integers, hasSize(4));
    assertThat(integers.get(0), equalTo(10));
    assertThat(integers.get(1), equalTo(11));
    assertThat(integers.get(2), equalTo(12));
    assertThat(integers.get(3), equalTo(13));
  }

  @Test
  public void typedSplitStringByString_InvalidSeparator()
  {
    assertThat(Strings.split(null, ",", Integer.class), nullValue());
    assertThat(Strings.split("1 2 3 4", (String)null, Integer.class), nullValue());
    assertThat(Strings.split("", ",", Integer.class), empty());
    assertThat(Strings.split("1,2,3,4", "", Integer.class), empty());
    assertThat(Strings.split(null, "", Integer.class), nullValue());
    assertThat(Strings.split("", (String)null, Integer.class), nullValue());
  }

  @Test
  public void splitTrim()
  {
    assertEqualsList(Strings.split("  one, two, three, four  ", ','));
    assertEqualsList(Strings.split("  one, two, three, four  ", ","));
    assertEqualsList(Strings.split("  one  ,  two , three ,  four   ", ','));
    assertEqualsList(Strings.split("  one  ,  two , three ,  four   ", ","));
  }

  private static void assertEqualsList(List<String> strings)
  {
    assertThat(strings, notNullValue());
    assertThat(strings, hasSize(4));
    assertThat(strings.get(0), equalTo("one"));
    assertThat(strings.get(1), equalTo("two"));
    assertThat(strings.get(2), equalTo("three"));
    assertThat(strings.get(3), equalTo("four"));
  }

  @Test
  public void splitEmptyItems()
  {
    List<String> strings = Strings.split(",,one, ,two,,", ',');
    assertEquals(2, strings.size());
    assertEquals("one", strings.get(0));
    assertEquals("two", strings.get(1));
  }

  @Test
  public void splitPathSegmentsByChar()
  {
    for(String path : new String[]
    {
        "compo/discography", "/compo/discography", "compo/discography/", "/compo/discography/"
    }) {
      List<String> segments = Strings.split(path, '/');
      assertEquals(2, segments.size());
      assertEquals("compo", segments.get(0));
      assertEquals("discography", segments.get(1));
    }
  }

  @Test
  public void splitPathSegmentsByString()
  {
    for(String path : new String[]
    {
        "compo/discography", "/compo/discography", "compo/discography/", "/compo/discography/"
    }) {
      List<String> segments = Strings.split(path, "/");
      assertEquals(2, segments.size());
      assertEquals("compo", segments.get(0));
      assertEquals("discography", segments.get(1));
    }
  }

  @Test
  public void splitPairs()
  {
    assertPairs(Strings.splitPairs("john:doe;jane:doe;", ';', ':'));
    assertPairs(Strings.splitPairs("john:doe;jane:doe", ';', ':'));
    assertPairs(Strings.splitPairs(" john : doe ; jane : doe  ", ';', ':'));
    assertPairs(Strings.splitPairs(" john : doe ; jane : doe ; ", ';', ':'));
  }

  private static void assertPairs(List<Pair> pairs)
  {
    assertThat(pairs.get(0).first(), equalTo("john"));
    assertThat(pairs.get(0).key(), equalTo("john"));
    assertThat(pairs.get(0).second(), equalTo("doe"));
    assertThat(pairs.get(0).value(), equalTo("doe"));
    assertThat(pairs.get(1).first(), equalTo("jane"));
    assertThat(pairs.get(1).key(), equalTo("jane"));
    assertThat(pairs.get(1).second(), equalTo("doe"));
    assertThat(pairs.get(1).value(), equalTo("doe"));
  }

  @Test
  public void firstSentence()
  {
    String phrase = "Managed class. Managed class used by...";
    assertEquals("Managed class.", Strings.firstSentence(phrase));

    phrase = "Managed utility.class. Managed class used by...";
    assertEquals("Managed utility.class.", Strings.firstSentence(phrase));

    phrase = "Managed {@link utility.class}. Managed class used by...";
    assertEquals("Managed {@link utility.class}.", Strings.firstSentence(phrase));
  }

  @Test
  public void firstWord()
  {
    assertNull(Strings.firstWord(null));
    assertEquals("", Strings.firstWord(""));
    assertEquals("One", Strings.firstWord("One."));

    assertEquals("One.two", Strings.firstWord("One.two three"));
    assertEquals("One.two", Strings.firstWord("One.two. three"));
    assertEquals("One...", Strings.firstWord("One... two three"));
    assertEquals("(One)", Strings.firstWord("(One) two three"));
    assertEquals("[One]", Strings.firstWord("[One] two three"));
    assertEquals("{One}", Strings.firstWord("{One} two three"));
    assertEquals("<One>", Strings.firstWord("<One> two three"));
    assertEquals("{One. two}", Strings.firstWord("{One. two} three"));

    String[] sentences = new String[]
    {
        "One two three.", "One\ttwo three.", "One\rtwo three.", "One\ntwo three.", "One, two three.", "One: two three.", "One; two three.", "One(two) three.",
        "One{two} three.", "One[two] three.", "One<two> three."
    };
    for(String s : sentences) {
      assertEquals("One", Strings.firstWord(s));
    }
  }

  @Test
  public void trim()
  {
    assertEquals("One two three", Strings.trim("One two three", '('));
    assertEquals("One two three", Strings.trim("(One two three", '('));
    assertEquals("One two three", Strings.trim("(One two three)", '('));
    assertEquals("[One two three]", Strings.trim("[One two three]", '('));
    assertEquals("One two three", Strings.trim("[One two three]", '['));
    assertEquals("One two three", Strings.trim("{One two three}", '{'));
    assertEquals("One two three", Strings.trim("<One two three>", '<'));
    assertEquals("One two three", Strings.trim("\r\nOne two three\r\n"));
    assertEquals("One two three", Strings.trim(" \r\n\tOne two three"));

    assertEquals("R", Strings.trim("R", ','));
    assertEquals("", Strings.trim(",", ','));
    assertEquals("", Strings.trim(",,", ','));
    assertEquals("", Strings.trim(",,,,,", ','));
    assertEquals("", Strings.trim("", ','));

    assertNull(Strings.trim(null, '('));
    assertNull(Strings.trim(null));
  }

  @Test
  public void removeFirstWord()
  {
    assertEquals("two three", Strings.removeFirstWord("One two three"));
    assertEquals("two three", Strings.removeFirstWord("One, two three"));
    assertEquals("two three", Strings.removeFirstWord("One \r\n two three"));
    assertEquals("two three", Strings.removeFirstWord("One\ttwo three"));
  }

  @Test
  public void escapeRexExp()
  {
    assertEquals("\\/", Strings.escapeRegExp("/"));
    assertEquals("\\.", Strings.escapeRegExp("."));
    assertEquals("\\*", Strings.escapeRegExp("*"));
    assertEquals("\\?", Strings.escapeRegExp("?"));
    assertEquals("\\|", Strings.escapeRegExp("|"));
    assertEquals("\\(", Strings.escapeRegExp("("));
    assertEquals("\\)", Strings.escapeRegExp(")"));
    assertEquals("\\[", Strings.escapeRegExp("["));
    assertEquals("\\]", Strings.escapeRegExp("]"));
    assertEquals("\\{", Strings.escapeRegExp("{"));
    assertEquals("\\}", Strings.escapeRegExp("}"));
    assertEquals("\\\\", Strings.escapeRegExp("\\"));
    assertEquals("\\^", Strings.escapeRegExp("^"));
    assertEquals("\\$", Strings.escapeRegExp("$"));
    assertEquals("a\\/b\\.c\\*d\\?e\\|f\\(g\\)h\\[i\\]j\\{k\\}l\\\\m\\^n\\$ bla bla bla", Strings.escapeRegExp("a/b.c*d?e|f(g)h[i]j{k}l\\m^n$ bla bla bla"));
    assertEquals("abcdefghijklmn", Strings.escapeRegExp("abcdefghijklmn"));
  }

  @Test
  public void join() throws Throwable
  {
    String[] strings = new String[]
    {
        "", null, "1", null, null, "2", null, "3"
    };
    assertEquals("1 2 3", Strings.join(strings));
    assertEquals("1 2 3", Strings.join(strings, null));
    assertEquals("1,2,3", Strings.join(strings, ','));
    assertEquals("1,2,3", Strings.join(strings, ","));
    assertEquals("1 2 3", Strings.join(Arrays.asList(strings)));
    assertEquals("1 2 3", Strings.join(Arrays.asList(strings), null));
    assertEquals("1,2,3", Strings.join(Arrays.asList(strings), ','));
    assertEquals("1,2,3", Strings.join(Arrays.asList(strings), ","));
    assertEquals("1, 2, 3", Strings.join(Arrays.asList(strings), ", "));
  }

  @Test
  public void concat()
  {
    assertEquals("", Strings.concat());
    assertEquals("", Strings.concat(null, null));
    assertEquals("123", Strings.concat(null, "1", null, null, "2", null, "3"));
  }

  @Test
  public void isNumeric()
  {
    assertTrue(Strings.isNumeric("0"));
    assertTrue(Strings.isNumeric("1234"));
    assertTrue(Strings.isNumeric("-1234"));
    assertTrue(Strings.isNumeric("1234L"));
    assertTrue(Strings.isNumeric("12.34"));
    assertTrue(Strings.isNumeric("12.34F"));
    assertTrue(Strings.isNumeric("1234E-2"));
    assertTrue(Strings.isNumeric("1234E+2"));
    assertFalse(Strings.isNumeric(null));
    assertFalse(Strings.isNumeric(""));
    assertFalse(Strings.isNumeric("asdf"));
    assertFalse(Strings.isNumeric("1234A"));
  }

  @Test
  public void isInteger()
  {
    assertTrue(Strings.isInteger("0"));
    assertTrue(Strings.isInteger("1234"));
    assertTrue(Strings.isInteger("1,234"));
    assertTrue(Strings.isInteger("-0"));
    assertTrue(Strings.isInteger("-1234"));
    assertTrue(Strings.isInteger("-1,234"));
    assertTrue(Strings.isInteger("+0"));
    assertTrue(Strings.isInteger("+1234"));
    assertTrue(Strings.isInteger("+1,234"));
    assertFalse(Strings.isInteger(null));
    assertFalse(Strings.isInteger(""));
    assertFalse(Strings.isInteger("asdf"));
    assertFalse(Strings.isInteger("1234A"));
  }

  @Test
  public void escapeFilesPattern()
  {
    assertEquals(".+\\.html", Strings.escapeFilesPattern("*.html"));
    assertEquals(".+-view-.+\\.html", Strings.escapeFilesPattern("*-view-*.html"));
    assertEquals("", Strings.escapeFilesPattern(""));
    assertNull(Strings.escapeFilesPattern(null));
  }

  @Test
  public void escapeXML() throws IOException
  {
    assertEquals("&quot;&apos;&amp;&lt;&gt;", Strings.escapeXML("\"'&<>"));
    assertEquals("copyright © j(s)-lib tools ® 2013", Strings.escapeXML("copyright © j(s)-lib tools ® 2013"));

    StringWriter writer = new StringWriter();
    Strings.escapeXML("\"'&<>", writer);
    assertEquals("&quot;&apos;&amp;&lt;&gt;", writer.toString());

    writer = new StringWriter();
    Strings.escapeXML("copyright © j(s)-lib tools ® 2013", writer);
    assertEquals("copyright © j(s)-lib tools ® 2013", writer.toString());
  }

  @Test
  public void escapeXmlOnFile() throws IOException
  {
    File file = new File("fixture/util/escape-xml");

    FileWriter writer = new FileWriter(file);
    Strings.escapeXML("\"'&<>", writer);
    writer.close();
    assertEquals("&quot;&apos;&amp;&lt;&gt;", Strings.load(file));
    file.delete();

    writer = new FileWriter(file);
    Strings.escapeXML("copyright © j(s)-lib tools ® 2013", writer);
    writer.close();
    assertEquals("copyright © j(s)-lib tools ® 2013", Strings.load(file));
    file.delete();
  }

  @Test
  public void replaceAll()
  {
    Pattern pattern = Pattern.compile("i");
    assertEquals("Th<em>i</em>s <em>i</em>s a text.", Strings.replaceAll("This is a text.", pattern, new Handler<String, String>()
    {
      @Override
      public String handle(String match)
      {
        return "<em>" + match + "</em>";
      }
    }));
  }

  @Test
  public void replaceAllWithMultipleGroups()
  {
    Pattern pattern = Pattern.compile("(i)(s)");
    assertEquals("Th<em>i</em><em>s</em> <em>i</em><em>s</em> a text.", Strings.replaceAll("This is a text.", pattern, new Handler<String, String>()
    {
      @Override
      public String handle(String argument)
      {
        return "<em>" + argument + "</em>";
      }
    }));
  }

  @Test
  public void injectProperties()
  {
    System.setProperty("catalina.base", "/usr/share/tomcat");
    assertEquals("app log: /usr/share/tomcat/logs/app.log", Strings.injectProperties("app log: ${catalina.base}/logs/app.log"));
    assertEquals("dir: /usr/share/tomcat/logs/ file: /usr/share/tomcat/logs/app.log",
        Strings.injectProperties("dir: ${catalina.base}/logs/ file: ${catalina.base}/logs/app.log"));
    assertEquals("/logs/app.log", Strings.injectProperties("/logs/app.log"));
  }

  @Test
  public void splitByString()
  {
    List<String> strings = Strings.split("Iulian Rotaru\r\nBogdan Laurentiu Jipa", "\r\n");
    assertEquals("Iulian Rotaru", strings.get(0));
    assertEquals("Bogdan Laurentiu Jipa", strings.get(1));
  }

  @Test
  public void getProtocol()
  {
    assertEquals("http", Strings.getProtocol("http://server.com"));
    assertNull(Strings.getProtocol(null));
    assertNull(Strings.getProtocol(""));
    assertNull(Strings.getProtocol("server.com/resource"));
    assertNull(Strings.getProtocol("http:/server.com"));
  }

  // ----------------------------------------------------------------------------------------------

  private enum EnumNames
  {
    NONE, POSTAL_ADDRESS, _POSTAL__ADDRESS_, none, postalAddress
  }
}
