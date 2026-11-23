package com.swak.test.security;

import com.swak.core.security.DefaultTokenJwtDetails;
import com.swak.core.security.JwtTokenUtils;
import com.swak.core.security.TokenJwtDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JWT 签发与解析的回归。
 * <p>
 * 这里是全项目唯一「Jackson 3 与 Jackson 2 混跑」的路径：claims 由 {@code JacksonUtils}（Jackson 3）
 * 序列化，jjwt 0.12 内部用 Jackson 2 解析，两边语义不一致时最容易在运行时才炸。
 */
class JwtRoundTripTest {

    /** HS256 要求密钥至少 256 bit，即 32 字节。 */
    private static final String SECRET = "01234567890123456789012345678901";

    @Test
    @DisplayName("TokenJwtDetails 签发后可完整还原")
    void detailsRoundTrip() {
        DefaultTokenJwtDetails details = new DefaultTokenJwtDetails();
        details.setUserId(42L);
        details.setUsername("colley");
        details.setEmail("colley@example.com");
        details.setPermissions(Set.of("admin", "dev"));
        details.setExpireTime(1795418000000L);

        String token = JwtTokenUtils.encode(details, SECRET);
        assertThat(token).isNotBlank();

        TokenJwtDetails decoded = JwtTokenUtils.decode(token, SECRET);
        assertThat(decoded.getUserId()).isEqualTo(42L);
        assertThat(decoded.getUsername()).isEqualTo("colley");
        assertThat(decoded.getEmail()).isEqualTo("colley@example.com");
        assertThat(decoded.getPermissions()).containsExactlyInAnyOrder("admin", "dev");
        assertThat(decoded.getExpireTime()).isEqualTo(1795418000000L);
    }

    @Test
    @DisplayName("自定义 claims 中的嵌套结构保持类型")
    void mapClaimsRoundTrip() {
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("id", 42);
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", "colley");
        claims.put("roles", List.of("admin", "dev"));
        claims.put("nested", nested);

        Claims parsed = JwtTokenUtils.decodeClaimJws(JwtTokenUtils.encode(claims, SECRET), SECRET);

        assertThat(parsed.getSubject()).isEqualTo("colley");
        assertThat(parsed.get("roles")).isInstanceOf(List.class);
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) parsed.get("roles");
        assertThat(roles).containsExactly("admin", "dev");
        assertThat(parsed.get("nested")).isInstanceOf(Map.class);
        assertThat(((Number) ((Map<?, ?>) parsed.get("nested")).get("id")).intValue()).isEqualTo(42);
    }

    @Test
    @DisplayName("密钥不匹配时解析抛 JwtException")
    void wrongSecretIsRejected() {
        String token = JwtTokenUtils.encode(new DefaultTokenJwtDetails(), SECRET);

        assertThatThrownBy(() -> JwtTokenUtils.decodeClaimJws(token, "another-secret-another-secret-32b"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("空 token 解码返回 null 而不是抛异常")
    void emptyTokenReturnsNull() {
        assertThat(JwtTokenUtils.decode("", SECRET)).isNull();
        assertThat(JwtTokenUtils.decodeClaimJws("", SECRET)).isNull();
    }
}
