
package com.swak.core.spectator.api;

import com.swak.core.spectator.impl.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;


/**
 * Common functions for use with gauges.
 */
public final class Functions {

  private static final Logger LOGGER = LoggerFactory.getLogger(Functions.class);

  private Functions() {
  }

  /**
   * Identity function that just returns the passed in value if it implements the
   * {@link java.lang.Number} interface.
   */
  public static final DoubleFunction<? extends Number> IDENTITY = new DoubleFunction<Number>() {
    @Override public double apply(double v) {
      return v;
    }
  };

  /**
   * Age function based on the system clock. See {@link #age(Clock)} for more details.
   */
  public static final DoubleFunction<AtomicLong> AGE = age(Clock.SYSTEM);

  /**
   * Returns a function that computes the age in seconds. The value passed into the function
   * should be a timestamp in milliseconds since the epoch. Typically this will be the return
   * value from calling {@link java.lang.System#currentTimeMillis()}.
   *
   * @param clock
   *     Clock used to get the current time for comparing with the passed in value.
   * @return
   *     Function that computes the age.
   */
  public static DoubleFunction<AtomicLong> age(final Clock clock) {
    return new DoubleFunction<AtomicLong>() {
      @Override public double apply(double t) {
        return (clock.wallTime() - t) / 1000.0;
      }
    };
  }

  /**
   * Returns a function that invokes a method on the object via reflection. If an
   * exception of any kind occurs then NaN will be returned and the exception will be logged.
   * The method must have an empty parameter list and return a primitive number value or a
   * subclass of {@link java.lang.Number}. The method will be set accessible so that private
   * methods can be used.
   *
   * @param method
   *     Method to execute on the passed in object.
   * @return
   *     Value returned by the method or NaN if an exception is thrown.
   */
  @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
  public static <T> ToDoubleFunction<T> invokeMethod(final Method method) {
    method.setAccessible(true);
    return (obj) -> {
      try {
        final Number n = (Number) method.invoke(obj);
        return n.doubleValue();
      } catch (Exception e) {
        LOGGER.warn("exception from method registered as a gauge [" + method + "]", e);
        return Double.NaN;
      }
    };
  }

  /**
   * Returns a predicate that matches if the {code}id.name(){code} value for the input meter
   * is equal to {code}name{code}. Example of usage:
   *
   * <pre>
   * long numberOfMatches = registry.stream().filter(Functions.nameEquals("foo")).count();
   * </pre>
   *
   * @param name
   *     The name to use for finding matching meter instances. Cannot be null.
   * @return
   *     A predicate function that can be used to filter a stream.
   */
  public static Predicate<Meter> nameEquals(String name) {
    Preconditions.checkNotNull(name, "name");
    return m -> name.equals(m.id().name());
  }
}
