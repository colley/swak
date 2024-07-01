
package com.swak.core.spectator.api.patterns;

import java.lang.ref.WeakReference;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Helper for polling gauges in a background thread. A shared executor is used with a
 * single thread. If registered gauge methods are cheap as they should be, then this
 * should be plenty of capacity to process everything regularly. If not, then this will
 * help limit the damage to a single core and avoid causing problems for the application.
 */
final class GaugePoller {

  private static final ThreadFactory FACTORY = new ThreadFactory() {
    private final AtomicInteger next = new AtomicInteger();

    @Override public Thread newThread(Runnable r) {
      final String name = "spectator-gauge-polling-" + next.getAndIncrement();
      final Thread t = new Thread(r, name);
      t.setDaemon(true);
      return t;
    }
  };

  private static final ScheduledExecutorService DEFAULT_EXECUTOR =
      Executors.newSingleThreadScheduledExecutor(FACTORY);

  static ScheduledFuture<?> schedule(long delay, Runnable task) {
    return DEFAULT_EXECUTOR.scheduleWithFixedDelay(task, delay, delay, TimeUnit.MILLISECONDS);
  }

  /** Schedule collection of gauges for a registry. */
  static <T> Future<?> schedule(WeakReference<T> ref, long delay, Consumer<T> poll) {
    return schedule(DEFAULT_EXECUTOR, ref, delay, poll);
  }

  /** Schedule collection of gauges for a registry. */
  @SuppressWarnings("PMD")
  static <T> Future<?> schedule(
      ScheduledExecutorService executor, WeakReference<T> ref, long delay, Consumer<T> poll) {
    final AtomicReference<Future<?>> futureRef = new AtomicReference<>();
    final Runnable cancel = () -> {
      Future<?> f = futureRef.get();
      if (f != null) {
        f.cancel(false);
      }
    };
    final Runnable task = () -> {
      try {
        T r = ref.get();
        if (r != null) {
          poll.accept(r);
        } else {
          cancel.run();
        }
      } catch (Throwable t) {
        cancel.run();
      }
    };
    futureRef.set(executor.scheduleWithFixedDelay(task, delay, delay, TimeUnit.MILLISECONDS));
    return futureRef.get();
  }

  private GaugePoller() {
  }
}
