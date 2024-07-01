
package com.swak.core.spectator.api;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/** Counter implementation for the no-op registry. */
enum NoopTimer implements Timer {
  /** Singleton instance. */
  INSTANCE;

  @Override public Id id() {
    return NoopId.INSTANCE;
  }

  @Override public boolean hasExpired() {
    return false;
  }

  @Override public void record(long amount, TimeUnit unit) {
  }

  @Override public Iterable<Measurement> measure() {
    return Collections.emptyList();
  }

  @Override public long count() {
    return 0L;
  }

  @Override public long totalTime() {
    return 0L;
  }
}
