
package com.swak.core.spectator.api;

import com.swak.core.spectator.impl.AtomicDouble;

import java.util.Collections;

/** Max gauge implementation for the default registry. */
class DefaultMaxGauge implements Gauge {

  private final Clock clock;
  private final Id id;
  private final AtomicDouble value;

  /** Create a new instance. */
  DefaultMaxGauge(Clock clock, Id id) {
    this.clock = clock;
    this.id = id;
    this.value = new AtomicDouble(Double.NaN);
  }

  @Override public Id id() {
    return id;
  }

  @Override public Iterable<Measurement> measure() {
    final Measurement m = new Measurement(id, clock.wallTime(), value());
    return Collections.singletonList(m);
  }

  @Override public boolean hasExpired() {
    return false;
  }

  @Override public void set(double v) {
    value.max(v);
  }

  @Override public double value() {
    return value.get();
  }
}
