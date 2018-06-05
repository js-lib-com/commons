package js.format.test;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import js.format.Currency;
import js.format.DateTimeFormat;
import js.format.FileSize;
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
import js.format.Number;
import js.format.Percent;
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
  public void formatNumber()
  {
    Format format = new Number(Locale.getDefault());
    assertEquals("12.34", format.format(12.34));

    format = new Number(new Locale("ro", "RO"));
    assertEquals("12,34", format.format(12.34));
  }

  @Test
  public void formatPercent()
  {
    Format format = new Percent(Locale.getDefault());
    assertEquals("12.34%", format.format(0.1234));

    format = new Percent(new Locale("ro", "RO"));
    assertEquals("12,34%", format.format(0.1234));
  }

  @Test
  public void formatCurrency()
  {
    Format format = new Currency(Locale.getDefault());
    assertEquals("$12.34", format.format(12.34));

    format = new Currency(new Locale("ro", "RO"));
    assertEquals("12,34 LEI", format.format(12.34));
  }

  @Test
  public void formatFileSize()
  {
    Format format = new FileSize(Locale.getDefault());
    assertEquals("1.21 KB", format.format(1234));

    format = new FileSize(new Locale("ro", "RO"));
    assertEquals("1,21 KB", format.format(1234));
  }

  @Test
  public void formatFullDate()
  {
    DateTimeFormat format = new FullDate();
    assertEquals("Sunday, March 15, 1964", format.format(getDate()));
    
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("15 martie 1964", format.format(getDate()));
  }

  @Test
  public void formatFullDateTime()
  {
    DateTimeFormat format = new FullDateTime();
    assertEquals("Sunday, March 15, 1964 2:40:00 PM UTC", format.format(getDate()));
    
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("15 martie 1964 14:40:00 UTC", format.format(getDate()));
  }

  @Test
  public void formatFullTime()
  {
    DateTimeFormat format = new FullTime();
    assertEquals("2:40:00 PM UTC", format.format(getDate()));
    
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("14:40:00 UTC", format.format(getDate()));
  }

  @Test
  public void formatLongDate()
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
  public void formatLongDateTime()
  {
    DateTimeFormat format = new LongDateTime();
    assertEquals("March 15, 1964 2:40:00 PM UTC", format.format(getDate()));
    
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("15 martie 1964 14:40:00 UTC", format.format(getDate()));
  }

  @Test
  public void formatLongTime()
  {
    DateTimeFormat format = new LongTime();
    assertEquals("2:40:00 PM UTC", format.format(getDate()));
    
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("14:40:00 UTC", format.format(getDate()));
  }

  @Test
  public void formatMediumDate()
  {
    DateTimeFormat format = new MediumDate();
    assertEquals("Mar 15, 1964", format.format(getDate()));
    
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("15.03.1964", format.format(getDate()));
  }

  @Test
  public void formatMediumDateTime()
  {
    DateTimeFormat format = new MediumDateTime();
    assertEquals("Mar 15, 1964 2:40:00 PM", format.format(getDate()));
    
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("15.03.1964 14:40:00", format.format(getDate()));
  }

  @Test
  public void formatMediumTime()
  {
    DateTimeFormat format = new MediumTime();
    assertEquals("2:40:00 PM", format.format(getDate()));
    
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("14:40:00", format.format(getDate()));
  }

  @Test
  public void formatShortDate()
  {
    DateTimeFormat format = new ShortDate();
    assertEquals("3/15/64", format.format(getDate()));
    
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("15.03.1964", format.format(getDate()));
  }

  @Test
  public void formatShortDateTime()
  {
    DateTimeFormat format = new ShortDateTime();
    assertEquals("3/15/64 2:40 PM", format.format(getDate()));
    
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("15.03.1964 14:40", format.format(getDate()));
  }

  @Test
  public void formatShortTime()
  {
    DateTimeFormat format = new ShortTime();
    assertEquals("2:40 PM", format.format(getDate()));
    
    format.setLocale(new Locale("ro", "RO"));
    assertEquals("14:40", format.format(getDate()));
  }

  // ----------------------------------------------------------------------------------------------
  // UTILITY METHODS

  private static Date getDate()
  {
    Calendar calendar = Calendar.getInstance();
    calendar.set(1964, 2, 15, 14, 40, 0);
    return calendar.getTime();
  }
}
