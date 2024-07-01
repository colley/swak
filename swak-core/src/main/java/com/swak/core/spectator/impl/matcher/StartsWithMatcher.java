
package com.swak.core.spectator.impl.matcher;


import java.io.Serializable;
import java.util.Objects;
import java.util.SortedSet;

/**
 * Matcher that checks if the string starts with a given character sequence.
 */
final class StartsWithMatcher implements Matcher, Serializable {

  private static final long serialVersionUID = 1L;

  private final String pattern;
  private final boolean ignoreCase;

  /** Create a new instance. */
  StartsWithMatcher(String pattern) {
    this(pattern, false);
  }

  /** Create a new instance. */
  StartsWithMatcher(String pattern, boolean ignoreCase) {
    this.pattern = pattern;
    this.ignoreCase = ignoreCase;
  }

  /** Pattern to check for at the start of the string. */
  String pattern() {
    return pattern;
  }

  @Override
  public int matches(String str, int start, int length) {
    boolean matched = ignoreCase
        ? str.regionMatches(true, 0, pattern, 0, pattern.length())
        : str.startsWith(pattern);
    return matched ? pattern.length() : Constants.NO_MATCH;
  }

  @Override
  public boolean matchesAfterPrefix(String str) {
    return true;
  }

  @Override
  public String prefix() {
    return pattern;
  }

  @Override
  public String containedString() {
    return pattern;
  }

  @Override
  public boolean isPrefixMatcher() {
    return true;
  }

  @Override
  public boolean isContainsMatcher() {
    return true;
  }

  @Override
  public SortedSet<String> trigrams() {
    return PatternUtils.computeTrigrams(pattern);
  }

  @Override
  public int minLength() {
    return pattern.length();
  }

  @Override
  public boolean isStartAnchored() {
    return true;
  }

  @Override
  public String toString() {
    return "^" + PatternUtils.escape(pattern);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StartsWithMatcher that = (StartsWithMatcher) o;
    return ignoreCase == that.ignoreCase && Objects.equals(pattern, that.pattern);
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + pattern.hashCode();
    result = 31 * result + Boolean.hashCode(ignoreCase);
    return result;
  }
}
