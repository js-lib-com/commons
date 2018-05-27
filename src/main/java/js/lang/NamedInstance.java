package js.lang;

/**
 * Identify an instance by its name in a context where multiple instances of the same type could exists.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public interface NamedInstance {
	/**
	 * Get instance name, unique in its scope. The scope on which name uniqueness should be guaranteed depends on system
	 * architecture but usually is local area network.
	 * 
	 * @return instance name.
	 */
	String getInstanceName();
}
