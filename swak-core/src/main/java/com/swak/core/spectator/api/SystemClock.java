
package com.swak.core.spectator.api;

/**
 * Clock implementation that uses {@link System#currentTimeMillis()} and {@link System#nanoTime()}.
 * Implemented as an enum to that the clock instance will be serializable if using in environments
 * like Spark or Flink.
 */
enum SystemClock implements Clock {

  /** Singleton instance for the system clock. */
  INSTANCE;

  @Override public long wallTime() {
    return System.currentTimeMillis();
  }

  @Override public long monotonicTime() {
    return System.nanoTime();
  }
}
