package js.util.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import js.io.WildcardFilter;
import js.lang.InvocationException;
import js.util.Classes;
import js.util.I18nFile;
import js.util.I18nPool;
import js.util.I18nRepository;
import js.util.Strings;

import org.junit.Test;

@SuppressWarnings(
{
    "rawtypes", "unchecked"
})
public class I18nUtilsUnitTest
{
  @Test
  public void testLocaleLanguageApi()
  {
    Locale he = new Locale("he");
    Locale iw = new Locale("iw", "IL");
    assertEquals(he.getLanguage(), iw.getLanguage());

    // language tag always uses new code
    iw = new Locale("iw");
    assertEquals("he", he.toLanguageTag());
    assertEquals("he", iw.toLanguageTag());

    // language argument is converter to lower case
    he = new Locale("HE");
    iw = new Locale("IW");
    assertEquals("iw", he.getLanguage());
    assertEquals("iw", iw.getLanguage());
    assertEquals("he", he.toLanguageTag());
    assertEquals("he", iw.toLanguageTag());

    // country argument is converter to upper case
    // use hyphen for language tag
    he = new Locale("he", "il");
    iw = new Locale("iw", "il");
    assertEquals("iw", he.getLanguage());
    assertEquals("iw", iw.getLanguage());
    assertEquals("he-IL", he.toLanguageTag());
    assertEquals("he-IL", iw.toLanguageTag());

    // variant is present into language tag
    he = new Locale("he", "IL", "POSIX");
    assertEquals("he-IL-POSIX", he.toLanguageTag());

    // script and extension is present into language tag
    Locale.Builder builder = new Locale.Builder();
    builder.setLanguage("en");
    builder.setScript("Latn");
    builder.setExtension('c', "ext");
    assertEquals("en-Latn-c-ext", builder.build().toLanguageTag());

    // xx is not valid code
    // this test case demonstrates there is no exception on bad language code
    Locale xx = new Locale("xx");
    assertNotNull(xx.getLanguage());
    assertEquals("xx", xx.getLanguage());
    assertEquals("xx", xx.toLanguageTag());

    // not initialized fields are empty
    Locale en = new Locale("en");
    assertTrue(en.getCountry().isEmpty());
    assertTrue(en.getVariant().isEmpty());
    assertTrue(en.getScript().isEmpty());
  }

  // ----------------------------------------------------
  // I18nFile

  @Test
  public void testI18nFile_Constructor()
  {
    assertNotNull(newI18nFile(new File("fixture/i18n")).getFile());
    assertNotNull(newI18nFile(new File("fixture/i18n").getAbsoluteFile()).getFile());
    assertNotNull(newI18nFile(new File("fixture/i18n"), new Locale("en")).getFile());
    assertNotNull(newI18nFile(new File("fixture/i18n"), new Locale("en")).getLocale());
  }

  @Test
  public void testI18nFile_Arguments()
  {
    try {
      newI18nFile(new File("fixture/i18n"), new Locale("en", "US", "POXIS"));
      fail("Locale variant should rise exception.");
    }
    catch(InvocationException e) {
      assertEquals("Locale variant is null or not empty.", e.getCause().getMessage());
    }

    Locale.Builder builder = new Locale.Builder();
    builder.setLanguage("en");
    builder.setScript("Latn");
    try {
      newI18nFile(new File("fixture/i18n"), builder.build());
      fail("Locale script should rise exception.");
    }
    catch(InvocationException e) {
      assertEquals("Locale script is null or not empty.", e.getCause().getMessage());
    }

    builder = new Locale.Builder();
    builder.setLanguage("en");
    builder.setExtension('c', "ext");
    try {
      newI18nFile(new File("fixture/i18n"), builder.build());
      fail("Locale extension should rise exception.");
    }
    catch(InvocationException e) {
      assertEquals("Locale extension is null or not empty.", e.getCause().getMessage());
    }
  }

  private static I18nFile newI18nFile(File file)
  {
    return Classes.newInstance(I18nFile.class, file);
  }

  private static I18nFile newI18nFile(File file, Locale locale)
  {
    return Classes.newInstance(I18nFile.class, file, locale);
  }

  // ----------------------------------------------------
  // I18nRepository

  @Test
  public void testI18nRepository_ConfigBuilder()
  {
    I18nRepository.ConfigBuilder builder = new I18nRepository.ConfigBuilder("fixture/i18n/app-language/${locale}/fo", "*.fo");
    assertTrue((boolean)Classes.getFieldValue(builder, "multiLocale"));
    assertEquals(new File("fixture/i18n/app-language/").getPath(), new File((String)Classes.getFieldValue(builder, "baseDir")).getPath());
    assertEquals("fo", Classes.getFieldValue(builder, "subpath"));
    assertEquals("*.fo", Classes.getFieldValue(builder, "filesPattern"));
  }

  @Test
  public void testI18nRepository_SingleLanguage()
  {
    Class repositoryClass = Classes.forName("js.util.I18nRepository$SingleLocale");

    File repositoryPath = new File("fixture/i18n/app/");
    FileFilter fileFilter = new WildcardFilter("*.htm");
    Iterable<I18nFile> repository = createRepository(repositoryClass, repositoryPath, fileFilter);
    assertNotNull(repository);

    List<String> files = new ArrayList<String>();
    for(I18nFile file : repository) {
      files.add(file.getName());
      assertNull(file.getLocale());
    }

    assertEquals(3, files.size());
    assertTrue(files.contains("about.htm"));
    assertTrue(files.contains("contact.htm"));
    assertTrue(files.contains("index.htm"));

    repositoryPath = new File("fixture/i18n/app/fo").getAbsoluteFile();
    fileFilter = new WildcardFilter("*.fo");
    repository = createRepository(repositoryClass, repositoryPath, fileFilter);
    assertNotNull(repository);

    files = new ArrayList<String>();
    for(I18nFile file : repository) {
      files.add(file.getName());
      assertNull(file.getLocale());
    }

    assertEquals(2, files.size());
    assertTrue(files.contains("contract.fo"));
    assertTrue(files.contains("cv.fo"));
  }

  @Test
  public void testI18nRepository_MultipleLanguage()
  {
    Class repositoryClass = Classes.forName("js.util.I18nRepository$MultipleLocale");

    File baseDir = new File("fixture/i18n/app-language/");
    String subpath = null;
    FileFilter fileFilter = new WildcardFilter("*.htm");
    Iterable<I18nFile> repository = createRepository(repositoryClass, baseDir, subpath, fileFilter);
    assertNotNull(repository);

    Set<Locale> locales = new HashSet<Locale>();
    List<String> files = new ArrayList<String>();
    for(I18nFile file : repository) {
      locales.add(file.getLocale());
      files.add(file.getLanguageTag() + "/" + file.getName());
    }

    assertEquals(2, locales.size());
    assertTrue(locales.contains(new Locale("en")));
    assertTrue(locales.contains(new Locale("ro")));

    assertEquals(6, files.size());
    assertTrue(files.contains("en/about.htm"));
    assertTrue(files.contains("en/contact.htm"));
    assertTrue(files.contains("en/index.htm"));
    assertTrue(files.contains("ro/about.htm"));
    assertTrue(files.contains("ro/contact.htm"));
    assertTrue(files.contains("ro/index.htm"));

    baseDir = new File("fixture/i18n/app-language/");
    fileFilter = new WildcardFilter("*.fo");
    subpath = "fo";
    repository = createRepository(repositoryClass, baseDir, subpath, fileFilter);
    assertNotNull(repository);

    locales.clear();
    files.clear();
    for(I18nFile file : repository) {
      locales.add(file.getLocale());
      files.add(file.getLanguageTag() + "/fo/" + file.getName());
    }

    assertEquals(2, locales.size());
    assertTrue(locales.contains(new Locale("en")));
    assertTrue(locales.contains(new Locale("ro")));

    assertEquals(4, files.size());
    assertTrue(files.contains("en/fo/contract.fo"));
    assertTrue(files.contains("en/fo/cv.fo"));
    assertTrue(files.contains("ro/fo/contract.fo"));
    assertTrue(files.contains("ro/fo/cv.fo"));
  }

  @Test
  public void testI18nRepository_MultipleLocale()
  {
    Class repositoryClass = Classes.forName("js.util.I18nRepository$MultipleLocale");

    File baseDir = new File("fixture/i18n/app-locale/");
    String subpath = null;
    FileFilter fileFilter = new WildcardFilter("*.htm");
    Iterable<I18nFile> repository = createRepository(repositoryClass, baseDir, subpath, fileFilter);
    assertNotNull(repository);

    Set<Locale> locales = new HashSet<Locale>();
    List<String> files = new ArrayList<String>();
    for(I18nFile file : repository) {
      locales.add(file.getLocale());
      files.add(file.getLanguageTag() + "/" + file.getName());
    }

    assertEquals(2, locales.size());
    assertTrue(locales.contains(new Locale("en", "US")));
    assertTrue(locales.contains(new Locale("en", "GB")));

    assertEquals(6, files.size());
    assertTrue(files.contains("en-US/about.htm"));
    assertTrue(files.contains("en-US/contact.htm"));
    assertTrue(files.contains("en-US/index.htm"));
    assertTrue(files.contains("en-GB/about.htm"));
    assertTrue(files.contains("en-GB/contact.htm"));
    assertTrue(files.contains("en-GB/index.htm"));

    baseDir = new File("fixture/i18n/app-locale/");
    fileFilter = new WildcardFilter("*.fo");
    subpath = "fo";
    repository = createRepository(repositoryClass, baseDir, subpath, fileFilter);
    assertNotNull(repository);

    locales.clear();
    files.clear();
    for(I18nFile file : repository) {
      locales.add(file.getLocale());
      files.add(file.getLanguageTag() + "/fo/" + file.getName());
    }

    assertEquals(2, locales.size());
    assertTrue(locales.contains(new Locale("en", "US")));
    assertTrue(locales.contains(new Locale("en", "GB")));

    assertEquals(4, files.size());
    assertTrue(files.contains("en-US/fo/contract.fo"));
    assertTrue(files.contains("en-US/fo/cv.fo"));
    assertTrue(files.contains("en-GB/fo/contract.fo"));
    assertTrue(files.contains("en-GB/fo/cv.fo"));
  }

  @Test
  public void testI18nRepository_MixedLocale()
  {
    Class repositoryClass = Classes.forName("js.util.I18nRepository$MultipleLocale");

    File baseDir = new File("fixture/i18n/app-mixed/");
    String subpath = null;
    FileFilter fileFilter = new WildcardFilter("*.htm");
    Iterable<I18nFile> repository = createRepository(repositoryClass, baseDir, subpath, fileFilter);
    assertNotNull(repository);

    Set<Locale> locales = new HashSet<Locale>();
    List<String> files = new ArrayList<String>();
    for(I18nFile file : repository) {
      locales.add(file.getLocale());
      files.add(file.getLanguageTag() + "/" + file.getName());
    }

    assertEquals(2, locales.size());
    assertTrue(locales.contains(new Locale("en", "US")));
    assertTrue(locales.contains(new Locale("ro")));

    assertEquals(6, files.size());
    assertTrue(files.contains("en-US/about.htm"));
    assertTrue(files.contains("en-US/contact.htm"));
    assertTrue(files.contains("en-US/index.htm"));
    assertTrue(files.contains("ro/about.htm"));
    assertTrue(files.contains("ro/contact.htm"));
    assertTrue(files.contains("ro/index.htm"));

    baseDir = new File("fixture/i18n/app-mixed/");
    fileFilter = new WildcardFilter("*.fo");
    subpath = "fo";
    repository = createRepository(repositoryClass, baseDir, subpath, fileFilter);
    assertNotNull(repository);

    locales.clear();
    files.clear();
    for(I18nFile file : repository) {
      locales.add(file.getLocale());
      files.add(file.getLanguageTag() + "/fo/" + file.getName());
    }

    assertEquals(2, locales.size());
    assertTrue(locales.contains(new Locale("en", "US")));
    assertTrue(locales.contains(new Locale("ro")));

    assertEquals(4, files.size());
    assertTrue(files.contains("en-US/fo/contract.fo"));
    assertTrue(files.contains("en-US/fo/cv.fo"));
    assertTrue(files.contains("ro/fo/contract.fo"));
    assertTrue(files.contains("ro/fo/cv.fo"));
  }

  // ----------------------------------------------------
  // I18nPool

  @Test
  public void testI18nPool_SingleLanguage() throws Exception
  {
    Class<?> factoryClass = Classes.forName("js.util.I18nPoolFactory");
    I18nPool<File> pool = Classes.invoke(factoryClass, "getInstance", false);
    assertNotNull(pool);

    pool.put("about", new File("fixture/tomcat/webapps/app-language/en/about.htm"), new Locale("en"));
    pool.put("contact", new File("fixture/tomcat/webapps/app-language/en/contact.htm"), (Locale)null);
    pool.put("index", new File("fixture/tomcat/webapps/app-language/en/index.htm"));

    assertTrue(pool.has("about", new Locale("en", "US")));
    assertTrue(pool.has("about", new Locale("en")));
    assertTrue(pool.has("about", (Locale)null));
    assertTrue(pool.has("about"));
    assertTrue(pool.has("contact"));
    assertTrue(pool.has("index"));

    for(Locale locale : new Locale[]
    {
        new Locale("en", "US"), new Locale("en"), null
    }) {
      assertEquals("about.htm", pool.get("about", locale).getName());
    }
    assertEquals("about.htm", pool.get("about").getName());
  }

  @Test
  public void testI18nPool_MultipleLanguage() throws Exception
  {
    Class<?> factoryClass = Classes.forName("js.util.I18nPoolFactory");
    I18nPool<File> pool = Classes.invoke(factoryClass, "getInstance", true);
    assertNotNull(pool);

    pool.put("about", new File("fixture/i18n/app-language/en/about.htm"), new Locale("en"));
    pool.put("about", new File("fixture/i18n/app-language/ro/about.htm"), new Locale("ro"));

    assertTrue(pool.has("about", new Locale("en")));
    File file = pool.get("about", new Locale("en"));
    assertNotNull(file);
    String content = Strings.load(file);
    assertTrue(content.contains("Hello"));

    assertTrue(pool.has("about", new Locale("ro")));
    file = pool.get("about", new Locale("ro"));
    assertNotNull(file);
    content = Strings.load(file);
    assertTrue(content.contains("Bună"));
  }

  // ----------------------------------------------------------------------------------------------
  // UTILITY METHODS

  private static Iterable<I18nFile> createRepository(Class repositoryClass, File baseDir, FileFilter fileFilter)
  {
    return (Iterable<I18nFile>)Classes.newInstance(repositoryClass, baseDir, fileFilter);
  }

  private static Iterable<I18nFile> createRepository(Class repositoryClass, File baseDir, String subpath, FileFilter fileFilter)
  {
    return (Iterable<I18nFile>)Classes.newInstance(repositoryClass, baseDir, subpath, fileFilter);
  }
}
