package com.swak.test.http;

import com.swak.core.http.RestTemplateBuilder;
import com.swak.core.http.SwakRestTemplate;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link SwakRestTemplate} 的真实回归：起一个本地 HTTP 服务，走真实 socket，不使用 mock。
 * <p>
 * 升级到 Spring Framework 7 时这里换掉了底层请求工厂（OkHttp → JDK HttpClient），
 * 参数编码、超时语义、模板变量展开都可能静默改变，因此逐条钉死。
 */
class SwakRestTemplateTest {

    private static HttpServer server;

    private static int port;

    /** 最近一次请求的原始 query 串 */
    private static final AtomicReference<String> LAST_QUERY = new AtomicReference<>();

    /** 最近一次请求的方法名 */
    private static final AtomicReference<String> LAST_METHOD = new AtomicReference<>();

    /** 最近一次请求的 body */
    private static final AtomicReference<String> LAST_BODY = new AtomicReference<>();

    public static class Echo {

        public String name;

        public int num;
    }

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        port = server.getAddress().getPort();
        server.createContext("/echo", exchange -> {
            record(exchange);
            String query = exchange.getRequestURI().getRawQuery();
            String json = "{\"method\":\"" + exchange.getRequestMethod() + "\",\"query\":\"" + (query == null ? "" : query)
                    + "\"}";
            write(exchange, 200, "application/json", json);
        });
        server.createContext("/pojo", exchange -> {
            record(exchange);
            write(exchange, 200, "application/json;charset=UTF-8", "{\"name\":\"张三\",\"num\":7}");
        });
        server.createContext("/slow", exchange -> {
            record(exchange);
            try {
                Thread.sleep(1500L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            write(exchange, 200, "text/plain;charset=UTF-8", "late");
        });
        server.createContext("/body", exchange -> {
            record(exchange);
            write(exchange, 200, "text/plain;charset=UTF-8", "ok:" + LAST_BODY.get());
        });
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(4));
        server.start();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @BeforeEach
    void reset() {
        LAST_QUERY.set(null);
        LAST_METHOD.set(null);
        LAST_BODY.set(null);
    }

    private static void record(HttpExchange exchange) throws IOException {
        LAST_METHOD.set(exchange.getRequestMethod());
        LAST_QUERY.set(exchange.getRequestURI().getRawQuery());
        try (InputStream in = exchange.getRequestBody()) {
            LAST_BODY.set(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private static void write(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static String base(String path) {
        return "http://127.0.0.1:" + port + path;
    }

    private static SwakRestTemplate client() {
        return RestTemplateBuilder.newBuilder().setConnectTimeout(1000).setReadTimeout(3000).build();
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> map = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return map;
        }
        for (String pair : rawQuery.split("&")) {
            int idx = pair.indexOf('=');
            String key = idx < 0 ? pair : pair.substring(0, idx);
            String value = idx < 0 ? "" : pair.substring(idx + 1);
            map.put(URLDecoder.decode(key, StandardCharsets.UTF_8), URLDecoder.decode(value, StandardCharsets.UTF_8));
        }
        return map;
    }

    @Test
    @DisplayName("getRequest：参数拼到 query 上，中文按 UTF-8 编码后服务端能原样解出")
    void getRequestEncodesChineseParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("ip", "10.0.0.1");
        params.put("city", "杭州");

        String result = client().getRequest(base("/echo"), params, String.class);

        assertThat(result).contains("\"method\":\"GET\"");
        Map<String, String> query = parseQuery(LAST_QUERY.get());
        assertThat(query).containsEntry("ip", "10.0.0.1").containsEntry("city", "杭州");
        // 原始 query 串里中文必须是百分号编码，不能是裸 UTF-8 字节
        assertThat(LAST_QUERY.get()).contains("%E6%9D%AD%E5%B7%9E");
    }

    @Test
    @DisplayName("postRequest：走 POST，参数同样拼在 query 上（无 body）")
    void postRequestUsesQueryString() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", "42");

        String result = client().postRequest(base("/echo"), params, String.class);

        assertThat(result).contains("\"method\":\"POST\"");
        assertThat(parseQuery(LAST_QUERY.get())).containsEntry("id", "42");
        assertThat(LAST_BODY.get()).isEmpty();
    }

    @Test
    @DisplayName("getRequest：URL 自带的 query 不能被入参覆盖")
    void getRequestKeepsExistingQuery() {
        Map<String, Object> params = new HashMap<>();
        params.put("b", "2");

        client().getRequest(base("/echo?a=1"), params, String.class);

        assertThat(parseQuery(LAST_QUERY.get())).containsEntry("a", "1").containsEntry("b", "2");
    }

    @Test
    @DisplayName("getRequest：入参为 null 的 value 渲染成字符串 null")
    void getRequestRendersNullValue() {
        Map<String, Object> params = new HashMap<>();
        params.put("k", null);

        client().getRequest(base("/echo"), params, String.class);

        assertThat(parseQuery(LAST_QUERY.get())).containsEntry("k", "null");
    }

    @Test
    @DisplayName("getForObject(Map 版 uriVariables)：不能返回 null")
    void getForObjectWithUriVariablesMap() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("id", "7");

        String result = client().getForObject(base("/echo?id={id}"), String.class, vars);

        assertThat(result).isNotNull();
        assertThat(parseQuery(LAST_QUERY.get())).containsEntry("id", "7");
    }

    @Test
    @DisplayName("postForEntity：elide 的 uriVariables 必须真正参与模板展开")
    void postForEntityExpandsUriVariables() {
        ResponseEntity<String> response = client().postForEntity(base("/echo?id={id}"), null, String.class, "9");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(parseQuery(LAST_QUERY.get())).containsEntry("id", "9");
    }

    @Test
    @DisplayName("JSON 响应能绑定到 POJO（Spring 7 走 Jackson 3 转换器）")
    void bindsJsonToPojo() {
        Echo echo = client().getForObject(base("/pojo"), Echo.class);

        assertThat(echo).isNotNull();
        assertThat(echo.name).isEqualTo("张三");
        assertThat(echo.num).isEqualTo(7);
    }

    @Test
    @DisplayName("readTimeout 生效：慢接口抛 ResourceAccessException")
    void readTimeoutIsEnforced() {
        SwakRestTemplate fast = RestTemplateBuilder.newBuilder().setConnectTimeout(500).setReadTimeout(300).build();

        assertThatThrownBy(() -> fast.getForObject(base("/slow"), String.class))
                .isInstanceOf(ResourceAccessException.class);
    }

    @Test
    @DisplayName("post 带 body 时 body 与 Content-Type 都按原样发出")
    void postBodyIsSent() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("k", "v");

        ResponseEntity<String> response = client().postForEntity(base("/body"), body, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(LAST_METHOD.get()).isEqualTo("POST");
        assertThat(LAST_BODY.get()).contains("\"k\":\"v\"");
    }

    @Test
    @DisplayName("默认构造不再依赖 httpclient5，开箱即可发起请求")
    void defaultConstructorWorks() {
        String result = new SwakRestTemplate().getForObject(base("/echo"), String.class);

        assertThat(result).contains("\"method\":\"GET\"");
    }
}
