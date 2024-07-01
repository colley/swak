package com.swak.core.web;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author colley.ma
 * @since 3.0.0
 */
@Data
public class SwakMvcPatterns {

    private final List<String> includePatterns = new ArrayList<>();

    private final List<String> excludePatterns = new ArrayList<>();

    public SwakMvcPatterns addPathPatterns(String... patterns) {
        return addPathPatterns(Arrays.asList(patterns));
    }

    public SwakMvcPatterns addPathPatterns(List<String> patterns) {
        this.includePatterns.addAll(patterns);
        return this;
    }

    public SwakMvcPatterns excludePathPatterns(String... patterns) {
        return excludePathPatterns(Arrays.asList(patterns));
    }

    public SwakMvcPatterns excludePathPatterns(List<String> patterns) {
        this.excludePatterns.addAll(patterns);
        return this;
    }
}
