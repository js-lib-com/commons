package js.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import js.io.WildcardFilter;
import js.lang.BugError;
import js.lang.Config;
import js.lang.ConfigException;

/**
 * Repository for files organized by locale. I18N repository has multiple sets of files with identical names, grouped by
 * locale, see ASCII diagram below. Support for multiple locale can miss in which case this repository degenerates to a
 * standard filesystem directory.
 * <p>
 * Repository has a base directory that contains a number of locale directories. By convention uses locale language tag
 * - see {@link Locale#toLanguageTag()}, to name these locale directories. Is not allowed to mix locale directory with
 * other sub-directories, that is, base directory should contain only valid locale directories. Files set are stored
 * into locale directory with optional sub-path.
 * <p>
 * Language tag should contain only lower case language and optional upper case country, separated by hyphen. Language
 * is encoded ISO 639 alpha-2 and for country uses ISO 3166 alpha-2. There is no support for locale variant, script or
 * extension.
 * 
 * <pre>
 * BASE / LOCALEi / SUB-PATH / FILEi
 *                           / FILEj
 *                           ~
 *      / LOCALEj / SUB-PATH / FILEi
 *                           / FILEj
 * </pre>
 * 
 * <p>
 * In order to create an I18N repository one needs a configuration object, see
 * {@link I18nRepository#I18nRepository(Config)}; in next section are described needed properties. To simplify
 * integration there is a dedicated configuration builder. Once instantiated, repository can create I18N files pool then
 * scan for files. If files pattern is provided scan only matching files. Scanning process takes care to initialize I18N
 * file locale from locale directory name.
 * <p>
 * If repository is created without multiple locale support, created I18N pool instance is single locale and I18N file
 * locale is not initialized and ignored by pool.
 * 
 * <pre>
 * // create and configure I18N repository
 * ConfigBuilder builder = new I18nRepository.ConfigBuilder(repositoryPath);
 * I18nRepository repository = new I18nRepository(builder.build());
 * 
 * // create single or multiple locale I18N pool accordingly repository support
 * I18nPool pool = repository.getPoolInstance();
 * 
 * // traverses all files from repository, that is, from all locale
 * for(I18nFile i18nFile : repository) {
 *   // create object instance and initialize it from file content
 *   File file = i18nFile.getFile();
 *   Template template = new Template(file);
 * 
 *   // store created object instance on cache, bound to file name and detected locale
 *   pool.put(file.getName(), template, i18nFile.getLocale());
 * }
 * </pre>
 * 
 * <p>
 * Repository instance is configured using a {@link Config} instance that is created by a builder, see
 * {@link I18nRepository.ConfigBuilder}. Configuration builder needs repository path and optional files pattern.
 * Repository path is a standard directories path but with ${locale} variable indicating locale directory position into
 * directories hierarchy, e.g. <code>/var/www/vhosts/site/${locale}/emails/</code>. Directory path can be relative or
 * absolute. If files pattern is not provided accept all files.
 * 
 * @author Iulian Rotaru
 */
public class I18nRepository implements Iterable<I18nFile>
{
  /** Iterable over I18N files from repository. */
  private final Iterable<I18nFile> iterable;

  /** Flag true if I18N repository has support for multiple locale. */
  private final boolean multiLocale;

  /**
   * Create I18N repository instance and configure it. See {@link ConfigBuilder} for configuration object properties.
   * 
   * @param config configuration object.
   * @throws IllegalArgumentException if configured base directory does not denote an existing directory.
   * @throws IOException if reading directories content fails, perhaps for lack of authorization.
   * @throws BugError if no files matching files pattern found or missing locale directory.
   */
  public I18nRepository(Config config) throws IOException
  {
    this.multiLocale = config.getProperty("multi-locale", Boolean.class, true);
    File baseDir = config.getProperty("base-dir", File.class);
    Params.isDirectory(baseDir, "Base directory");

    FileFilter fileFilter = new WildcardFilter(config.getProperty("files-pattern", WildcardFilter.ACCEPT_ALL));

    if(this.multiLocale) {
      this.iterable = new MultipleLocale(baseDir, config.getProperty("sub-path"), fileFilter);
    }
    else {
      this.iterable = new SingleLocale(baseDir, fileFilter);
    }
  }

  /**
   * Factory method for I18N pool instances. Returned pool instance may be single or multiple locale, depending on this
   * repository configuration, see {@link #multiLocale}. This factory method just delegates {@link I18nPoolFactory}.
   * 
   * @param <T> instance type.
   * @return I18N pool instance.
   */
  public <T> I18nPool<T> getPoolInstance()
  {
    return I18nPoolFactory.getInstance(multiLocale);
  }

  /** Implements iterable interface so that repository can be traversed via for-each loop. */
  @Override
  public Iterator<I18nFile> iterator()
  {
    return iterable.iterator();
  }

  // ----------------------------------------------------
  // Configuration builder

  /**
   * Configuration builder for I18N repository. Repository instance is configured using a {@link Config} instance that
   * is created by this builder. Configuration builder needs repository path and optional files pattern. Repository path
   * is a standard directories path but with ${locale} variable indicating locale directory position into directories
   * hierarchy, e.g. <code>/var/www/vhosts/site/${locale}/emails/</code>. Directory path can be relative or absolute. If
   * files pattern is not provided accept all files.
   * <p>
   * For completeness here are the properties required by I18N repository.
   * <table>
   * <caption>Properties</caption>
   * <tr>
   * <td>Name
   * <td>Type
   * <td>Default
   * <td>Description
   * <tr>
   * <td>multi-locale
   * <td>{@link Boolean}
   * <td>true
   * <td>I18N repository is usually multiple locale; if single locale is desired set this property to false.
   * <tr>
   * <td>base-dir
   * <td>{@link File}
   * <td>null
   * <td>Mandatory repository base directory, absolute or relative. Base directory contains locale directories as direct
   * children.
   * <tr>
   * <td>sub-path
   * <td>{@link String}
   * <td>null
   * <td>Optional sub-path following after locale directory.
   * <tr>
   * <td>files-pattern
   * <td>{@link String}
   * <td>WildcardFilter.ACCEPT_ALL
   * <td>Pattern for the files to match. Should have format as accepted by {@link WildcardFilter}.
   * </table>
   * 
   * 
   * @author Iulian Rotaru
   */
  public static class ConfigBuilder extends js.lang.ConfigBuilder
  {
    /** Locale variable present into repository directory path to mark sub-directory used for locale discriminate. */
    private static final String LOCALE_VARIABLE = "${locale}";

    /** Support for multiple locale. */
    private final boolean multiLocale;
    /** Base directory for I18N repository. */
    private final String baseDir;
    /** Optional sub-path following locale directory, default to null */
    private final String subpath;
    /** Optional files pattern, default to null, in which case all files are accepted. */
    private final String filesPattern;

    /**
     * Create configuration builder for given repository path and optional files pattern. Repository path may have
     * locale variable <code>${locale}</code> and optional sub-path, e.g.
     * <code>/var/www/vhosts/site/${locale}/emails/</code>.
     * 
     * @param repositoryPath repository path,
     * @param filesPattern optional files pattern, null if not used.
     * @throws IllegalArgumentException if <code>repositoryPath</code> argument is null or empty, inferred base
     *           directory does not exist or <code>filesPattern</code> argument is empty.
     */
    public ConfigBuilder(String repositoryPath, String filesPattern)
    {
      Params.notNullOrEmpty(repositoryPath, "Repository path");
      Params.notEmpty(filesPattern, "Files pattern");

      repositoryPath = Files.normalizePath(repositoryPath);
      int localeVarialeIndex = repositoryPath.indexOf(LOCALE_VARIABLE);
      if(localeVarialeIndex != -1) {
        this.multiLocale = true;

        this.baseDir = repositoryPath.substring(0, localeVarialeIndex);
        File baseDir = new File(this.baseDir);
        if(!baseDir.isDirectory()) {
          throw new IllegalArgumentException(String.format("Base directory |%s| not found.", baseDir));
        }

        // create temporary, empty sub-path to deal with optional trailing path separator
        String subpath = "";
        int subpathIndex = repositoryPath.indexOf(File.separatorChar, localeVarialeIndex);
        if(subpathIndex != -1) {
          subpath = repositoryPath.substring(subpathIndex + 1);
        }
        this.subpath = subpath.isEmpty() ? null : subpath;

      }
      else {
        // single locale repository does not have locale directory nor sub-path
        // so base directory is in fact supplied repository path
        this.multiLocale = false;
        this.baseDir = repositoryPath;
        this.subpath = null;
      }
      this.filesPattern = filesPattern;
    }

    @Override
    public Config build() throws ConfigException
    {
      Config config = new Config("i18n-repository");
      config.setProperty("multi-locale", multiLocale);
      config.setProperty("base-dir", baseDir);
      config.setProperty("sub-path", subpath);
      config.setProperty("files-pattern", filesPattern);
      return config;
    }
  }

  // ----------------------------------------------------
  // I18N repository implementations, both single and multiple locale

  /**
   * I18N repository implementation optimized for single locale.
   * 
   * @author Iulian Rotaru
   */
  private static class SingleLocale implements Iterable<I18nFile>
  {
    /** Repository files filtered accordingly configured files pattern. */
    private File[] files;

    /**
     * Construct single locale repository for requested repository directory and file filter.
     * 
     * @param repositoryDir repository directory,
     * @param fileFilter files filter, possibly accepting all.
     * @throws IOException if listing repository files fails.
     * @throws BugError if no files matching files filter found.
     */
    public SingleLocale(File repositoryDir, FileFilter fileFilter) throws IOException
    {
      files = repositoryDir.listFiles(fileFilter);
      if(files == null) {
        throw new IOException(String.format("Listing files from repository directory |%s| fails.", repositoryDir));
      }
      if(files == null || files.length == 0) {
        throw new BugError("No files |%s| found in repository directory |%s|.", fileFilter, repositoryDir);
      }
    }

    /** Get single locale repository iterator. */
    @Override
    public Iterator<I18nFile> iterator()
    {
      return new FileIterator();
    }

    /**
     * Single locale iterator for I18N repository. Iterator is not reusable nor thread safe.
     * 
     * @author Iulian Rotaru
     */
    private class FileIterator implements Iterator<I18nFile>
    {
      /** Current file index. */
      private int index;

      /** Create file iterator instance. */
      public FileIterator()
      {
        this.index = -1;
      }

      /**
       * Return true if there are more I18N files into single locale repository.
       * 
       * @return true if there are more files.
       */
      @Override
      public boolean hasNext()
      {
        return ++index < files.length;
      }

      /**
       * Get current processing file from single locale repository. This method should be called after
       * {@link #hasNext()} otherwise behavior is not defined.
       * 
       * @return current processing file.
       */
      @Override
      public I18nFile next()
      {
        return new I18nFile(files[index]);
      }

      /**
       * Iterator remove operation is not supported.
       * 
       * @throws UnsupportedOperationException always since remove operation is not supported.
       */
      @Override
      public void remove()
      {
        throw new UnsupportedOperationException();
      }
    }
  }

  /**
   * Multiple locale I18N repository. This I18N repository implementation is in fact a list of I18N files, in no
   * particular order.
   * <p>
   * This class scans for locale directory into given base directory; locale directory name should match
   * {@link #LOCALE_PATTERN}. It is considered a bug if no locale directory found. Then scan locale directory for files
   * matching given file filter. If sub-path argument is provided add it to locale directory and scan resulting
   * directory. It is also considered a bug if no matching files found.
   * <p>
   * Matching files are added to internal list but no particular order is guaranteed.
   * 
   * @author Iulian Rotaru
   */
  private static class MultipleLocale implements Iterable<I18nFile>
  {
    /**
     * Locale pattern has 2 lower case letters for language code and 2 upper case letters for country code, separated by
     * hyphen.
     */
    private static final Pattern LOCALE_PATTERN = Pattern.compile("^[a-z]{2}(?:\\-[A-Z]{2})?$");

    /** Repository files list contains files from all locale directories. */
    private List<I18nFile> files;

    /**
     * Construct multiple locale repository for requested base directory, file filter and optional sub-path.
     * 
     * @param baseDir repository base directory,
     * @param subpath optional sub-path, null if unused,
     * @param fileFilter file filter, possibly accepting all.
     * @throws IOException if listing directories content fails.
     * @throws BugError if no locale directories found on base directory, sub-path directory not found or no files
     *           matching file filter found on a locale directory.
     */
    public MultipleLocale(File baseDir, String subpath, FileFilter fileFilter) throws IOException
    {
      // locale directories present into repository base directory, listed in no particular order
      File[] localeDirs = baseDir.listFiles(new FileFilter()
      {
        @Override
        public boolean accept(File path)
        {
          // accept path if is a directory and its name match locale pattern
          return path.isDirectory() && LOCALE_PATTERN.matcher(path.getName()).matches();
        }
      });
      if(localeDirs == null) {
        throw new IOException(String.format("Listing locale on base directory |%s| fails.", baseDir));
      }
      if(localeDirs.length == 0) {
        throw new BugError("No locale directories found in |%s|.", baseDir);
      }

      this.files = new ArrayList<I18nFile>();
      for(File localeDir : localeDirs) {
        // locale directory name is used as language tag for associated locale
        // takes care to extract language tag before adding subpath
        String languageTag = localeDir.getName();
        if(subpath != null) {
          localeDir = new File(localeDir, subpath);
          if(!localeDir.isDirectory()) {
            throw new BugError("Invalid sub-path |%s| argument. Sub-directory |%s| not found.", subpath, localeDir);
          }
        }

        File[] localeFiles = localeDir.listFiles(fileFilter);
        if(localeFiles == null) {
          throw new IOException(String.format("Listing files on locale directory |%s| fails.", localeDir));
        }
        if(localeFiles.length == 0) {
          throw new BugError("No files |%s| found in repository locale directory |%s|.", fileFilter, localeDir);
        }
        for(File localeFile : localeFiles) {
          this.files.add(new I18nFile(localeFile, Locale.forLanguageTag(languageTag)));
        }
      }
    }

    /** Return and iterator over repository files. */
    @Override
    public Iterator<I18nFile> iterator()
    {
      return files.iterator();
    }
  }
}
