
package com.swak.core.spectator.api;

import com.swak.core.spectator.impl.SwapMeter;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/** Wraps another timer allowing the underlying type to be swapped. */
final class SwapTimer extends SwapMeter<Timer> implements Timer {

  /** Create a new instance. */
  SwapTimer(Registry registry, LongSupplier versionSupplier, Id id, Timer underlying) {
    super(registry, versionSupplier, id, underlying);
  }

  @Override public Timer lookup() {
    return registry.timer(id);
  }

  @Override public Clock clock() {
    return registry.clock();
  }

  @Override public void record(long amount, TimeUnit unit) {
    get().record(amount, unit);
  }

  @Override public long count() {
    return get().count();
  }

  @Override public long totalTime() {
    return get().totalTime();
  }

  @SuppressWarnings("unchecked")
  @Override public BatchUpdater batchUpdater(int batchSize) {
    BatchUpdater updater = get().batchUpdater(batchSize);
    // Registry implementations can implement `Consumer<Supplier<Timer>>` to allow the
    // meter to be resolved when flushed and avoid needing to hold on to a particular
    // instance of the meter that might have expired and been removed from the registry.
    if (updater instanceof Consumer<?>) {
      ((Consumer<Supplier<Timer>>) updater).accept(this::get);
    }
    return updater;
  }
}
