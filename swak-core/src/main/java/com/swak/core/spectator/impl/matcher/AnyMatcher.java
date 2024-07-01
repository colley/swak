
package com.swak.core.spectator.impl.matcher;

/** Matcher that matches any character. */
enum AnyMatcher implements Matcher {

  /** Singleton instance. */
  INSTANCE;

  @Override
  public int matches(String str, int start, int length) {
    return (length > 0) ? start + 1 : Constants.NO_MATCH;
  }

  @Override
  public int minLength() {
    return 1;
  }

  @Override
  public String toString() {
    return ".";
  }
}
