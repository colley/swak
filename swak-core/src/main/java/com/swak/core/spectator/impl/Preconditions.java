
package com.swak.core.spectator.impl;

/**
 * Internal convenience methods that help a method or constructor check whether it was invoked
 * correctly.
 *
 * <p><b>This class is an internal implementation detail only intended for use within spectator.
 * It is subject to change without notice.</b></p>
 */
public final class Preconditions {
  private Preconditions() {
  }

  /**
   * Ensures the object reference is not null.
   */
  public static <T> T checkNotNull(T obj, String name) {
    if (obj == null) {
      String msg = String.format("parameter '%s' cannot be null", name);
      throw new NullPointerException(msg);
    }
    return obj;
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance.
   */
  public static void checkArg(boolean expression, String errMsg) {
    if (!expression) {
      throw new IllegalArgumentException(errMsg);
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance.
   */
  public static void checkState(boolean expression, String errMsg) {
    if (!expression) {
      throw new IllegalStateException(errMsg);
    }
  }
}
