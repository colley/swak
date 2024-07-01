
package com.swak.core.spectator.api;

final class CounterBatchUpdater implements Counter.BatchUpdater {

  private final Counter counter;
  private final int batchSize;

  private int count;
  private double sum;

  CounterBatchUpdater(Counter counter, int batchSize) {
    this.counter = counter;
    this.batchSize = batchSize;
    this.count = 0;
    this.sum = 0.0;
  }

  @Override
  public void add(double amount) {
    if (Double.isFinite(amount) && amount > 0.0) {
      sum += amount;
      ++count;
      if (count >= batchSize) {
        flush();
      }
    }
  }

  @Override
  public void flush() {
    counter.add(sum);
    sum = 0.0;
    count = 0;
  }

  @Override
  public void close() throws Exception {
    flush();
  }
}
