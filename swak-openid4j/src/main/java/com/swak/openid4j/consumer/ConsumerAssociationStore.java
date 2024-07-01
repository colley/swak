/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package com.swak.openid4j.consumer;

import com.google.inject.ImplementedBy;
import com.swak.openid4j.association.Association;


@ImplementedBy(InMemoryConsumerAssociationStore.class)
public interface ConsumerAssociationStore {
    void save(String opUrl, Association association);

    Association load(String opUrl, String handle);

    Association load(String opUrl);

    void remove(String opUrl, String handle);
}
