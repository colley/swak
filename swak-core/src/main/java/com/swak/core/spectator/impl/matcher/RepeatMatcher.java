
package com.swak.core.spectator.impl.matcher;


import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedSet;
import java.util.function.Function;

/** Matcher that looks for a fixed number of repetitions. */
final class RepeatMatcher implements Matcher, Serializable {

  private static final long serialVersionUID = 1L;

  private final Matcher repeated;
  private final int min;
  private final int max;

  /** Create a new instance. */
  RepeatMatcher(Matcher repeated, int min, int max) {
    this.repeated = repeated;
    this.min = min;
    this.max = max;
  }

  Matcher repeated() {
    return repeated;
  }

  int min() {
    return min;
  }

  int max() {
    return max;
  }

  @Override
  public String containedString() {
    return min == 0 ? null : repeated.containedString();
  }

  @Override
  public SortedSet<String> trigrams() {
    return min == 0 ? Collections.emptySortedSet() : repeated.trigrams();
  }

  @Override
  public int matches(String str, int start, int length) {
    final int end = start + length;
    int pos = start;
    int numMatches = 0;
    while (pos >= 0 && numMatches < max) {
      int p = repeated.matches(str, pos, end - pos);
      if (p >= 0) {
        ++numMatches;
        pos = p;
      } else {
        return (numMatches >= min) ? pos : Constants.NO_MATCH;
      }
    }
    return pos;
  }

  @Override
  public int minLength() {
    return min * repeated.minLength();
  }

  @Override
  public Matcher rewrite(Function<Matcher, Matcher> f) {
    return new RepeatMatcher(f.apply(repeated), min, max);
  }

  @Override
  public Matcher rewriteEnd(Function<Matcher, Matcher> f) {
    return new RepeatMatcher(f.apply(repeated), min, max);
  }

  @Override
  public String toString() {
    return min == max
        ? "(?:" + repeated + "){" + min + "}"
        : "(?:" + repeated + "){" + min + "," + max + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RepeatMatcher that = (RepeatMatcher) o;
    return min == that.min
        && max == that.max
        && Objects.equals(repeated, that.repeated);
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + repeated.hashCode();
    result = 31 * result + min;
    result = 31 * result + max;
    return result;
  }
}
