package com.swak.i18n.aggregate;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public interface ResourceBundleLocator {

    ResourceBundle getResourceBundle(Locale locale);
}
