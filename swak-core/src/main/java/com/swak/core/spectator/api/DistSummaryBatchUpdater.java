
package com.swak.core.spectator.api;

class DistSummaryBatchUpdater implements DistributionSummary.BatchUpdater {

  private final DistributionSummary distSummary;
  private final int batchSize;

  private int count;
  private final long[] amounts;

  DistSummaryBatchUpdater(DistributionSummary distSummary, int batchSize) {
    this.distSummary = distSummary;
    this.batchSize = batchSize;
    this.count = 0;
    this.amounts = new long[batchSize];
  }

  @Override
  public void record(long amount) {
    if (amount >= 0) {
      amounts[count++] = amount;
      if (count >= batchSize) {
        flush();
      }
    }
  }

  @Override
  public void flush() {
    distSummary.record(amounts, count);
    count = 0;
  }

  @Override
  public void close() throws Exception {
    flush();
  }
}
