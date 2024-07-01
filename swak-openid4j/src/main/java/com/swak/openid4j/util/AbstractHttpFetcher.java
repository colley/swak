package com.swak.openid4j.util;

import java.io.IOException;
import java.util.Map;

public abstract class AbstractHttpFetcher implements HttpFetcher {

    /**
     * Default set of HTTP request options to be used when placing HTTP
     * requests, if a custom one was not specified.
     */
    private HttpRequestOptions _defaultOptions = new HttpRequestOptions();

    /* (non-Javadoc)
     * @see org.openid4java.util.HttpFetcher#get(java.lang.String)
     */
    public HttpResponse get(String url) throws IOException {
        return get(url, _defaultOptions);
    }

    public abstract HttpResponse get(String url, HttpRequestOptions requestOptions) throws IOException;

    /* (non-Javadoc)
     * @see org.openid4java.util.HttpFetcher#getDefaultRequestOptions()
     */
    public HttpRequestOptions getDefaultRequestOptions() {
        return _defaultOptions;
    }

    /* (non-Javadoc)
     * @see org.openid4java.util.HttpFetcher#getRequestOptions()
     */
    public HttpRequestOptions getRequestOptions() {
        return new HttpRequestOptions(_defaultOptions);
    }

    /* (non-Javadoc)
     * @see org.openid4java.util.HttpFetcher#head(java.lang.String)
     */
    public HttpResponse head(String url) throws IOException {
        return head(url, _defaultOptions);
    }

    public abstract HttpResponse post(String url, Map<String, String> parameters,
                                      HttpRequestOptions requestOptions) throws IOException;

    /* (non-Javadoc)
     * @see org.openid4java.util.HttpFetcher#head(java.lang.String)
     */
    public HttpResponse post(String url, Map<String, String> parameters) throws IOException {
        return post(url, parameters, _defaultOptions);
    }

    public abstract HttpResponse head(String url, HttpRequestOptions requestOptions) throws IOException;


    /* (non-Javadoc)
     * @see org.openid4java.util.HttpFetcher#setDefaultRequestOptions(org.openid4java.util.HttpRequestOptions)
     */
    public void setDefaultRequestOptions(HttpRequestOptions defaultOptions) {
        this._defaultOptions = defaultOptions;
    }
}
