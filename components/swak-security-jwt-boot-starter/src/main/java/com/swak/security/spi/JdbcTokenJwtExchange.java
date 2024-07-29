package com.swak.security.spi;

import com.swak.common.dto.base.Entity;
import com.swak.common.exception.SwakAssert;
import com.swak.common.util.StringPool;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Date;
import java.util.Objects;

public class JdbcTokenJwtExchange implements TokenJwtExchange {

    private final JdbcTemplate jdbcTemplate;

    private final String tableName;

    public JdbcTokenJwtExchange(JdbcTemplate jdbcTemplate, String tableName) {
        this.jdbcTemplate = jdbcTemplate;
        SwakAssert.notBlank(tableName, "tableName must not blank");
        this.tableName = tableName;
    }

    public JdbcTokenJwtExchange(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, "token_jwt");
    }

    @Override
    public void storeTokenJwt(String token, String tokenJwt, Long expireSeconds) {
        Date expireTime = new Date(System.currentTimeMillis() + (expireSeconds * 1000L));
        String sql = String.format("REPLACE INTO %s (token,token_jwt,expire_time) VALUES (?,?,?)", tableName);
        jdbcTemplate.update(sql, token, tokenJwt, expireTime);
    }

    @Override
    public String takeTokenJwt(String token) {
        TokenJwtInfo tokenJwtInfo = getTokenJwtInfo(token);
        if (Objects.isNull(tokenJwtInfo)) {
            return StringPool.EMPTY;
        }
        if (tokenJwtInfo.getExpireTime().before(new Date())) {
            //过期了
            this.remove(token);
            return StringPool.EMPTY;
        }
        return tokenJwtInfo.getTokenJwt();
    }

    private TokenJwtInfo getTokenJwtInfo(String token){
        String sql = String.format("SELECT * FROM  %s where token=?", tableName);
        return jdbcTemplate.queryForObject(sql, (rs, num) -> {
            TokenJwtInfo tokenJwt = new TokenJwtInfo();
            tokenJwt.setToken(rs.getString("token"));
            tokenJwt.setTokenJwt(rs.getString("token_jwt"));
            tokenJwt.setId(rs.getLong("id"));
            tokenJwt.setExpireTime(rs.getDate("expire_time"));
            return tokenJwt;
        }, token);
    }


    @Override
    public void refresh(String token, String tokenJwt, Long expireSeconds) {
        Date expireTime = new Date(System.currentTimeMillis() + (expireSeconds * 1000L));
        TokenJwtInfo tokenJwtInfo = getTokenJwtInfo(token);
        if(Objects.nonNull(tokenJwtInfo)) {
            expireTime = new Date(tokenJwtInfo.getExpireTime().getTime() + (expireSeconds * 1000L));
        }
        String sql = String.format("REPLACE INTO %s (token,token_jwt,expire_time) VALUES (?,?,?)", tableName);
        jdbcTemplate.update(sql, token, tokenJwt, expireTime);
    }

    @Override
    public void remove(String token) {
        String sql = String.format("delete FROM  %s where token=?", tableName);
        jdbcTemplate.update(sql, token);
    }

    @Override
    public int priority() {
        return SPI_PRIORITY - 1;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class TokenJwtInfo extends Entity {
        private Long id;
        private String token;
        private String tokenJwt;
        private Date expireTime;
    }
}
