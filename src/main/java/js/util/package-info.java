/**
 * Utility functions for strings, files, dynamic types manipulation, parameters validation and more. Most 
 * classes from utility package are utility classes with all method declared as static. Also methods
 * exhibit functional like behavior, that is, have no side effects and does not manipulate external state.
 * For this reasons classes from this package are inherently thread safe. 
 *  
 * <p>
 * Note that strings, files, types and parameters utility classes allow for sub-classing so that they
 * can be extended and used as a name space. For this reason they are not final and have protected constructor.
 * <pre>
 *	public class com.pack.Strings extends js.util.String {
 *		public String randomName(String string) {
 *			return ...
 *		}
 *	}
 * 
 *	import com.pack.Strings;
 *	...
 *	String name = Strings.randomName(source);    // uses newly added method
 *	if(Strings.isMember(name)) {                 // uses method defined by this library
 *		...
 *	} 
 * </pre>
 * @author Iulian Rotaru
 * @version final
 */
package js.util;

