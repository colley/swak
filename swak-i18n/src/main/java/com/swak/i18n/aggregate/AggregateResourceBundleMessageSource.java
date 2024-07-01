package com.swak.i18n.aggregate;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.swak.common.exception.SwakAssert;
import com.swak.common.util.StringPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.AbstractResourceBasedMessageSource;
import org.springframework.util.ClassUtils;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author colley.ma
 * @since 3.0.0
 */
@Slf4j
public class AggregateResourceBundleMessageSource extends AbstractResourceBasedMessageSource
        implements BeanClassLoaderAware, InitializingBean {

    private ClassLoader bundleClassLoader;

    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    private ResourceBundleLocator resourceBundleLocator;

    private final boolean aggregate;

    private final Map<Locale, ResourceBundle> cachedResourceBundles = new ConcurrentHashMap<>();

    private final Map<ResourceBundle, Map<String, Map<Locale, MessageFormat>>> cachedBundleMessageFormats =
            new ConcurrentHashMap<>();

    public AggregateResourceBundleMessageSource(boolean aggregate) {
        setDefaultEncoding(StringPool.UTF8);
        this.aggregate = aggregate;
    }

    public AggregateResourceBundleMessageSource(String[] bundleNames) {
        this(bundleNames, false);
    }

    public AggregateResourceBundleMessageSource(String[] bundleNames, boolean aggregate) {
        SwakAssert.state(ArrayUtils.isNotEmpty(bundleNames), "bundleNames must not be empty");
        super.addBasenames(bundleNames);
        this.aggregate = aggregate;
    }

    protected ClassLoader getBundleClassLoader() {
        return (this.bundleClassLoader != null ? this.bundleClassLoader : this.beanClassLoader);
    }

    public ResourceBundle doGetBundle(Locale locale) {
        ResourceBundle rb = resourceBundleLocator.getResourceBundle(locale);
        if (rb != null) {
            log.debug("{} i18n resource found.", Joiner.on(",").join(getBasenameSet()));
        } else {
            log.warn("{} i18n resource not found.", Joiner.on(",").join(getBasenameSet()));
        }
        return rb;
    }

    protected ResourceBundle getResourceBundle(Locale locale) {
        if (getCacheMillis() >= 0) {
            return doGetBundle(locale);
        } else {
            ResourceBundle bundle = this.cachedResourceBundles.get(locale);
            if (Objects.nonNull(bundle)) {
                return bundle;
            }
            try {
                bundle = doGetBundle(locale);
                if (Objects.nonNull(bundle)) {
                    this.cachedResourceBundles.put(locale, bundle);
                }
                return bundle;
            } catch (MissingResourceException ex) {
                if (log.isWarnEnabled()) {
                    log.warn("ResourceBundle [" + Joiner.on(",").join(getBasenameSet()) + "] " +
                            "not found for MessageSource: " + ex.getMessage());
                }
                return null;
            }
        }
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        ResourceBundle bundle = getResourceBundle(locale);
        if (bundle != null) {
            MessageFormat messageFormat = getMessageFormat(bundle, code, locale);
            if (messageFormat != null) {
                return messageFormat;
            }
        }
        return null;
    }

    protected MessageFormat getMessageFormat(ResourceBundle bundle, String code, Locale locale)
            throws MissingResourceException {
        Map<String, Map<Locale, MessageFormat>> codeMap = this.cachedBundleMessageFormats.get(bundle);
        Map<Locale, MessageFormat> localeMap = null;
        if (codeMap != null) {
            localeMap = codeMap.get(code);
            if (localeMap != null) {
                MessageFormat result = localeMap.get(locale);
                if (result != null) {
                    return result;
                }
            }
        }
        String msg = getStringOrNull(bundle, code);
        if (msg != null) {
            if (codeMap == null) {
                codeMap = new ConcurrentHashMap<>();
                Map<String, Map<Locale, MessageFormat>> existing = this.cachedBundleMessageFormats.putIfAbsent(bundle, codeMap);
                if (existing != null) {
                    codeMap = existing;
                }
            }
            if (localeMap == null) {
                localeMap = new ConcurrentHashMap<>();
                Map<Locale, MessageFormat> existing = codeMap.putIfAbsent(code, localeMap);
                if (existing != null) {
                    localeMap = existing;
                }
            }
            MessageFormat result = createMessageFormat(msg, locale);
            localeMap.put(locale, result);
            return result;
        }
        return null;
    }

    protected String getStringOrNull(ResourceBundle bundle, String key) {
        if (bundle.containsKey(key)) {
            try {
                return bundle.getString(key);
            } catch (MissingResourceException ex) {
                // Assume key not found for some other reason
                // -> do NOT throw the exception to allow for checking parent message source.
            }
        }
        return null;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.bundleClassLoader = classLoader;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.resourceBundleLocator = new AggregateResourceBundleLocator(Lists.newArrayList(getBasenameSet()),
                getBundleClassLoader(), aggregate);
    }
}
