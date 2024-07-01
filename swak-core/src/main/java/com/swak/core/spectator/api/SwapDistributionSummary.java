
package com.swak.core.spectator.api;

import com.swak.core.spectator.impl.SwapMeter;

import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/** Wraps another distribution summary allowing the underlying type to be swapped. */
final class SwapDistributionSummary extends SwapMeter<DistributionSummary> implements DistributionSummary {

  /** Create a new instance. */
  SwapDistributionSummary(
      Registry registry,
      LongSupplier versionSupplier,
      Id id,
      DistributionSummary underlying) {
    super(registry, versionSupplier, id, underlying);
  }

  @Override public DistributionSummary lookup() {
    return registry.distributionSummary(id);
  }

  @Override public void record(long amount) {
    get().record(amount);
  }

  @Override public void record(long[] amounts, int n) {
    get().record(amounts, n);
  }

  @Override
  public long count() {
    return get().count();
  }

  @Override
  public long totalAmount() {
    return get().totalAmount();
  }

  @SuppressWarnings("unchecked")
  @Override public BatchUpdater batchUpdater(int batchSize) {
    BatchUpdater updater = get().batchUpdater(batchSize);
    // Registry implementations can implement `Consumer<Supplier<DistributionSummary>>` to
    // allow the meter to be resolved when flushed and avoid needing to hold on to a particular
    // instance of the meter that might have expired and been removed from the registry.
    if (updater instanceof Consumer<?>) {
      ((Consumer<Supplier<DistributionSummary>>) updater).accept(this::get);
    }
    return updater;
  }
}
