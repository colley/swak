
package com.swak.core.spectator.api;

/**
 * Measures the rate of change based on calls to increment.
 */
public interface Counter extends Meter {
  /** Update the counter by one. */
  default void increment() {
    add(1.0);
  }

  /**
   * Update the counter by {@code amount}.
   *
   * @param amount
   *     Amount to add to the counter.
   */
  default void increment(long amount) {
    add(amount);
  }

  /** Update the counter by the specified amount. */
  void add(double amount);

  /**
   * The cumulative count since this counter was last reset. How often a counter
   * is reset depends on the underlying registry implementation.
   */
  default long count() {
    return (long) actualCount();
  }

  /**
   * The cumulative count as a floating point value since this counter was last reset. How
   * often a counter is reset depends on the underlying registry implementation.
   */
  double actualCount();

  /**
   * Returns a helper that can be used to more efficiently update the counter within a
   * single thread. For example, if you need to update a meter within a loop where the
   * rest of the loop body is fairly cheap, the instrumentation code may add considerable
   * overhead if done in the loop body. A batched updater can offset a fair amount of that
   * cost, but the updates may be delayed a bit in reaching the meter. The updates will only
   * be seen after the updater is explicitly flushed.
   *
   * The caller should ensure that the updater is closed after using to guarantee any resources
   * associated with it are cleaned up. In some cases failure to close the updater could result
   * in a memory leak.
   *
   * @param batchSize
   *     Number of updates to batch before forcing a flush to the meter.
   * @return
   *     Batch updater implementation for this meter.
   */
  default BatchUpdater batchUpdater(int batchSize) {
    return new CounterBatchUpdater(this, batchSize);
  }

  /** See {@link #batchUpdater(int)}. */
  interface BatchUpdater extends AutoCloseable {
    /** Update the counter by one. */
    default void increment() {
      add(1.0);
    }

    /**
     * Update the counter by {@code amount}.
     *
     * @param amount
     *     Amount to add to the counter.
     */
    default void increment(long amount) {
      add(amount);
    }

    /** Update the counter by the specified amount. */
    void add(double amount);

    /** Push updates to the associated counter. */
    void flush();
  }
}
