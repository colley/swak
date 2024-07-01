
package com.swak.core.spectator.api;

import com.swak.core.spectator.api.patterns.PolledMeter;
import com.swak.core.spectator.impl.Config;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Registry to manage a set of meters.
 */
public interface Registry extends Iterable<Meter> {

  /**
   * The clock used by the registry for timing events.
   */
  Clock clock();

  /**
   * Configuration settings used for this registry.
   */
  default RegistryConfig config() {
    return Config.defaultConfig();
  }

  /**
   * Creates an identifier for a meter. All ids passed into other calls should be created by the
   * registry.
   *
   * @param name
   *     Description of the measurement that is being collected.
   */
  Id createId(String name);

  /**
   * Creates an identifier for a meter. All ids passed into other calls should be created by the
   * registry.
   *
   * @param name
   *     Description of the measurement that is being collected.
   * @param tags
   *     Other dimensions that can be used to classify the measurement.
   */
  Id createId(String name, Iterable<Tag> tags);


  /**
   * Returns a map that can be used to associate state with the registry. Users instrumenting
   * their application will most likely never need to use this method.
   *
   * The primary use case is for building custom meter types that need some additional state
   * beyond the core types supported by the registry. This map can be used to store the state
   * so that the lifecycle of the data is connected to the registry. For an example, see some
   * of the built in patterns such as {@link com.swak.core.spectator.api.patterns.LongTaskTimer}.
   */
  ConcurrentMap<Id, Object> state();

  /**
   * Measures the rate of some activity. A counter is for continuously incrementing sources like
   * the number of requests that are coming into a server.
   *
   * @param id
   *     Identifier created by a call to {@link #createId}
   */
  Counter counter(Id id);

  /**
   * Measures the rate and variation in amount for some activity. For example, it could be used to
   * get insight into the variation in response sizes for requests to a server.
   *
   * @param id
   *     Identifier created by a call to {@link #createId}
   */
  DistributionSummary distributionSummary(Id id);

  /**
   * Measures the rate and time taken for short running tasks.
   *
   * @param id
   *     Identifier created by a call to {@link #createId}
   */
  Timer timer(Id id);

  /**
   * Represents a value sampled from another source. For example, the size of queue. The caller
   * is responsible for sampling the value regularly and calling {@link Gauge#set(double)}.
   * Registry implementations are free to expire the gauge if it has not been updated in the
   * last minute. If you do not want to worry about the sampling, then use {@link PolledMeter}
   * instead.
   *
   * @param id
   *     Identifier created by a call to {@link #createId}
   */
  Gauge gauge(Id id);

  /**
   * Measures the maximum value recorded since the last reset. For example, to measure the
   * maximum number of concurrent requests to a service. In many cases it is better to use
   * a {@link #distributionSummary(Id)} which provides a max along with other stats for most
   * registry implementations.
   *
   * @param id
   *     Identifier created by a call to {@link #createId}
   */
  Gauge maxGauge(Id id);

  /**
   * Returns the meter associated with a given id.
   *
   * @param id
   *     Identifier for the meter.
   * @return
   *     Instance of the meter or null if there is no match.
   */
  Meter get(Id id);

  /** Iterator for traversing the set of meters in the registry. */
  @Override Iterator<Meter> iterator();

  /////////////////////////////////////////////////////////////////
  // Additional helper methods below

  /**
   * Returns the first underlying registry that is an instance of {@code c}.
   */
  @SuppressWarnings("unchecked")
  default <T extends Registry> T underlying(Class<T> c) {
    if (c == null) {
      return null;
    } else if (c.isAssignableFrom(getClass())) {
      return (T) this;
    } else if (this instanceof CompositeRegistry) {
      return ((CompositeRegistry) this).find(c);
    } else {
      return null;
    }
  }

  /**
   * Creates an identifier for a meter.
   *
   * @param name
   *     Description of the measurement that is being collected.
   * @param tags
   *     Other dimensions that can be used to classify the measurement.
   * @return
   *     Identifier for a meter.
   */
  default Id createId(String name, String... tags) {
    try {
      return createId(name, Utils.toIterable(tags));
    } catch (Exception e) {
      propagate(e);
      return NoopId.INSTANCE;
    }
  }

  /**
   * Creates an identifier for a meter.
   *
   * @param name
   *     Description of the measurement that is being collected.
   * @param tags
   *     Other dimensions that can be used to classify the measurement.
   * @return
   *     Identifier for a meter.
   */
  default Id createId(String name, Map<String, String> tags) {
    try {
      return createId(name).withTags(tags);
    } catch (Exception e) {
      propagate(e);
      return NoopId.INSTANCE;
    }
  }

  /**
   * Measures the rate of some activity.
   *
   * @param name
   *     Description of the measurement that is being collected.
   * @return
   *     Counter instance with the corresponding id.
   */
  default Counter counter(String name) {
    return counter(createId(name));
  }

  /**
   * Measures the rate of some activity.
   *
   * @param name
   *     Description of the measurement that is being collected.
   * @param tags
   *     Other dimensions that can be used to classify the measurement.
   * @return
   *     Counter instance with the corresponding id.
   */
  default Counter counter(String name, Iterable<Tag> tags) {
    return counter(createId(name, tags));
  }

  /**
   * Measures the rate of some activity.
   *
   * @param name
   *     Description of the measurement that is being collected.
   * @param tags
   *     Other dimensions that can be used to classify the measurement.
   * @return
   *     Counter instance with the corresponding id.
   */
  default Counter counter(String name, String... tags) {
    return counter(createId(name, tags));
  }

  /**
   * Measures the sample distribution of events.
   *
   * @param name
   *     Description of the measurement that is being collected.
   * @return
   *     Summary instance with the corresponding id.
   */
  default DistributionSummary distributionSummary(String name) {
    return distributionSummary(createId(name));
  }

  /**
   * Measures the sample distribution of events.
   *
   * @param name
   *     Description of the measurement that is being collected.
   * @param tags
   *     Other dimensions that can be used to classify the measurement.
   * @return
   *     Summary instance with the corresponding id.
   */
  default DistributionSummary distributionSummary(String name, Iterable<Tag> tags) {
    return distributionSummary(createId(name, tags));
  }

  /**
   * Measures the sample distribution of events.
   *
   * @param name
   *     Description of the measurement that is being collected.
   * @param tags
   *     Other dimensions that can be used to classify the measurement.
   * @return
   *     Summary instance with the corresponding id.
   */
  default DistributionSummary distributionSummary(String name, String... tags) {
    return distributionSummary(createId(name, tags));
  }

  /**
   * Measures the time taken for short tasks.
   *
   * @param name
   *     Description of the measurement that is being collected.
   * @return
   *     Timer instance with the corresponding id.
   */
  default Timer timer(String name) {
    return timer(createId(name));
  }

  /**
   * Measures the time taken for short tasks.
   *
   * @param name
   *     Description of the measurement that is being collected.
   * @param tags
   *     Other dimensions that can be used to classify the measurement.
   * @return
   *     Timer instance with the corresponding id.
   */
  default Timer timer(String name, Iterable<Tag> tags) {
    return timer(createId(name, tags));
  }

  /**
   * Measures the time taken for short tasks.
   *
   * @param name
   *     Description of the measurement that is being collected.
   * @param tags
   *     Other dimensions that can be used to classify the measurement.
   * @return
   *     Timer instance with the corresponding id.
   */
  default Timer timer(String name, String... tags) {
    return timer(createId(name, tags));
  }

  /**
   * Represents a value sampled from another source.
   *
   * @param name
   *     Description of the measurement that is being collected.
   * @return
   *     Gauge instance with the corresponding id.
   */
  default Gauge gauge(String name) {
    return gauge(createId(name));
  }

  /**
   * Represents a value sampled from another source.
   *
   * @param name
   *     Description of the measurement that is being collected.
   * @param tags
   *     Other dimensions that can be used to classify the measurement.
   * @return
   *     Gauge instance with the corresponding id.
   */
  default Gauge gauge(String name, Iterable<Tag> tags) {
    return gauge(createId(name, tags));
  }

  /**
   * Represents a value sampled from another source.
   *
   * @param name
   *     Description of the measurement that is being collected.
   * @param tags
   *     Other dimensions that can be used to classify the measurement.
   * @return
   *     Gauge instance with the corresponding id.
   */
  default Gauge gauge(String name, String... tags) {
    return gauge(createId(name, tags));
  }

  /**
   * Measures the maximum value recorded since the last reset.
   *
   * @param name
   *     Description of the measurement that is being collected.
   * @return
   *     Gauge instance with the corresponding id.
   */
  default Gauge maxGauge(String name) {
    return maxGauge(createId(name));
  }

  /**
   * Measures the maximum value recorded since the last reset.
   *
   * @param name
   *     Description of the measurement that is being collected.
   * @param tags
   *     Other dimensions that can be used to classify the measurement.
   * @return
   *     Gauge instance with the corresponding id.
   */
  default Gauge maxGauge(String name, Iterable<Tag> tags) {
    return maxGauge(createId(name, tags));
  }

  /**
   * Measures the maximum value recorded since the last reset.
   *
   * @param name
   *     Description of the measurement that is being collected.
   * @param tags
   *     Other dimensions that can be used to classify the measurement.
   * @return
   *     Gauge instance with the corresponding id.
   */
  default Gauge maxGauge(String name, String... tags) {
    return maxGauge(createId(name, tags));
  }


  /**
   * Returns a stream with the current flattened set of measurements across all meters.
   * This should typically be preferred over {@link #stream()} to get the data as it will
   * automatically handle expired meters, NaN values, etc.
   */
  default Stream<Measurement> measurements() {
    return stream()
        .filter(m -> !m.hasExpired())
        .flatMap(m -> StreamSupport.stream(m.measure().spliterator(), false))
        .filter(m -> !Double.isNaN(m.value()));
  }

  /** Returns a stream of all registered meters. */
  default Stream<Meter> stream() {
    return StreamSupport.stream(spliterator(), false);
  }

  /**
   * Returns a stream of all registered counters. This operation is mainly used for testing as
   * a convenient way to get an aggregated value. For example, to generate a summary of all
   * counters with name "foo":
   *
   * <pre>
   * LongSummaryStatistics summary = r.counters()
   *   .filter(Functions.nameEquals("foo"))
   *   .collect(Collectors.summarizingLong(Counter::count));
   * </pre>
   */
  default Stream<Counter> counters() {
    return stream().filter(m -> m instanceof Counter).map(m -> (Counter) m);
  }

  /**
   * Returns a stream of all registered distribution summaries. This operation is mainly used for
   * testing as a convenient way to get an aggregated value. For example, to generate a summary of
   * the counts and total amounts for all distribution summaries with name "foo":
   *
   * <pre>
   * LongSummaryStatistics countSummary = r.distributionSummaries()
   *   .filter(Functions.nameEquals("foo"))
   *   .collect(Collectors.summarizingLong(DistributionSummary::count));
   *
   * LongSummaryStatistics totalSummary = r.distributionSummaries()
   *   .filter(Functions.nameEquals("foo"))
   *   .collect(Collectors.summarizingLong(DistributionSummary::totalAmount));
   *
   * double avgAmount = (double) totalSummary.getSum() / countSummary.getSum();
   * </pre>
   */
  default Stream<DistributionSummary> distributionSummaries() {
    return stream().filter(m -> m instanceof DistributionSummary).map(m -> (DistributionSummary) m);
  }

  /**
   * Returns a stream of all registered timers. This operation is mainly used for testing as a
   * convenient way to get an aggregated value. For example, to generate a summary of
   * the counts and total amounts for all timers with name "foo":
   *
   * <pre>
   * LongSummaryStatistics countSummary = r.timers()
   *   .filter(Functions.nameEquals("foo"))
   *   .collect(Collectors.summarizingLong(Timer::count));
   *
   * LongSummaryStatistics totalSummary = r.timers()
   *   .filter(Functions.nameEquals("foo"))
   *   .collect(Collectors.summarizingLong(Timer::totalTime));
   *
   * double avgTime = (double) totalSummary.getSum() / countSummary.getSum();
   * </pre>
   */
  default Stream<Timer> timers() {
    return stream().filter(m -> m instanceof Timer).map(m -> (Timer) m);
  }

  /**
   * Returns a stream of all registered gauges. This operation is mainly used for testing as a
   * convenient way to get an aggregated value. For example, to generate a summary of
   * the values for all gauges with name "foo":
   *
   * <pre>
   * DoubleSummaryStatistics valueSummary = r.gauges()
   *   .filter(Functions.nameEquals("foo"))
   *   .collect(Collectors.summarizingDouble(Gauge::value));
   *
   * double sum = (double) valueSummary.getSum();
   * </pre>
   */
  default Stream<Gauge> gauges() {
    return stream().filter(m -> m instanceof Gauge).map(m -> (Gauge) m);
  }

  /**
   * Log a warning and if enabled propagate the exception {@code t}. As a general rule
   * instrumentation code should degrade gracefully and avoid impacting the core application. If
   * the user makes a mistake and causes something to break, then it should not impact the
   * application unless that mistake triggers a problem outside of the instrumentation code.
   * However, in test code it is often better to throw so that mistakes are caught and corrected.
   *
   * This method is used to handle exceptions internal to the instrumentation code. Propagation
   * is controlled by the {@link RegistryConfig#propagateWarnings()} setting. If the setting
   * is true, then the exception will be propagated. Otherwise the exception will only get logged
   * as a warning.
   *
   * @param msg
   *     Message written out to the log.
   * @param t
   *     Exception to log and optionally propagate.
   */
  default void propagate(String msg, Throwable t) {
    LoggerFactory.getLogger(getClass()).warn(msg, t);
    if (config().propagateWarnings()) {
      if (t instanceof RuntimeException) {
        throw (RuntimeException) t;
      } else {
        throw new RuntimeException(t);
      }
    }
  }
  /**
   * Log a warning using the message from the exception and if enabled propagate the
   * exception {@code t}. For more information see {@link #propagate(String, Throwable)}.
   *
   * @param t
   *     Exception to log and optionally propagate.
   */
  default void propagate(Throwable t) {
    if (t != null) {
      propagate(t.getMessage(), t);
    }
  }
}
