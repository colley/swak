
package com.swak.core.spectator.api;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Clock implementation that allows the user to explicitly control the time. Typically used for
 * unit tests.
 */
public class ManualClock implements Clock {

  private final AtomicLong wall;
  private final AtomicLong monotonic;

  /** Create a new instance. */
  public ManualClock() {
    this(0L, 0L);
  }

  /**
   * Create a new instance.
   *
   * @param wallInit
   *     Initial value for the wall time.
   * @param monotonicInit
   *     Initial value for the monotonic time.
   */
  public ManualClock(long wallInit, long monotonicInit) {
    wall = new AtomicLong(wallInit);
    monotonic = new AtomicLong(monotonicInit);
  }

  @Override public long wallTime() {
    return wall.get();
  }

  @Override public long monotonicTime() {
    return monotonic.get();
  }

  /** Set the wall time to the value {@code t}. */
  public void setWallTime(long t) {
    wall.set(t);
  }

  /** Set the monotonic time to the value {@code t}. */
  public void setMonotonicTime(long t) {
    monotonic.set(t);
  }
}
