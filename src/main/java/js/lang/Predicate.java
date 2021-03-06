package js.lang;

/**
 * Generic predicate evaluator. A predicate test generic condition on a given value and return boolean.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public interface Predicate {
	/**
	 * Predicate test condition.
	 * 
	 * @param value value to evaluate.
	 * @return true if condition is fulfilled.
	 */
	boolean test(Object value);
}
