package js.lang;

/**
 * Piece of functionality executed after some asynchronous logic completion. It is designed to be used by - but not limited to,
 * {@link AsyncTask} and remote method invocation, like in sample code listed below. On first example {@link #handle(Object)}
 * method is called transparently by asynchronous task after its execution completion.
 * 
 * <pre>
 * AsyncTask&lt;Object&gt; task = new AsyncTask&lt;Object&gt;(callback) {
 * 	protected Object handle() throws Throwable {
 * 		// asynchronous task logic;
 * 	}
 * 	// here asynchronous task logic invoke Callback#handle with value returned by above AsyncTask#execute
 * };
 * task.start();
 * </pre>
 * 
 * <pre>
 * WeatherService weatherService = Factory.getInstance(&quot;http://hub.bbnet.ro/&quot;, WeatherService.class);
 * weatherService.getCurrentWeather(47.1569, 27.5903, new Callback&lt;Weather&gt;() {
 * 	public handle(Weather weather) {
 * 		// do something useful with returned weather value
 * 	}
 * });
 * </pre>
 * 
 * @param <Value> value type returned by asynchronous logic.
 * 
 * @author Iulian Rotaru
 */
public interface Callback<Value> {
	/**
	 * Hook to be executed after asynchronous logic execution ends. This method is meant to process given value in a sound
	 * manner and is not expected to throw exceptions.
	 * 
	 * @param value value returned by asynchronous logic.
	 */
	void handle(Value value);
}