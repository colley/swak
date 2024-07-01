
package com.swak.core.spectator.impl;

import com.swak.core.spectator.api.Clock;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class for managing a set of AtomicLong instances mapped to a particular step interval.
 * The current implementation keeps an array of with two items where one is the current value
 * being updated and the other is the value from the previous interval and is only available for
 * polling.
 *
 * <p><b>This class is an internal implementation detail only intended for use within spectator.
 * It is subject to change without notice.</b></p>
 */
public class StepDouble implements StepValue {

  private final double init;
  private final Clock clock;
  private final long step;

  private volatile double previous;
  private final AtomicDouble current;

  private final AtomicLong lastInitPos;

  /** Create a new instance. */
  public StepDouble(double init, Clock clock, long step) {
    this.init = init;
    this.clock = clock;
    this.step = step;
    previous = init;
    current = new AtomicDouble(init);
    lastInitPos = new AtomicLong(clock.wallTime() / step);
  }

  private void rollCount(long now) {
    final long stepTime = now / step;
    final long lastInit = lastInitPos.get();
    if (lastInit < stepTime && lastInitPos.compareAndSet(lastInit, stepTime)) {
      final double v = current.getAndSet(init);
      // Need to check if there was any activity during the previous step interval. If there was
      // then the init position will move forward by 1, otherwise it will be older. No activity
      // means the previous interval should be set to the `init` value.
      previous = (lastInit == stepTime - 1) ? v : init;
    }
  }

  /** Get the AtomicDouble for the current bucket. */
  public AtomicDouble getCurrent() {
    return getCurrent(clock.wallTime());
  }

  /** Get the AtomicDouble for the current bucket. */
  public AtomicDouble getCurrent(long now) {
    rollCount(now);
    return current;
  }

  /** Get the value for the last completed interval. */
  public double poll() {
    return poll(clock.wallTime());
  }

  /** Get the value for the last completed interval. */
  public double poll(long now) {
    rollCount(now);
    return previous;
  }

  /** Get the value for the last completed interval as a rate per second. */
  @Override public double pollAsRate() {
    return pollAsRate(clock.wallTime());
  }

  /** Get the value for the last completed interval as a rate per second. */
  @Override public double pollAsRate(long now) {
    final double amount = poll(now);
    final double period = step / 1000.0;
    return amount / period;
  }

  /** Get the timestamp for the end of the last completed interval. */
  @Override public long timestamp() {
    return lastInitPos.get() * step;
  }

  @Override public String toString() {
    return "StepDouble{init="  + init
        + ", previous=" + previous
        + ", current=" + current.get()
        + ", lastInitPos=" + lastInitPos.get() + '}';
  }
}