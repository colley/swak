package com.swak.security.config;

import com.google.common.collect.Sets;
import com.swak.common.dto.base.DTO;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * @author colley.ma
 * @since 3.0.0
 */
@Data
public class WhitelistConfig implements DTO {

    private Set<String> authWhitelist = new HashSet<>();

    private Set<String> staticWhitelist = new HashSet<>();

    public WhitelistConfig authMatchers(String... antPatterns) {
        if (antPatterns != null) {
            authWhitelist.addAll(Sets.newHashSet(antPatterns));
        }
        return this;
    }

    public WhitelistConfig staticMatchers(String... antPatterns) {
        if (antPatterns != null) {
            staticWhitelist.addAll(Sets.newHashSet(antPatterns));
        }
        return this;
    }
}
