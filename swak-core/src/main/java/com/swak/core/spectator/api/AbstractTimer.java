
package com.swak.core.spectator.api;

/**
 * Base class to simplify implementing a {@link Timer}.
 */
public abstract class AbstractTimer implements Timer {

  /** Clock to use for measuring the time of calls. */
  protected final Clock clock;

  /** Create a new instance. */
  public AbstractTimer(Clock clock) {
    this.clock = clock;
  }

  @Override public Clock clock() {
    return clock;
  }
}
