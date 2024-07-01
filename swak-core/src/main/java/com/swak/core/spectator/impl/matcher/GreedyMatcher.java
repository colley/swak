
package com.swak.core.spectator.impl.matcher;


/** Base type for matchers that consume until next match. */
interface GreedyMatcher extends Matcher {

  /** Merge in the matcher that comes after in the sequence. */
  Matcher mergeNext(Matcher after);
}
