
package com.swak.core.spectator.api;

import java.util.Collections;

/** Distribution summary implementation for the no-op registry. */
enum NoopDistributionSummary implements DistributionSummary {

  /** Singleton instance. */
  INSTANCE;

  @Override public Id id() {
    return NoopId.INSTANCE;
  }

  @Override public boolean hasExpired() {
    return false;
  }

  @Override public void record(long amount) {
  }

  @Override public Iterable<Measurement> measure() {
    return Collections.emptyList();
  }

  @Override public long count() {
    return 0L;
  }

  @Override public long totalAmount() {
    return 0L;
  }
}
