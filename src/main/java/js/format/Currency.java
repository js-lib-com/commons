package js.format;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Currency format - $12.34. Currency string representation has a numeric value and a currency symbol, both depending on locale
 * settings.
 * <p>
 * Currency formatter implementation is based on Java {@link NumberFormat} that is not synchronized. Therefore this
 * implementation is not thread safe.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public final class Currency implements Format {
	/** Formatter for currency. Both numeric part and currency symbol are subject of locale adjustments. */
	private NumberFormat numberFormat;

	/** Create currency formatter with default locale settings. */
	public Currency() {
		this(Locale.getDefault());
	}

	/**
	 * Create currency formatter for given locale settings.
	 * 
	 * @param locale locale settings.
	 */
	public Currency(Locale locale) {
		this.numberFormat = NumberFormat.getCurrencyInstance(locale);
		this.numberFormat.setGroupingUsed(true);
		this.numberFormat.setMinimumFractionDigits(2);
		this.numberFormat.setMaximumFractionDigits(2);
	}

	/**
	 * Format numeric value as currency representation using locale settings. Returns empty string if <code>value</code>
	 * argument is null.
	 * 
	 * @param value numeric value.
	 * @return numeric value represented as locale currency or empty string.
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
