
package com.swak.i18n.aggregate;

import java.io.IOException;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Enumeration;

 final class GetResources implements PrivilegedAction<Enumeration<URL>> {

    private final String resourceName;
    private final ClassLoader classLoader;

    public static GetResources action(ClassLoader classLoader, String resourceName) {
        return new GetResources( classLoader, resourceName );
    }

    private GetResources(ClassLoader classLoader, String resourceName) {
        this.classLoader = classLoader;
        this.resourceName = resourceName;
    }

    @Override
    public Enumeration<URL> run() {
        try {
            return classLoader.getResources( resourceName );
        }
        catch (IOException e) {
            // Collections.emptyEnumeration() would be 1.7
            return Collections.enumeration( Collections.<URL>emptyList() );
        }
    }
}
