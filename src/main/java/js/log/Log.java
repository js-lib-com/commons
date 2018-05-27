package js.log;

/**
 * A log instance has a name and supplies means to write log messages with priority level. Depending on logging system
 * configuration certain classes or levels can be disabled in which case messages are simple discarded. Current implementation
 * handle below levels, listed in priority order. If a level is enabled all level below it are also enabled. For example, if
 * enable <code>WARN</code>, <code>ERROR</code>, <code>FATAL</code> and <code>BUG</code> are automatically enabled.
 * <ol>
 * <li>TRACE - the same as debug but with finer granularity,
 * <li>DEBUG - fine-grained informational events that are most useful to debug an application,
 * <li>INFO - informational messages that highlight the progress of the application at coarse-grained level,
 * <li>WARN - potentially harmful situations,
 * <li>ERROR - error events that might still allow the application to continue running,
 * <li>FATAL - very severe error events that will presumably lead the application to abort,
 * <li>BUG - unexpected condition likely to be a bug.
 * </ol>
 * 
 * <p>
 * For every level there are two writing methods: one accepting an object message and the second with formatted string and
 * variable number of arguments. Object message is simply converted to string using implementation specific logic. At a minimum,
 * implementation should use {@link Object#toString()}. Object message is always converted to string even if it is a
 * {@link Throwable}. For stack trace dump uses {@link #dump(Object, Throwable)}.
 * <p>
 * Anyway, the real workhorse is string formatted variant, that is invoked even if string is not formatted, hence no formatting
 * arguments. This writing variant is well suited for for formatted messages since it actually build the message only if level
 * is enabled.
 * 
 * <pre>
 * // StringBuild is auto-created and execute strings merge before invoking debug writer, even if debug level is disabled
 * log.debug("Class loaded " + class.getName());
 * 
 * // formatted string and arguments are processed internally by debug writer only if debug level is enabled
 * log.debug("Class loaded %s.", class.getName());
 * </pre>
 * 
 * <h3>Stack Trace Dump</h3>
 * Logger writing methods does process all objects as strings, even if an exception is passed. In below sample code
 * <code>exception</code> is converted to string before the actual write operation.
 * 
 * <pre>
 * catch(IOException exception) {
 * 	log.error(exception);
 * }
 * </pre>
 * 
 * If one wants a throwable stack trace needs to explicitly invoke it using {@link #dump(Object, Throwable)} method.
 * 
 * <pre>
 * catch(IOException exception) {
 * 	log.dump("Fatal error on IO. Stack trace follow:", exception);
 * }
 * </pre>
 * 
 * @author Iulian Rotaru
 * @version final
 */
public interface Log {
	/**
	 * Write object message to logger if <code>trace</code> level is enabled. This method converts object message to string and
	 * write it as a single log record. Note that string conversion is performed even if given object message is a
	 * {@link Throwable}. If one needs throwable stack dump should explicitly use {@link #dump(Object, Throwable)}.
	 * 
	 * @param message object message.
	 */
	void trace(Object message);

	/**
	 * Write formatted message to this logger if <code>trace</code> level is enabled. This method uses formatted message to
	 * build the string message and write it as a single log record. Note that the actual string building is performed only only
	 * if level is enabled.
	 * <p>
	 * Message should be formatted as defined by {@link String#format(String, Object...)} standard method. It is caller
	 * responsibility to ensure the number and type of given argument match formatted message place holders. If message is not
	 * well formed or arguments are invalid log record is silently discarded.
	 * 
	 * @param message formatted message,
	 * @param args optional arguments for formatted message.
	 */
	void trace(String message, Object... args);

	/**
	 * Write object message to logger if <code>debug</code> level is enabled. See {@link #trace(Object)} for details.
	 * 
	 * @param message object message.
	 */
	void debug(Object message);

	/**
	 * Write formatted message to this logger if <code>debug</code> level is enabled. See {@link #trace(String, Object...)} for
	 * details.
	 * 
	 * @param message formatted message,
	 * @param args optional arguments for formatted message.
	 */
	void debug(String message, Object... args);

	/**
	 * Write object message to logger if <code>info</code> level is enabled. See {@link #trace(Object)} for details.
	 * 
	 * @param message object message.
	 */
	void info(Object message);

	/**
	 * Write formatted message to this logger if <code>info</code> level is enabled. See {@link #trace(String, Object...)} for
	 * details.
	 * 
	 * @param message formatted message,
	 * @param args optional arguments for formatted message.
	 */
	void info(String message, Object... args);

	/**
	 * Write object message to logger if <code>warn</code> level is enabled. See {@link #trace(Object)} for details.
	 * 
	 * @param message object message.
	 */
	void warn(Object message);

	/**
	 * Write formatted message to this logger if <code>warn</code> level is enabled. See {@link #trace(String, Object...)} for
	 * details.
	 * 
	 * @param message formatted message,
	 * @param args optional arguments for formatted message.
	 */
	void warn(String message, Object... args);

	/**
	 * Write object message to logger if <code>error</code> level is enabled. See {@link #trace(Object)} for details.
	 * 
	 * @param message object message.
	 */
	void error(Object message);

	/**
	 * Write formatted message to this logger if <code>error</code> level is enabled. See {@link #trace(String, Object...)} for
	 * details.
	 * 
	 * @param message formatted message,
	 * @param args optional arguments for formatted message.
	 */
	void error(String message, Object... args);

	/**
	 * Write object message to logger if <code>fatal</code> level is enabled. See {@link #trace(Object)} for details.
	 * 
	 * @param message object message.
	 */
	void fatal(Object message);

	/**
	 * Write formatted message to this logger if <code>fatal</code> level is enabled. See {@link #trace(String, Object...)} for
	 * details.
	 * 
	 * @param message formatted message,
	 * @param args optional arguments for formatted message.
	 */
	void fatal(String message, Object... args);

	/**
	 * Write a message about a condition that is likely to be a bug. This denote the code reaches a point that is not expected.
	 * For example switch default when expect all cases to be processed or an expected value is missing, see sample code.
	 * 
	 * <pre>
	 * switch(state) {
	 * 	case PLAYING:
	 * 	. . .
	 * 	break;
	 * 
	 * case PAUSED:
	 * 	. . .
	 * 	break;
	 *         
	 * default:
	 * 	log.bug(&quot;Player state not handled.&quot;);
	 * }
	 * </pre>
	 * 
	 * <pre>
	 * Connection connection = connections.remove(listener);
	 * if (connection == null) {
	 * 	log.bug(&quot;Unbind called without bind.&quot;);
	 * 	return;
	 * }
	 * </pre>
	 * 
	 * @param message formatted message,
	 * @param args optional arguments for formatted message.
	 */
	void bug(String message, Object... args);

	/**
	 * Bug message variant with integrated condition test. If condition is not true write formatted message but only if bug
	 * level is enabled. If bug level is not enabled expected condition is not considered and of course message is not written.
	 * 
	 * @param expectedCondition condition expected to be true,
	 * @param message formatted message,
	 * @param args optional arguments for formatted message.
	 */
	void bug(boolean expectedCondition, String message, Object... args);

	/**
	 * Dump throwable stack trace to this logger using <code>fatal</code> level. Object message is converted to string and write
	 * before stack trace as a separate log record. This method guarantees atomic operation, that is, no other log record
	 * present between object message and stack dump.
	 * <p>
	 * If <code>fatal</code> level is not enabled this method does nothing.
	 * 
	 * @param message object message,
	 * @param throwable throwable to dump stack trace for.
	 */
	void dump(Object message, Throwable throwable);

	/**
	 * Write message directly to logger with specified logging level. Anyway, if level is disabled message is not printed. This
	 * method is considered low level and is not intended for general use.
	 * 
	 * @param level requested log level,
	 * @param message log message.
	 */
	void print(LogLevel level, String message);
}
