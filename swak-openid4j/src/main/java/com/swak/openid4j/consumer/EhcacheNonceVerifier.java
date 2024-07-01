
package com.swak.openid4j.consumer;

import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import java.util.Date;

@Slf4j
public class EhcacheNonceVerifier extends AbstractNonceVerifier {
    private static final boolean DEBUG = log.isDebugEnabled();

    private Cache _cache;

    public EhcacheNonceVerifier(int maxAge) {
        super(maxAge);
    }

    public void setCache(Cache cache) {
        if (cache.getCacheConfiguration().getTimeToLiveSeconds() != _maxAgeSeconds) {
            throw new IllegalArgumentException("Max Age: " + _maxAgeSeconds + ", same expected for cache, but found: " + cache.getCacheConfiguration().getTimeToLiveSeconds());
        }

        if (cache.getCacheConfiguration().getTimeToLiveSeconds() != cache.getCacheConfiguration().getTimeToIdleSeconds()) {
            throw new IllegalArgumentException("Cache must have same timeToLive (" + cache.getCacheConfiguration().
                    getTimeToLiveSeconds() + ") as timeToIdle (" + cache.getCacheConfiguration().getTimeToIdleSeconds() + ")");
        }

        _cache = cache;
    }

    protected int seen(Date now, String opUrl, String nonce) {
        String pair = opUrl + '#' + nonce;
        Element element = new Element(pair, pair);

        if (_cache.get(pair) != null) {
            log.error("Possible replay attack! Already seen nonce: " + nonce);
            return SEEN;
        }

        _cache.put(element);

        if (DEBUG) log.debug("Nonce verified: " + nonce);

        return OK;
    }
}
