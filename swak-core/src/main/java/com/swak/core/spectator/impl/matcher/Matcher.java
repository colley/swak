
package com.swak.core.spectator.impl.matcher;


import com.swak.core.spectator.impl.PatternExpr;
import com.swak.core.spectator.impl.PatternMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Internal interface for matcher implementations. Provides some additional methods
 * that are not exposed on the public {@link PatternMatcher} interface.
 */
interface Matcher extends PatternMatcher {

  /**
   * Check if a string matches the pattern.
   *
   * @param str
   *     Input string to check.
   * @param start
   *     Starting position in the string to use when checking for a match.
   * @param length
   *     Maximum length after the starting position to consider when checking for a
   *     match.
   * @return
   *     If there is a match, then the returned value will be the position in the
   *     string just after the match. Otherwise {@link Constants#NO_MATCH} will be
   *     returned.
   */
  int matches(String str, int start, int length);

  @Override
  default boolean matches(String str) {
    return matches(str, 0, str.length()) >= 0;
  }

  @Override
  default PatternMatcher ignoreCase() {
    Matcher m = rewrite(PatternUtils::ignoreCase);
    return new IgnoreCaseMatcher(m);
  }

  @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
  @Override
  default List<PatternMatcher> expandOrClauses(int max) {
    List<Matcher> ms = PatternUtils.expandOrClauses(this, max);
    if (ms == null)
      return null;
    List<PatternMatcher> results = new ArrayList<>(ms.size());
    for (Matcher m : ms) {
      results.add(Optimizer.optimize(m));
    }
    return results;
  }

  @Override
  default PatternExpr toPatternExpr(int max) {
    return PatternUtils.toPatternExpr(this, max);
  }

  @Override
  default String toSqlPattern() {
    return PatternUtils.toSqlPattern(this);
  }

  /** Cast the matcher to type {@code T}. */
  @SuppressWarnings("unchecked")
  default <T> T as() {
    return (T) this;
  }

  /**
   * Return a new matcher by recursively applying the rewrite function to all sub-matchers.
   */
  default Matcher rewrite(Function<Matcher, Matcher> f) {
    return f.apply(this);
  }

  /**
   * Return a new matcher by recursively applying the rewrite function to all sub-matchers in
   * the final position of the sequence.
   */
  default Matcher rewriteEnd(Function<Matcher, Matcher> f) {
    return f.apply(this);
  }
}
