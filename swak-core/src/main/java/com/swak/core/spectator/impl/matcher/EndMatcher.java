
package com.swak.core.spectator.impl.matcher;


/** Matcher that matches if the position is at the end of the input string. */
enum EndMatcher implements Matcher {

  /** Singleton instance. */
  INSTANCE;

  @Override
  public int matches(String str, int start, int length) {
    return (start == str.length()) ? start : Constants.NO_MATCH;
  }

  @Override
  public int minLength() {
    return 0;
  }

  @Override
  public boolean isEndAnchored() {
    return true;
  }

  @Override
  public String toString() {
    return "$";
  }
}
