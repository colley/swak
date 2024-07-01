
package com.swak.core.spectator.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/** Distribution summary implementation for the default registry. */
final class DefaultDistributionSummary implements DistributionSummary {

  private final Clock clock;
  private final Id id;
  private final AtomicLong count;
  private final AtomicLong totalAmount;

  /** Create a new instance. */
  DefaultDistributionSummary(Clock clock, Id id) {
    this.clock = clock;
    this.id = id;
    count = new AtomicLong(0L);
    totalAmount = new AtomicLong(0L);
  }

  @Override public Id id() {
    return id;
  }

  @Override public boolean hasExpired() {
    return false;
  }

  @Override public void record(long amount) {
    if (amount >= 0) {
      totalAmount.addAndGet(amount);
      count.incrementAndGet();
    }
  }

  @Override public Iterable<Measurement> measure() {
    final long now = clock.wallTime();
    final List<Measurement> ms = new ArrayList<>(2);
    ms.add(new Measurement(id.withTag(Statistic.count), now, count.get()));
    ms.add(new Measurement(id.withTag(Statistic.totalAmount), now, totalAmount.get()));
    return ms;
  }

  @Override public long count() {
    return count.get();
  }

  @Override public long totalAmount() {
    return totalAmount.get();
  }
}
