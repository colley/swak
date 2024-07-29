package com.swak.security.spi;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.swak.common.dto.Response;

import java.util.concurrent.TimeUnit;

public class DefaultTokenJwtExchange implements TokenJwtExchange {

    private final Cache<String, String> USER_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS).maximumSize(500).build();


    @Override
    public void storeTokenJwt(String token, String userJwt, Long expireSeconds) {
        USER_CACHE.put(token, userJwt);
        Response.success();
    }

    @Override
    public String takeTokenJwt(String token) {
        return USER_CACHE.getIfPresent(token);
    }

    @Override
    public void refresh(String token, String userJwt, Long expireTime) {
        USER_CACHE.put(token, userJwt);
    }

    @Override
    public void remove(String token) {
        USER_CACHE.invalidate(token);
    }
}
