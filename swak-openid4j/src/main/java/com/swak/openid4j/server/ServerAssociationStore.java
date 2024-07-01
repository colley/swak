
package com.swak.openid4j.server;

import com.swak.openid4j.association.Association;
import com.swak.openid4j.exception.AssociationException;


public interface ServerAssociationStore {
    Association generate(String type, int expiryIn) throws AssociationException;

    Association load(String handle);

    void remove(String handle);
}
