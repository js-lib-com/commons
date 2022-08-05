package com.jslib.util;

import java.io.File;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.jslib.io.WildcardFilter;

/**
 * List of strings following a given pattern. Given a source of strings this utility class collects only strings obeying a
 * requested pattern. It can be used for example to collect files of a certain type. Pattern is that accepted by
 * {@link WildcardFilter}.
 * <p>
 * This class is especially useful for collecting files from directories like below. It also takes care to handle null files
 * list that {@link File#list()} can return on exceptional conditions.
 * 
 * <pre>
 * File directory = new File(&quot;.&quot;);
 * FilteredStrings sources = new FilteredStrings(&quot;*.java&quot;);
 * sources.addAll(directory.list());
 * for (String source : sources) {
 * 	// do something with java source
 * }
 * </pre>
 * 
 * @author Iulian Rotaru
 */
public class FilteredStrings extends AbstractList<String> {
	/** List used to collect strings. */
	private List<String> list = new ArrayList<String>();

	/** Wildcard filter used to match pattern and given string. */
	private WildcardFilter filter;

	/**
	 * Construct a new filtered strings instance. Pattern argument is that accepted by {@link WildcardFilter}.
	 * 
	 * @param pattern string pattern.
	 */
	public FilteredStrings(String pattern) {
		this.filter = new WildcardFilter(pattern);
	}

	/**
	 * Add a string if it matches this filter pattern.
	 * 
	 * @param string string to add to this strings list.
	 * @return true if given <code>string</code> argument matches the pattern and was added to this strings list.
	 */
	@Override
	public boolean add(String string) {
		if (filter.accept(string)) {
			list.add(string);
			return true;
		}
		return false;
	}

	/**
	 * Add strings accepted by this filter pattern but prefixed with given string. Strings argument can come from
	 * {@link File#list()} and can be null in which case this method does nothing. This method accept a prefix argument; it is
	 * inserted at every string start before actually adding to strings list.
	 * 
	 * <pre>
	 * File directory = new File(&quot;.&quot;);
	 * FilteredStrings files = new FilteredStrings(&quot;index.*&quot;);
	 * files.addAll(&quot;/var/www/&quot;, directory.list());
	 * </pre>
	 * 
	 * If <code>directory</code> contains files like <em>index.htm</em>, <em>index.css</em>, etc. will be added to
	 * <code>files</code> but prefixed like <em>/var/www/index.htm</em>, respective <em>/var/www/index.css</em>.
	 * 
	 * @param prefix prefix to insert on every string,
	 * @param strings strings to scan for pattern, possible null.
	 */
	public void addAll(String prefix, String[] strings) {
		if (strings == null) {
			return;
		}
		for (String string : strings) {
			if (filter.accept(string)) {
				list.add(prefix + string);
			}
		}
	}

	/**
	 * Add strings accepted by this filter predicate. Strings argument can come from {@link File#list()} and can be null in
	 * which case this method does nothing.
	 * 
	 * @param strings strings to scan for pattern, possible null.
	 */
	public void addAll(String[] strings) {
		if (strings == null) {
			return;
		}
		for (String string : strings) {
			add(string);
		}
	}

	/**
	 * Returns an iterator instance usable to traverse this strings list.
	 * 
	 * @return this strings list iterator.
	 */
	@Override
	public Iterator<String> iterator() {
		return list.iterator();
	}

	/**
	 * Get the size of this strings list.
	 * 
	 * @return this strings list size.
	 */
	@Override
	public int size() {
		return list.size();
	}

	/**
	 * Random, indexed access to a string from this strings list.
	 * 
	 * @return the string for requested <code>index</code>.
	 * @throws IndexOutOfBoundsException if the <code>index</code> is out of range.
	 */
	@Override
	public String get(int index) throws IndexOutOfBoundsException {
		return list.get(index);
	}
}
