package com.swak.openid4j.server;

import com.swak.openid4j.association.Association;
import com.swak.openid4j.exception.AssociationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;


public class InMemoryServerAssociationStore implements ServerAssociationStore {
    private static Log _log = LogFactory.getLog(InMemoryServerAssociationStore.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    private String _timestamp;
    private int _counter;
    private Map _handleMap;

    public InMemoryServerAssociationStore() {
        _timestamp = Long.toString(new Date().getTime());
        _counter = 0;
        _handleMap = new HashMap();
    }

    public synchronized Association generate(String type, int expiryIn)
            throws AssociationException {
        removeExpired();

        String handle = _timestamp + "-" + _counter++;

        Association association = Association.generate(type, handle, expiryIn);

        _handleMap.put(handle, association);

        if (DEBUG) _log.debug("Generated association, handle: " + handle +
                " type: " + type +
                " expires in: " + expiryIn + " seconds.");

        return association;
    }

    public synchronized Association load(String handle) {
        removeExpired();

        return (Association) _handleMap.get(handle);
    }

    public synchronized void remove(String handle) {
        if (DEBUG) _log.debug("Removing association, handle: " + handle);

        _handleMap.remove(handle);

        removeExpired();
    }

    private synchronized void removeExpired() {
        Set handleToRemove = new HashSet();
        Iterator handles = _handleMap.keySet().iterator();
        while (handles.hasNext()) {
            String handle = (String) handles.next();

            Association association = (Association) _handleMap.get(handle);

            if (association.hasExpired())
                handleToRemove.add(handle);
        }

        handles = handleToRemove.iterator();
        while (handles.hasNext()) {
            String handle = (String) handles.next();

            if (DEBUG) _log.debug("Removing expired association, handle: " + handle);

            _handleMap.remove(handle);
        }
    }

    protected synchronized int size() {
        return _handleMap.size();
    }
}
