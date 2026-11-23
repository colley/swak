package com.swak.test.json;

import com.swak.common.dto.Response;
import com.swak.common.dto.Result;
import com.swak.common.util.JacksonUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Jackson 2 → 3 之后对外 JSON 契约的快照。
 * <p>
 * 升级过程中这些行为都会「静默改变」——编译不报错、单测不报错，但前端拿到的报文变了。
 * 这里把每条显式钉死，任何一条被改动都会在 CI 上直接失败：
 * <ul>
 *     <li>Date 由时间戳数字变成 ISO-8601 字符串；</li>
 *     <li>Locale 由 {@code zh_CN} 变成 BCP 47 的 {@code zh-CN}；</li>
 *     <li>属性默认按字母序输出（Jackson 3 的 MapperFeature 默认值变了）；</li>
 *     <li>null 属性不输出（NON_NULL），但集合/Map 内部的 null 元素保留。</li>
 * </ul>
 */
class JacksonContractTest {

    static class ContractBean {
        public String name = "swak";
        public String nullField = null;
        public Date when = new Date(0L);
        public Locale locale = Locale.CHINA;
        public int num = 7;
        public boolean flag = true;
    }

    @Test
    @DisplayName("Response 报文结构固定：code/data/msg/success/timestamp/traceId")
    void responseShape() {
        String json = JacksonUtils.toJSONString(Response.success("ok"));
        JsonNode node = JacksonUtils.parseObject(json);

        assertThat(node.propertyNames())
                .containsExactly("code", "data", "msg", "success", "timestamp", "traceId");
        assertThat(node.get("code").asInt()).isZero();
        assertThat(node.get("data").asString()).isEqualTo("ok");
        assertThat(node.get("msg").asString()).isEqualTo("操作成功");
        assertThat(node.get("success").asBoolean()).isTrue();
        assertThat(node.get("timestamp").asLong()).isPositive();
        assertThat(node.get("traceId").asString()).isEmpty();
    }

    @Test
    @DisplayName("普通 Bean 快照：Date 走 ISO-8601、Locale 走 BCP 47、属性按字母序")
    void beanSnapshot() {
        assertThat(JacksonUtils.toJSONString(new ContractBean())).isEqualTo(
                "{\"flag\":true,\"locale\":\"zh-CN\",\"name\":\"swak\",\"num\":7,"
                        + "\"when\":\"1970-01-01T00:00:00.000Z\"}");
    }

    @Test
    @DisplayName("Date 序列化结果是可被 Instant 解析的 ISO-8601，而不是 epoch 数字")
    void dateIsIso8601() {
        JsonNode node = JacksonUtils.parseObject(JacksonUtils.toJSONString(new ContractBean()));

        JsonNode when = node.get("when");
        assertThat(when.isString())
                .as("Jackson 2 时代这里是数字 0，改为字符串属于破坏性变更")
                .isTrue();
        assertThat(Instant.parse(when.asString())).isEqualTo(Instant.EPOCH);
    }

    @Test
    @DisplayName("Locale 使用 BCP 47：zh_CN -> zh-CN")
    void localeUsesBcp47() {
        JsonNode node = JacksonUtils.parseObject(JacksonUtils.toJSONString(new ContractBean()));
        assertThat(node.get("locale").asString()).isEqualTo("zh-CN");
    }

    @Test
    @DisplayName("null 属性不输出，但集合与 Map 内的 null 元素保留")
    void nullHandling() {
        assertThat(JacksonUtils.toJSONString(Arrays.asList("a", null, "b"))).isEqualTo("[\"a\",null,\"b\"]");
        assertThat(JacksonUtils.toJSONString(Collections.singletonMap("k", null))).isEqualTo("{\"k\":null}");
        assertThat(JacksonUtils.toJSONString(null)).isEqualTo("null");
    }

    @Test
    @DisplayName("Response 可原样反序列化回对象")
    void responseRoundTrip() {
        String json = JacksonUtils.toJSONString(Response.success("ok"));
        // Response 是接口，反序列化要落到具体实现 Result
        @SuppressWarnings("unchecked")
        Response<String> parsed = JacksonUtils.parseObject(json, Result.class);

        assertThat(parsed.getData()).isEqualTo("ok");
        assertThat(parsed.isSuccess()).isTrue();
    }
}
