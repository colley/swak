
package com.swak.core.spectator.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/** Timer implementation for the default registry. */
final class DefaultTimer extends AbstractTimer {

  private final Id id;
  private final AtomicLong count;
  private final AtomicLong totalTime;

  /** Create a new instance. */
  DefaultTimer(Clock clock, Id id) {
    super(clock);
    this.id = id;
    count = new AtomicLong(0L);
    totalTime = new AtomicLong(0L);
  }

  @Override public Id id() {
    return id;
  }

  @Override public boolean hasExpired() {
    return false;
  }

  @Override public void record(long amount, TimeUnit unit) {
    if (amount >= 0L) {
      final long nanos = TimeUnit.NANOSECONDS.convert(amount, unit);
      totalTime.addAndGet(nanos);
      count.incrementAndGet();
    }
  }

  @Override public Iterable<Measurement> measure() {
    final long now = clock.wallTime();
    final List<Measurement> ms = new ArrayList<>(2);
    ms.add(new Measurement(id.withTag(Statistic.count), now, count.get()));
    ms.add(new Measurement(id.withTag(Statistic.totalTime), now, totalTime.get()));
    return ms;
  }

  @Override public long count() {
    return count.get();
  }

  @Override public long totalTime() {
    return totalTime.get();
  }
}
