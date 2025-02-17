
package com.swak.core.spectator.impl;

import com.swak.core.spectator.api.*;
import com.swak.core.spectator.api.patterns.PolledMeter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p><b>This class is an internal implementation detail only intended for use within spectator.
 * It is subject to change without notice.</b></p>
 *
 * <p>Simple scheduler for recurring tasks based on a fixed size thread pool. This
 * class is mostly intended for running short lived tasks at a regular interval.</p>
 *
 * <p><b>Usage</b></p>
 *
 * <pre>
 * Scheduler scheduler = new Scheduler(registry, "spectator-polling", 2);
 *
 * Scheduler.Options options = new Scheduler.Options()
 *   .withFrequency(Scheduler.Policy.FIXED_RATE_SKIP_IF_LONG, Duration.ofSeconds(10));
 * scheduler.schedule(options, () -> doWork());
 * </pre>
 *
 * <p><b>Metrics</b></p>
 *
 * The following metrics can be used to monitor the behavior of the scheduler:
 *
 * <ul>
 *   <li><code>spectator.scheduler.queueSize</code>: gauge reporting the number of
 *       items in the queue. Note, that for repeating tasks the items will almost
 *       always be in queue except during execution.</li>
 *   <li><code>spectator.scheduler.poolSize</code>: gauge reporting the number of
 *       threads available in the pool.</li>
 *   <li><code>spectator.scheduler.activeThreads</code>: gauge reporting the number of
 *       threads that are currently executing a task.</li>
 *   <li><code>spectator.scheduler.taskExecutionTime</code>: timer reporting the
 *       execution time of an individual task.</li>
 *   <li><code>spectator.scheduler.taskExecutionDelay</code>: timer reporting the
 *       delay between the desired execution time of a task and when it was actually
 *       executed. A high execution delay means that the scheduler cannot keep up
 *       with the amount of work. This might indicate more threads are needed.</li>
 *   <li><code>spectator.scheduler.skipped</code>: counter reporting the number of
 *       executions that were skipped because the task did not complete before the
 *       next scheduled execution time.</li>
 *   <li><code>spectator.scheduler.uncaughtExceptions</code>: counter reporting the
 *       number of times an exception is propagated out from a task.</li>
 * </ul>
 *
 * All metrics with have an {@code id} dimension to distinguish a particular scheduler
 * instance.
 */
public class Scheduler {

  /**
   * Create a thread factory using thread names based on the id. All threads will
   * be configured as daemon threads.
   */
  private static ThreadFactory newThreadFactory(final String id) {
    return new ThreadFactory() {
      private final AtomicInteger next = new AtomicInteger();

      @Override public Thread newThread(Runnable r) {
        final String name = "spectator-" + id + "-" + next.getAndIncrement();
        final Thread t = new Thread(r, name);
        t.setDaemon(true);
        return t;
      }
    };
  }

  private static Id newId(Registry registry, String id, String name) {
    return registry.createId("spectator.scheduler." + name, "id", id);
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);

  private final DelayQueue<DelayedTask> queue = new DelayQueue<>();

  private final Clock clock;

  private final Stats stats;

  private final ThreadFactory factory;
  private final Thread[] threads;

  private final Lock lock = new ReentrantLock();

  private volatile boolean started = false;
  private volatile boolean shutdown = false;

  /**
   * Create a new instance.
   *
   * @param registry
   *     Registry to use for collecting metrics. The clock from the registry will also be
   *     used as the clock source for accessing the time.
   * @param id
   *     Id for this instance of the scheduler. Used to distinguish between instances of
   *     the scheduler for metrics and thread names. Threads will be named as
   *     {@code spectator-$id-$i}.
   * @param poolSize
   *     Number of threads to have in the pool. The threads will not be started until the
   *     first task is scheduled.
   */
  public Scheduler(Registry registry, String id, int poolSize) {
    this.clock = registry.clock();

    PolledMeter.using(registry)
        .withId(newId(registry, id, "queueSize"))
        .monitorSize(queue);
    stats = new Stats(registry, id);

    this.factory = newThreadFactory(id);
    this.threads = new Thread[poolSize];
  }

  /**
   * Schedule a repetitive task.
   *
   * @param options
   *     Options for controlling the execution of the task. See {@link Options}
   *     for more information.
   * @param task
   *     Task to execute.
   * @return
   *     Future that can be used for cancelling the current and future executions of
   *     the task. There is no value associated with the task so the future is just for
   *     checking if it is still running to stopping it from running in the future.
   */
  public ScheduledFuture<?> schedule(Options options, Runnable task) {
    if (!started) {
      startThreads();
    }
    DelayedTask t = new DelayedTask(clock, options, task);
    queue.put(t);
    return t;
  }

  /**
   * Shutdown and cleanup resources associated with the scheduler. All threads will be
   * interrupted, but this method does not block for them to all finish execution.
   */
  public void shutdown() {
    lock.lock();
    try {
      shutdown = true;
      for (int i = 0; i < threads.length; ++i) {
        if (threads[i] != null && threads[i].isAlive()) {
          threads[i].interrupt();
          threads[i] = null;
        }
      }
    } finally {
      lock.unlock();
    }
  }

  private void startThreads() {
    lock.lock();
    try {
      if (!shutdown) {
        started = true;
        for (int i = 0; i < threads.length; ++i) {
          if (threads[i] == null || !threads[i].isAlive() || threads[i].isInterrupted()) {
            threads[i] = factory.newThread(new Worker());
            threads[i].start();
            LOGGER.debug("started thread {}", threads[i].getName());
          }
        }
      }
    } finally {
      lock.unlock();
    }
  }

  /** Repetition schedulingPolicy for scheduled tasks. */
  public enum Policy {
    /** Run a task once. */
    RUN_ONCE,

    /** Run a task repeatedly using a fixed delay between executions. */
    FIXED_DELAY,

    /**
     * Run a task repeatedly attempting to maintain a consistent rate of execution.
     * If the execution time is less than the desired frequencyMillis, then the start times
     * will be at a consistent interval. If the execution time exceeds the frequencyMillis,
     * then some executions will be skipped.
     *
     * The primary use case for this mode is when we want to maintain a consistent
     * frequencyMillis, but want to avoid queuing up many tasks if the system cannot keep
     * up. Fixed delay is often inappropriate because for the normal case it will
     * drift by the execution time of the task.
     */
    FIXED_RATE_SKIP_IF_LONG
  }

  /** Options to control how a task will get executed. */
  public static class Options {
    private Policy schedulingPolicy = Policy.RUN_ONCE;
    private long initialDelay = 0L;
    private long frequencyMillis = 0L;
    private boolean stopOnFailure = false;

    /**
     * How long to wait after a task has been scheduled to the first execution. If
     * not set, then it will be scheduled immediately.
     */
    public Options withInitialDelay(Duration delay) {
      initialDelay = delay.toMillis();
      return this;
    }

    /**
     * Configure the task to execute repeatedly.
     *
     * @param policy
     *     Repetition schedulingPolicy to use for the task. See {@link Policy} for the
     *     supported options.
     * @param frequency
     *     How frequently to repeat the execution. The interpretation of this
     *     parameter will depend on the {@link Policy}.
     */
    public Options withFrequency(Policy policy, Duration frequency) {
      this.schedulingPolicy = policy;
      this.frequencyMillis = frequency.toMillis();
      return this;
    }

    /**
     * Should a repeated task stop executing if an exception propagates out of
     * the task? Defaults to false.
     */
    public Options withStopOnFailure(boolean flag) {
      this.stopOnFailure = flag;
      return this;
    }
  }

  /**
   * Collection of stats that are updated as part of executing the tasks.
   */
  static class Stats {

    private final Registry registry;
    private final AtomicInteger activeCount;
    private final Timer taskExecutionTime;
    private final Timer taskExecutionDelay;
    private final Counter skipped;
    private final Id uncaughtExceptionsId;

    /** Create a new instance. */
    Stats(Registry registry, String id) {
      this.registry = registry;
      activeCount = PolledMeter.using(registry)
          .withId(newId(registry, id, "activeThreads"))
          .monitorValue(new AtomicInteger());
      taskExecutionTime = registry.timer(newId(registry, id, "taskExecutionTime"));
      taskExecutionDelay = registry.timer(newId(registry, id, "taskExecutionDelay"));
      skipped = registry.counter(newId(registry, id, "skipped"));
      uncaughtExceptionsId = newId(registry, id, "uncaughtExceptions");
    }

    /** Increment the number of active tasks. */
    void incrementActiveTaskCount() {
      activeCount.incrementAndGet();
    }

    /** Decrement the number of active tasks. */
    void decrementActiveTaskCount() {
      activeCount.decrementAndGet();
    }

    /** Timer for measuring the execution time of the task. */
    Timer taskExecutionTime() {
      return taskExecutionTime;
    }

    /**
     * Timer for measuring the delay for the task. This should be close to zero, but if
     * the system is overloaded or having trouble, then there might be a large delay.
     */
    Timer taskExecutionDelay() {
      return taskExecutionDelay;
    }

    /**
     * Counter that will be incremented each time an expected execution is
     * skipped when using {@link Policy#FIXED_RATE_SKIP_IF_LONG}.
     */
    Counter skipped() {
      return skipped;
    }

    /**
     * Increment the uncaught exception counter tagged with simple class name of the
     * exception.
     */
    void incrementUncaught(Throwable t) {
      final String cls = t.getClass().getSimpleName();
      registry.counter(uncaughtExceptionsId.withTag("exception", cls)).increment();
    }
  }

  /**
   * Wraps the user supplied task with metadata for subsequent executions.
   */
  static class DelayedTask implements ScheduledFuture<Void> {
    private final Clock clock;
    private final Options options;
    private final Runnable task;

    private final long initialExecutionTime;
    private long nextExecutionTime;

    private volatile Thread thread = null;
    private volatile boolean cancelled = false;

    /**
     * Create a new instance.
     *
     * @param clock
     *     Clock for computing the next execution time for the task.
     * @param options
     *     Options for how to repeat the execution.
     * @param task
     *     User specified task to execute.
     */
    DelayedTask(Clock clock, Options options, Runnable task) {
      this.clock = clock;
      this.options = options;
      this.task = task;
      this.initialExecutionTime = clock.wallTime() + options.initialDelay;
      this.nextExecutionTime = initialExecutionTime;
    }

    /** Returns the next scheduled execution time. */
    long getNextExecutionTime() {
      return nextExecutionTime;
    }

    /**
     * Update the next execution time based on the options for this task.
     *
     * @param skipped
     *     Counter that will be incremented each time an expected execution is
     *     skipped when using {@link Policy#FIXED_RATE_SKIP_IF_LONG}.
     */
    void updateNextExecutionTime(Counter skipped) {
      switch (options.schedulingPolicy) {
        case FIXED_DELAY:
          nextExecutionTime = clock.wallTime() + options.frequencyMillis;
          break;
        case FIXED_RATE_SKIP_IF_LONG:
          final long now = clock.wallTime();
          nextExecutionTime += options.frequencyMillis;
          while (nextExecutionTime < now) {
            nextExecutionTime += options.frequencyMillis;
            skipped.increment();
          }
          break;
        default:
          break;
      }
    }

    /**
     * Execute the task and if reschedule another execution.
     *
     * @param queue
     *     Queue for the pool. This task will be added to the queue to schedule
     *     future executions.
     * @param stats
     *     Handle to stats that should be updated based on the execution of the
     *     task.
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    void runAndReschedule(DelayQueue<DelayedTask> queue, Stats stats) {
      thread = Thread.currentThread();
      boolean scheduleAgain = options.schedulingPolicy != Policy.RUN_ONCE;
      try {
        if (!isDone()) {
          task.run();
        }
      } catch (Throwable t) {
        // This catches Throwable because we cannot control the task and thus cannot
        // ensure it is well behaved with respect to exceptions.
        LOGGER.warn("task execution failed", t);
        stats.incrementUncaught(t);
        scheduleAgain = !options.stopOnFailure;
      } finally {
        thread = null;
        if (scheduleAgain && !isDone()) {
          updateNextExecutionTime(stats.skipped());
          queue.put(this);
        } else {
          cancelled = true;
        }
      }
    }

    @Override public long getDelay(TimeUnit unit) {
      final long delayMillis = Math.max(nextExecutionTime - clock.wallTime(), 0L);
      return unit.convert(delayMillis, TimeUnit.MILLISECONDS);
    }

    @Override public int compareTo(Delayed other) {
      final long d1 = getDelay(TimeUnit.MILLISECONDS);
      final long d2 = other.getDelay(TimeUnit.MILLISECONDS);
      return Long.compare(d1, d2);
    }

    @Override public boolean cancel(boolean mayInterruptIfRunning) {
      cancelled = true;
      Thread t = thread;
      if (mayInterruptIfRunning && t != null) {
        t.interrupt();
      }
      return true;
    }

    @Override public boolean isCancelled() {
      return cancelled;
    }

    @Override public boolean isDone() {
      return cancelled;
    }

    @Override public Void get() throws InterruptedException, ExecutionException {
      throw new UnsupportedOperationException();
    }

    @Override public Void get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Actual task running in the threads. It will block on trying to get a task to
   * execute from the queue until a task is ready.
   */
  private final class Worker implements Runnable {
    @Override public void run() {
      try {
        // Note: do not use Thread.interrupted() because it will clear the interrupt
        // status of the thread.
        while (!Thread.currentThread().isInterrupted()) {
          try {
            DelayedTask task = queue.take();
            stats.incrementActiveTaskCount();

            final long delay = clock.wallTime() - task.getNextExecutionTime();
            stats.taskExecutionDelay().record(delay, TimeUnit.MILLISECONDS);

            stats.taskExecutionTime().recordRunnable(() -> task.runAndReschedule(queue, stats));
          } catch (InterruptedException e) {
            LOGGER.debug("task interrupted", e);
            break;
          } finally {
            stats.decrementActiveTaskCount();
          }
        }
      } finally {
        startThreads();
      }
    }
  }
}
