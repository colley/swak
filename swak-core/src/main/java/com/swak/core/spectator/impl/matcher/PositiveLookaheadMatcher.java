
package com.swak.core.spectator.impl.matcher;


import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

/**
 * Matcher that does a positive lookahead. If the sub-matcher matches, then it will return
 * a match, but will not advance the position.
 */
final class PositiveLookaheadMatcher implements Matcher, Serializable {

  private static final long serialVersionUID = 1L;

  private final Matcher matcher;

  /** Create a new instance. */
  PositiveLookaheadMatcher(Matcher matcher) {
    this.matcher = matcher;
  }

  Matcher matcher() {
    return matcher;
  }

  @Override
  public int matches(String str, int start, int length) {
    int pos = matcher.matches(str, start, length);
    return (pos >= 0) ? start : Constants.NO_MATCH;
  }

  @Override
  public int minLength() {
    return 0;
  }

  @Override
  public Matcher rewrite(Function<Matcher, Matcher> f) {
    return f.apply(new PositiveLookaheadMatcher(matcher.rewrite(f)));
  }

  @Override
  public Matcher rewriteEnd(Function<Matcher, Matcher> f) {
    return f.apply(new PositiveLookaheadMatcher(matcher.rewriteEnd(f)));
  }

  @Override
  public String toString() {
    return "(?=" + matcher + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PositiveLookaheadMatcher that = (PositiveLookaheadMatcher) o;
    return Objects.equals(matcher, that.matcher);
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + matcher.hashCode();
    return result;
  }
}
