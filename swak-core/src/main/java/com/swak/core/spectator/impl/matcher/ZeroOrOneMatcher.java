
package com.swak.core.spectator.impl.matcher;


import com.swak.core.spectator.impl.Preconditions;

import java.io.Serializable;
import java.util.Objects;
import java.util.SortedSet;
import java.util.function.Function;

/**
 * Matcher that looks for a pattern zero or one time followed by another pattern.
 */
final class ZeroOrOneMatcher implements GreedyMatcher, Serializable {

  private static final long serialVersionUID = 1L;

  private final Matcher repeated;
  private final Matcher next;

  /** Create a new instance. */
  ZeroOrOneMatcher(Matcher repeated, Matcher next) {
    this.repeated = Preconditions.checkNotNull(repeated, "repeated");
    this.next = Preconditions.checkNotNull(next, "next");
  }

  /** Return the matcher for the repeated portion. */
  Matcher repeated() {
    return repeated;
  }

  /** Return the matcher for the portion that follows. */
  Matcher next() {
    return next;
  }

  @Override
  public String containedString() {
    return next.containedString();
  }

  @Override
  public SortedSet<String> trigrams() {
    return next.trigrams();
  }

  @Override
  public int matches(String str, int start, int length) {
    final int end = start + length;
    int pos = repeated.matches(str, start, end - start);
    if (pos >= start) {
      pos = next.matches(str, pos, end - pos);
      if (pos >= start)
        return pos;
    }
    return next.matches(str, start, end - start);
  }

  @Override
  public int minLength() {
    return next.minLength();
  }

  @Override
  public boolean isEndAnchored() {
    return next.isEndAnchored();
  }

  @Override
  public boolean alwaysMatches() {
    return repeated instanceof AnyMatcher && next instanceof TrueMatcher;
  }

  @Override
  public Matcher mergeNext(Matcher after) {
    if (after instanceof TrueMatcher) {
      return this;
    }
    Matcher m = (next instanceof TrueMatcher) ? after : SeqMatcher.create(next, after);
    return new ZeroOrOneMatcher(repeated, m);
  }

  @Override
  public Matcher rewrite(Function<Matcher, Matcher> f) {
    return f.apply(new ZeroOrOneMatcher(repeated.rewrite(f), next.rewrite(f)));
  }

  @Override
  public Matcher rewriteEnd(Function<Matcher, Matcher> f) {
    return f.apply(new ZeroOrOneMatcher(repeated, next.rewriteEnd(f)));
  }

  @Override
  public String toString() {
    return "(?:" + repeated + ")?" + (next instanceof TrueMatcher ? "" : next.toString());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ZeroOrOneMatcher that = (ZeroOrOneMatcher) o;
    return Objects.equals(repeated, that.repeated) && Objects.equals(next, that.next);
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + repeated.hashCode();
    result = 31 * result + next.hashCode();
    return result;
  }
}
