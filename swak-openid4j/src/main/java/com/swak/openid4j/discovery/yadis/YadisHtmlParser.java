

package com.swak.openid4j.discovery.yadis;

import com.swak.openid4j.exception.YadisException;

public interface YadisHtmlParser {
    /**
     * Parses the HTML input stream and scans for the Yadis XRDS location in the
     * HTML HEAD Meta tags.
     *
     * @param input input data stream
     * @return String the XRDS location URL, or null if not found
     * @throws YadisException on parsing errors or Yadis protocal violations
     */
    String getHtmlMeta(String input) throws YadisException;
}
