package js.log;

/**
 * Logger context stores named diagnostic context data to current thread. Thread named diagnostic data can be injected
 * into every log message from thread till {@link #clear()} is called. These class is specifically designed for
 * multithreaded environments and allow to separate otherwise intermixed log messages. Implementation is expected to
 * propagate this log context on child threads, most probable using {@link InheritableThreadLocal}.
 * <p>
 * Here is an example of a hypothetical servlet. On every request, service method stores context name and remote address
 * with <code>app</code>, respective <code>ip</code> names, on log context; every log message will include context name
 * and remote address no matter if called directly from servlet service or from a nested method.
 * 
 * <pre>
 * private static final LogContext logContext = LogFactory.getLogContext();
 * 
 * protected void service(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
 * 	// store request remote address to current thread
 *  logContext.put("app", httpRequest.getContextPath());
 *  logContext.put("ip", httpRequest.getRemoteHost());
 * 	try {
 * 		// log message can be configured to include request remote address
 * 		log.debug(...);
 * 	}
 * 	finally {
 * 		// takes care to cleanup request remote address from current thread since thread can be reused
 * 		logContext.clear();
 * 	}
 * }
 * </pre>
 * 
 * @author Iulian Rotaru
 * @version final
 */
public interface LogContext
{
  /**
   * Put named diagnostic context data on logger related to current thread. Diagnostic data will reside on current
   * thread till thread terminates or {@link #clear()} is called on current thread. Override value if named diagnostic
   * data already exists.
   * <p>
   * If given <code>name</code> is null or empty this method silently does nothing and if <code>value</code> is null
   * remove named diagnostic context data.
   * 
   * @param name name for diagnostic context data, not null,
   * @param value diagnostic context value, possible null.
   */
  void put(String name, String value);

  /** Cleanup all diagnostic data from this logger context. */
  void clear();
}
