
package com.swak.core.spectator.api;

/** Key/value pair used to classify and drill into measurements. */
public interface Tag {
  /** Key for the tag. */
  String key();

  /** Value for the tag. */
  String value();

  /** Create an immutable instance of a tag. */
  static Tag of(String key, String value) {
    return new BasicTag(key, value);
  }
}
