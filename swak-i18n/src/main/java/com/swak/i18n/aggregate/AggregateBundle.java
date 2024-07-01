package com.swak.i18n.aggregate;

import com.swak.i18n.util.IteratorEnumeration;

import java.util.*;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class AggregateBundle  extends ResourceBundle {
    private final Map<String, Object> contents = new HashMap<String, Object>();

    public AggregateBundle(List<ResourceBundle> bundles) {
        if ( bundles != null ) {
            for ( ResourceBundle bundle : bundles ) {
                Enumeration<String> keys = bundle.getKeys();
                while ( keys.hasMoreElements() ) {
                    String oneKey = keys.nextElement();
                    if ( !contents.containsKey( oneKey ) ) {
                        contents.put( oneKey, bundle.getObject( oneKey ) );
                    }
                }
            }
        }
    }

    @Override
    public Enumeration<String> getKeys() {
        return new IteratorEnumeration<>(contents.keySet().iterator());
    }

    @Override
    protected Object handleGetObject(String key) {
        return contents.get( key );
    }
}