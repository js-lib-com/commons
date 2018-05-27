package js.util.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import js.lang.BugError;
import js.util.Types;

import org.junit.Test;

public class TypesUnitTest
{
  @Test
  public void testEqualsAny()
  {
    assertTrue(Types.equalsAny(Date.class, Object.class, Date.class));
    assertFalse(Types.equalsAny(String.class, Object.class, Date.class));
    assertFalse(Types.equalsAny(null, Object.class));
  }

  @Test
  public void testIsArray()
  {
    assertTrue(Types.isArray(int[].class));
    assertTrue(Types.isArray(new int[0]));
    assertFalse(Types.isArray(List.class));
    assertFalse(Types.isArray(null));
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void testIsArrayLike()
  {
    int[] array = new int[1];
    assertTrue(Types.isArrayLike(array));
    assertTrue(Types.isArrayLike(array.getClass()));

    Collection collection = Collections.emptyList();
    assertTrue(Types.isArrayLike(collection));
    assertTrue(Types.isArrayLike(collection.getClass()));

    List list = new ArrayList();
    assertTrue(Types.isArrayLike(list));
    assertTrue(Types.isArrayLike(list.getClass()));

    Set set = new HashSet();
    assertTrue(Types.isArrayLike(set));
    assertTrue(Types.isArrayLike(set.getClass()));

    Map map = new HashMap();
    assertFalse(Types.isArrayLike(map));
    assertFalse(Types.isArrayLike(map.getClass()));

    array = null;
    assertFalse(Types.isArray(array));
  }

  @Test
  public void testGetBoxingClass() throws ClassNotFoundException
  {
    assertEquals(Boolean.class, Types.getBoxingClass(boolean.class));
    assertEquals(Boolean.class, Types.getBoxingClass(Boolean.class));

    assertEquals(Byte.class, Types.getBoxingClass(byte.class));
    assertEquals(Byte.class, Types.getBoxingClass(Byte.class));

    assertEquals(Character.class, Types.getBoxingClass(char.class));
    assertEquals(Character.class, Types.getBoxingClass(Character.class));

    assertEquals(Short.class, Types.getBoxingClass(short.class));
    assertEquals(Short.class, Types.getBoxingClass(Short.class));

    assertEquals(Integer.class, Types.getBoxingClass(int.class));
    assertEquals(Integer.class, Types.getBoxingClass(Integer.class));

    assertEquals(Long.class, Types.getBoxingClass(long.class));
    assertEquals(Long.class, Types.getBoxingClass(Long.class));

    assertEquals(Float.class, Types.getBoxingClass(float.class));
    assertEquals(Float.class, Types.getBoxingClass(Float.class));

    assertEquals(Double.class, Types.getBoxingClass(double.class));
    assertEquals(Double.class, Types.getBoxingClass(Double.class));

    try {
      Types.getBoxingClass(Person.class);
      fail("Not boxed types should rise exception.");
    }
    catch(BugError e) {
      assertEquals("Trying to get boxing class from not boxed type.", e.getMessage());
    }
  }

  @Test
  public void testIsDate()
  {
    assertTrue(Types.isDate(Date.class));
    assertTrue(Types.isDate(java.sql.Date.class));
    assertTrue(Types.isDate(Time.class));
    assertTrue(Types.isDate(Timestamp.class));
    assertTrue(Types.isDate(new Date()
    {
      private static final long serialVersionUID = -5044225237647579457L;
    }.getClass()));

    assertFalse(Types.isDate(null));
  }

  @Test
  public void testIsKindOf()
  {
    assertTrue(Types.isKindOf(Boolean.class, boolean.class));
    assertTrue(Types.isKindOf(boolean.class, Boolean.class));
    assertTrue(Types.isKindOf(SimpleDateFormat.class, DateFormat.class));
    assertTrue(Types.isKindOf(BufferedInputStream.class, InputStream.class));
    assertTrue(Types.isKindOf(BufferedInputStream.class, Closeable.class));
    assertTrue(Types.isKindOf(Person.class, IPerson.class));
  }

  @Test
  public void testIsInstanceOf()
  {
    assertTrue(Types.isInstanceOf(new Date()
    {
      private static final long serialVersionUID = -5044225237647579457L;
    }, Date.class));

    assertTrue(Types.isInstanceOf(new Boolean("true"), boolean.class));
    assertTrue(Types.isInstanceOf(true, Boolean.class));
    assertTrue(Types.isInstanceOf(new Boolean("true"), Boolean.class));

    assertTrue(Types.isInstanceOf(new Byte((byte)1), byte.class));
    assertTrue(Types.isInstanceOf((byte)1, Byte.class));
    assertTrue(Types.isInstanceOf(new Byte((byte)1), Byte.class));

    assertTrue(Types.isInstanceOf(new Character('a'), char.class));
    assertTrue(Types.isInstanceOf('a', Character.class));
    assertTrue(Types.isInstanceOf(new Character('a'), Character.class));

    assertTrue(Types.isInstanceOf(new Short((short)1), short.class));
    assertTrue(Types.isInstanceOf((short)1, Short.class));
    assertTrue(Types.isInstanceOf(new Short((short)1), Short.class));

    assertTrue(Types.isInstanceOf(new Integer(1), int.class));
    assertTrue(Types.isInstanceOf(1, Integer.class));
    assertTrue(Types.isInstanceOf(new Integer(1), Integer.class));

    assertTrue(Types.isInstanceOf(new Long(1L), long.class));
    assertTrue(Types.isInstanceOf(1L, Long.class));
    assertTrue(Types.isInstanceOf(new Long(1L), Long.class));

    assertTrue(Types.isInstanceOf(new Float(1.0F), float.class));
    assertTrue(Types.isInstanceOf(1.0F, Float.class));
    assertTrue(Types.isInstanceOf(new Float(1.0F), Float.class));

    assertTrue(Types.isInstanceOf(new Double(1.0), double.class));
    assertTrue(Types.isInstanceOf(1.0, Double.class));
    assertTrue(Types.isInstanceOf(new Double(1.0), Double.class));

    assertFalse(Types.isInstanceOf(null, boolean.class));
    assertFalse(Types.isInstanceOf(null, Boolean.class));
  }

  @Test
  public void testIsVoid()
  {
    assertTrue(Types.isVoid(Void.class));
    assertTrue(Types.isVoid(Void.TYPE));
    assertTrue(Types.isVoid(void.class));
    assertFalse(Types.isVoid(Object.class));
  }

  @Test
  public void testIsBoolean()
  {
    assertTrue(Types.isBoolean(true));
    assertTrue(Types.isBoolean(false));
    assertFalse(Types.isBoolean(""));
    assertFalse(Types.isBoolean(new Date()));
    assertFalse(Types.isBoolean(null));
    assertTrue(Types.isBoolean(boolean.class));
    assertTrue(Types.isBoolean(Boolean.class));
    assertFalse(Types.isBoolean(Object.class));
  }

  @Test
  public void testAsBoolean()
  {
    assertTrue(Types.asBoolean(true));
    assertFalse(Types.asBoolean(false));
    assertTrue(Types.asBoolean((byte)1));
    assertFalse(Types.asBoolean((byte)0));
    assertTrue(Types.asBoolean((short)1));
    assertFalse(Types.asBoolean((short)0));
    assertTrue(Types.asBoolean(1));
    assertTrue(Types.asBoolean(-1));
    assertFalse(Types.asBoolean(0));
    assertTrue(Types.asBoolean(1L));
    assertTrue(Types.asBoolean(-1L));
    assertFalse(Types.asBoolean(0L));
    assertTrue(Types.asBoolean(1.0F));
    assertTrue(Types.asBoolean(-1.0F));
    assertFalse(Types.asBoolean(0.0F));
    assertTrue(Types.asBoolean(1.0));
    assertTrue(Types.asBoolean(-1.0));
    assertFalse(Types.asBoolean(0.0));
    assertFalse(Types.asBoolean(null));

    assertTrue(Types.asBoolean(" "));
    assertFalse(Types.asBoolean(""));
    assertTrue(Types.asBoolean(new Character('a')));
    assertTrue(Types.asBoolean((char)0));

    assertTrue(Types.asBoolean(new int[]
    {
      1
    }));
    assertFalse(Types.asBoolean(new int[] {}));

    List<Integer> list = new ArrayList<Integer>();
    assertFalse(Types.asBoolean(list));
    list.add(1);
    assertTrue(Types.asBoolean(list));

    Map<String, Integer> map = new HashMap<String, Integer>();
    assertFalse(Types.asBoolean(map));
    map.put("", 1);
    assertTrue(Types.asBoolean(map));
  }

  @Test
  public void testIsNullPrimitive()
  {
    assertFalse(Types.isBoolean(null));
    assertFalse(Types.isCharacter(null));
    assertFalse(Types.isDate(null));
    assertFalse(Types.isEnum(null));
    assertFalse(Types.isNumber(null));
    assertFalse(Types.isPrimitive(null));
  }

  @Test
  public void testIsNumber()
  {
    assertTrue(Types.isNumber((byte)1));
    assertTrue(Types.isNumber((short)1));
    assertTrue(Types.isNumber(1));
    assertTrue(Types.isNumber(1L));
    assertTrue(Types.isNumber(1.0F));
    assertTrue(Types.isNumber(1.0));
    assertTrue(Types.isNumber(Number.class));
    assertTrue(Types.isNumber(Byte.class));
    assertTrue(Types.isNumber(Integer.class));
    assertTrue(Types.isNumber(Long.class));
    assertTrue(Types.isNumber(Float.class));
    assertTrue(Types.isNumber(Double.class));
    assertTrue(Types.isNumber(BigDecimal.class));

    assertFalse(Types.isNumber('c'));
    assertFalse(Types.isNumber(""));
    assertFalse(Types.isNumber(null));
    assertFalse(Types.isNumber(new Object()));
    assertFalse(Types.isNumber(Object.class));
  }

  @Test
  public void testIsCharacter()
  {
    assertTrue(Types.isCharacter('c'));
    assertTrue(Types.isCharacter(char.class));
    assertTrue(Types.isCharacter(Character.class));
    assertFalse(Types.isCharacter(Object.class));
    assertFalse(Types.isCharacter(""));
    assertFalse(Types.isCharacter(null));
    assertFalse(Types.isCharacter(0));
  }

  @Test
  public void testIsEnum()
  {
    assertTrue(Types.isEnum(Order.class));
    assertTrue(Types.isEnum(Order.ONE));
    assertFalse(Types.isEnum(Enum.class));
    assertFalse(Types.isEnum(null));
    assertFalse(Types.isEnum(Object.class));
    assertFalse(Types.isEnum(new Object()));
  }

  @Test
  public void testIsPrimitiveLike()
  {
    assertTrue(Types.isPrimitiveLike((byte)1));
    assertTrue(Types.isPrimitiveLike((short)1));
    assertTrue(Types.isPrimitiveLike(1));
    assertTrue(Types.isPrimitiveLike(1L));
    assertTrue(Types.isPrimitiveLike(1.0F));
    assertTrue(Types.isPrimitiveLike(1.0));
    assertTrue(Types.isPrimitiveLike(Number.class));
    assertTrue(Types.isPrimitiveLike(Byte.class));
    assertTrue(Types.isPrimitiveLike(Integer.class));
    assertTrue(Types.isPrimitiveLike(Long.class));
    assertTrue(Types.isPrimitiveLike(Float.class));
    assertTrue(Types.isPrimitiveLike(Double.class));
    assertTrue(Types.isPrimitiveLike(BigDecimal.class));

    assertTrue(Types.isPrimitiveLike(true));
    assertTrue(Types.isPrimitiveLike(false));
    assertTrue(Types.isPrimitiveLike(boolean.class));
    assertTrue(Types.isPrimitiveLike(Boolean.class));

    assertTrue(Types.isPrimitiveLike(Order.class));
    assertTrue(Types.isPrimitiveLike(Order.ONE));

    assertTrue(Types.isPrimitiveLike(Date.class));
    assertTrue(Types.isPrimitiveLike(java.sql.Date.class));
    assertTrue(Types.isPrimitiveLike(Time.class));
    assertTrue(Types.isPrimitiveLike(Timestamp.class));
    assertTrue(Types.isPrimitiveLike(new Date()
    {
      private static final long serialVersionUID = -5044225237647579457L;
    }.getClass()));

    assertTrue(Types.isPrimitiveLike('c'));
    assertTrue(Types.isPrimitiveLike(char.class));
    assertTrue(Types.isPrimitiveLike(Character.class));

    assertTrue(Types.isPrimitiveLike(""));
    assertTrue(Types.isPrimitiveLike("x"));
    assertTrue(Types.isPrimitiveLike(String.class));

    assertFalse(Types.isPrimitiveLike(Object.class));
    assertFalse(Types.isPrimitiveLike(Enum.class));
    assertFalse(Types.isPrimitiveLike(new int[0]));
    assertFalse(Types.isPrimitiveLike(null));
  }

  @Test
  public void testIsCollection()
  {
    assertTrue(Types.isCollection(List.class));
    assertTrue(Types.isCollection(Collections.emptyList()));
    assertTrue(Types.isCollection(ArrayList.class));
    assertFalse(Types.isCollection(Map.class));
    assertFalse(Types.isCollection(new int[0]));
    assertFalse(Types.isCollection(int[].class));
    assertFalse(Types.isCollection(null));
    assertFalse(Types.isCollection(""));
  }

  @Test
  public void testIsMap()
  {
    assertTrue(Types.isMap(Map.class));
    assertTrue(Types.isMap(HashMap.class));
    assertTrue(Types.isMap(Collections.emptyMap()));
    assertFalse(Types.isMap(List.class));
    assertFalse(Types.isMap(null));
    assertFalse(Types.isMap(""));
  }

  @Test
  public void testIsClass()
  {
    assertTrue(Types.isClass("comp.prj.Class"));
    assertTrue(Types.isClass("comp.prj.Class$InnerClass"));
    assertTrue(Types.isClass("comp.prj.Class_1"));
    assertTrue(Types.isClass("comp.prj.Class_1$InnerClass_1"));
    assertFalse(Types.isClass(""));
    assertFalse(Types.isClass(null));
    assertFalse(Types.isClass("1comp.prj.Class"));
    assertFalse(Types.isClass("comp.prj.class"));
    assertFalse(Types.isClass("comp.prj.Class$innerClass"));
  }

  @Test
  public void testIsConcrete()
  {
    assertFalse(Types.isConcrete(null));
    assertTrue(Types.isConcrete(File.class));
    assertFalse(Types.isConcrete(TimeZone.class));
  }

  @Test
  public void testAsIterable()
  {
    Iterator<?> it = Types.asIterable(new int[]
    {
        1, 2
    }).iterator();
    assertEquals(1, (int)it.next());
    assertEquals(2, (int)it.next());
    try {
      it.next();
      fail("Overbounds iteration should rise exception.");
    }
    catch(ArrayIndexOutOfBoundsException e) {}

    List<Integer> list = new ArrayList<Integer>();
    list.add(1);
    list.add(2);

    it = Types.asIterable(list).iterator();
    assertEquals(1, (int)it.next());
    assertEquals(2, (int)it.next());
    assertFalse(it.hasNext());

    it = Types.asIterable(true).iterator();
    assertFalse(it.hasNext());

    it = Types.asIterable(null).iterator();
    assertFalse(it.hasNext());
  }

  // ----------------------------------------------------------------------------------------------
  // FIXTURE

  private static interface IPerson
  {
  }

  private static class Person implements IPerson
  {
  }

  private static enum Order
  {
    ONE, TWO
  }
}
