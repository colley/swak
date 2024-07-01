package com.swak.openid4j.discovery.xri;

import com.google.inject.ImplementedBy;
import com.swak.openid4j.discovery.XriIdentifier;
import com.swak.openid4j.exception.DiscoveryException;

import java.util.List;

@ImplementedBy(XriDotNetProxyResolver.class)
public interface XriResolver {
    List discover(XriIdentifier paramXriIdentifier) throws DiscoveryException;

    XriIdentifier parseIdentifier(String paramString) throws DiscoveryException;
}
