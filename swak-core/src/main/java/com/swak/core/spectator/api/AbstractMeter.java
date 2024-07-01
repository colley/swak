
package com.swak.core.spectator.api;

import java.lang.ref.WeakReference;

/**
 * Helper base class for meters that maintains a weak reference to the object being measured.
 * Meters that are based on polling rather than activity need some other way to define the
 * lifespan and in this case that is if the reference still exists. Basing this off a weak
 * reference means there doesn't need to be an explicit de-registration and registering to
 * collect insight will not prevent garbage collection of the underlying object.
 */
public abstract class AbstractMeter<T> implements Meter {
  /** Clock to use for getting measurement timestamps. */
  protected final Clock clock;

  /** Identifier for the meter. */
  protected final Id id;

  /** Reference to the underlying object used to compute the measurements. */
  protected final WeakReference<T> ref;

  /**
   * Create a new instance.
   *
   * @param clock
   *     Clock to use for getting measurement timestamps. Typically should be the clock used by
   *     the registry (see {@link Registry#clock()}).
   * @param id
   *     Identifier for the meter.
   * @param obj
   *     Reference to the underlying object used to compute the measurements.
   */
  public AbstractMeter(Clock clock, Id id, T obj) {
    this.clock = clock;
    this.id = id;
    this.ref = new WeakReference<>(obj);
  }

  @Override public Id id() {
    return this.id;
  }

  @Override public boolean hasExpired() {
    return this.ref.get() == null;
  }
}
