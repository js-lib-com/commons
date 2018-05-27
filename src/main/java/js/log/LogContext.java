package js.log;

/**
 * Logger context stores diagnostic context data to current thread. Thread diagnostic data can be injected into every log
 * message from thread till {@link #pop()} is called. These class is specifically designed for multithreaded environments and
 * allow to separate otherwise intermixed log messages.
 * <p>
 * Here is an example of a hypothetical servlet. On every request, service method stores remote address on log context; every
 * log message will include remote address no matter if called directly from servlet service or from a nested method.
 * 
 * <pre>
 * private static final LogContext logContext = LogFactory.getLogContext();
 * 
 * protected void service(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
 * 	// store request remote address to current thread
 * 	logContext.push(httpRequest.getRemoteHost());
 * 	try {
 * 		// log message can be configured to include request remote address
 * 		log.debug(...);
 * 	}
 * 	finally {
 * 		// takes care to cleanup request remote address from current thread since thread can be reused
 * 		logContext.pop();
 * 	}
 * }
 * </pre>
 * 
 * @author Iulian Rotaru
 * @version final
 */
public interface LogContext {
	/**
	 * Push diagnostic context data on logger related to current thread. Diagnostic data will reside on current thread till
	 * thread terminates or {@link #pop()} is called on current thread.
	 * 
	 * @param diagnosticContext diagnostic context data.
	 */
	void push(String diagnosticContext);

	/**
	 * Cleanup diagnostic data from logger context. Is not mandatory to pair every {@link #push(String)} with this pop; if this
	 * method is not called pushed diagnostic data remains on thread till thread terminates.
	 */
	void pop();
}
