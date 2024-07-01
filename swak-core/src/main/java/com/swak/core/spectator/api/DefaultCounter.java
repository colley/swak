
package com.swak.core.spectator.api;

import com.swak.core.spectator.impl.AtomicDouble;

import java.util.Collections;

/** Counter implementation for the default registry. */
final class DefaultCounter implements Counter {

  private final Clock clock;
  private final Id id;
  private final AtomicDouble count;

  /** Create a new instance. */
  DefaultCounter(Clock clock, Id id) {
    this.clock = clock;
    this.id = id;
    this.count = new AtomicDouble(0.0);
  }

  @Override public Id id() {
    return id;
  }

  @Override public boolean hasExpired() {
    return false;
  }

  @Override public Iterable<Measurement> measure() {
    long now = clock.wallTime();
    double v = count.get();
    return Collections.singleton(new Measurement(id, now, v));
  }

  @Override public void add(double amount) {
    if (Double.isFinite(amount) && amount > 0.0) {
      count.addAndGet(amount);
    }
  }

  @Override public double actualCount() {
    return count.get();
  }
}
