
package com.swak.core.spectator.api;

/**
 * A measurement sampled from a meter.
 */
public final class Measurement {

  private final Id id;
  private final long timestamp;
  private final double value;

  /** Create a new instance. */
  public Measurement(Id id, long timestamp, double value) {
    this.id = id;
    this.timestamp = timestamp;
    this.value = value;
  }

  /** Identifier for the measurement. */
  public Id id() {
    return id;
  }

  /**
   * The timestamp in milliseconds since the epoch for when the measurement was taken.
   */
  public long timestamp() {
    return timestamp;
  }

  /** Value for the measurement. */
  public double value() {
    return value;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || !(obj instanceof Measurement)) return false;
    Measurement other = (Measurement) obj;
    return id.equals(other.id)
      && timestamp == other.timestamp
      && Double.compare(value, other.value) == 0;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int hc = prime;
    hc = prime * hc + id.hashCode();
    hc = prime * hc + Long.valueOf(timestamp).hashCode();
    hc = prime * hc + Double.valueOf(value).hashCode();
    return hc;
  }

  @Override
  public String toString() {
    return "Measurement(" + id.toString() + "," + timestamp + "," + value + ")";
  }
}
