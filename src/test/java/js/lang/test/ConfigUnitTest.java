package js.lang.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import js.converter.ConverterException;
import js.lang.Config;
import js.lang.ConfigBuilder;

import org.junit.Test;

/**
 * Test managed objects configuration.
 * 
 * @author Iulin Rotaru
 */
public class ConfigUnitTest
{
  @Test
  public void testConstructor()
  {
    Config config = new Config("config");
    assertEquals("config", config.getName());
    assertNull(config.getValue());
    assertNull(config.getParent());
    assertFalse(config.hasChildren());
    assertEquals(0, config.getChildrenCount());
    assertEquals(0, config.getProperties().size());
  }

  @Test
  public void testAddChild()
  {
    Config config = new Config("root");
    Config child = new Config("child");
    config.addChild(child);
    assertEquals(1, config.getChildrenCount());
    assertEquals(child, config.getChild("child"));
    assertEquals(config, child.getParent());
  }

  @Test
  public void testHasChildren()
  {
    Config parent = new Config("parent");
    assertFalse(parent.hasChildren());
    Config child = new Config("child");
    parent.addChild(child);
    assertTrue(parent.hasChildren());
  }

  @Test
  public void testHasChild()
  {
    Config parent = new Config("parent");
    assertFalse(parent.hasChild("child"));
    Config child = new Config("child");
    parent.addChild(child);
    assertTrue(parent.hasChild("child"));
  }

  @Test
  public void testGetChild() throws Exception
  {
    Config config = getConfig("<?xml version='1.0' ?>" + //
        "<config>" + //
        "   <section />" + //
        "</config>");

    Config section = config.getChild("section");
    assertNotNull(section);
    assertEquals("section", section.getName());
    assertNull("Searching for fake section should return null.", config.getChild("fake-section"));
  }

  @Test
  public void testGetChildByIndex()
  {
    Config parent = new Config("parent");
    parent.addChild(new Config("child0"));
    parent.addChild(new Config("child1"));
    assertEquals("child0", parent.getChild(0).getName());
    assertEquals("child1", parent.getChild(1).getName());
  }

  @Test
  public void testSetAttribute()
  {
    Config config = new Config("config");
    config.setAttribute("attr1", "attr1");
    assertEquals("attr1", config.getAttribute("attr1"));
    assertNull(config.getAttribute("attr2"));
  }

  @Test
  public void testAttributeType()
  {
    Config config = new Config("config");
    config.setAttribute("file", "path");
    assertEquals("path", config.getAttribute("file"));
    assertEquals(new File("path"), config.getAttribute("file", File.class));
  }

  @Test
  public void testAttributeDefaultValue()
  {
    Config config = new Config("config");
    assertEquals("default", config.getAttribute("attr", "default"));
    assertEquals(new File("path"), config.getAttribute("attr", File.class, new File("path")));
  }

  @Test
  public void testHasAttribute()
  {
    Config config = new Config("config");
    config.setAttribute("attr1", "value1");
    assertTrue(config.hasAttribute("attr1"));
    assertTrue(config.hasAttribute("attr1", "value1"));
    assertFalse(config.hasAttribute("attr2"));
  }

  @Test
  public void testSetProperties()
  {
    Config config = new Config("config");
    Properties properties = config.getProperties();
    assertNotNull(properties);
    config.setProperties(new Properties());
    assertNotSame(properties, config.getProperties());
  }

  @Test
  public void testSetProperty()
  {
    Config config = new Config("config");
    config.setProperty("prop1", "prop1");
    assertEquals("prop1", config.getProperty("prop1"));

    // null set is NOP
    config.setProperty("prop1", null);
    assertEquals("prop1", config.getProperty("prop1"));

    File file = new File("file");
    config.setProperty("file", file);
    assertEquals(file.getPath(), config.getProperty("file"));
    assertEquals("prop1", config.getProperty("prop1"));
  }

  @Test
  public void testOverrideProperty()
  {
    Config config = new Config("config");
    config.setProperty("prop1", "prop1");
    assertEquals("prop1", config.getProperty("prop1"));
    config.setProperty("prop1", new File("file"));
    assertEquals("file", config.getProperty("prop1"));
  }

  @Test
  public void testUnusedProperties()
  {
    Config config = new Config("config");
    config.setProperty("prop1", "value1");
    config.setProperty("prop2", "value2");
    config.getProperty("prop1");

    Properties properties = config.getProperties();
    assertTrue(properties.containsKey("prop1"));
    assertTrue(properties.containsKey("prop2"));

    properties = config.getUnusedProperties();
    assertFalse(properties.containsKey("prop1"));
    assertTrue(properties.containsKey("prop2"));
  }

  @Test
  public void testHasProperty()
  {
    Config config = new Config("config");
    config.setProperty("prop1", "value1");
    assertTrue(config.hasProperty("prop1"));
    assertFalse(config.hasProperty("prop2"));
  }

  @Test
  public void testPropertyDefaultValue()
  {
    Config config = new Config("config");
    assertEquals("value", config.getProperty("prop", "value"));
    assertEquals(new File("file"), config.getProperty("prop", File.class, new File("file")));
  }

  @Test
  public void testPropertyType() throws Exception
  {
    Config config = getConfig("<?xml version='1.0' ?>" + //
        "<config>" + //
        "   <section>" + //
        "       <property name='scope' value='SESSION' />" + //
        "       <property name='year' value='2012' />" + //
        "       <property name='file' value='/var/log/tomcat/catalina.out' />" + //
        "       <property name='name' value='John Doe' />" + //
        "       <property name='url' value='http://server.com/' />" + //
        "       <property name='date' value='2012-12-23T23:23:23Z' />" + //
        "   </section>" + //
        "</config>");

    Config section = config.getChild("section");
    String scope = section.getProperty("scope", String.class);
    int year = section.getProperty("year", int.class);
    File file = section.getProperty("file", File.class);
    String name = section.getProperty("name", String.class);
    URL url = section.getProperty("url", URL.class);

    assertEquals("Invalid enumeration property.", "SESSION", scope);
    assertEquals("Invalid integer property", 2012, year);
    assertEquals("Invalid file property", "/var/log/tomcat/catalina.out", file.getPath().replaceAll("\\\\", "/"));
    assertEquals("Invalid string property", "John Doe", name);
    assertEquals("Invalid URL property", "http://server.com/", url.toExternalForm());
  }

  @Test
  public void testPropertyConverterException()
  {
    Config config = new Config("config");
    config.setProperty("prop", "value");
    try {
      config.getProperty("prop", Config.class);
      fail("Unsupported type should generate exception.");
    }
    catch(ConverterException e) {
      assertEquals("No registered converter for |class js.lang.Config|.", e.getMessage());
    }
  }

  @Test
  public void testSetValue()
  {
    Config config = new Config("config");
    config.setValue("value1");
    assertEquals("value1", config.getValue());
    config.setValue(new File("file"));
    assertEquals("file", config.getValue());
  }

  @Test
  public void testGetRoot()
  {
    Config parent = new Config("parent");
    Config child = new Config("child");
    Config nephew = new Config("nephew");
    parent.addChild(child);
    child.addChild(nephew);

    assertEquals(parent, parent.getRoot());
    assertEquals(parent, child.getRoot());
    assertEquals(parent, nephew.getRoot());
  }

  @Test
  public void testGetParent()
  {
    Config parent = new Config("parent");
    Config child = new Config("child");
    Config nephew = new Config("nephew");
    parent.addChild(child);
    child.addChild(nephew);

    assertNull(parent.getParent());
    assertEquals(parent, child.getParent());
    assertEquals(child, nephew.getParent());
  }

  @Test
  public void testFindChildren() throws Exception
  {
    Config config = getConfig("<?xml version='1.0' ?>" + //
        "<config>" + //
        "   <section>" + //
        "   	<element name='1' />" + //
        "   </section>" + //
        "   <section>" + //
        "   	<element name='2' />" + //
        "   </section>" + //
        "</config>");

    List<Config> sections = config.findChildren("section");
    assertEquals(2, sections.size());
    assertEquals("section", sections.get(0).getName());
    assertEquals("section", sections.get(1).getName());

    assertEquals(1, sections.get(0).getChildren().size());
    assertEquals("1", sections.get(0).getChildren().get(0).getAttribute("name"));
    assertEquals(1, sections.get(1).getChildren().size());
    assertEquals("2", sections.get(1).getChildren().get(0).getAttribute("name"));

    assertTrue("Searching for fake sections should return empty list.", config.findChildren("fake-section").isEmpty());
  }

  @Test
  public void testValue()
  {
    Config config = new Config("config");
    assertNull(config.getValue());

    config.setValue("value");
    assertEquals("value", config.getValue());

    config.setValue(new File("value"));
    assertEquals("value", config.getValue());
    assertEquals(new File("value"), config.getValue(File.class));

    config.setValue(null);
    assertNull(config.getValue());
    assertNull(config.getValue(File.class));
    assertNull(config.getValue(Object.class));
  }

  @Test
  public void testValueConverterException()
  {
    Config config = new Config("config");
    try {
      config.setValue(new Object());
      fail("Unsupported type should generate exception.");
    }
    catch(ConverterException e) {
      assertEquals("No registered converter for |class java.lang.Object|.", e.getMessage());
    }
  }

  @Test
  public void testDump()
  {
    Config parent = new Config("parent");
    Config child = new Config("child");
    Config nephew = new Config("nephew");
    parent.addChild(child);
    child.addChild(nephew);

    PrintStream out = System.out;
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    System.setOut(new PrintStream(buffer));
    parent.dump();
    System.setOut(out);
    assertEquals("parent\r\n\tchild\r\n\t\tnephew\r\n", buffer.toString());
  }

  // ----------------------------------------------------------------------------------------------
  // UTILITY METHODS

  private static Config getConfig(String configXML) throws Exception
  {
    ConfigBuilder builder = new ConfigBuilder(configXML);
    return builder.build();
  }
}
