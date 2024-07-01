
package com.swak.core.spectator.impl;

/**
 * Base for {@link StepLong} and {@link StepDouble}.
 */
public interface StepValue {

  /** Get the value for the last completed interval as a rate per second. */
  double pollAsRate();

  /** Get the value for the last completed interval as a rate per second. */
  double pollAsRate(long now);

  /** Get the timestamp for the end of the last completed interval. */
  long timestamp();
}
