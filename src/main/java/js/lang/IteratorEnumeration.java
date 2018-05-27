package js.lang;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Convert iterable or iterator interfaces to enumeration.
 *
 * @param <E> enumeration element
 * 
 * @author Iulian Rotaru
 * @version final
 */
public final class IteratorEnumeration<E> implements Enumeration<E> {
	/** Internal iterator. */
	private final Iterator<E> iterator;

	/**
	 * Construct enumeration from iterable.
	 * 
	 * @param iterable iterable.
	 */
	public IteratorEnumeration(Iterable<E> iterable) {
		this.iterator = iterable.iterator();
	}

	/**
	 * Construct enumeration from iterator.
	 * 
	 * @param iterator iterator.
	 */
	public IteratorEnumeration(Iterator<E> iterator) {
		this.iterator = iterator;
	}

	/** Implements enumeration next element. */
	@Override
	public E nextElement() {
		return iterator.next();
	}

	/** Implements enumeration has more elements. */
	@Override
	public boolean hasMoreElements() {
		return iterator.hasNext();
	}
}