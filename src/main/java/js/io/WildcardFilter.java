package js.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import js.util.Params;

/**
 * Filters files using path pattern with wildcards. This filter selects files and directories based on path pattern. Pattern can
 * contain one or more wildcard characters. Although this class implements file filter interfaces it can be used for generic
 * string pattern matching, see {@link #accept(String)}.
 * <p>
 * The wildcard matcher uses the characters '?' and '*' to represent a single or multiple wildcard characters.
 * 
 * <pre>
 * File dir = new File(&quot;.&quot;);
 * FileFilter fileFilter = new WildcardFileFilter(&quot;*test*.java&quot;);
 * for (File file: dir.listFiles(fileFilter)) {
 * 	...
 * }
 * </pre>
 * 
 * @author Iulian Rotaru
 */
public class WildcardFilter implements FileFilter, FilenameFilter, Serializable {
	/** Java serialization version. */
	private static final long serialVersionUID = -780074911185887594L;

	/** Path pattern accepting all files. */
	public static final String ACCEPT_ALL = "*";

	/** Pattern used to match file names and strings. */
	private final String pattern;

	/**
	 * Construct a new case-sensitive wildcard filter instance.
	 *
	 * @param pattern pattern used to match file names and strings.
	 * @throws IllegalArgumentException if the pattern is null or empty.
	 */
	public WildcardFilter(String pattern) {
		Params.notNullOrEmpty(pattern, "Pattern");
		this.pattern = pattern;
	}

	/**
	 * Test if file name matches this filter pattern. This method has a directory parameter required by interface signature but
	 * is not used by current implementation.
	 * <p>
	 * If <code>name</code> argument is null or empty this predicate returns false.
	 * 
	 * @param dir unused file directory, for interface compatibility.
	 * @param name the file name, null or empty accepted.
	 * @return true if the <code>name</code> argument matches this filter pattern.
	 */
	@Override
	public boolean accept(File dir, String name) {
		return match(name, pattern);
	}

	/**
	 * Test if name of the file matches this filter pattern. Returns false if <code>file</code> argument is null.
	 *
	 * @param file the file to check, null accepted.
	 * @return true if name of the file matches this filter pattern.
	 */
	@Override
	public boolean accept(File file) {
		return file != null ? match(file.getName(), pattern) : false;
	}

	/**
	 * Returns true if string value matches this filter pattern. Returns false if <code>string</code> argument is null or empty.
	 * 
	 * @param string string value, null or empty accepted.
	 * @return true if string value matches this filter pattern.
	 */
	public boolean accept(String string) {
		return match(string, pattern);
	}

	/** Instance string representation. */
	@Override
	public String toString() {
		return pattern;
	}

	/**
	 * Test if string value matches given pattern. Returns false if <code>string</code> argument is null or empty. Pattern
	 * argument is guaranteed by this class logic to be not null and not empty.
	 * 
	 * @param string string value to match against pattern, null or empty accepted,
	 * @param pattern not null or empty pattern.
	 * @return true if string value matches the pattern.
	 */
	private static boolean match(String string, String pattern) {
		if (string == null || string.isEmpty()) {
			return false;
		}
		String[] wcs = tokenize(pattern);

		boolean anyChars = false;
		int textIdx = 0;
		int wcsIdx = 0;
		Stack<int[]> backtrack = new Stack<int[]>();

		// loop around a backtrack stack, to handle complex * matching
		do {
			if (backtrack.size() > 0) {
				int[] array = backtrack.pop();
				wcsIdx = array[0];
				textIdx = array[1];
				anyChars = true;
			}

			// loop whilst tokens and text left to process
			while (wcsIdx < wcs.length) {

				if (wcs[wcsIdx].equals("?")) {
					// ? so move to next text char
					textIdx++;
					anyChars = false;

				} else if (wcs[wcsIdx].equals("*")) {
					// set any chars status
					anyChars = true;
					if (wcsIdx == wcs.length - 1) {
						textIdx = string.length();
					}

				} else {
					// matching text token
					if (anyChars) {
						// any chars then try to locate text token
						textIdx = string.indexOf(wcs[wcsIdx], textIdx);
						if (textIdx == -1) {
							// token not found
							break;
						}
						int repeat = string.indexOf(wcs[wcsIdx], textIdx + 1);
						if (repeat >= 0) {
							backtrack.push(new int[] { wcsIdx, repeat });
						}
					} else {
						// matching from current position
						if (!string.startsWith(wcs[wcsIdx], textIdx)) {
							// couldnt match token
							break;
						}
					}

					// matched text token, move text index to end of matched token
					textIdx += wcs[wcsIdx].length();
					anyChars = false;
				}

				wcsIdx++;
			}

			// full match
			if (wcsIdx == wcs.length && textIdx == string.length()) {
				return true;
			}

		} while (backtrack.size() > 0);

		return false;
	}

	/**
	 * Splits a string into a number of tokens.
	 * 
	 * @param string string to split.
	 * @return tokens list.
	 */
	private static String[] tokenize(String string) {
		if (string.indexOf("?") == -1 && string.indexOf("*") == -1) {
			return new String[] { string };
		}

		char[] textArray = string.toCharArray();
		List<String> tokens = new ArrayList<String>();
		StringBuilder tokenBuilder = new StringBuilder();

		for (int i = 0; i < textArray.length; i++) {
			if (textArray[i] != '?' && textArray[i] != '*') {
				// collect non wild card characters
				tokenBuilder.append(textArray[i]);
				continue;
			}

			if (tokenBuilder.length() != 0) {
				tokens.add(tokenBuilder.toString());
				tokenBuilder.setLength(0);
			}
			if (textArray[i] == '?') {
				tokens.add("?");
			} else if (tokens.size() == 0 || (i > 0 && tokens.get(tokens.size() - 1).equals("*") == false)) {
				tokens.add("*");
			}
		}

		if (tokenBuilder.length() != 0) {
			tokens.add(tokenBuilder.toString());
		}

		return (String[]) tokens.toArray(new String[tokens.size()]);
	}
}
