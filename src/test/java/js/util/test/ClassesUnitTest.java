package js.util.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import js.io.ReaderInputStream;
import js.lang.BugError;
import js.lang.NoSuchBeingException;
import js.util.Classes;
import js.util.Strings;

public class ClassesUnitTest
{
  @Test
  public void javaComplianceResourceName()
  {
    // Class#getResourceAsStream uses both relative and absolute resource path
    // obviously, if resource path does not start with path separator is relative to class location into package
    // as a consequence if resources include package it should be absolute and start with leading path separator

    assertNotNull(ClassesUnitTest.class.getResourceAsStream("/js/util/test/ClassesUnitTest.class"));
    assertNotNull(ClassesUnitTest.class.getResourceAsStream("ClassesUnitTest.class"));
    // resource path with package but without leading path separator are considered relative to class
    assertNull(ClassesUnitTest.class.getResourceAsStream("js/util/test/ClassesUnitTest.class"));

    // ClaassLoader#getResourceAsStream does not use relative paths and is pretty normal since class loader is global
    // resource paths are always absolute and does not start with leading path separator

    assertNotNull(ClassesUnitTest.class.getClassLoader().getResourceAsStream("js/util/test/ClassesUnitTest.class"));
    assertNull(ClassesUnitTest.class.getClassLoader().getResourceAsStream("ClassesUnitTest.class"));
    assertNull(ClassesUnitTest.class.getClassLoader().getResourceAsStream("/js/util/test/ClassesUnitTest.class"));
  }

  @Test
  public void getResource() throws IOException
  {
    assertNotNull(Classes.getResource("js/util/test/ClassesUnitTest.class"));
    // not documented feature: accept but ignore leading path separator
    assertNotNull(Classes.getResource("/js/util/test/ClassesUnitTest.class"));

    URL url = Classes.getResource("js/util/test/resource.txt");
    assertNotNull(url);
    assertResource(url.openStream());

    url = Classes.getResource("resource.txt");
    assertNotNull(url);
    assertResource(url.openStream());
  }

  @Test
  public void getPackageURL()
  {
    URL url = Classes.getResource("js/util/test/");
    assertNotNull(url);
    assertEquals("file", url.getProtocol());
    assertTrue(url.getPath().endsWith("js/util/test/"));

    // test package root denoted by empty resource name
    url = Classes.getResource("");
    assertNotNull(url);
    assertEquals("file", url.getProtocol());
    // this assert is working only if Maven target directory convention is not changed
    assertTrue(url.getPath().endsWith("test-classes/"));
  }

  @Test
  public void getResourceAsStream() throws IOException
  {
    assertNotNull(Classes.getResourceAsStream("js/util/test/ClassesUnitTest.class"));
    // not documented feature: accept but ignore leading path separator
    assertNotNull(Classes.getResourceAsStream("/js/util/test/ClassesUnitTest.class"));

    InputStream stream = Classes.getResourceAsStream("js/util/test/resource.txt");
    assertNotNull(stream);
    assertResource(stream);

    stream = Classes.getResourceAsStream("resource.txt");
    assertNotNull(stream);
    assertResource(stream);
  }

  @Test
  public void getResourceAsReader() throws IOException
  {
    assertNotNull(Classes.getResourceAsReader("js/util/test/ClassesUnitTest.class"));
    // not documented feature: accept but ignore leading path separator
    assertNotNull(Classes.getResourceAsReader("/js/util/test/ClassesUnitTest.class"));

    Reader reader = Classes.getResourceAsReader("/js/util/test/resource.txt");
    assertNotNull(reader);
    assertResource(new ReaderInputStream(reader));

    reader = Classes.getResourceAsReader("resource.txt");
    assertNotNull(reader);
    assertResource(new ReaderInputStream(reader));
  }

  private static void assertResource(InputStream inputStream) throws IOException
  {
    String expected = "resource.txt";
    String concrete = Strings.load(inputStream);
    assertEquals(expected, concrete);
  }

  @Test
  public void getResourceAsString() throws IOException
  {
    assertNotNull(Classes.getResourceAsString("js/util/test/ClassesUnitTest.class"));
    // not documented feature: accept but ignore leading path separator
    assertNotNull(Classes.getResourceAsString("/js/util/test/ClassesUnitTest.class"));

    String expected = "resource.txt";
    String concrete = Classes.getResourceAsString("/js/util/test/resource.txt");
    assertNotNull(concrete);
    assertEquals(expected, concrete);

    concrete = Classes.getResourceAsString("resource.txt");
    assertNotNull(concrete);
    assertEquals(expected, concrete);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getInnerClass() throws Exception
  {
    Class<?> innerClass = Classes.forName("js.util.test.ClassesUnitTest$InnerClass");
    assertNotNull(innerClass);
    assertEquals(InnerClass.class, innerClass);

    Constructor<InnerClass> constructor = (Constructor<InnerClass>)innerClass.getDeclaredConstructor();
    constructor.setAccessible(true);
    InnerClass innerObject = constructor.newInstance();
    assertNotNull(innerObject);
    assertTrue(innerObject instanceof InnerClass);
  }

  @Test
  public void getGetter_GET() throws NoSuchMethodException
  {
    class Person
    {
      @SuppressWarnings("unused")
      boolean getPhoneNumber()
      {
        return false;
      }
    }
    Method method = Classes.getGetter(Person.class, "phone-number");
    assertThat(method, notNullValue());
  }

  @Test
  public void getGetter_IS() throws NoSuchMethodException
  {
    class Person
    {
      @SuppressWarnings("unused")
      boolean isPhoneNumber()
      {
        return false;
      }
    }
    Method method = Classes.getGetter(Person.class, "phone-number");
    assertThat(method, notNullValue());
  }

  @Test(expected = NoSuchMethodException.class)
  public void getGetter_Exception() throws NoSuchMethodException
  {
    class Person
    {
    }
    Classes.getGetter(Person.class, "phone-number");
  }

  @Test
  public void invoke_ConcreteType() throws Throwable
  {
    assertEquals(6, (int)Classes.invoke(new InnerClass(), "addIntegers", 1, 2, 3));
  }

  @Test
  public void invoke_Primitives() throws Throwable
  {
    assertEquals(3, (int)Classes.invoke(new InnerClass(), "addInts", 1, 2));
  }

  /**
   * Method to be invoked has formal parameters declared as interfaces. Trying to invoke it using classes utility should
   * rise no such member exception. This is because Class#getMethod(String, Class...), used by
   * {@link Classes#invoke(Object, String, Object...)}, must be supplied with the very declared types as searched method
   * formal parameters.
   * 
   * @throws Throwable rethrows interface invocation.
   */
  @Test
  public void invoke_Interface() throws Throwable
  {
    assertEquals(3, (int)Classes.invoke(new InnerClass(), "addNumbers", new Integer(1), new Integer(2)));
  }

  @Test
  public void invoke_InnerClass() throws Exception
  {
    InnerClass object = new InnerClass();
    assertEquals(6, (int)Classes.invoke(object, "addIntegers", 1, 2, 3));
  }

  @Test
  public void invoke_Superclass() throws Exception
  {
    SuperClass object = new SuperClass();
    assertEquals(6, (int)Classes.invoke(object, InnerClass.class, "addIntegers", 1, 2, 3));
  }

  @Test
  public void newInstance()
  {
    Object arg = Classes.newInstance("js.util.test.ClassesUnitTest$InnerClass");
    Object obj = Classes.newInstance("js.util.test.ClassesUnitTest$InnerClass", arg);
    assertNotNull(obj);
    assertTrue(obj instanceof InnerClass);
  }

  @Test
  public void newInstance_ByClass() throws ClassNotFoundException
  {
    Object arg = Classes.newInstance("js.util.test.ClassesUnitTest$InnerClass");
    Object obj = Classes.newInstance(InnerClass.class, arg);
    assertNotNull(obj);
    assertTrue(obj instanceof InnerClass);
  }

  @Test
  public void newInstance_WithNullArgument()
  {
    Object obj = Classes.newInstance("js.util.test.ClassesUnitTest$InnerClass", (Object)null);
    assertNotNull(obj);
    assertTrue(obj instanceof InnerClass);
  }

  @Test(expected = NoSuchBeingException.class)
  public void newInstance_WithLastArgumentOfBadType()
  {
    Object innerInterface = Classes.newInstance("js.util.test.ClassesUnitTest$InnerClass");
    Classes.newInstance("js.util.test.ClassesUnitTests$InnerClass", innerInterface, 123);
  }

  @Test(expected = BugError.class)
  public void newInstance_Array()
  {
    Classes.newInstance(Object[].class);
  }

  @Test
  public void newInstance_ArrayList()
  {
    assertTrue(Classes.newInstance(ArrayList.class) instanceof List);
  }

  @Test
  public void newInstance_HashMap()
  {
    assertTrue(Classes.newInstance(HashMap.class) instanceof Map);
  }

  @Test
  public void listPackageResources_LocalClasses()
  {
    Collection<String> resources = Classes.listPackageResources("js.util.test", "*.class");
    assertTrue(resources.size() > 0);
    assertTrue(resources.contains("js/util/test/Base64UnitTest.class"));
    assertTrue(resources.contains("js/util/test/ClassesUnitTest.class"));
    assertTrue(resources.contains("js/util/test/ClassesUnitTest$InnerClass.class"));
  }

  @Test
  public void listPackageResources_PackageRoot()
  {
    Collection<String> resources = Classes.listPackageResources("", "*.txt");
    assertEquals(1, resources.size(), 0);
    assertTrue(resources.contains("resource.txt"));
  }

  public void listPackageResources_Jar() throws ClassNotFoundException, IOException
  {
    // TODO: implement or remove this tets case
  }

  @Test
  public void isInstantiable()
  {
    assertFalse(Classes.isInstantiable(InnerInterface.class));
    assertFalse(Classes.isInstantiable(AbstractClass.class));
    assertFalse(Classes.isInstantiable(NoDefaultConstructor.class));
    assertTrue(Classes.isInstantiable(InnerClass.class));
  }

  @Test
  public void getFieldValue()
  {
    InnerClass object = new InnerClass();
    assertEquals("string", Classes.getFieldValue(object, "string"));
  }

  @Test
  public void setFieldValue()
  {
    InnerClass object = new InnerClass();
    Classes.setFieldValue(object, "string", "value");
    assertEquals("value", object.string);
  }

  @Test
  public void getParameterTypes()
  {
    Type[] types = Classes.getParameterTypes(new Object[]
    {
        1964, "John Doe", true, new Date()
    });

    assertEquals(4, types.length);
    assertEquals(Integer.class, types[0]);
    assertEquals(String.class, types[1]);
    assertEquals(Boolean.class, types[2]);
    assertEquals(Date.class, types[3]);
  }

  @Test
  public void loadService()
  {
    InnerInterface service = Classes.loadService(InnerInterface.class);
    assertNotNull(service);
    assertTrue(service instanceof InnerClass);
    assertEquals("string", Classes.getFieldValue(service, "string"));
  }

  // ------------------------------------------------------
  // FIXTURE

  private static class NoDefaultConstructor
  {
    @SuppressWarnings("unused")
    public NoDefaultConstructor(int fake)
    {
    }
  }

  private static abstract class AbstractClass
  {
  }

  private static interface InnerInterface
  {
  }

  public static class InnerClass implements InnerInterface
  {
    private String string = "string";

    public InnerClass()
    {
    }

    public InnerClass(InnerInterface ii)
    {
    }

    public InnerClass(InnerInterface ii, String s)
    {
    }

    Integer addIntegers(Integer i1, Integer i2, Integer i3)
    {
      return i1 + i2 + i3;
    }

    Integer addInts(int i1, int i2)
    {
      return i1 + i2;
    }

    Integer addNumbers(Number n1, Number n2)
    {
      return n1.intValue() + n2.intValue();
    }

    void method(String name, int age)
    {
    }
  }

  public static class SuperClass extends InnerClass
  {

  }
}
