
package com.swak.core.spectator.api;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/** Id implementation for the no-op registry. */
final class NoopId implements Id {
  /** Singleton instance. */
  static final Id INSTANCE = new NoopId();

  private NoopId() {
  }

  @Override public String name() {
    return "noop";
  }

  @Override public Iterable<Tag> tags() {
    return Collections.emptyList();
  }

  @Override public Id withTag(String k, String v) {
    return this;
  }

  @Override public Id withTag(Tag tag) {
    return this;
  }

  @Override public Id withTags(Iterable<Tag> tags) {
    return this;
  }

  @Override public Id withTags(Tag... tags) {
    return this;
  }

  @Override public Id withTags(String... tags) {
    return this;
  }

  @Override public Id withTags(Map<String, String> tags) {
    return this;
  }

  @Override public Id withTag(String k, boolean v) {
    return this;
  }

  @SuppressWarnings("PMD.UseObjectForClearerAPI")
  @Override public Id withTags(String k1, String v1, String k2, String v2) {
    return this;
  }

  @SuppressWarnings("PMD.UseObjectForClearerAPI")
  @Override public Id withTags(String k1, String v1, String k2, String v2, String k3, String v3) {
    return this;
  }

  @Override public String toString() {
    return name();
  }

  @Override public String getKey(int i) {
    return "name";
  }

  @Override public String getValue(int i) {
    return name();
  }

  @Override public int size() {
    return 1;
  }

  @Override public Id filter(BiPredicate<String, String> predicate) {
    return this;
  }

  @Override public Id filterByKey(Predicate<String> predicate) {
    return this;
  }

  @Override public Iterator<Tag> iterator() {
    return Collections.emptyIterator();
  }

  @Override public Spliterator<Tag> spliterator() {
    return Spliterators.emptySpliterator();
  }
}
