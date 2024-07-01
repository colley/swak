

package com.swak.openid4j.discovery.html;

import com.swak.openid4j.exception.DiscoveryException;
import com.swak.openid4j.exception.OpenIDException;
import com.swak.openid4j.util.OpenID4jDOMParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.html.dom.HTMLDocumentImpl;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLHeadElement;
import org.w3c.dom.html.HTMLLinkElement;

import java.util.Arrays;
import java.util.List;

/**
 * A {@link HtmlParser} implementation using the DOMParser of CyberNeko HTML.
 */
@Slf4j
public class CyberNekoDOMHtmlParser implements HtmlParser {
    private static final boolean DEBUG = log.isDebugEnabled();

    /*
     * (non-Javadoc)
     *
     * @see org.openid4java.discovery.html.HtmlParser#parse(java.lang.String,
     *      org.openid4java.discovery.html.HtmlResult)
     */
    public void parseHtml(String htmlData, HtmlResult result)
            throws DiscoveryException {
        if (DEBUG)
            log.debug("Parsing HTML data:\n" + htmlData);

        HTMLDocumentImpl doc = this.parseDocument(htmlData);

        NodeList heads = doc.getElementsByTagName("head");
        if (heads.getLength() != 1)
            throw new DiscoveryException(
                    "HTML response must have exactly one HEAD element, "
                            + "found " + heads.getLength() + " : "
                            + heads.toString(),
                    OpenIDException.DISCOVERY_HTML_PARSE_ERROR);

        HTMLHeadElement head = (HTMLHeadElement) doc.getHead();
        NodeList linkElements = head.getElementsByTagName("LINK");
        for (int i = 0, len = linkElements.getLength(); i < len; i++) {
            HTMLLinkElement linkElement = (HTMLLinkElement) linkElements.item(i);
            setResult(linkElement.getRel(), linkElement.getHref(), result);
        }

        if (DEBUG)
            log.debug("HTML discovery result:\n" + result);
    }

    private HTMLDocumentImpl parseDocument(String htmlData) throws DiscoveryException {
        OpenID4jDOMParser parser = new OpenID4jDOMParser();
        try {
            parser.parse(OpenID4jDOMParser.createInputSource(htmlData));
        } catch (Exception e) {
            throw new DiscoveryException("Error parsing HTML message",
                    OpenIDException.DISCOVERY_HTML_PARSE_ERROR, e);
        }

        if (parser.isIgnoredHeadStartElement()) {
            throw new DiscoveryException(
                    "HTML response must have exactly one HEAD element.",
                    OpenIDException.DISCOVERY_HTML_PARSE_ERROR);
        }

        return (HTMLDocumentImpl) parser.getDocument();
    }

    /**
     * Set the result from <code>rel</code> and <code>href</code> that
     * parsed from node <code>link</code>.
     *
     * @param rel    the <code>rel</code>
     * @param href   the <code>href</code>
     * @param result the result to set
     * @throws DiscoveryException if the value has been setted yet, that is to say, find more
     *                            than one entries with the same name(attribute value of
     *                            <code>rel</code>).
     */
    private void setResult(String rel, String href, HtmlResult result)
            throws DiscoveryException {
        List relations = Arrays.asList(rel.split(" "));

        // openid.server
        if (relations.contains("openid.server")) {
            if (result.getOP1Endpoint() != null)
                throw new DiscoveryException(
                        "More than one openid.server entries found",
                        OpenIDException.DISCOVERY_HTML_PARSE_ERROR);

            if (DEBUG)
                log.debug("Found OpenID1 endpoint: " + href);

            result.setEndpoint1(href);
        }

        // openid.delegate
        if (relations.contains("openid.delegate")) {
            if (result.getDelegate1() != null)
                throw new DiscoveryException(
                        "More than one openid.delegate entries found",
                        OpenIDException.DISCOVERY_HTML_PARSE_ERROR);

            if (DEBUG)
                log.debug("Found OpenID1 delegate: " + href);

            result.setDelegate1(href);
        }

        // openid2.provider
        if (relations.contains("openid2.provider")) {
            if (result.getOP2Endpoint() != null)
                throw new DiscoveryException(
                        "More than one openid.server entries found",
                        OpenIDException.DISCOVERY_HTML_PARSE_ERROR);

            if (DEBUG)
                log.debug("Found OpenID2 endpoint: " + href);

            result.setEndpoint2(href);
        }

        // openid2.local_id
        if (relations.contains("openid2.local_id")) {
            if (result.getDelegate2() != null)
                throw new DiscoveryException(
                        "More than one openid2.local_id entries found",
                        OpenIDException.DISCOVERY_HTML_PARSE_ERROR);

            if (DEBUG)
                log.debug("Found OpenID2 localID: " + href);

            result.setDelegate2(href);
        }
    }
}
