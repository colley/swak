
package com.swak.core.spectator.api;

import com.swak.core.spectator.impl.SwapMeter;

import java.util.function.LongSupplier;

/** Wraps another gauge allowing the underlying type to be swapped. */
final class SwapGauge extends SwapMeter<Gauge> implements Gauge {

  /** Create a new instance. */
  SwapGauge(Registry registry, LongSupplier versionSupplier, Id id, Gauge underlying) {
    super(registry, versionSupplier, id, underlying);
  }

  @Override public Gauge lookup() {
    return registry.gauge(id);
  }

  @Override public void set(double value) {
    get().set(value);
  }

  @Override public double value() {
    return get().value();
  }
}
