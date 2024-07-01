
package com.swak.core.spectator.api.patterns;

import com.swak.core.spectator.api.*;

import java.util.Collections;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A counter that also keeps track of the time since last update.
 */
public final class IntervalCounter implements Counter {

  private static final double MILLIS_PER_SECOND = (double) TimeUnit.SECONDS.toMillis(1L);

  /**
   * Create a new instance.
   *
   * @param registry
   *    Registry to use.
   * @param id
   *    Identifier for the metric being registered.
   * @return
   *    Counter instance.
   */
  public static IntervalCounter get(Registry registry, Id id) {
    ConcurrentMap<Id, Object> state = registry.state();
    Object c = Utils.computeIfAbsent(state, id, i -> new IntervalCounter(registry, i));
    if (!(c instanceof IntervalCounter)) {
      Utils.propagateTypeError(registry, id, IntervalCounter.class, c.getClass());
      c = new IntervalCounter(new NoopRegistry(), id);
    }
    return (IntervalCounter) c;
  }

  private final Clock clock;
  private final Id id;
  private final Counter counter;
  private final AtomicLong lastUpdated;

  /**
   * Create a new IntervalCounter using the given registry and base id.
   */
  IntervalCounter(Registry registry, Id id) {
    this.clock = registry.clock();
    this.id = id;
    this.counter = registry.counter(id.withTag(Statistic.count));
    this.lastUpdated = PolledMeter.using(registry)
        .withId(id)
        .withTag(Statistic.duration)
        .monitorValue(new AtomicLong(0L), Functions.age(clock));
  }

  @Override public void add(double amount) {
    counter.add(amount);
    lastUpdated.set(clock.wallTime());
  }

  @Override public double actualCount() {
    return counter.actualCount();
  }

  @Override public Id id() {
    return id;
  }

  /**
   * Return the number of seconds since the last time the counter was incremented.
   */
  public double secondsSinceLastUpdate() {
    final long now = clock.wallTime();
    return  (now - lastUpdated.get()) / MILLIS_PER_SECOND;
  }

  @Override public Iterable<Measurement> measure() {
    return Collections.emptyList();
  }

  @Override public boolean hasExpired() {
    return false;
  }
}
