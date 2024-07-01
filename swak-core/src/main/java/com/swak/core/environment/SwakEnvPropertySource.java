package com.swak.core.environment;

import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * SwakEnvPropertySource.java
 *
 * @author colley.ma
 * @since 3.0.0
 */
public class SwakEnvPropertySource extends MapPropertySource {

    public SwakEnvPropertySource(String name, Map<String, Object> source) {
        super(name, source);
    }

    public SwakEnvPropertySource(Map<String, Object> source) {
        super("SWAK-ENV", source);
    }

    public SwakEnvPropertySource() {
        super("SWAK-ENV", new HashMap<>());
    }
    public SwakEnvPropertySource put(String name, Object value) {
        super.source.put(name, value);
        return this;
    }
}
