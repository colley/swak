package com.swak.openid4j.discovery.xri;

import com.google.inject.Inject;
import com.swak.openid4j.discovery.DiscoveryInformation;
import com.swak.openid4j.discovery.XriIdentifier;
import com.swak.openid4j.discovery.xrds.XrdsParser;
import com.swak.openid4j.discovery.xrds.XrdsServiceEndpoint;
import com.swak.openid4j.exception.DiscoveryException;
import com.swak.openid4j.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class XriDotNetProxyResolver implements XriResolver {
    private static Logger _log = LoggerFactory.getLogger(XriDotNetProxyResolver.class);

    private static final boolean DEBUG = _log.isDebugEnabled();

    private final HttpFetcher _httpFetcher;

    private static final String PROXY_URL = "https://xri.net/";

    private static final String XRDS_QUERY = "_xrd_r=application/xrds+xml";

    private static final String XRDS_PARSER_CLASS_NAME_KEY = "discovery.xrds.parser";

    private static final XrdsParser XRDS_PARSER;

    static {
        String className = OpenID4jUtils.getProperty("discovery.xrds.parser");
        if (DEBUG)
            _log.debug("discovery.xrds.parser:" + className);
        try {
            XRDS_PARSER = (XrdsParser) Class.forName(className).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Inject
    public XriDotNetProxyResolver(HttpFetcherFactory httpFetcherfactory) {
        this._httpFetcher = httpFetcherfactory.createFetcher(HttpRequestOptions.getDefaultOptionsForDiscovery());
    }

    public XriDotNetProxyResolver() {
        this(new HttpFetcherFactory());
    }

    public List discover(XriIdentifier xri) throws DiscoveryException {
        String hxri = "https://xri.net/" + xri.getIdentifier() + "?" + "_xrd_r=application/xrds+xml";
        _log.info("Performing discovery on HXRI: " + hxri);
        try {
            HttpResponse resp = this._httpFetcher.get(hxri);
            if (resp == null || 200 != resp.getStatusCode())
                throw new DiscoveryException("Error retrieving HXRI: " + hxri);
            Set targetTypes = DiscoveryInformation.OPENID_OP_TYPES;
            List endpoints = XRDS_PARSER.parseXrds(resp.getBody(), targetTypes);
            List<DiscoveryInformation> results = new ArrayList();
            Iterator<XrdsServiceEndpoint> endpointIter = endpoints.iterator();
            while (endpointIter.hasNext()) {
                XrdsServiceEndpoint endpoint = endpointIter.next();
                Iterator<String> typesIter = endpoint.getTypes().iterator();
                while (typesIter.hasNext()) {
                    String type = typesIter.next();
                    if (!targetTypes.contains(type))
                        continue;
                    try {
                        results.add(new DiscoveryInformation(new URL(endpoint.getUri()), parseIdentifier(endpoint.getCanonicalId()), "http://specs.openid.net/auth/2.0/signon".equals(type) ? endpoint.getLocalId() : (DiscoveryInformation.OPENID1_SIGNON_TYPES.contains(type) ? endpoint.getDelegate() : null), type));
                    } catch (MalformedURLException e) {
                        throw new DiscoveryException("Invalid endpoint URL discovered: " + endpoint.getUri());
                    }
                }
            }
            return results;
        } catch (IOException e) {
            throw new DiscoveryException("Error performing discovery on HXRI: " + hxri);
        }
    }

    public XriIdentifier parseIdentifier(String identifier) throws DiscoveryException {
        _log.warn("Creating XRI identifier with the friendly XRI identifier as the IRI/URI normal forms.");
        return new XriIdentifier(identifier, identifier, identifier);
    }
}
