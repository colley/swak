

package com.swak.openid4j.discovery.yadis;

import com.swak.openid4j.exception.OpenIDException;
import com.swak.openid4j.exception.YadisException;
import com.swak.openid4j.util.OpenID4jDOMParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.html.dom.HTMLDocumentImpl;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLHeadElement;
import org.w3c.dom.html.HTMLMetaElement;

import javax.xml.transform.TransformerException;


@Slf4j
public class CyberNekoDOMYadisHtmlParser implements YadisHtmlParser {
    private static final boolean DEBUG = log.isDebugEnabled();

    /*
     * (non-Javadoc)
     *
     * @see org.openid4java.discovery.yadis.YadisParser#getHtmlMeta(java.lang.String)
     */
    public String getHtmlMeta(String input) throws YadisException {
        String xrdsLocation = null;

        HTMLDocumentImpl doc = this.parseDocument(input);
        if (DEBUG) {
            try {
                log.debug("document:\n" + OpenID4jDOMParser.toXmlString(doc));
            } catch (TransformerException e) {
                log.debug("An exception occurs while transforming the document to string in debugging.", e);
            }
        }

        NodeList heads = doc.getElementsByTagName("head");
        if (heads.getLength() != 1)
            throw new YadisException(
                    "HTML response must have exactly one HEAD element, "
                            + "found " + heads.getLength() + " : "
                            + heads.toString(),
                    OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE);

        HTMLHeadElement head = (HTMLHeadElement) doc.getHead();
        NodeList metaElements = head.getElementsByTagName("META");
        if (metaElements == null || metaElements.getLength() == 0) {
            if (DEBUG)
                log.debug("No <meta> element found under <html><head>. " +
                        "See Yadis specification, section 6.2.5/1.");
        } else {
            for (int i = 0, len = metaElements.getLength(); i < len; i++) {
                HTMLMetaElement metaElement = (HTMLMetaElement) metaElements.item(i);

                String httpEquiv = metaElement.getHttpEquiv();
                if (YadisResolver.YADIS_XRDS_LOCATION.equalsIgnoreCase(httpEquiv)) {
                    if (xrdsLocation != null)
                        throw new YadisException(
                                "More than one "
                                        + YadisResolver.YADIS_XRDS_LOCATION
                                        + "META tags found in HEAD: "
                                        + head.toString(),
                                OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE);

                    xrdsLocation = metaElement.getContent();
                    if (DEBUG)
                        log.debug("Found " + YadisResolver.YADIS_XRDS_LOCATION
                                + " META tags.");
                }
            }
        }
        return xrdsLocation;
    }

    private HTMLDocumentImpl parseDocument(String htmlData) throws YadisException {
        OpenID4jDOMParser parser = new OpenID4jDOMParser();
        try {
            parser.parse(OpenID4jDOMParser.createInputSource(htmlData));
        } catch (Exception e) {
            throw new YadisException("Error parsing HTML message",
                    OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE, e);
        }

        if (parser.isIgnoredHeadStartElement()) {
            throw new YadisException("HTML response must have exactly one HEAD element.",
                    OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE);
        }

        return (HTMLDocumentImpl) parser.getDocument();
    }
}
