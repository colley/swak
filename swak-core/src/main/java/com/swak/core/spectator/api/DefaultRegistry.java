
package com.swak.core.spectator.api;

/** Default implementation of registry. */
public final class DefaultRegistry extends AbstractRegistry {

  /** Create a new instance. */
  public DefaultRegistry() {
    this(Clock.SYSTEM);
  }

  /** Create a new instance. */
  public DefaultRegistry(Clock clock) {
    super(clock);
  }

  /** Create a new instance. */
  public DefaultRegistry(Clock clock, RegistryConfig config) {
    super(clock, config);
  }

  @Override protected Counter newCounter(Id id) {
    return new DefaultCounter(clock(), id);
  }

  @Override protected DistributionSummary newDistributionSummary(Id id) {
    return new DefaultDistributionSummary(clock(), id);
  }

  @Override protected Timer newTimer(Id id) {
    return new DefaultTimer(clock(), id);
  }

  @Override protected Gauge newGauge(Id id) {
    return new DefaultGauge(clock(), id);
  }

  @Override protected Gauge newMaxGauge(Id id) {
    return new DefaultMaxGauge(clock(), id);
  }

  /**
   * Reset the state of this registry. All meters and other associated state will be lost.
   * Though it is typically recommended to use a new instance for each test, if that is not
   * possible for some reason, this method can be used to reset the state before a given
   * unit test.
   */
  @SuppressWarnings("PMD.UselessOverridingMethod")
  @Override public void reset() {
    // Overridden to increase visibility from protected in base class to public
    super.reset();
  }
}
