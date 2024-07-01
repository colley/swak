
package com.swak.core.spectator.impl.matcher;


/** Matcher that matches if the position is at the start of the input string. */
enum StartMatcher implements Matcher {

  /** Singleton instance. */
  INSTANCE;

  @Override
  public int matches(String str, int start, int length) {
    return (start == 0) ? 0 : Constants.NO_MATCH;
  }

  @Override
  public int minLength() {
    return 0;
  }

  @Override
  public boolean isStartAnchored() {
    return true;
  }

  @Override
  public String toString() {
    return "^";
  }
}
