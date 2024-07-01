
package com.swak.core.spectator.impl;

import com.swak.core.spectator.api.RegistryConfig;

/**
 * Helper methods for accessing configuration settings.
 *
 * <p><b>This class is an internal implementation detail only intended for use within spectator.
 * It is subject to change without notice.</b></p>
 */
public final class Config {

  private static final String PREFIX = "spectator.api.";

  private static final RegistryConfig DEFAULT_CONFIG = k -> System.getProperty(PREFIX + k);

  private Config() {
  }

  /**
   * Returns a default implementation of the registry config backed by system properties.
   */
  public static RegistryConfig defaultConfig() {
    return DEFAULT_CONFIG;
  }
}
