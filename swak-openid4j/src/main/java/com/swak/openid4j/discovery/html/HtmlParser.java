package com.swak.openid4j.discovery.html;

import com.swak.openid4j.exception.DiscoveryException;

/**
 * Html parser.
 */
public interface HtmlParser {
    /**
     * Parses the HTML data and stores in the result the discovered openid
     * information.
     *
     * @param htmlData HTML data obtained from the URL identifier.
     * @param result   The HTML result.
     * @throws DiscoveryException
     */
    void parseHtml(String htmlData, HtmlResult result) throws DiscoveryException;
}
