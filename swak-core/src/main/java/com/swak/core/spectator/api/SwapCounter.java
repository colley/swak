
package com.swak.core.spectator.api;

import com.swak.core.spectator.impl.SwapMeter;

import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/** Wraps another counter allowing the underlying type to be swapped. */
final class SwapCounter extends SwapMeter<Counter> implements Counter {

  /** Create a new instance. */
  SwapCounter(Registry registry, LongSupplier versionSupplier, Id id, Counter underlying) {
    super(registry, versionSupplier, id, underlying);
  }

  @Override public Counter lookup() {
    return registry.counter(id);
  }

  @Override public void add(double amount) {
    get().add(amount);
  }

  @Override public double actualCount() {
    return get().actualCount();
  }

  @SuppressWarnings("unchecked")
  @Override public BatchUpdater batchUpdater(int batchSize) {
    BatchUpdater updater = get().batchUpdater(batchSize);
    // Registry implementations can implement `Consumer<Supplier<Counter>>` to allow the
    // meter to be resolved when flushed and avoid needing to hold on to a particular
    // instance of the meter that might have expired and been removed from the registry.
    if (updater instanceof Consumer<?>) {
      ((Consumer<Supplier<Counter>>) updater).accept(this::get);
    }
    return updater;
  }
}
