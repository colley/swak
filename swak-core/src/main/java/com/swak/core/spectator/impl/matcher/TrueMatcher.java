
package com.swak.core.spectator.impl.matcher;


/** Matcher that always returns true and does not advance the position. */
enum TrueMatcher implements Matcher {

  /** Singleton instance. */
  INSTANCE;

  @Override
  public int matches(String str, int start, int length) {
    return start;
  }

  @Override
  public int minLength() {
    return 0;
  }

  @Override
  public boolean alwaysMatches() {
    return true;
  }

  @Override
  public String toString() {
    return "";
  }
}
