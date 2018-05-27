package js.format;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Percent value format - 12.34%. Percent string representation has a numeric value and a percent symbol, both depending on
 * locale settings.
 * <p>
 * Percent formatter implementation is based on Java {@link NumberFormat} that is not synchronized. Therefore this
 * implementation is not thread safe.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public final class Percent implements Format {
	/** Formatter for percent. Both numeric part and percent symbol are subject of locale adjustments. */
	private NumberFormat numberFormat;

	/** Create percent formatter with default locale settings. */
	public Percent() {
		this(Locale.getDefault());
	}

	/**
	 * Create percent formatter for given locale settings.
	 * 
	 * @param locale locale settings.
	 */
	public Percent(Locale locale) {
		this.numberFormat = NumberFormat.getPercentInstance(locale);
		this.numberFormat.setGroupingUsed(true);
		this.numberFormat.setMinimumFractionDigits(2);
		this.numberFormat.setMaximumFractionDigits(2);
	}

	/**
	 * Format numeric value as percent representation using locale settings. Returns empty string if <code>value</code> argument
	 * is null.
	 * 
	 * @param value numeric value.
	 * @return numeric value represented as locale percent or empty string.
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
