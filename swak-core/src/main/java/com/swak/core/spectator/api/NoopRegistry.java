
package com.swak.core.spectator.api;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

/**
 * Registry implementation that does nothing. This is typically used to allow for performance tests
 * to see how much overhead is being added by instrumentation. This implementation tries to do the
 * minimum amount possible without requiring code changes for users.
 */
public final class NoopRegistry implements Registry {

  // Since we don't know how callers might be using this a noop implementation
  // of the map could cause unexpected issues.
  private final ConcurrentMap<Id, Object> state = new ConcurrentHashMap<>();

  @Override public Clock clock() {
    return Clock.SYSTEM;
  }

  @Override public Id createId(String name) {
    return NoopId.INSTANCE;
  }

  @Override public Id createId(String name, Iterable<Tag> tags) {
    return NoopId.INSTANCE;
  }

  @Override public Id createId(String name, String... tags) {
    return NoopId.INSTANCE;
  }

  @Override public Id createId(String name, Map<String, String> tags) {
    return NoopId.INSTANCE;
  }

  @Override public ConcurrentMap<Id, Object> state() {
    return state;
  }

  @Override public Counter counter(Id id) {
    return NoopCounter.INSTANCE;
  }

  @Override public DistributionSummary distributionSummary(Id id) {
    return NoopDistributionSummary.INSTANCE;
  }

  @Override public Timer timer(Id id) {
    return NoopTimer.INSTANCE;
  }

  @Override public Gauge gauge(Id id) {
    return NoopGauge.INSTANCE;
  }

  @Override public Gauge maxGauge(Id id) {
    return NoopGauge.INSTANCE;
  }

  @Override public Meter get(Id id) {
    return null;
  }

  @Override public Iterator<Meter> iterator() {
    return Collections.emptyIterator();
  }

  @Override public Spliterator<Meter> spliterator() {
    return Spliterators.emptySpliterator();
  }

  @Override public Stream<Measurement> measurements() {
    return Stream.empty();
  }

  @Override public Stream<Meter> stream() {
    return Stream.empty();
  }

  @Override public Stream<Counter> counters() {
    return Stream.empty();
  }

  @Override public Stream<DistributionSummary> distributionSummaries() {
    return Stream.empty();
  }

  @Override public Stream<Timer> timers() {
    return Stream.empty();
  }

  @Override public Stream<Gauge> gauges() {
    return Stream.empty();
  }

  @Override public void propagate(String msg, Throwable t) {
    // Since everything will have the same id when using this registry, it can result
    // in type errors when checking against the state, see #503.
  }

  @Override public void propagate(Throwable t) {
  }
}
