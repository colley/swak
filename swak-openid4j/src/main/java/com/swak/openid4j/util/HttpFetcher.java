package com.swak.openid4j.util;

import com.google.inject.ImplementedBy;

import java.io.IOException;
import java.util.Map;

/**
 * Interface for fetching HTTP requests. The default implementation caches
 * responses, but can be replaced by custom implementations.
 */
@ImplementedBy(HttpCache.class)
public interface HttpFetcher {

    /**
     * Returns the default {@link HttpRequestOptions}. Note that this does
     * not return a clone, so manipulating the object returned here will
     * manipulate the {@link HttpRequestOptions} used by the {@link HttpFetcher}.
     */
     HttpRequestOptions getDefaultRequestOptions();

    /**
     * Gets a clone of the default HttpRequestOptions.
     */
     HttpRequestOptions getRequestOptions();

     void setDefaultRequestOptions(HttpRequestOptions defaultOptions);

    /**
     * GETs a HTTP URL. A cached copy will be returned if one exists.
     *
     * @param url The HTTP URL to GET.
     * @return A HttpResponse object containing the fetched data.
     * @see HttpResponse
     */
    public HttpResponse get(String url) throws IOException;

    /**
     * GETs a HTTP URL. A cached copy will be returned if one exists and the
     * supplied options match it.
     *
     * @param url The HTTP URL to GET.
     * @return A HttpResponse object containing the fetched data.
     */
     HttpResponse get(String url, HttpRequestOptions requestOptions) throws IOException;

     HttpResponse head(String url) throws IOException;

     HttpResponse head(String url, HttpRequestOptions requestOptions) throws IOException;

     HttpResponse post(String url, Map<String, String> parameters) throws IOException;

     HttpResponse post(String url, Map<String, String> parameters,
                             HttpRequestOptions requestOptions) throws IOException;

}
