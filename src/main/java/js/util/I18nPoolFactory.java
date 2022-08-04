package js.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import js.lang.BugError;

/**
 * Internal factory for {@link I18nPool} instances. This class provides a single factory method, see
 * {@link #getInstance(boolean)}, that creates the actual instances. Support for multiple locale can be missing in which case
 * this factory creates a simplified, optimized I18N pool instance. In this case locale is not used and all implemented methods
 * silently ignore <code>locale</code> argument, if provided.
 * 
 * <pre>
 * I18nPool&lt;Template&gt; pool = I18nPoolFactory.getInstance(false); // I18N pool without multiple locale support
 * pool.put(&quot;index.htm&quot;, new Template(&quot;index&quot;), Locale.getDefault()); // locale is silently ignored
 * </pre>
 * 
 * <p>
 * For I18N pool with multiple locale support returned instance is actually a map of maps. Outer map uses locale language tag
 * for key while inner map uses object name. For instances with multiple locale support, <code>locale</code> argument is
 * mandatory and all implemented methods throw illegal arguments exception if <code>locale</code> is missing.
 * 
 * <pre>
 * I18nPool&lt;Template&gt; pool = I18nPoolFactory.getInstance(true); // I18N pool with multiple locale support
 * pool.put(&quot;index.htm&quot;, new Template(&quot;index&quot;)); // missing locale rise illegal argument exception
 * </pre>
 * 
 * @author Iulian Rotaru
 */
final class I18nPoolFactory {
	/**
	 * Create new I18N pool instance. If multiple locale support is not requested this factory creates a simplified, optimized
	 * I18N pool instance. In this case, if optional <code>locale</code> parameter is provided, it is silently ignored, see
	 * {@link I18nPool} interface.
	 * <p>
	 * For I18N pool with multiple locale support returned instance is actually a map of maps. Outer map uses locale language
	 * tag for key while inner map uses object name.
	 * 
	 * @param multiLocale flag for multiple locale support.
	 * @param <T> instance type.
	 * @return new I18N pool instance.
	 */
	public static <T> I18nPool<T> getInstance(boolean multiLocale) {
		if (multiLocale) {
			return new LocalePool<T>();
		} else {
			return new SimplePool<T>();
		}
	}

	/**
	 * I18N pool implementation optimized for repositories without multiple locale. This simplified I18N pool implementation is
	 * in fact a map of named objects. Locale is not used; all methods silently ignore <code>locale</code> argument, if
	 * provided.
	 * 
	 * @param <T> pool object type.
	 * @author Iulian Rotaru
	 */
	private static class SimplePool<T> implements I18nPool<T> {
		/** Cache storage. */
		private final Map<String, T> map = new HashMap<String, T>();

		@Override
		public boolean put(String name, T t, Locale... locale) {
			Params.notNullOrEmpty(name, "Name");
			return map.put(name, t) != null;
		}

		@Override
		public T get(String name, Locale... locale) {
			Params.notNullOrEmpty(name, "Name");
			return map.get(name);
		}

		@Override
		public boolean has(String name, Locale... locale) {
			Params.notNullOrEmpty(name, "Name");
			return map.containsKey(name);
		}
	}

	/**
	 * Locale sensitive I18N pool implementation.
	 * 
	 * @param <T> pool object type.
	 * @author Iulian Rotaru
	 */
	private static class LocalePool<T> implements I18nPool<T> {
		/**
		 * Locale sensitive cache storage. On interface this pool uses locale to put and retrieve named instances but internally
		 * uses locale language tag for key, see {@link Locale#toLanguageTag()}. As requested by I18NPool interface locale
		 * should be based only on language and optional country, encoded with ISO 639 alpha-2, respective ISO 3166 alpha-2;
		 * script, variant and extension should be empty.
		 * <p>
		 * Implementation avoid using locale instances for map keys. Accordingly Java Locale API, ISO 639 alpha-2 language code
		 * is not stable; there are updated language codes that will render in different keys. For example Locale("he") and
		 * Locale("iw") will result in different map keys. Using locale language tag ensure always is used the new language
		 * code, in previous example <code>he</code>.
		 */
		private final Map<String, Map<String, T>> localeMaps = new HashMap<String, Map<String, T>>();

		@Override
		public boolean put(String name, T t, Locale... locale) {
			Params.notNullOrEmpty(name, "Name");
			Params.notNullOrEmpty(locale, "Locale");
			Params.empty(locale[0].getVariant(), "Locale variant");
			Params.empty(locale[0].getScript(), "Locale script");
			Params.empty(locale[0].getExtensionKeys(), "Locale extension");

			Map<String, T> maps = localeMaps.get(locale[0].toLanguageTag());
			if (maps == null) {
				maps = new HashMap<String, T>();
				localeMaps.put(locale[0].toLanguageTag(), maps);
			}
			return maps.put(name, t) != null;
		}

		@Override
		public T get(String name, Locale... locale) {
			Params.notNullOrEmpty(name, "Name");
			Params.notNullOrEmpty(locale, "Locale");
			return maps(locale[0]).get(name);
		}

		@Override
		public boolean has(String name, Locale... locale) {
			Params.notNullOrEmpty(name, "Name");
			Params.notNullOrEmpty(locale, "Locale");
			return maps(locale[0]).containsKey(name);
		}

		/**
		 * Get named objects map bound to requested locale settings.
		 * 
		 * @param locale locale settings to retrieve objects map for.
		 * @return objects map bound to requested locale settings.
		 * @throws IllegalArgumentException if <code>locale</code> argument has variant, script or extension.
		 */
		private Map<String, T> maps(Locale locale) {
			Params.empty(locale.getVariant(), "Locale variant");
			Params.empty(locale.getScript(), "Locale script");
			Params.empty(locale.getExtensionKeys(), "Locale extension");

			Map<String, T> maps = localeMaps.get(locale.toLanguageTag());
			if (maps == null) {
				throw new BugError("Missing repository for locale |%s|.", locale);
			}
			return maps;
		}
	}
}
