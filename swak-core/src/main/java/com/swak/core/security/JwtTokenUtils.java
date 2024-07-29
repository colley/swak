package com.swak.core.security;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;
import com.swak.common.util.JacksonUtils;
import com.swak.common.util.StringPool;
import com.swak.common.util.UUIDHexGenerator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;

/**
 * JwtTokenUtils.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public class JwtTokenUtils {

    public static String encode(TokenJwtDetails jwtDetails, String jwtSecret) {
        if (Objects.nonNull(jwtDetails)) {
            Map<String, Object> claims = JacksonUtils.convertValue(jwtDetails);
            JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256, TextCodec.BASE64.encode(jwtSecret))
                    .setClaims(claims);
            return jwtBuilder.compact();
        }
        return StringPool.EMPTY;
    }

    public static TokenJwtDetails decode(String jwt, String jwtSecret) {
        if (StringUtils.isEmpty(jwt)) {
            return null;
        }
        Claims claims = decodeClaimJws(jwt, jwtSecret);
        return JacksonUtils.convertValue(claims, new TypeReference<DefaultTokenJwtDetails>() {
        });
    }


    public static String encode(Map<String, Object> claims, String jwtSecret) {
        if (MapUtils.isNotEmpty(claims)) {
            JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256, TextCodec.BASE64.encode(jwtSecret))
                    .setClaims(claims);
            return jwtBuilder.compact();
        }
        return StringPool.EMPTY;
    }

    public static Claims decodeClaimJws(String jwt, String jwtSecret) {
        if (StringUtils.isEmpty(jwt)) {
            return null;
        }
        return Jwts.parser().setSigningKey(TextCodec.BASE64.encode(jwtSecret)).parseClaimsJws(jwt).getBody();
    }

    public static void main(String[] args) {
        TokenJwtDetails jwtDetails = new DefaultTokenJwtDetails();
        jwtDetails.setExpireTime(System.currentTimeMillis());
        jwtDetails.setEmail("418234751@qq.com");
        jwtDetails.setUserId(1L);
        jwtDetails.setUsername("admin");
        jwtDetails.setToken(UUIDHexGenerator.generator());
        jwtDetails.setLoginTime(System.currentTimeMillis());
        jwtDetails.setPermissions(Sets.newHashSet("role"));
        System.out.println(JwtTokenUtils.encode(jwtDetails,StringPool.KEY));
        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInBlcm1pc3Npb25zIjpbInJvbGUiXSwiZXhwaXJlVGltZSI6MTcyMTM1OTczNjYyNSwibG9naW5UaW1lIjoxNzIxMzU5NzM2NjUzLCJ0b2tlbiI6ImZmODA4MDgxOTBjOTA5MWYwMTkwYzkwOTFmNGQwMDAxIiwidXNlcm5hbWUiOiJhZG1pbiIsImVtYWlsIjoiNDE4MjM0NzUxQHFxLmNvbSJ9.BngIPhfLMaYtBnV5qwzJ7rWo82egzvWBTEiJfZMuUcI";
        System.out.println(JSON.toJSONString(JwtTokenUtils.decode(jwt,StringPool.KEY)));

    }
}
