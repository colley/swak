package com.swak.openid4j.util;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.http.conn.ssl.X509HostnameVerifier;

import javax.net.ssl.SSLContext;

public class HttpFetcherFactory {

    private final Provider<HttpFetcher> _provider;

    @Inject
    public HttpFetcherFactory(Provider<HttpFetcher> provider) {
        _provider = provider;
    }

    /**
     * Public constructor for non-Guice installations. Results in
     * {@link HttpCache} being used as the {@link HttpFetcher}
     */
    public HttpFetcherFactory() {
        this(new HttpCacheProvider());
    }

    public HttpFetcherFactory(SSLContext sslContext) {
        this(new HttpCacheProvider(sslContext));
    }

    public HttpFetcherFactory(SSLContext sslContext, X509HostnameVerifier hostnameVerifier) {
        this(new HttpCacheProvider(sslContext, hostnameVerifier));
    }

    public HttpFetcher createFetcher(HttpRequestOptions defaultOptions) {
        final HttpFetcher fetcher = _provider.get();
        fetcher.setDefaultRequestOptions(defaultOptions);
        return fetcher;
    }

    private static class HttpCacheProvider implements Provider<HttpFetcher> {

        private final SSLContext sslContext;

        private final X509HostnameVerifier hostnameVerifier;

        public HttpCacheProvider(SSLContext sslContext, X509HostnameVerifier hostnameVerifier) {
            this.sslContext = sslContext;
            this.hostnameVerifier = hostnameVerifier;
        }

        public HttpCacheProvider(SSLContext sslContext) {
            this(sslContext, null);
        }

        public HttpCacheProvider() {
            this(null, null);
        }

        public HttpFetcher get() {
            return new HttpCache(this.sslContext, this.hostnameVerifier);
        }
    }
}
