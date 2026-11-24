package com.swak.test.core.web;

import com.swak.core.web.ServletUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link ServletUtils} 的请求/响应工具方法。
 * <p>
 * 依赖 {@link RequestContextHolder}，这里用 spring-test 的 {@link MockHttpServletRequest}
 * 挂到当前线程的 RequestAttributes 上，不启动 Servlet 容器。
 */
@DisplayName("Servlet 工具类")
class ServletUtilsTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    // ---------------------------------------------------------------- 参数

    @Test
    @DisplayName("getParameter：存在、缺失走默认值")
    void getParameter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("name", "swak");
        bind(request);

        assertThat(ServletUtils.getParameter("name")).isEqualTo("swak");
        assertThat(ServletUtils.getParameter("missing")).isNull();
        assertThat(ServletUtils.getParameter("missing", "def")).isEqualTo("def");
        assertThat(ServletUtils.getParameter("name", "def")).isEqualTo("swak");
    }

    @Test
    @DisplayName("无请求上下文时 getRequest / getUserAgent / getIpAddr 返回 null，getParameter 直接 NPE")
    void withoutRequestContext() {
        RequestContextHolder.resetRequestAttributes();

        assertThat(ServletUtils.getRequest()).isNull();
        assertThat(ServletUtils.getUserAgent()).isNull();
        assertThat(ServletUtils.getIpAddr()).isNull();
        assertThatThrownBy(() -> ServletUtils.getParameter("name"))
                .isInstanceOf(NullPointerException.class);
    }

    // ---------------------------------------------------------------- 附件

    @Test
    @DisplayName("writeAttachment：文件名按 UTF-8 编码，内容原样写出")
    void writeAttachment() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        byte[] content = "hello".getBytes(StandardCharsets.UTF_8);

        ServletUtils.writeAttachment(response, "report 1.csv", content);

        assertThat(response.getHeader("Content-Disposition"))
                .isEqualTo("attachment;filename=report+1.csv");
        assertThat(response.getContentType()).isEqualTo("application/octet-stream");
        assertThat(response.getContentAsByteArray()).isEqualTo(content);
    }

    // ---------------------------------------------------------------- 内网判定

    @Test
    @DisplayName("internalIp：10/172.16-31/192.168/127.0.0.1 判为内网，公网与非法输入判为外网")
    void internalIp() {
        assertThat(ServletUtils.internalIp("10.1.2.3")).isTrue();
        assertThat(ServletUtils.internalIp("172.16.0.1")).isTrue();
        assertThat(ServletUtils.internalIp("172.31.255.254")).isTrue();
        assertThat(ServletUtils.internalIp("192.168.1.1")).isTrue();
        assertThat(ServletUtils.internalIp("127.0.0.1")).isTrue();

        assertThat(ServletUtils.internalIp("172.32.0.1")).isFalse();
        assertThat(ServletUtils.internalIp("8.8.8.8")).isFalse();
        // 解析失败与空串都会得到 null 字节数组，而 null 被视为内网（当前实现的既有行为）
        assertThat(ServletUtils.internalIp("not-an-ip")).isTrue();
        assertThat(ServletUtils.internalIp("")).isTrue();
    }

    @Test
    @DisplayName("textToNumericFormatV4：1/2/3/4 段与各类非法输入的边界")
    void textToNumericFormatV4() {
        assertThat(ServletUtils.textToNumericFormatV4("127.0.0.1"))
                .containsExactly((byte) 127, (byte) 0, (byte) 0, (byte) 1);
        assertThat(ServletUtils.textToNumericFormatV4("4294967295"))
                .containsExactly((byte) 255, (byte) 255, (byte) 255, (byte) 255);
        assertThat(ServletUtils.textToNumericFormatV4("1.2"))
                .containsExactly((byte) 1, (byte) 0, (byte) 0, (byte) 2);
        assertThat(ServletUtils.textToNumericFormatV4("1.2.3"))
                .containsExactly((byte) 1, (byte) 2, (byte) 0, (byte) 3);

        assertThat(ServletUtils.textToNumericFormatV4("")).isNull();
        assertThat(ServletUtils.textToNumericFormatV4("4294967296")).isNull();
        assertThat(ServletUtils.textToNumericFormatV4("999.1.1.1")).isNull();
        assertThat(ServletUtils.textToNumericFormatV4("1.2.3.4.5")).isNull();
        assertThat(ServletUtils.textToNumericFormatV4("a.b.c.d")).isNull();
        assertThat(ServletUtils.textToNumericFormatV4("1.99999999")).isNull();
        assertThat(ServletUtils.textToNumericFormatV4("1.2.70000")).isNull();

        assertThatThrownBy(() -> ServletUtils.textToNumericFormatV4(null))
                .isInstanceOf(NullPointerException.class);
    }

    // ---------------------------------------------------------------- UA / IP

    @Test
    @DisplayName("getUserAgent：无 UA 头返回空串，有则原样返回")
    void getUserAgent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertThat(ServletUtils.getUserAgent(request)).isEmpty();

        request.addHeader("User-Agent", "swak-agent/1.0");
        assertThat(ServletUtils.getUserAgent(request)).isEqualTo("swak-agent/1.0");

        bind(request);
        assertThat(ServletUtils.getUserAgent()).isEqualTo("swak-agent/1.0");
    }

    @Test
    @DisplayName("getIpAddr：按头顺序取第一个有效值，全为 unknown 时回退 remoteAddr")
    void getIpAddrFromHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.16.0.5");

        request.addHeader("X-Forwarded-For", "203.0.113.9, 10.0.0.1");
        assertThat(ServletUtils.getIpAddr(request)).isEqualTo("203.0.113.9");

        MockHttpServletRequest realIpOnly = new MockHttpServletRequest();
        realIpOnly.setRemoteAddr("172.16.0.6");
        realIpOnly.addHeader("X-Real-IP", "198.51.100.7");
        assertThat(ServletUtils.getIpAddr(realIpOnly)).isEqualTo("198.51.100.7");

        MockHttpServletRequest unknownHeaders = new MockHttpServletRequest();
        unknownHeaders.setRemoteAddr("172.16.0.7");
        unknownHeaders.addHeader("X-Forwarded-For", "unknown");
        unknownHeaders.addHeader("X-Real-IP", " ");
        assertThat(ServletUtils.getIpAddr(unknownHeaders)).isEqualTo("172.16.0.7");

        MockHttpServletRequest unknownFirst = new MockHttpServletRequest();
        unknownFirst.setRemoteAddr("172.16.0.8");
        unknownFirst.addHeader("X-Forwarded-For", "unknown,10.1.2.3");
        assertThat(ServletUtils.getIpAddr(unknownFirst)).isEqualTo("10.1.2.3");

        bind(unknownFirst);
        assertThat(ServletUtils.getIpAddr()).isEqualTo("10.1.2.3");
    }

    @Test
    @DisplayName("getClientIPByHeader：只在调用方给定的头里查找")
    void getClientIPByHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.9");
        request.addHeader("X-Custom-IP", "1.1.1.1");

        assertThat(ServletUtils.getClientIPByHeader(request, "X-Custom-IP")).isEqualTo("1.1.1.1");
        assertThat(ServletUtils.getClientIPByHeader(request, "X-Not-There")).isEqualTo("10.0.0.9");
    }

    @Test
    @DisplayName("getMultistageReverseProxyIp：多级代理取第一个非 unknown 的地址")
    void getMultistageReverseProxyIp() {
        assertThat(ServletUtils.getMultistageReverseProxyIp(null)).isNull();
        assertThat(ServletUtils.getMultistageReverseProxyIp("1.1.1.1")).isEqualTo("1.1.1.1");
        assertThat(ServletUtils.getMultistageReverseProxyIp("unknown,2.2.2.2")).isEqualTo("2.2.2.2");
        assertThat(ServletUtils.getMultistageReverseProxyIp("1.1.1.1,2.2.2.2")).isEqualTo("1.1.1.1");
        assertThat(ServletUtils.getMultistageReverseProxyIp(",2.2.2.2")).isEqualTo(",2.2.2.2");
    }

    @Test
    @DisplayName("isUnknown：空白、null、unknown（忽略大小写）都为 true")
    void isUnknown() {
        assertThat(ServletUtils.isUnknown(null)).isTrue();
        assertThat(ServletUtils.isUnknown("")).isTrue();
        assertThat(ServletUtils.isUnknown("   ")).isTrue();
        assertThat(ServletUtils.isUnknown("unknown")).isTrue();
        assertThat(ServletUtils.isUnknown("UNKNOWN")).isTrue();
        assertThat(ServletUtils.isUnknown("1.1.1.1")).isFalse();
    }

    private static void bind(MockHttpServletRequest request) {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
