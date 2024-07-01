
package com.swak.core.spectator.api;

import java.util.concurrent.TimeUnit;

final class TimerBatchUpdater implements Timer.BatchUpdater {

  private final Timer timer;
  private final int batchSize;

  private int count;
  private final long[] amounts;

  TimerBatchUpdater(Timer timer, int batchSize) {
    this.timer = timer;
    this.batchSize = batchSize;
    this.count = 0;
    this.amounts = new long[batchSize];
  }

  @Override
  public void record(long amount, TimeUnit unit) {
    if (amount >= 0L) {
      amounts[count++] = unit.toNanos(amount);
      if (count >= batchSize) {
        flush();
      }
    }
  }

  @Override
  public void flush() {
    timer.record(amounts, count, TimeUnit.NANOSECONDS);
    count = 0;
  }

  @Override
  public void close() throws Exception {
    flush();
  }
}
