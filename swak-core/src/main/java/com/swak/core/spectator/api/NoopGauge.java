
package com.swak.core.spectator.api;

import java.util.Collections;

/** Gauge implementation for the no-op registry. */
enum NoopGauge implements Gauge {
  /** Singleton instance. */
  INSTANCE;

  @Override public Id id() {
    return NoopId.INSTANCE;
  }

  @Override public Iterable<Measurement> measure() {
    return Collections.emptyList();
  }

  @Override public boolean hasExpired() {
    return false;
  }

  @Override public void set(double v) {
  }

  @Override public double value() {
    return Double.NaN;
  }
}
