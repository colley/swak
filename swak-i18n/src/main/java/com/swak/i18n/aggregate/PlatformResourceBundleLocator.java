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

    /**
     * Creates a new {@link PlatformResourceBundleLocator}.
     *
     * @param bundleName  the name of the bundle to load
     * @param classLoader the classloader to be used for loading the bundle. If {@code null}, the current thread context
     *                    classloader and finally Hibernate Validator's own classloader will be used for loading the specified
     *                    bundle.
     * @since 5.2
     */
    public PlatformResourceBundleLocator(String bundleName, ClassLoader classLoader) {
        this(bundleName, classLoader, false);
    }

    /**
     * Creates a new {@link PlatformResourceBundleLocator}.
     *
     * @param bundleName  the name of the bundle to load
     * @param classLoader the classloader to be used for loading the bundle. If {@code null}, the current thread context
     *                    classloader and finally Hibernate Validator's own classloader will be used for loading the specified
     *                    bundle.
     * @param aggregate   Whether or not all resource bundles of a given name should be loaded and potentially merged.
     * @since 5.2
     */
    public PlatformResourceBundleLocator(String bundleName, ClassLoader classLoader, boolean aggregate) {
        SwakAssert.notBlank(bundleName, "bundleName");
        this.bundleName = bundleName;
        this.classLoader = classLoader;
        this.aggregate = aggregate && RESOURCE_BUNDLE_CONTROL_INSTANTIABLE;
    }

    /**
     * Search current thread classloader for the resource bundle. If not found,
     * search validator (this) classloader.
     *
     * @param locale The locale of the bundle to load.
     * @return the resource bundle or {@code null} if none is found.
     */
    @Override
    public ResourceBundle getResourceBundle(Locale locale) {
        ResourceBundle rb = null;

        if (classLoader != null) {
            rb = loadBundle(
                    classLoader, locale, bundleName
                            + " not found by user-provided classloader"
            );
        }
        if (rb == null) {
            ClassLoader classLoader = run(GetClassLoader.fromContext());
            if (classLoader != null) {
                rb = loadBundle(
                        classLoader, locale, bundleName
                                + " not found by thread context classloader"
                );
            }
        }
        if (rb == null) {
            ClassLoader classLoader = run(GetClassLoader.fromClass(PlatformResourceBundleLocator.class));
            rb = loadBundle(
                    classLoader, locale, bundleName
                            + " not found by validator classloader"
            );
        }
        if (rb != null) {
            log.debug("{} found.", bundleName);
        } else {
            log.debug("{} not found.", bundleName);
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

    /**
     * Runs the given privileged action, using a privileged block if required.
     * <p>
     * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
     * privileged actions within HV's protection domain.
     */
    private static <T> T run(PrivilegedAction<T> action) {
        return System.getSecurityManager() != null ? AccessController.doPrivileged(action) : action.run();
    }

    /**
     * Check whether ResourceBundle.Control is available, which is needed for bundle aggregation. If not, we'll skip
     * resource aggregation.
     * <p>
     * It is *not* available
     * <ul>
     * <li>in the Google App Engine environment</li>
     * <li>when running HV as Java 9 named module (which would be the case when adding a module-info descriptor to the
     * HV JAR)</li>
     * </ul>
     *
     * @see <a href="http://code.google.com/appengine/docs/java/jrewhitelist.html">GAE JRE whitelist</a>
     * @see <a href="https://hibernate.atlassian.net/browse/HV-1023">HV-1023</a>
     * @see <a href="http://download.java.net/java/jdk9/docs/api/java/util/ResourceBundle.Control.html>ResourceBundle.Control</a>
     */
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
            log.info("cannot instantiate AggregateResourceBundle.CONTROL");
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
                log.info("load resourceName :{}", url.toString());
                Properties properties = new Properties();
                properties.load(url.openStream());
                aggregatedProperties.putAll(properties);
            }
            return aggregatedProperties;
        }
    }
}