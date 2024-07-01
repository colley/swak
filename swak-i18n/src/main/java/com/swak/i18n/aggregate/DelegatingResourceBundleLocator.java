package com.swak.i18n.aggregate;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class DelegatingResourceBundleLocator implements ResourceBundleLocator {

    private final ResourceBundleLocator delegate;

    public DelegatingResourceBundleLocator(ResourceBundleLocator delegate) {
        this.delegate = delegate;
    }

    @Override
    public ResourceBundle getResourceBundle(Locale locale) {
        return delegate == null ? null : delegate.getResourceBundle( locale );
    }
}
