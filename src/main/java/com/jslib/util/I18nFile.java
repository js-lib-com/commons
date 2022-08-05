package com.jslib.util;

import java.io.File;
import java.util.Locale;

/**
 * An immutable file on a {@link I18nRepository}, bound to a specific locale settings. If parent repository has no multiple
 * locale support this class {@link #locale} field is always null.
 * <p>
 * If <code>locale</code> argument is provided to constructor, it should be based only on language and optional country, encoded
 * with ISO 639 alpha-2, respective ISO 3166 alpha-2; script, variant and extension are empty. Constructor throws illegal
 * argument exception if <code>locale</code> argument has variant, script or extension.
 * 
 * @author Iulian Rotaru
 */
public class I18nFile {
	/** The file path. */
	private final File file;

	/**
	 * Locale settings this file belongs to, always null if parent repository has no support for multiple locale. Locale
	 * settings is based only on language and optional country, encoded with ISO 639 alpha-2, respective ISO 3166 alpha-2;
	 * script, variant and extension are empty.
	 */
	private final Locale locale;

	/**
	 * Construct not locale sensitive I18N file instance. Locale settings field is initialized to null.
	 * 
	 * @param file underlying filesystem file path.
	 */
	I18nFile(File file) throws IllegalArgumentException {
		this.file = file;
		this.locale = null;
	}

	/**
	 * Construct I18N file instance bound to specified locale settings. Given locale should be based only on language and
	 * optional country, encoded with ISO 639 alpha-2, respective ISO 3166 alpha-2; script, variant and extension should be
	 * empty.
	 * 
	 * @param file underlying filesystem file path,
	 * @param locale locale settings.
	 * @throws IllegalArgumentException if <code>locale</code> argument is null or has variant, script or extension.
	 */
	I18nFile(File file, Locale locale) throws IllegalArgumentException {
		this.file = file;

		Params.notNull(locale, "Locale");
		Params.empty(locale.getVariant(), "Locale variant");
		Params.empty(locale.getScript(), "Locale script");
		Params.empty(locale.getExtensionKeys(), "Locale extension");
		this.locale = locale;
	}

	/**
	 * Get path of the underlying filesystem file.
	 * 
	 * @return underlying filesystem file path.
	 * @see #file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Convenient method to retrieve file name.
	 * 
	 * @return file name, never null.
	 * @see #file
	 */
	public String getName() {
		return file.getName();
	}

	/**
	 * Get bound locale settings or null, if parent repository has no multiple locale support. If not null, locale settings is
	 * based only on language and optional country, encoded with ISO 639 alpha-2, respective ISO 3166 alpha-2; script, variant
	 * and extension are empty.
	 * 
	 * @return bound locale settings, possible null.
	 * @see #locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Convenient method to retrieve locale language tag, possible null.
	 * 
	 * @return locale language tag or null if locale is missing.
	 * @see #locale
	 */
	public String getLanguageTag() {
		return locale != null ? locale.toLanguageTag() : null;
	}
}
