

package com.swak.openid4j.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A date formatter based on the Internet Date/Time format. This is defined in section 5.6 of RFC 3339.
 *
 * <p>A date formatted in this way looks like:<br />
 * <b><code>2005-05-15T17:11:51Z</code></b>
 *
 * @see <a href="http://www.ietf.org/rfc/rfc3339.txt">RFC 3339: section 5.6</a>
 */
public class InternetDateFormat extends SimpleDateFormat {
    private static Logger _log = LoggerFactory.getLogger(InternetDateFormat.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    public static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final TimeZone GMT_TIME_ZONE = TimeZone.getTimeZone("GMT");

    public InternetDateFormat() {
        super(PATTERN, Locale.SIMPLIFIED_CHINESE);
        setTimeZone(GMT_TIME_ZONE);
    }

    public Date parse(String source) throws ParseException {
        Date date = super.parse(source.toUpperCase());

        if (DEBUG) _log.debug("Parsed " + source + " into Data object: " + date);

        return date;
    }
}
