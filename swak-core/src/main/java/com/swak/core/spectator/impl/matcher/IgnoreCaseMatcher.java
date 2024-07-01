
package com.swak.core.spectator.impl.matcher;


import com.swak.core.spectator.impl.PatternMatcher;

import java.io.Serializable;
import java.util.Objects;

/** Matcher that ignores the case when checking against the input string. */
final class IgnoreCaseMatcher implements PatternMatcher, Serializable {

  private static final long serialVersionUID = 1L;

  private final PatternMatcher matcher;

  /**
   * Underlying matcher to use for checking the string. It should have already been converted
   * to match on the lower case version of the string.
   */
  IgnoreCaseMatcher(PatternMatcher matcher) {
    this.matcher = matcher;
  }

  @Override
  public boolean matches(String str) {
    return matcher.matches(str);
  }

  @Override
  public String toString() {
    return "(?i)" + matcher.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IgnoreCaseMatcher that = (IgnoreCaseMatcher) o;
    return Objects.equals(matcher, that.matcher);
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + matcher.hashCode();
    return result;
  }
}
