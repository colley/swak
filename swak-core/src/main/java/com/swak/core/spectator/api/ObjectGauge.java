
package com.swak.core.spectator.api;

import java.util.Collections;
import java.util.function.ToDoubleFunction;

/**
 * Gauge that is defined by executing a {@link ToDoubleFunction} on an object.
 */
class ObjectGauge<T> extends AbstractMeter<T> {

  private final ToDoubleFunction<T> f;

  /**
   * Create a gauge that samples the provided number for the value.
   *
   * @param clock
   *     Clock used for accessing the current time.
   * @param id
   *     Identifier for the gauge.
   * @param obj
   *     {@link Object} used to access the value.
   * @param f
   *     Function that is applied on the value for the number. The operation {@code f.apply(obj)}
   *     should be thread-safe.
   */
  ObjectGauge(Clock clock, Id id, T obj, ToDoubleFunction<T> f) {
    super(clock, id, obj);
    this.f = f;
  }

  @Override public Iterable<Measurement> measure() {
    return Collections.singleton(new Measurement(id, clock.wallTime(), value()));
  }

  /** Return the current value for evaluating `f` over `obj`. */
  double value() {
    final T obj = ref.get();
    return (obj == null) ? Double.NaN : f.applyAsDouble(obj);
  }
}
