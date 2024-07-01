
package com.swak.core.spectator.api;

/**
 * A meter with a single value that can only be sampled at a point in time. A typical example is
 * a queue size.
 */
public interface Gauge extends Meter {

  /**
   * Set the current value of the gauge.
   *
   * @param value
   *     Most recent measured value.
   */
  default void set(double value) {
    // For backwards compatibility with older versions of spectator prior to set being
    // required on the gauge. Default implementation should be removed in a future release.
  }

  /** Returns the current value. */
  double value();
}
