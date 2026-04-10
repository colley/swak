package com.swak.i18n.aggregate;


import com.google.common.collect.Sets;
import com.swak.common.exception.SwakAssert;
import com.swak.i18n.util.GetClassLoader;
import com.swak.i18n.util.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class PlatformResourceBundleLocator implements ResourceBundleLocator {

    private static final Logger log = LoggerFactory.getLogger(PlatformResourceBundleLocator.class);
    private static final boolean RESOURCE_BUNDLE_CONTROL_INSTANTIABLE = determineAvailabilityOfResourceBundleControl();

    private final String bundleName;
    private final ClassLoader classLoader;
    private final boolean aggregate;

    public PlatformResourceBundleLocator(String bundleName) {
        this(bundleName, null);
    }

    public PlatformResourceBundleLocator(String bundleName, ClassLoader classLoader) {
        this(bundleName, classLoader, false);
    }


    public PlatformResourceBundleLocator(String bundleName, ClassLoader classLoader, boolean aggregate) {
        SwakAssert.notBlank(bundleName, "bundleName");
        this.bundleName = bundleName;
        this.classLoader = classLoader;
        this.aggregate = aggregate && RESOURCE_BUNDLE_CONTROL_INSTANTIABLE;
    }


    @Override
    public ResourceBundle getResourceBundle(Locale locale) {
        ResourceBundle rb = null;

        if (classLoader != null) {
            rb = loadBundle(
                    classLoader, locale, "[Swak-I18n] "+bundleName
                            + " not found by user-provided classloader"
            );
        }
        if (rb == null) {
            ClassLoader classLoader = run(GetClassLoader.fromContext());
            if (classLoader != null) {
                rb = loadBundle(
                        classLoader, locale, "[Swak-I18n] "+bundleName
                                + " not found by thread context classloader"
                );
            }
        }
        if (rb == null) {
            ClassLoader classLoader = run(GetClassLoader.fromClass(PlatformResourceBundleLocator.class));
            rb = loadBundle(
                    classLoader, locale, "[Swak-I18n] "+bundleName
                            + " not found by validator classloader"
            );
        }
        if (rb != null) {
            log.debug("[Swak-I18n] {} found.", bundleName);
        } else {
            log.debug("[Swak-I18n] {} not found.", bundleName);
        }
        return rb;
    }

    private ResourceBundle loadBundle(ClassLoader classLoader, Locale locale, String message) {
        ResourceBundle rb = null;
        try {
            if (aggregate) {
                rb = ResourceBundle.getBundle(
                        bundleName,
                        locale,
                        classLoader,
                        PlatformResourceBundleLocator.AggregateResourceBundle.CONTROL
                );
            } else {
                rb = ResourceBundle.getBundle(
                        bundleName,
                        locale,
                        classLoader
                );
            }
        } catch (MissingResourceException e) {
            log.trace(message);
        }
        return rb;
    }


    private static <T> T run(PrivilegedAction<T> action) {
        return System.getSecurityManager() != null ? AccessController.doPrivileged(action) : action.run();
    }


    private static boolean determineAvailabilityOfResourceBundleControl() {
        try {
            ResourceBundle.Control dummyControl = PlatformResourceBundleLocator.AggregateResourceBundle.CONTROL;
            if (dummyControl == null) {
                return false;
            }
            Method getModule = run(GetMethod.action(Class.class, "getModule"));
            // not on Java 9
            if (getModule == null) {
                return true;
            }
            // on Java 9, check whether HV is a named module
            Object module = getModule.invoke(PlatformResourceBundleLocator.class);
            Method isNamedMethod = run(GetMethod.action(module.getClass(), "isNamed"));
            boolean isNamed = (Boolean) isNamedMethod.invoke(module);
            return !isNamed;
        } catch (Throwable e) {
            log.info("[Swak-I18n] cannot instantiate AggregateResourceBundle.CONTROL");
            return false;
        }
    }

    /**
     * Inspired by <a href="http://stackoverflow.com/questions/4614465/is-it-possible-to-include-resource-bundle-files-within-a-resource-bundle">this</a>
     * Stack Overflow question.
     */
    private static class AggregateResourceBundle extends ResourceBundle {

        protected static final Control CONTROL = new PlatformResourceBundleLocator.AggregateResourceBundleControl();
        private final Properties properties;

        protected AggregateResourceBundle(Properties properties) {
            this.properties = properties;
        }

        @Override
        protected Object handleGetObject(String key) {
            return properties.get(key);
        }

        @Override
        public Enumeration<String> getKeys() {
            Set<String> keySet = Sets.newHashSet();
            keySet.addAll(properties.stringPropertyNames());
            if (parent != null) {
                keySet.addAll(Collections.list(parent.getKeys()));
            }
            return Collections.enumeration(keySet);
        }
    }

    private static class AggregateResourceBundleControl extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(
                String baseName,
                Locale locale,
                String format,
                ClassLoader loader,
                boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            // only *.properties files can be aggregated. Other formats are delegated to the default implementation
            if (!"java.properties".equals(format)) {
                return super.newBundle(baseName, locale, format, loader, reload);
            }
            String resourceName = toBundleName(baseName, locale) + ".properties";
            Properties properties = load(resourceName, loader);
            return properties.size() == 0 ? null :
                    new PlatformResourceBundleLocator.AggregateResourceBundle(properties);
        }

        private Properties load(String resourceName, ClassLoader loader) throws IOException {
            Properties aggregatedProperties = new Properties();
            Enumeration<URL> urls = run(GetResources.action(loader, resourceName));
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                log.info("[Swak-I18n] load resourceName :{}", url.toString());
                Properties properties = new Properties();
                properties.load(url.openStream());
                aggregatedProperties.putAll(properties);
            }
            return aggregatedProperties;
        }
    }
}