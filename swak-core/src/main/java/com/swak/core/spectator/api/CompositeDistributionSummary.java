
package com.swak.core.spectator.api;

import java.util.Collection;
import java.util.Iterator;

/** Distribution summary implementation for the composite registry. */
final class CompositeDistributionSummary extends CompositeMeter<DistributionSummary> implements DistributionSummary {

  /** Create a new instance. */
  CompositeDistributionSummary(Id id, Collection<DistributionSummary> summaries) {
    super(id, summaries);
  }

  @Override public void record(long amount) {
    for (DistributionSummary d : meters) {
      d.record(amount);
    }
  }

  @Override public long count() {
    Iterator<DistributionSummary> it = meters.iterator();
    return it.hasNext() ? it.next().count() : 0L;
  }

  @Override public long totalAmount() {
    Iterator<DistributionSummary> it = meters.iterator();
    return it.hasNext() ? it.next().totalAmount() : 0L;
  }
}
