package js.lang;

/**
 * An object that is displayed on user interfaces. It has a string
 * representation, see {@link #toDisplay()}, that is more user friendly and may
 * be subject to localization.
 * <p>
 * If string representation has more words is recommended - but not mandatory,
 * to use space as separator.
 * 
 * @author Iulian Rotaru
 */
public interface Displayable {
    /**
     * Return object string representation for user interface.
     * 
     * @return object string representation.
     */
    String toDisplay();
}
