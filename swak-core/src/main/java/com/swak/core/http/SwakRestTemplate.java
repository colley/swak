package com.swak.core.http;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class SwakRestTemplate implements SwakRestOperations {

    private RestTemplate restTemplate;

    public SwakRestTemplate() {
        this.restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    }

    public SwakRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * GET请求
     *
     * @param url
     * @param params
     */
    @Override
    public <T> T getRequest(String url, Map<String, Object> params, Class<T> responseType) {
        return getRequest(url, params, responseType, Charset.forName("UTF-8"));
    }

    /**
     * POST请求
     *
     * @param url
     * @param params
     */
    @Override
    public <T> T postRequest(String url, Map<String, Object> params, Class<T> responseType) {
        return postRequest(url, params, responseType, Charset.forName("UTF-8"));
    }

    @Override
    public <T> T getRequest(String url, Map<String, Object> params, Class<T> responseType, Charset charset) {
        log.info("request url:{},params:{}", url, JSON.toJSONString(params));
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParams(multiValueParam(params));
        return getForObject(builder.build().encode(charset).toUri(), responseType);

    }

    /**
     * POST请求
     *
     * @param url
     * @param params
     */
    @Override
    public <T> T postRequest(String url, Map<String, Object> params, Class<T> responseType, Charset charset) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParams(multiValueParam(params));

        return postForObject(builder.build().encode(charset).toUri(), null, responseType);
    }

    private MultiValueMap<String, String> multiValueParam(Map<String, Object> params) {
        MultiValueMap<String, String> multiValueParam = new LinkedMultiValueMap<String, String>();
        if (MapUtils.isNotEmpty(params)) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = Objects.toString(entry.getValue());
                multiValueParam.add(key, value);
            }
        }

        return multiValueParam;
    }

    @Override
    public <T> T getForObject(String url, Class<T> responseType, Object... uriVariables) throws RestClientException {
        return restTemplate.getForObject(url, responseType, uriVariables);
    }

    @Override
    public <T> T getForObject(String url, Class<T> responseType, Map<String, ?> uriVariables)
            throws RestClientException {
        return null;
    }

    @Override
    public <T> T getForObject(URI url, Class<T> responseType) throws RestClientException {
        return restTemplate.getForObject(url, responseType);
    }

    @Override
    public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Object... uriVariables)
            throws RestClientException {
        return restTemplate.getForEntity(url, responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Map<String, ?> uriVariables)
            throws RestClientException {
        return restTemplate.getForEntity(url, responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> getForEntity(URI url, Class<T> responseType) throws RestClientException {
        return restTemplate.getForEntity(url, responseType);
    }

    @Override
    public HttpHeaders headForHeaders(String url, Object... uriVariables) throws RestClientException {
        return restTemplate.headForHeaders(url, uriVariables);
    }

    @Override
    public HttpHeaders headForHeaders(String url, Map<String, ?> uriVariables) throws RestClientException {
        return restTemplate.headForHeaders(url, uriVariables);
    }

    @Override
    public HttpHeaders headForHeaders(URI url) throws RestClientException {
        return restTemplate.headForHeaders(url);
    }

    @Override
    public URI postForLocation(String url, Object request, Object... uriVariables) throws RestClientException {
        return restTemplate.postForLocation(url, request, uriVariables);
    }

    @Override
    public URI postForLocation(String url, Object request, Map<String, ?> uriVariables) throws RestClientException {
        return restTemplate.postForLocation(url, request, uriVariables);
    }

    @Override
    public URI postForLocation(URI url, Object request) throws RestClientException {
        return restTemplate.postForLocation(url, request);
    }

    @Override
    public <T> T postForObject(String url, Object request, Class<T> responseType, Object... uriVariables)
            throws RestClientException {
        return restTemplate.postForObject(url, request, responseType, uriVariables);
    }

    @Override
    public <T> T postForObject(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables)
            throws RestClientException {
        return restTemplate.postForObject(url, request, responseType, uriVariables);
    }

    @Override
    public <T> T postForObject(URI url, Object request, Class<T> responseType) throws RestClientException {
        log.info("request url:{}", url.toString());
        return restTemplate.postForObject(url, request, responseType);
    }

    @Override
    public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType,
                                               Object... uriVariables) throws RestClientException {
        return restTemplate.postForEntity(url, request, responseType);
    }

    @Override
    public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType,
                                               Map<String, ?> uriVariables) throws RestClientException {
        return restTemplate.postForEntity(url, request, responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> postForEntity(URI url, Object request, Class<T> responseType)
            throws RestClientException {
        return restTemplate.postForEntity(url, request, responseType);
    }

    @Override
    public void put(String url, Object request, Object... uriVariables) throws RestClientException {
        restTemplate.put(url, request, uriVariables);
    }

    @Override
    public void put(String url, Object request, Map<String, ?> uriVariables) throws RestClientException {
        restTemplate.put(url, request, uriVariables);
    }

    @Override
    public void put(URI url, Object request) throws RestClientException {
        restTemplate.put(url, request);
    }

    @Override
    public <T> T patchForObject(String url, Object request, Class<T> responseType, Object... uriVariables)
            throws RestClientException {
        return restTemplate.patchForObject(url, request, responseType, uriVariables);
    }

    @Override
    public <T> T patchForObject(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables)
            throws RestClientException {
        return restTemplate.patchForObject(url, request, responseType, uriVariables);
    }

    @Override
    public <T> T patchForObject(URI url, Object request, Class<T> responseType) throws RestClientException {
        return restTemplate.patchForObject(url, request, responseType);
    }

    @Override
    public void delete(String url, Object... uriVariables) throws RestClientException {
        restTemplate.delete(url, uriVariables);
    }

    @Override
    public void delete(String url, Map<String, ?> uriVariables) throws RestClientException {
        restTemplate.delete(url, uriVariables);
    }

    @Override
    public void delete(URI url) throws RestClientException {
        restTemplate.delete(url);
    }

    @Override
    public Set<HttpMethod> optionsForAllow(String url, Object... uriVariables) throws RestClientException {
        return restTemplate.optionsForAllow(url, uriVariables);
    }

    @Override
    public Set<HttpMethod> optionsForAllow(String url, Map<String, ?> uriVariables) throws RestClientException {
        return restTemplate.optionsForAllow(url, uriVariables);
    }

    @Override
    public Set<HttpMethod> optionsForAllow(URI url) throws RestClientException {
        return restTemplate.optionsForAllow(url);
    }

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
                                          Class<T> responseType, Object... uriVariables) throws RestClientException {
        return restTemplate.exchange(url, method, requestEntity, responseType, uriVariables);

    }

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
                                          Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
        return restTemplate.exchange(url, method, requestEntity, responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity,
                                          Class<T> responseType) throws RestClientException {
        return restTemplate.exchange(url, method, requestEntity, responseType);
    }

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
                                          ParameterizedTypeReference<T> responseType, Object... uriVariables) throws RestClientException {
        return restTemplate.exchange(url, method, requestEntity, responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
                                          ParameterizedTypeReference<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
        return restTemplate.exchange(url, method, requestEntity, responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity,
                                          ParameterizedTypeReference<T> responseType) throws RestClientException {
        return restTemplate.exchange(url, method, requestEntity, responseType);
    }

    @Override
    public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity, Class<T> responseType)
            throws RestClientException {
        return restTemplate.exchange(requestEntity, responseType);
    }

    @Override
    public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity, ParameterizedTypeReference<T> responseType)
            throws RestClientException {
        return restTemplate.exchange(requestEntity, responseType);
    }

    @Override
    public <T> T execute(String url, HttpMethod method, RequestCallback requestCallback,
                         ResponseExtractor<T> responseExtractor, Object... uriVariables) throws RestClientException {
        return restTemplate.execute(url, method, requestCallback, responseExtractor, uriVariables);
    }

    @Override
    public <T> T execute(String url, HttpMethod method, RequestCallback requestCallback,
                         ResponseExtractor<T> responseExtractor, Map<String, ?> uriVariables) throws RestClientException {
        return restTemplate.execute(url, method, requestCallback, responseExtractor, uriVariables);
    }

    @Override
    public <T> T execute(URI url, HttpMethod method, RequestCallback requestCallback,
                         ResponseExtractor<T> responseExtractor) throws RestClientException {
        return restTemplate.execute(url, method, requestCallback, responseExtractor);
    }
}