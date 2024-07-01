
package com.swak.core.spectator.api;

/**
 * The valid set of statistics that can be reported by timers and distribution summaries.
 */
public enum Statistic implements Tag {
  /** A value sampled at a point in time. */
  gauge,

  /** Rate per second for calls to record. */
  count,

  /** The maximum amount recorded. */
  max,

  /** The sum of the amounts recorded. */
  totalAmount,

  /** The sum of the squares of the amounts recorded. */
  totalOfSquares,

  /** The sum of the times recorded. */
  totalTime,

  /** Number of currently active tasks for a long task timer. */
  activeTasks,

  /** Duration of a running task. */
  duration,

  /** Value used to compute a distributed percentile estimate. */
  percentile;

  @Override public String key() {
    return "statistic";
  }

  @Override public String value() {
    return name();
  }
}
