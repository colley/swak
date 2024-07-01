
package com.swak.core.spectator.impl.matcher;


/** Matcher that always returns false. */
enum FalseMatcher implements Matcher {

  /** Singleton instance. */
  INSTANCE;

  @Override
  public int matches(String str, int start, int length) {
    return Constants.NO_MATCH;
  }

  @Override
  public int minLength() {
    return 0;
  }

  @Override
  public boolean neverMatches() {
    return true;
  }

  @Override
  public String toString() {
    // Pattern that can never match, nothing can be after the end
    return "$.";
  }
}
