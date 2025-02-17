
package com.swak.core.spectator.api.patterns;

import com.swak.core.spectator.api.BasicTag;
import com.swak.core.spectator.api.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Base class used for builders that need to allow for additional tagging to be
 * added onto a base id. This is typically used in conjunction with {@link IdBuilder}.
 */
@SuppressWarnings("unchecked")
public class TagsBuilder<T extends TagsBuilder<T>> {

  /** Create a new instance. */
  protected TagsBuilder() {
    // This class is only intended to be created by a sub-class. Since there are no
    // abstract methods at this time it is documented via the protected constructor
    // rather than making the class abstract.
  }

  /** Set of extra tags that the sub-class should add in to the id. */
  protected final List<Tag> extraTags = new ArrayList<>();

  /** Add an additional tag value. */
  public T withTag(String k, String v) {
    extraTags.add(new BasicTag(k, v));
    return (T) this;
  }

  /** Add an additional tag value. */
  public T withTag(String k, Boolean v) {
    return withTag(k, Boolean.toString(v));
  }

  /** Add an additional tag value based on the name of the enum. */
  public <E extends Enum<E>> T withTag(String k, Enum<E> v) {
    return withTag(k, v.name());
  }

  /** Add an additional tag value. */
  public T withTag(Tag t) {
    extraTags.add(t);
    return (T) this;
  }

  /** Add additional tag values. */
  public T withTags(String... tags) {
    for (int i = 0; i < tags.length; i += 2) {
      extraTags.add(new BasicTag(tags[i], tags[i + 1]));
    }
    return (T) this;
  }

  /** Add additional tag values. */
  public T withTags(Tag... tags) {
    Collections.addAll(extraTags, tags);
    return (T) this;
  }

  /** Add additional tag values. */
  public T withTags(Iterable<Tag> tags) {
    for (Tag t : tags) {
      extraTags.add(t);
    }
    return (T) this;
  }

  /** Add additional tag values. */
  public T withTags(Map<String, String> tags) {
    for (Map.Entry<String, String> entry : tags.entrySet()) {
      extraTags.add(new BasicTag(entry.getKey(), entry.getValue()));
    }
    return (T) this;
  }
}
