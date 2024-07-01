package com.swak.i18n.aggregate;

import com.google.common.collect.Lists;
import com.swak.common.exception.SwakAssert;
import org.hibernate.validator.internal.util.CollectionHelper;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class AggregateResourceBundleLocator extends DelegatingResourceBundleLocator {
    private final List<String> bundleNames;
    private final ClassLoader classLoader;

    private  boolean aggregate;

    public AggregateResourceBundleLocator(List<String> bundleNames) {
        this( bundleNames, null,false);
    }

    public AggregateResourceBundleLocator(List<String> bundleNames,ClassLoader classLoader) {
        this( bundleNames, null,classLoader);
    }

    public AggregateResourceBundleLocator(List<String> bundleNames,ClassLoader classLoader,boolean aggregate) {
        this( bundleNames, null,classLoader);
        this.aggregate = aggregate;
    }


    public AggregateResourceBundleLocator(List<String> bundleNames, ResourceBundleLocator delegate) {
        this( bundleNames, delegate, null );
    }

    public AggregateResourceBundleLocator(List<String> bundleNames, ResourceBundleLocator delegate,
                                          ClassLoader classLoader) {
        super(delegate);
        SwakAssert.notEmpty( bundleNames,"bundleNames");
        this.bundleNames = Lists.newArrayList(bundleNames);
        this.classLoader = classLoader;
    }

    @Override
    public ResourceBundle getResourceBundle(Locale locale) {
        List<ResourceBundle> sourceBundles = new ArrayList<ResourceBundle>();
        for ( String bundleName : bundleNames ) {
            ResourceBundleLocator resourceBundleLocator =
                    new PlatformResourceBundleLocator( bundleName, classLoader,aggregate);
            ResourceBundle resourceBundle = resourceBundleLocator.getResourceBundle(locale);
            if ( resourceBundle != null ) {
                sourceBundles.add(resourceBundle);
            }
        }
        ResourceBundle bundleFromDelegate = super.getResourceBundle( locale );
        if ( bundleFromDelegate != null ) {
            sourceBundles.add( bundleFromDelegate );
        }
        return sourceBundles.isEmpty() ? null :
                new AggregateBundle( sourceBundles );
    }
}
