package js.format.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import js.format.DateTimeFormat;
import js.format.Format;
import js.format.FullDate;
import js.format.FullDateTime;
import js.format.FullTime;
import js.format.LongDate;
import js.format.LongDateTime;
import js.format.LongTime;
import js.format.MediumDate;
import js.format.MediumDateTime;
import js.format.MediumTime;
import js.format.ShortDate;
import js.format.ShortDateTime;
import js.format.ShortTime;

import org.junit.Before;
import org.junit.Test;

public class FormatUnitTest
{
  @Before
  public void beforeTest() throws Exception
  {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Test
  public void testNumber()
  {
    Format format = newInstance("js.format.Number", Locale.getDefault());
    assertEquals("12.34", format.format(12.34));
    format = newInstance("js.format.Number", new Locale("ro", "RO"));
    assertEquals("12,34", format.format(12.34));
  }

  @Test
  public void testPercent()
  {
    Format format = newInstance("js.format.Percent", Locale.getDefault());
    assertEquals("12.34%", format.format(0.1234));
    format = newInstance("js.format.Percent", new Locale("ro", "RO"));
    assertEquals("12,34%", format.format(0.1234));
  }

  @Test
  public void testCurrency()
  {
    Format format = newInstance("js.format.Currency", Locale.getDefault());
    assertEquals("$12.34", format.format(12.34));
    format = newInstance("js.format.Currency", new Locale("ro", "RO"));
    assertEquals("12,34 LEI", format.format(12.34));
  }

  @Test
  public void testFileSize()
  {
    Format format = newInstance("js.format.FileSize", Locale.getDefault());
    assertEquals("1.21 KB", format.format(1234));
    format = newInstance("js.format.FileSize", new Locale("ro", "RO"));
    assertEquals("1,21 KB", format.format(1234));
  }

  @Test
  public void testFullDate()
  {
    DateTimeFormat format = new FullDate();
    assertEquals("Sunday, March 15, 1964", format.format(getDate()));
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("15 martie 1964", format.format(getDate()));
  }

  @Test
  public void testFullDateTime()
  {
    DateTimeFormat format = new FullDateTime();
    assertEquals("Sunday, March 15, 1964 2:40:00 PM UTC", format.format(getDate()));
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("15 martie 1964 14:40:00 UTC", format.format(getDate()));
  }

  @Test
  public void testFullTime()
  {
    DateTimeFormat format = new FullTime();
    assertEquals("2:40:00 PM UTC", format.format(getDate()));
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("14:40:00 UTC", format.format(getDate()));
  }

  @Test
  public void testLongDate()
  {
    DateTimeFormat format = new LongDate();
    assertEquals("March 15, 1964", format.format(getDate()));
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("15 martie 1964", format.format(getDate()));

    format = new LongDate();
    assertEquals("March 15, 1964", format.format(getDate()));
    format.setLocale(new Locale("en", "UK"));

    assertEquals("March 15, 1964", format.format(getDate()));
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("15 martie 1964", format.format(getDate()));
  }

  @Test
  public void testLongDateTime()
  {
    DateTimeFormat format = new LongDateTime();
    assertEquals("March 15, 1964 2:40:00 PM UTC", format.format(getDate()));
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("15 martie 1964 14:40:00 UTC", format.format(getDate()));
  }

  @Test
  public void testLongTime()
  {
    DateTimeFormat format = new LongTime();
    assertEquals("2:40:00 PM UTC", format.format(getDate()));
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("14:40:00 UTC", format.format(getDate()));
  }

  @Test
  public void testMediumDate()
  {
    DateTimeFormat format = new MediumDate();
    assertEquals("Mar 15, 1964", format.format(getDate()));
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("15.03.1964", format.format(getDate()));
  }

  @Test
  public void testMediumDateTime()
  {
    DateTimeFormat format = new MediumDateTime();
    assertEquals("Mar 15, 1964 2:40:00 PM", format.format(getDate()));
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("15.03.1964 14:40:00", format.format(getDate()));
  }

  @Test
  public void testMediumTime()
  {
    DateTimeFormat format = new MediumTime();
    assertEquals("2:40:00 PM", format.format(getDate()));
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("14:40:00", format.format(getDate()));
  }

  @Test
  public void testShortDate()
  {
    DateTimeFormat format = new ShortDate();
    assertEquals("3/15/64", format.format(getDate()));
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("15.03.1964", format.format(getDate()));
  }

  @Test
  public void testShortDateTime()
  {
    DateTimeFormat format = new ShortDateTime();
    assertEquals("3/15/64 2:40 PM", format.format(getDate()));
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("15.03.1964 14:40", format.format(getDate()));
  }

  @Test
  public void testShortTime()
  {
    DateTimeFormat format = new ShortTime();
    assertEquals("2:40 PM", format.format(getDate()));
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("14:40", format.format(getDate()));
  }

  // ----------------------------------------------------------------------------------------------
  // UTILITY METHODS

  @SuppressWarnings("unchecked")
  private static Format newInstance(String formatName, Locale locale)
  {
    try {
      Class<? extends Format> formatClass = (Class<? extends Format>)Class.forName(formatName);
      Constructor<? extends Format> constructor = formatClass.getConstructor(Locale.class);
      constructor.setAccessible(true);
      return constructor.newInstance(locale);
    }
    catch(Exception e) {
      fail(e.getMessage());
    }
    return null;
  }

  private static Date getDate()
  {
    Calendar calendar = Calendar.getInstance();
    calendar.set(1964, 2, 15, 14, 40, 0);
    return calendar.getTime();
  }
}
