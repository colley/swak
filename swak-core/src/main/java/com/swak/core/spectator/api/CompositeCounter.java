
package com.swak.core.spectator.api;

import java.util.Collection;
import java.util.Iterator;

/** Counter implementation for the composite registry. */
final class CompositeCounter extends CompositeMeter<Counter> implements Counter {

  /** Create a new instance. */
  CompositeCounter(Id id, Collection<Counter> counters) {
    super(id, counters);
  }

  @Override public void add(double amount) {
    for (Counter c : meters) {
      c.add(amount);
    }
  }

  @Override public double actualCount() {
    Iterator<Counter> it = meters.iterator();
    return it.hasNext() ? it.next().actualCount() : 0.0;
  }
}
