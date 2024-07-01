
package com.swak.core.spectator.api;

import java.util.Collections;

/** Counter implementation for the no-op registry. */
enum NoopCounter implements Counter {
  /** Singleton instance. */
  INSTANCE;

  @Override public Id id() {
    return NoopId.INSTANCE;
  }

  @Override public boolean hasExpired() {
    return false;
  }

  @Override public void add(double amount) {
  }

  @Override public Iterable<Measurement> measure() {
    return Collections.emptyList();
  }

  @Override public double actualCount() {
    return 0.0;
  }
}
