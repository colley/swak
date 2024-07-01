package com.swak.openid4j.discovery.html;

import com.google.inject.Inject;
import com.swak.openid4j.discovery.DiscoveryInformation;
import com.swak.openid4j.discovery.UrlIdentifier;
import com.swak.openid4j.exception.DiscoveryException;
import com.swak.openid4j.exception.OpenIDException;
import com.swak.openid4j.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class HtmlResolver {
    private static final boolean DEBUG = log.isDebugEnabled();

    private static final String HTML_PARSER_CLASS_NAME_KEY = "discovery.html.parser";
    private static final HtmlParser HTML_PARSER;

    private final HttpFetcher _httpFetcher;

    static {
        String className = OpenID4jUtils.getProperty(HTML_PARSER_CLASS_NAME_KEY);
        if (DEBUG) log.debug(HTML_PARSER_CLASS_NAME_KEY + ":" + className);
        try {
            HTML_PARSER = (HtmlParser) Class.forName(className).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Inject
    public HtmlResolver(HttpFetcherFactory httpFetcherFactory) {
        _httpFetcher = httpFetcherFactory.createFetcher(
                HttpRequestOptions.getDefaultOptionsForDiscovery());
    }

    /**
     * Maximum number of redirects to be followed for the HTTP calls.
     */
    private int _maxRedirects = 10;

    /**
     * Gets the internal limit configured for the maximum number of redirects
     * to be followed for the HTTP calls.
     */
    public int getMaxRedirects() {
        return _maxRedirects;
    }

    /**
     * Sets the maximum number of redirects to be followed for the HTTP calls.
     */
    public void setMaxRedirects(int maxRedirects) {
        this._maxRedirects = maxRedirects;
    }

    /**
     * Performs HTML discovery on the supplied URL identifier.
     *
     * @param identifier The URL identifier.
     * @return List of DiscoveryInformation entries discovered
     * obtained from the URL Identifier.
     */
    public List discoverHtml(UrlIdentifier identifier)
            throws DiscoveryException {
        return discoverHtml(identifier, _httpFetcher);
    }

    /**
     * Performs HTML discovery on the supplied URL identifier.
     *
     * @param identifier  The URL identifier.
     * @param httpFetcher {@link HttpFetcher} object to use for placing the call.
     * @return List of DiscoveryInformation entries discovered
     * obtained from the URL Identifier.
     */
    public List discoverHtml(UrlIdentifier identifier, HttpFetcher httpFetcher)
            throws DiscoveryException {
        // initialize the results of the HTML discovery
        HtmlResult result = new HtmlResult();

        HttpRequestOptions requestOptions = httpFetcher.getRequestOptions();
        requestOptions.setContentType("text/html");

        try {
            HttpResponse resp = httpFetcher.get(identifier.toString(), requestOptions);

            if (HttpStatus.SC_OK != resp.getStatusCode())
                throw new DiscoveryException("GET failed on " +
                        identifier.toString() +
                        " Received status code: " + resp.getStatusCode(),
                        OpenIDException.DISCOVERY_HTML_GET_ERROR);

            result.setClaimed(new UrlIdentifier(resp.getFinalUri()));

            if (resp.getBody() == null)
                throw new DiscoveryException(
                        "No HTML data read from " + identifier.toString(),
                        OpenIDException.DISCOVERY_HTML_NODATA_ERROR);

            HTML_PARSER.parseHtml(resp.getBody(), result);
        } catch (IOException e) {
            throw new DiscoveryException("Fatal transport error: ",
                    OpenIDException.DISCOVERY_HTML_GET_ERROR, e);
        }

        log.info("HTML discovery completed on: " + identifier);

        return extractDiscoveryInformation(result);
    }

    /**
     * Extracts OpenID discovery endpoints from a HTML discovery result.
     *
     * @param htmlResult HTML discovery result.
     * @return List of DiscoveryInformation endpoints.
     * @throws DiscoveryException when invalid information is discovered.
     */
    private List extractDiscoveryInformation(HtmlResult htmlResult)
            throws DiscoveryException {
        ArrayList htmlList = new ArrayList();

        if (htmlResult.getOP2Endpoint() != null) {
            DiscoveryInformation extracted = new DiscoveryInformation(
                    htmlResult.getOP2Endpoint(),
                    htmlResult.getClaimedId(),
                    htmlResult.getDelegate2(),
                    DiscoveryInformation.OPENID2);

            if (DEBUG)
                log.debug("OpenID2-signon HTML discovery endpoint: " + extracted);

            htmlList.add(extracted);
        }

        if (htmlResult.getOP1Endpoint() != null) {
            DiscoveryInformation extracted = new DiscoveryInformation(
                    htmlResult.getOP1Endpoint(),
                    htmlResult.getClaimedId(),
                    htmlResult.getDelegate1(),
                    DiscoveryInformation.OPENID11);

            if (DEBUG)
                log.debug("OpenID1-signon HTML discovery endpoint: " + extracted);

            htmlList.add(extracted);
        }

        return htmlList;
    }

}
