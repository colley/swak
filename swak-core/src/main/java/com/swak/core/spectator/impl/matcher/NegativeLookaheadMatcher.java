
package com.swak.core.spectator.impl.matcher;


import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

/**
 * Matcher that does a negative lookahead. If the sub-matcher matches, then it will return
 * no match, otherwise it will return the starting the position.
 */
final class NegativeLookaheadMatcher implements Matcher, Serializable {

  private static final long serialVersionUID = 1L;

  private final Matcher matcher;

  /** Create a new instance. */
  NegativeLookaheadMatcher(Matcher matcher) {
    this.matcher = matcher;
  }

  Matcher matcher() {
    return matcher;
  }

  @Override
  public int matches(String str, int start, int length) {
    int pos = matcher.matches(str, start, length);
    return (pos >= 0) ? Constants.NO_MATCH : start;
  }

  @Override
  public int minLength() {
    return 0;
  }

  @Override
  public Matcher rewrite(Function<Matcher, Matcher> f) {
    return f.apply(new NegativeLookaheadMatcher(matcher.rewrite(f)));
  }

  @Override
  public Matcher rewriteEnd(Function<Matcher, Matcher> f) {
    return f.apply(new NegativeLookaheadMatcher(matcher.rewriteEnd(f)));
  }

  @Override
  public String toString() {
    return "(?!" + matcher + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NegativeLookaheadMatcher that = (NegativeLookaheadMatcher) o;
    return Objects.equals(matcher, that.matcher);
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + matcher.hashCode();
    return result;
  }
}
