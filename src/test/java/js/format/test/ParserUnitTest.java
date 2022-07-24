package js.format.test;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
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

public class ParserUnitTest
{
  @Before
  public void beforeTest() throws Exception
  {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Test
  public void parseNumber() throws ParseException
  {
    Format format = new Number(Locale.getDefault());
    assertEquals(12.34, format.parse("12.34"));

    format = new Number(new Locale("ro", "RO"));
    assertEquals(12.34, format.parse("12,34"));
  }

  @Test
  public void parsePercent() throws ParseException
  {
    Format format = new Percent(Locale.getDefault());
    assertEquals(0.1234, format.parse("12.34%"));

    format = new Percent(new Locale("ro", "RO"));
    assertEquals(0.1234, format.parse("12,34 %"));
  }

  @Test
  public void parseCurrency() throws ParseException
  {
    Format format = new Currency(Locale.getDefault());
    assertEquals(12.34, format.parse("$12.34"));

    format = new Currency(new Locale("ro", "RO"));
    assertEquals(12.34, format.parse("12,34 RON"));
  }

  @Test
  public void parseFileSize() throws ParseException
  {
    Format format = new FileSize(Locale.getDefault());
    assertEquals(1239L, format.parse("1.21 KB"));

    format = new FileSize(new Locale("ro", "RO"));
    assertEquals(1239L, format.parse("1,21 KB"));
  }

  @Test
  public void parseFileSize_LowerCase() throws ParseException
  {
    Format format = new FileSize(Locale.getDefault());
    assertEquals(1239L, format.parse("1.21 kb"));

    format = new FileSize(new Locale("ro", "RO"));
    assertEquals(1239L, format.parse("1,21 kb"));
  }

  @Test
  public void parseFileSize_MultipleSpaces() throws ParseException
  {
    Format format = new FileSize(Locale.getDefault());
    assertEquals(1239L, format.parse("1.21   KB"));

    format = new FileSize(new Locale("ro", "RO"));
    assertEquals(1239L, format.parse("1,21   KB"));
  }

  @Test
  public void parseFileSize_InvalidValue() throws ParseException
  {
    Format format = new FileSize(Locale.getDefault());
    assertEquals(1024L, format.parse("1!21 KB"));
  }

  @Test(expected = ParseException.class)
  public void parseFileSize_MissingUnits() throws ParseException
  {
    Format format = new FileSize(Locale.getDefault());
    format.parse("1.21");
  }

  @Test(expected = ParseException.class)
  public void parseFileSize_BadUnits() throws ParseException
  {
    Format format = new FileSize(Locale.getDefault());
    format.parse("1.21 VB");
  }

  @Test
  public void parseFullDate() throws ParseException
  {
    DateTimeFormat format = new FullDate();
    assertEquals(getDate(), format.parse("Sunday, March 15, 1964"));

    format.setLocale(new Locale("ro", "RO"));
    assertEquals(getDate(), format.parse("duminică, 15 martie 1964"));
  }

  @Test
  public void parseFullDateTime() throws ParseException
  {
    DateTimeFormat format = new FullDateTime();
    assertEquals(getDateTime(), format.parse("Sunday, March 15, 1964 at 2:40:00 PM Coordinated Universal Time"));

    format.setLocale(new Locale("ro", "RO"));
    assertEquals(getDateTime(), format.parse("duminică, 15 martie 1964, 14:40:00 Timpul universal coordonat"));
  }

  @Test
  public void parseFullTime() throws ParseException
  {
    DateTimeFormat format = new FullTime();
    assertEquals(getTime(), format.parse("2:40:00 PM UTC"));

    format.setLocale(new Locale("ro", "RO"));
    assertEquals(getTime(), format.parse("14:40:00 UTC"));
  }

  @Test
  public void parseLongDate() throws ParseException
  {
    DateTimeFormat format = new LongDate();
    assertEquals(getDate(), format.parse("March 15, 1964"));
    format.setLocale(new Locale("ro", "RO"));
    assertEquals(getDate(), format.parse("15 martie 1964"));

    format = new LongDate();
    assertEquals(getDate(), format.parse("March 15, 1964"));
    format.setLocale(new Locale("en", "UK"));

    assertEquals(getDate(), format.parse("March 15, 1964"));
    format.setLocale(new Locale("ro", "RO"));
    assertEquals(getDate(), format.parse("15 martie 1964"));
  }

  @Test
  public void parseLongDateTime() throws ParseException
  {
    DateTimeFormat format = new LongDateTime();
    assertEquals(getDateTime(), format.parse("March 15, 1964 at 2:40:00 PM UTC"));

    format.setLocale(new Locale("ro", "RO"));
    assertEquals(getDateTime(), format.parse("15 martie 1964, 14:40:00 UTC"));
  }

  @Test
  public void parseLongTime() throws ParseException
  {
    DateTimeFormat format = new LongTime();
    assertEquals(getTime(), format.parse("2:40:00 PM UTC"));

    format.setLocale(new Locale("ro", "RO"));
    assertEquals(getTime(), format.parse("14:40:00 UTC"));
  }

  @Test
  public void parseMediumDate() throws ParseException
  {
    DateTimeFormat format = new MediumDate();
    assertEquals(getDate(), format.parse("Mar 15, 1964"));

    format.setLocale(new Locale("ro", "RO"));
    assertEquals(getDate(), format.parse("15 mar. 1964"));
  }

  @Test
  public void parseMediumDateTime() throws ParseException
  {
    DateTimeFormat format = new MediumDateTime();
    assertEquals(getDateTime(), format.parse("Mar 15, 1964, 2:40:00 PM"));
    
    format.setLocale(new Locale("ro", "RO"));
    assertEquals(getDateTime(), format.parse("15 mar. 1964, 14:40:00"));
  }

  @Test
  public void parseMediumTime() throws ParseException
  {
    DateTimeFormat format = new MediumTime();
    assertEquals(getTime(), format.parse("2:40:00 PM"));
    
    format.setLocale(new Locale("ro", "RO"));
    assertEquals(getTime(), format.parse("14:40:00"));
  }

  @Test
  public void parseShortDate() throws ParseException
  {
    DateTimeFormat format = new ShortDate();
    assertEquals(getDate(), format.parse("3/15/64"));
    
    format.setLocale(new Locale("ro", "RO"));
    assertEquals(getDate(), format.parse("15.03.1964"));
  }

  @Test
  public void parseShortDateTime() throws ParseException
  {
    DateTimeFormat format = new ShortDateTime();
    assertEquals(getDateTime(), format.parse("3/15/64, 2:40 PM"));
    
    format.setLocale(new Locale("ro", "RO"));
    assertEquals(getDateTime(), format.parse("15.03.1964, 14:40"));
  }

  @Test
  public void parseShortTime() throws ParseException
  {
    DateTimeFormat format = new ShortTime();
    assertEquals(getTime(), format.parse("2:40 PM"));
    
    format.setLocale(new Locale("ro", "RO"));
    assertEquals(getTime(), format.parse("14:40"));
  }

  // ----------------------------------------------------------------------------------------------
  // UTILITY METHODS

  private static Date getDateTime()
  {
    Calendar calendar = Calendar.getInstance();
    calendar.set(1964, 2, 15, 14, 40, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }

  private static Date getDate()
  {
    Calendar calendar = Calendar.getInstance();
    calendar.set(1964, 2, 15, 0, 0, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }

  private static Date getTime()
  {
    Calendar calendar = Calendar.getInstance();
    calendar.set(1970, 0, 1, 14, 40, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }
}
