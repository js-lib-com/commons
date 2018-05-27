package js.format;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Numeric value format - 12.34. This class is a thin wrapper for Java {@link NumberFormat} with grouping.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public final class Number implements Format {
	/** Number formatter. */
	private NumberFormat numberFormat;

	/** Create number formatter with default locale settings. */
	public Number() {
		this(Locale.getDefault());
	}

	/**
	 * Create number formatter with given locale settings.
	 * 
	 * @param locale locale settings.
	 */
	public Number(Locale locale) {
		this.numberFormat = NumberFormat.getNumberInstance(locale);
		this.numberFormat.setGroupingUsed(true);
	}

	/**
	 * Format numeric value using locale settings. Returns empty string if <code>value</code> argument is null.
	 * 
	 * @param value numeric value.
	 * @return numeric value represented as locale string.
	 * @throws IllegalArgumentException if <code>value</code> argument is not an instance of Number.
	 */
	@Override
	public String format(Object value) {
		if (value == null) {
			return "";
		}
		return numberFormat.format(value);
	}
}
