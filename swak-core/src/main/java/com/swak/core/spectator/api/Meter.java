
package com.swak.core.spectator.api;

/**
 * A device for collecting a set of measurements. Note, this interface is only intended to be
 * implemented by registry implementations.
 */
public interface Meter {

  /**
   * Identifier used to lookup this meter in the registry.
   */
  Id id();

  /**
   * Get the set of measurements for this meter.
   */
  Iterable<Measurement> measure();

  /**
   * Indicates whether the meter is expired. For example, a counter might expire if there is no
   * activity within a given time frame.
   */
  boolean hasExpired();
}
