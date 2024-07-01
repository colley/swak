
package com.swak.core.spectator.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Base class for composite implementations of core meter types.
 */
class CompositeMeter<T extends Meter> implements Meter {

  /** Identifier for the meter. */
  protected final Id id;

  /** Underlying meters that are keeping the data. */
  protected final Collection<T> meters;

  /**
   * Create a new instance.
   *
   * @param id
   *     Identifier for the meter.
   * @param meters
   *     Set of meters that make up the composite.
   */
  CompositeMeter(Id id, Collection<T> meters) {
    this.id = id;
    this.meters = meters;
  }

  @Override public Id id() {
    return this.id;
  }

  @Override public boolean hasExpired() {
    for (Meter m : meters) {
      if (m != null && !m.hasExpired()) return false;
    }
    return true;
  }

  @Override public Iterable<Measurement> measure() {
    final List<Measurement> ms = new ArrayList<>();
    for (Meter m : meters) {
      if (m != null) {
        for (Measurement measurement : m.measure()) {
          ms.add(measurement);
        }
      }
    }
    return ms;
  }
}
