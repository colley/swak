package com.swak.i18n;

import com.google.common.collect.Sets;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.convert.DurationUnit;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Set;

/**
 * @author colley.ma
 * @since 3.0.0
 */
@Data
@Accessors(chain = true)
public class I18nSourceProperties {

    private Set<String> baseNames = Sets.newLinkedHashSet();

    private Locale defaultLocale;

    /**
     * Message bundles encoding.
     */
    private Charset encoding = StandardCharsets.UTF_8;

    @DurationUnit(ChronoUnit.SECONDS)
    private Duration cacheDuration;

    /**
     * Whether to fall back to the system Locale if no files for a specific Locale have
     * been found. if this is turned off, the only fallback will be the default file (e.g.
     * "messages.properties" for basename "messages").
     */
    private boolean fallbackToSystemLocale = true;

    /**
     * Whether to always apply the MessageFormat rules, parsing even messages without
     * arguments.
     */
    private boolean alwaysUseMessageFormat = false;

    /**
     * Whether to use the message code as the default message instead of throwing a
     * "NoSuchMessageException". Recommended during development only.
     */
    private boolean useCodeAsDefaultMessage = false;
}
