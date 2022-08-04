package js.lang;

/**
 * A looper is one that makes loops. A loop is a processing unit that is executed repetitively. Processing duration is not
 * restricted. Anyway, {@link #loop()} implementation should consider that thread checks for interruption state between loops -
 * for this reason blocking IO read operations are not allowed.
 * <p>
 * This interface is designed to work with {@link LooperThread}, see sample code below.
 * 
 * <pre>
 * class DemoLooper implements Looper {
 * 	private LooperThread thread;
 * 
 * 	DemoLooper() {
 * 		tread = new LooperThread(this);
 * 		thread.start();
 * 	}
 * 
 * 	void stop() {
 * 		thread.stop();
 * 	}
 * 
 * 	void loop() throws Exception {
 * 		...
 * 	}
 * }
 * </pre>
 * 
 * @author Iulian Rotaru
 */
public interface Looper {
	/**
	 * Execute a single processing loop. Implementation has no formal restriction on processing duration but should consider
	 * thread interruption state is checked between loops.
	 * 
	 * @throws Exception every exception on loop processing is bubbled up.
	 */
	void loop() throws Exception;
}
