
package com.swak.core.spectator.api;

import java.time.Duration;

/**
 * Configuration settings for the registry.
 */
public interface RegistryConfig {

  /**
   * Get the value associated with a key.
   *
   * @param k
   *     Key to lookup in the config.
   * @return
   *     Value for the key or null if no key is present.
   */
  String get(String k);

  /** Should an exception be thrown for warnings? */
  default boolean propagateWarnings() {
    String v = get("propagateWarnings");
    return v != null && Boolean.parseBoolean(v);
  }

  /**
   * For classes based on {@link com.swak.core.spectator.api.AbstractRegistry} this setting is used
   * to determine the maximum number of registered meters permitted. This limit is used to help
   * protect the system from a memory leak if there is a bug or irresponsible usage of registering
   * meters.
   *
   * @return
   *     Maximum number of distinct meters that can be registered at a given time. The default is
   *     {@link java.lang.Integer#MAX_VALUE}.
   */
  default int maxNumberOfMeters() {
    String v = get("maxNumberOfMeters");
    return (v == null) ? Integer.MAX_VALUE : Integer.parseInt(v);
  }

  /** How often registered gauges should get polled. */
  default Duration gaugePollingFrequency() {
    String v = get("gaugePollingFrequency");
    return (v == null) ? Duration.ofSeconds(10) : Duration.parse(v);
  }
}
