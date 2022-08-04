package js.util;

import java.util.Locale;

/**
 * Locale sensitive cache for named objects. An object into I18N pool is identified by an application level unique name and by
 * locale settings. Both single and multiple locale implementations should be supported. Locale parameter is used only for
 * implementations with multiple locale support; implementation should throw illegal argument exception if <code>locale</code>
 * argument is missing. For single locale implementations <code>locale</code> argument is silently ignored.
 * <p>
 * Used locale should be based only on language and optional country, encoded with ISO 639 alpha-2, respective ISO 3166 alpha-2.
 * Implementation should throw illegal argument exception if provided locale has script, variant or extension. Also, for string
 * representation, implementations should use {@link Locale#toLanguageTag()} that guarantee usage of newly language codes. This
 * is because, accordingly Java {@link Locale} API, language encoded ISO 639 alpha-2 is not a stable standard and has some codes
 * changed.
 * <p>
 * To facilitate loading I18N cache from files system there is companion {@link I18nRepository} class. In code snippet below,
 * iterate files from repository and update pool. Note that I18N file has locale detected by repository iterator while scanning
 * the files system. For single locale repository detected locale is null but is ignored anyway by I18N pool, that is created
 * also a single locale instance.
 * 
 * <pre>
 * // create single or multiple locale I18N pool accordingly repository support
 * I18nPool pool = repository.getPoolInstance();
 * 
 * for (I18nFile i18nFile : repository) {
 * 	// create object instance and initialize it from file content
 * 	File file = i18nFile.getFile();
 * 	Template template = new Template(file);
 * 
 * 	// store created object instance on cache, bound to file name and detected locale
 * 	pool.put(file.getName(), template, i18nFile.getLocale());
 * }
 * </pre>
 * 
 * @param <T> type of objects stored by this cache.
 * @author Iulian Rotaru
 */
public interface I18nPool<T> {
	/**
	 * Store an instance on this cache bound to given name and optional locale settings. To later retrieve stored instance one
	 * should know both instance name and locale settings. For implementations without multiple locale support, optional
	 * <code>locale</code> argument is silently ignored and not even tested for validity.
	 * 
	 * @param name unique name with application scope,
	 * @param t object instance to store on cache,
	 * @param locale optional locale settings, mandatory for implementations with multiple locale support.
	 * @return true if object instance were already present.
	 * @throws IllegalArgumentException if <code>name</code> argument is null or empty or <code>locale</code> argument is
	 *             missing, null or has variant, script or extension, on an implementation with multiple locale support.
	 */
	boolean put(String name, T t, Locale... locale);

	/**
	 * Get instance identified by name and optional locale settings. Returns null if no instance found with specified criteria.
	 * For implementations without multiple locale support, optional <code>locale</code> argument is silently ignored and not
	 * even tested for validity.
	 * 
	 * @param name unique name with application scope,
	 * @param locale optional locale settings, mandatory for implementations with multiple locale support.
	 * @return named instance or null.
	 * @throws IllegalArgumentException if <code>name</code> argument is null or empty or <code>locale</code> argument is
	 *             missing, null or has variant, script or extension, on an implementation with multiple locale support.
	 */
	T get(String name, Locale... locale);

	/**
	 * Test if cache has an instance identified by given name and optional locale settings. For implementations without multiple
	 * locale support, optional <code>locale</code> argument is silently ignored and not even tested for validity.
	 * 
	 * @param name unique name with application scope,
	 * @param locale optional locale settings, mandatory for implementations with multiple locale support.
	 * @return true if there is an instance identified by given criteria.
	 * @throws IllegalArgumentException if <code>name</code> argument is null or empty or <code>locale</code> argument is
	 *             missing, null or has variant, script or extension, on an implementation with multiple locale support.
	 */
	boolean has(String name, Locale... locale);
}
