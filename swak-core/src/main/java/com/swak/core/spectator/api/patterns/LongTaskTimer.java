
package com.swak.core.spectator.api.patterns;

import com.swak.core.spectator.api.*;
import com.swak.core.spectator.impl.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Timer intended to track a small number of long running tasks. Example would be something like
 * a batch hadoop job. Though "long running" is a bit subjective the assumption is that anything
 * over a minute is long running.
 */
public final class LongTaskTimer implements com.swak.core.spectator.api.LongTaskTimer {

  /**
   * Creates a timer for tracking long running tasks.
   *
   * @param registry
   *     Registry to use.
   * @param id
   *     Identifier for the metric being registered.
   * @return
   *     Timer instance.
   */
  public static LongTaskTimer get(Registry registry, Id id) {
    Preconditions.checkNotNull(registry, "registry");
    Preconditions.checkNotNull(id, "id");
    ConcurrentMap<Id, Object> state = registry.state();
    Object obj = Utils.computeIfAbsent(state, id, i -> {
      LongTaskTimer timer = new LongTaskTimer(registry, id);
      PolledMeter.using(registry)
          .withId(id)
          .withTag(Statistic.activeTasks)
          .monitorValue(timer, LongTaskTimer::activeTasks);
      PolledMeter.using(registry)
          .withId(id)
          .withTag(Statistic.duration)
          .monitorValue(timer, t -> t.duration() / NANOS_PER_SECOND);
      return timer;
    });
    if (!(obj instanceof LongTaskTimer)) {
      Utils.propagateTypeError(registry, id, LongTaskTimer.class, obj.getClass());
      obj = new LongTaskTimer(new NoopRegistry(), id);
    }
    return (LongTaskTimer) obj;
  }

  private static final double NANOS_PER_SECOND = (double) TimeUnit.SECONDS.toNanos(1L);

  private final Clock clock;
  private final Id id;
  private final ConcurrentMap<Long, Long> tasks = new ConcurrentHashMap<>();
  private final AtomicLong nextTask = new AtomicLong(0L);

  /** Create a new instance. */
  private LongTaskTimer(Registry registry, Id id) {
    this.clock = registry.clock();
    this.id = id;
  }

  @Override public Id id() {
    return id;
  }

  @Override public boolean hasExpired() {
    return false;
  }

  @Override public long start() {
    long task = nextTask.getAndIncrement();
    tasks.put(task, clock.monotonicTime());
    return task;
  }

  @Override public long stop(long task) {
    Long startTime = tasks.get(task);
    if (startTime != null) {
      tasks.remove(task);
      return clock.monotonicTime() - startTime;
    } else {
      return -1L;
    }
  }

  @Override public long duration(long task) {
    Long startTime = tasks.get(task);
    return (startTime != null) ? (clock.monotonicTime() - startTime) : -1L;
  }

  @Override public long duration() {
    long now = clock.monotonicTime();
    long sum = 0L;
    for (long startTime : tasks.values()) {
      sum += now - startTime;
    }
    return sum;
  }

  @Override public int activeTasks() {
    return tasks.size();
  }

  @Override public Iterable<Measurement> measure() {
    final List<Measurement> ms = new ArrayList<>(2);
    final long now = clock.wallTime();
    final double durationSeconds = duration() / NANOS_PER_SECOND;
    ms.add(new Measurement(id.withTag(Statistic.duration), now, durationSeconds));
    ms.add(new Measurement(id.withTag(Statistic.activeTasks), now, activeTasks()));
    return ms;
  }
}
