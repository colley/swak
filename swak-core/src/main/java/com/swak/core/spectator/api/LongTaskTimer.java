
package com.swak.core.spectator.api;

/**
 * Timer intended to track a small number of long running tasks. Example would be something like
 * a batch hadoop job. Though "long running" is a bit subjective the assumption is that anything
 * over a minute is long running.
 */
public interface LongTaskTimer extends Meter {
  /**
   * Start keeping time for a task and return a task id that can be used to look up how long
   * it has been running.
   */
  long start();

  /**
   * Indicates that a given task has completed.
   *
   * @param task
   *     Id for the task to stop. This should be the value returned from {@link #start()}.
   * @return
   *     Duration for the task in nanoseconds. A -1 value will be returned for an unknown task.
   */
  long stop(long task);

  /**
   * Returns the current duration for a given active task.
   *
   * @param task
   *     Id for the task to stop. This should be the value returned from {@link #start()}.
   * @return
   *     Duration for the task in nanoseconds. A -1 value will be returned for an unknown task.
   */
  long duration(long task);

  /** Returns the cumulative duration of all current tasks in nanoseconds. */
  long duration();

  /** Returns the current number of tasks being executed. */
  int activeTasks();
}
