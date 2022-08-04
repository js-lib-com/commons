package js.lang;

/**
 * Generic logic that process an argument and return a value. This <code>Handler</code> and {@link Callback} are in fact the
 * same beast but with separated usage pattern: <code>handler</code> is called to process an argument and returns a value -
 * handy for specialization of generic logic whereas <code>callback</code> is used at completion of an asynchronous task and
 * does not return value since value is passed as argument.
 * 
 * @param <Value> type of value to return.
 * @param <Argument> type of argument.
 * 
 * @author Iulian Rotaru
 */
public interface Handler<Value, Argument> {
	/**
	 * Apply some processing on argument and return resulting value.
	 * 
	 * @param argument argument passed by invoker.
	 * @return processing result.
	 */
	Value handle(Argument argument);
}
