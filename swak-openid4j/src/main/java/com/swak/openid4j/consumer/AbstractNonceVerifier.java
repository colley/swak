package com.swak.openid4j.consumer;

import com.swak.openid4j.util.InternetDateFormat;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.util.Date;


@Slf4j
public abstract class AbstractNonceVerifier implements NonceVerifier {
    private static final boolean DEBUG = log.isDebugEnabled();

    protected static InternetDateFormat _dateFormat = new InternetDateFormat();

    protected int _maxAgeSeconds;

    /**
     * @param maxAge maximum token age in seconds
     */
    protected AbstractNonceVerifier(int maxAge) {
        _maxAgeSeconds = maxAge;
    }

    public int getMaxAge() {
        return _maxAgeSeconds;
    }

    public void setMaxAge(int ageSeconds) {
        _maxAgeSeconds = ageSeconds;
    }

    /**
     * Checks if nonce date is valid and if it is in the max age boundary. Other checks are delegated to {@link #seen(java.util.Date, String, String)}
     */
    public synchronized int seen(String opUrl, String nonce) {
        if (DEBUG) log.debug("Verifying nonce: " + nonce);

        Date now = new Date();

        try {
            Date nonceDate = _dateFormat.parse(nonce);

            if (isTooOld(now, nonceDate)) {
                log.warn("Nonce is too old: " + nonce);
                return TOO_OLD;
            }

            return seen(now, opUrl, nonce);
        } catch (ParseException e) {
            log.error("Error verifying the nonce: " + nonce, e);
            return INVALID_TIMESTAMP;
        }
    }

    /**
     * Subclasses should implement this method and check if the nonce was seen before.
     * The nonce timestamp was verified at this point, it is valid and it is in the max age boudary.
     *
     * @param now The timestamp used to check the max age boudary.
     */
    protected abstract int seen(Date now, String opUrl, String nonce);

    protected boolean isTooOld(Date now, Date nonce) {
        long age = now.getTime() - nonce.getTime();

        return age > _maxAgeSeconds * 1000;
    }
}
