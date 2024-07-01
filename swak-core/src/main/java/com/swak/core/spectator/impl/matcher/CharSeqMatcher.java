
package com.swak.core.spectator.impl.matcher;


import java.io.Serializable;
import java.util.Objects;
import java.util.SortedSet;

/** Matcher that checks for a sequence of characters. */
final class CharSeqMatcher implements Matcher, Serializable {

  private static final long serialVersionUID = 1L;

  private final String pattern;
  private final boolean ignoreCase;

  /** Create a new instance. */
  CharSeqMatcher(char pattern) {
    this(String.valueOf(pattern));
  }

  /** Create a new instance. */
  CharSeqMatcher(String pattern) {
    this(pattern, false);
  }

  /** Create a new instance. */
  CharSeqMatcher(String pattern, boolean ignoreCase) {
    this.pattern = pattern;
    this.ignoreCase = ignoreCase;
  }

  /** Sub-string to look for within the string being checked. */
  String pattern() {
    return pattern;
  }

  @Override
  public String containedString() {
    return pattern;
  }

  @Override
  public SortedSet<String> trigrams() {
    return PatternUtils.computeTrigrams(pattern);
  }

  @Override
  public int matches(String str, int start, int length) {
    final int plength = pattern.length();
    boolean matched = ignoreCase
        ? str.regionMatches(true, start, pattern, 0, plength)
        : str.startsWith(pattern, start);
    //System.out.println(matched + "::-" + str.substring(start));
    return matched ? start + plength : Constants.NO_MATCH;
  }

  @Override
  public int minLength() {
    return pattern.length();
  }

  @Override
  public String toString() {
    return PatternUtils.escape(pattern);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CharSeqMatcher that = (CharSeqMatcher) o;
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
