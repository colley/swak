
package com.swak.core.spectator.api;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Wraps an iterator with one that will filter the values using the specified predicate.
 */
class FilteredIterator<T> implements Iterator<T> {

  private final Iterator<T> it;
  private final Predicate<T> p;
  private T item;

  /**
   * Create a new instance.
   *
   * @param it
   *     Inner iterator that should be filtered.
   * @param p
   *     Predicate for finding matching results.
   */
  FilteredIterator(Iterator<T> it, Predicate<T> p) {
    this.it = it;
    this.p = p;
    findNext();
  }

  private void findNext() {
    while (it.hasNext()) {
      item = it.next();
      if (p.test(item)) {
        return;
      }
    }
    item = null;
  }

  @Override public boolean hasNext() {
    return item != null;
  }

  @SuppressWarnings("PMD.UnnecessaryLocalBeforeReturn")
  @Override public T next() {
    if (item == null) {
      throw new NoSuchElementException("next() called after reaching end of iterator");
    }
    // Note: PMD flags this, but the local tmp is necessary because findNext will change
    // the value of item
    T tmp = item;
    findNext();
    return tmp;
  }

  @Override public void remove() {
    throw new UnsupportedOperationException("remove");
  }
}
