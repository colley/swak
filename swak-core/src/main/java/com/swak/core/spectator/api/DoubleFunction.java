
package com.swak.core.spectator.api;

import java.util.function.ToDoubleFunction;

/**
 * Function to extract a double value from an object.
 */
public abstract class DoubleFunction<T extends Number> implements ToDoubleFunction<T> {

  @Override public double applyAsDouble(T n) {
    return apply(n.doubleValue());
  }

  /**
   * Apply a transform to the value `v`.
   *
   * @param v
   *     Double value to transform.
   * @return
   *     Result of applying this function to `v`.
   */
  public abstract double apply(double v);
}
