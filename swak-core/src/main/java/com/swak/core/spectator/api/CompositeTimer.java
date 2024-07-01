
package com.swak.core.spectator.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/** Timer implementation for the composite registry. */
final class CompositeTimer extends CompositeMeter<Timer> implements Timer {

  private final Clock clock;

  /** Create a new instance. */
  CompositeTimer(Id id, Clock clock, Collection<Timer> timers) {
    super(id, timers);
    this.clock = clock;
  }

  @Override public Clock clock() {
    return clock;
  }

  @Override public void record(long amount, TimeUnit unit) {
    for (Timer t : meters) {
      t.record(amount, unit);
    }
  }

  @Override public long count() {
    Iterator<Timer> it = meters.iterator();
    return it.hasNext() ? it.next().count() : 0L;
  }

  @Override public long totalTime() {
    Iterator<Timer> it = meters.iterator();
    return it.hasNext() ? it.next().totalTime() : 0L;
  }
}
