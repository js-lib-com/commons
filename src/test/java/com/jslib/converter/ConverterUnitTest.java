package com.jslib.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.jslib.lang.OrdinalEnum;
import com.jslib.util.Strings;
import com.jslib.util.Types;

import junit.framework.TestCase;

public class ConverterUnitTest
{
  private Converter converter;

  @Before
  public void beforeTest() throws Exception
  {
    converter = ConverterRegistry.getConverter();
  }

  /** String conversion must return the very same source string. */
  @Test
  public void string()
  {
    String s1 = "test string";
    assertEquals(s1, converter.asObject(s1, String.class));
  }

  @Test
  public void booleans()
  {
    String[] trues = new String[]
    {
        "on", "yes", "1", "true", "On", "ON", "Yes", "YES", "True", "TRUE"
    };
    for(String s : trues) {
      Boolean b = converter.asObject(s, Boolean.class);
      assertTrue(b);
      assertEquals("true", converter.asString(b));
      b = converter.asObject(s, boolean.class);
      assertTrue(b);
      assertEquals("true", converter.asString(b));
    }

    Boolean b = converter.asObject("anything else is false", Boolean.class);
    assertFalse(b);
    assertEquals("false", converter.asString(b));
    assertNull(converter.asObject(null, Boolean.class));
  }

  @Test
  public void characters()
  {
    assertEquals('s', (char)converter.asObject("s", Character.class));
    assertEquals('s', (char)converter.asObject("s", char.class));

    assertNull(converter.asObject(null, Character.class));
    assertNull(converter.asObject(null, char.class));

    assertEquals("s", converter.asString('s'));
    try {
      assertEquals('s', (char)converter.asObject("string", char.class));
      fail();
    }
    catch(ConverterException e) {
      assertEquals("Trying to convert a larger string into a single character.", e.getMessage());
    }
  }

  @Test
  public void dates()
  {
    Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    for(Class<?> clazz : new Class<?>[]
    {
        Date.class, java.sql.Date.class, Time.class, Timestamp.class
    }) {
      for(String date : new String[]
      {
          "1964-03-15T14:20:00", "1964-03-15T14:20:00.000", "1964-03-15T14:20:00Z", "1964-03-15T14:20:00.000Z"
      }) {
        c.setTime((Date)converter.asObject(date, clazz));
        assertEquals(1964, c.get(Calendar.YEAR));
        assertEquals(2, c.get(Calendar.MONTH));
        assertEquals(15, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(14, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, c.get(Calendar.MINUTE));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));
        assertEquals(1, c.get(Calendar.ERA));
      }
    }

    assertNull(converter.asObject(null, Date.class));
    assertNull(converter.asObject(null, java.sql.Date.class));
    assertNull(converter.asObject(null, Time.class));
    assertNull(converter.asObject(null, Timestamp.class));

    long t = c.getTimeInMillis();
    String s = "1964-03-15T14:20:00Z";
    assertEquals(s, converter.asString(new Date(t)));
    assertEquals(s, converter.asString(new java.sql.Date(t)));
    assertEquals(s, converter.asString(new Time(t)));
    assertEquals(s, converter.asString(new Timestamp(t)));
  }

  @Test
  public void negativeDate()
  {
    // https://en.wikipedia.org/wiki/ISO_8601
    // by convention 1 BC is labeled +0000, 2 BC is labeled −0001, ...
    // so that Caesar death from 44 BC is encoded as -0043

    Date caesarDeath = converter.asObject("-0043-03-15T00:00:00", Date.class);
    assertNotNull(caesarDeath);

    Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    c.setTime(caesarDeath);
    assertEquals(44, c.get(Calendar.YEAR));
    assertEquals(2, c.get(Calendar.MONTH));
    assertEquals(15, c.get(Calendar.DAY_OF_MONTH));
    assertEquals(0, c.get(Calendar.HOUR_OF_DAY));
    assertEquals(0, c.get(Calendar.MINUTE));
    assertEquals(0, c.get(Calendar.SECOND));
    assertEquals(0, c.get(Calendar.MILLISECOND));
    assertEquals(0, c.get(Calendar.ERA));

    assertEquals("-0043-03-15T00:00:00Z", converter.asString(c.getTime()));

    // at this point calendar era is 0, that is, BC
    c.set(Calendar.YEAR, 1);
    assertEquals("0000-03-15T00:00:00Z", converter.asString(c.getTime()));
    c.set(Calendar.YEAR, 2);
    assertEquals("-0001-03-15T00:00:00Z", converter.asString(c.getTime()));
  }

  @Test
  public void invalidDateFormat()
  {
    boolean exception = false;
    try {
      converter.asObject("1964-X3-15T14:20:00", Date.class);
    }
    catch(ConverterException e) {
      exception = true;
    }
    assertTrue("Invalid date format should rise exception.", exception);
  }

  private volatile int failsCount;

  @Test
  public void concurrentDateParsing() throws InterruptedException
  {
    final int THREADS_COUNT = 1000;
    final String dateString = "1964-03-15T14:30:00Z";

    Thread[] threads = new Thread[THREADS_COUNT];
    failsCount = 0;
    for(int i = 0; i < THREADS_COUNT; ++i) {
      threads[i] = new Thread(new Runnable()
      {
        @Override
        public void run()
        {
          try {
            Date date = converter.asObject(dateString, Date.class);
            // not not try to reuse df between threads. SimpleDateFormat is not thread safe
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            TestCase.assertEquals("1964-03-15T14:30:00Z", df.format(date));
          }
          catch(Throwable t) {
            ++failsCount;
          }
        }
      });
      threads[i].start();
    }

    for(Thread thread : threads) {
      thread.join();
    }
    assertEquals(String.format("Concurent date parsing test not passed. There are %d failing threads.", failsCount), 0, failsCount);
  }

  @Test
  public void enums()
  {
    State state = converter.asObject("ACTIVE", State.class);
    assertTrue(Types.isEnum(state));
    assertEquals(State.ACTIVE, state);

    assertNull(converter.asObject(null, State.class));

    state = State.DETACHED;
    assertEquals("DETACHED", converter.asString(state));
  }

  @Test
  public void emptyEnumConstant()
  {
    assertNull(converter.asObject("", State.class));
  }

  @Test
  public void invalidEnumConstant()
  {
    boolean exception = false;
    try {
      converter.asObject("BAD_VALUE", State.class);
    }
    catch(ConverterException e) {
      exception = true;
    }
    assertTrue("Bad enum string should rise exception", exception);
  }

  @Test
  public void ordinalEnum()
  {
    OrdinalState state = converter.asObject("1", OrdinalState.class);
    assertTrue(Types.isEnum(state));
    assertEquals(OrdinalState.ACTIVE, state);

    assertNull(converter.asObject(null, OrdinalState.class));

    state = OrdinalState.DETACHED;
    assertEquals("2", converter.asString(state));
  }

  @Test
  public void file()
  {
    File file = converter.asObject("/var/tmp/file.tmp", File.class);
    assertEquals("file.tmp", file.getName());

    file = new File("readme");
    assertTrue(converter.asString(file).endsWith("readme"));
  }

  @Test
  public void numbers()
  {
    assertEquals((byte)0, (byte)converter.asObject("0", byte.class));
    assertEquals((byte)44, (byte)converter.asObject("44", byte.class));
    assertEquals((byte)44, (byte)converter.asObject("44", Byte.class));
    assertEquals("44", converter.asString(44));

    assertEquals((short)0, (short)converter.asObject("0", short.class));
    assertEquals((short)4444, (short)converter.asObject("4444", short.class));
    assertEquals((short)4444, (short)converter.asObject("4444", Short.class));
    assertEquals("4444", converter.asString(4444));

    assertEquals(0, (int)converter.asObject("0", int.class));
    assertEquals(444444, (int)converter.asObject("444444", int.class));
    assertEquals(444444, (int)converter.asObject("444444", Integer.class));
    assertEquals("444444", converter.asString(444444));

    assertEquals(0L, (long)converter.asObject("0", long.class));
    assertEquals(44444444L, (long)converter.asObject("44444444", long.class));
    assertEquals(44444444L, (long)converter.asObject("44444444", Long.class));
    assertEquals("44444444", converter.asString(44444444));

    assertEquals(44.44F, converter.asObject("44.44", float.class), 0.0);
    assertEquals(44.44F, converter.asObject("44.44", Float.class), 0.0);
    assertEquals(44.44F, converter.asObject("4.444E1", float.class), 0.0);
    assertEquals(44.44F, converter.asObject("4.444E1", Float.class), 0.0);
    assertEquals("44.44", converter.asString(44.44));

    assertEquals(44444444.44444444, converter.asObject("44444444.44444444", double.class), 0);
    assertEquals(44444444.44444444, converter.asObject("44444444.44444444", Double.class), 0);
    assertEquals(44444444.44444444, converter.asObject("4.444444444444444E7", double.class), 0);
    assertEquals(44444444.44444444, converter.asObject("4.444444444444444E7", Double.class), 0);
    assertEquals("4.444444444444444E7", converter.asString(44444444.44444444));

    assertNull(converter.asObject(null, byte.class));
    assertNull(converter.asObject(null, short.class));
    assertNull(converter.asObject(null, int.class));
    assertNull(converter.asObject(null, long.class));
    assertNull(converter.asObject(null, float.class));
    assertNull(converter.asObject(null, double.class));
  }

  @Test
  public void parseDefaultNumbers()
  {
    assertEquals((byte)0, (byte)converter.asObject("", byte.class));
    assertEquals((byte)0, (byte)converter.asObject("", Byte.class));
    assertEquals((short)0, (short)converter.asObject("", short.class));
    assertEquals((short)0, (short)converter.asObject("", Short.class));
    assertEquals(0, (int)converter.asObject("", int.class));
    assertEquals(0, (int)converter.asObject("", Integer.class));
    assertEquals(0L, (long)converter.asObject("", long.class));
    assertEquals(0L, (long)converter.asObject("", Long.class));
    assertEquals(0.0F, (float)converter.asObject("", float.class), 0.0);
    assertEquals(0.0F, converter.asObject("", Float.class), 0.0);
    assertEquals(0.0, converter.asObject("", double.class), 0.0);
    assertEquals(0.0, converter.asObject("", Double.class), 0.0);
  }

  /**
   * Number converter accept JSON format as number string representation and is first parsed internally into a double,
   * then rounded to given type. Anyway, this process can lead to loss of precision, please see first part of this test.
   * 
   * Excerpt from language spec, 5.1.2: Conversion of an int or a long value to float, or of a long value to double, may
   * result in loss of precision-that is, the result may lose some of the least significant bits of the value. In this
   * case, the resulting floating-point value will be a correctly rounded version of the integer value, using IEEE 754
   * round-to-nearest mode.
   */
  @Test
  public void conversionPrecision()
  {
    long l = 9223372036854775807L;
    double d = l;
    assertTrue(l == (long)d);

    l = 9223372036854775806L;
    d = l;
    assertTrue(l != (long)d);

    int i = 2147483647;
    float f = i;
    assertTrue(i == (int)f);

    i = 2147483646;
    f = i;
    assertTrue(i != (int)f);

    l = Long.parseLong("9223372036854775806");
    d = Double.parseDouble("9223372036854775806");
    assertTrue(l != (long)d);

    assertEquals(9223372036854775806L, (long)converter.asObject("9223372036854775806", long.class));
    assertEquals("9223372036854775806", converter.asString(converter.asObject("9223372036854775806", long.class)));

    // numbers, if decimal separator is present, are internally converted to double
    // then cast to long, but with loss of precision
    l = converter.asObject("9223372036854775806.0", long.class);
    assertFalse(9223372036854775806L == l);
    assertFalse("9223372036854775806".equals(converter.asString(converter.asObject("9223372036854775806.0", long.class))));
  }

  @Test
  public void timezone()
  {
    String s = "Europe/Bucharest";
    TimeZone timezone = converter.asObject(s, TimeZone.class);
    assertEquals("Eastern European Standard Time", timezone.getDisplayName());
    assertEquals("Europe/Bucharest", timezone.getID());

    assertNull(converter.asObject(null, TimeZone.class));

    timezone = TimeZone.getTimeZone(s);
    assertEquals(s, converter.asString(timezone));
    assertFalse(timezone == converter.asObject(converter.asString(timezone), TimeZone.class));
    assertEquals(timezone.getID(), ((TimeZone)converter.asObject(converter.asString(timezone), TimeZone.class)).getID());
  }

  @Test
  public void url() throws MalformedURLException
  {
    String s = "http://mailvee.com/login.xsp?token=q1w2e3r4t5y6u7i8o9p0";
    URL url = converter.asObject(s, URL.class);
    assertEquals(s, url.toExternalForm());

    assertNull(converter.asObject(null, URL.class));

    url = new URL(s);
    assertEquals(s, converter.asString(url));
    assertFalse(url == converter.asObject(converter.asString(url), URL.class));
  }

  @Test
  public void userDefinedConverter()
  {
    ConverterRegistry registry = ConverterRegistry.getInstance();
    Converter converter = registry.getConverterInstance();

    Person p1 = new Person("John", "Doe");
    assertFalse(registry.hasClassConverter(Person.class));

    // attempt to convert without user defined converter registered should rise exception
    boolean exception = false;
    try {
      converter.asString(p1);
    }
    catch(ConverterException e) {
      exception = true;
    }
    assertTrue(exception);

    registry.registerConverter(Person.class, PersonConverter.class);
    assertEquals("John Doe", converter.asString(p1));

    Person p2 = converter.asObject(converter.asString(p1), Person.class);
    assertNotNull(p2);
    assertEquals("John", p2.name);
    assertEquals("Doe", p2.surname);
  }

  // ----------------------------------------------------------------------------------------------
  // FIXTURE

  private static class Person
  {
    private String name;
    private String surname;

    @SuppressWarnings("unused")
    public Person()
    {
    }

    public Person(String name, String surname)
    {
      this.name = name;
      this.surname = surname;
    }
  }

  private static class PersonConverter implements Converter
  {
    @SuppressWarnings("unchecked")
    @Override
    public <T> T asObject(String string, Class<T> valueType) throws IllegalArgumentException, ConverterException
    {
      List<String> names = Strings.split(string);
      return (T)new Person(names.get(0), names.get(1));
    }

    @Override
    public String asString(Object object) throws ConverterException
    {
      Person p = (Person)object;
      return Strings.concat(p.name, ' ', p.surname);
    }
  }

  private static enum State
  {
    NONE, ACTIVE, DETACHED
  }

  private static enum OrdinalState implements OrdinalEnum
  {
    NONE, ACTIVE, DETACHED
  }
}
