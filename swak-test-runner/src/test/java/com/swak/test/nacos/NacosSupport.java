package com.swak.test.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.swak.core.http.RestTemplateBuilder;
import com.swak.core.http.SwakRestTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 真实 Nacos 环境下的公共工具：探活、发布配置、创建客户端。
 * <p>
 * 服务地址取自系统属性 {@code swak.nacos.server}，默认 {@code 127.0.0.1:8848}。
 */
final class NacosSupport {

    static final String SERVER_ADDR = System.getProperty("swak.nacos.server", "127.0.0.1:8848");

    static final String GROUP = "DEFAULT_GROUP";

    private static final SwakRestTemplate HTTP = RestTemplateBuilder.newBuilder()
            .setConnectTimeout(2000).setReadTimeout(5000).build();

    private NacosSupport() {
    }

    /**
     * 通过 curl 可用的同一套 OpenAPI 发布配置，顺便复用组件自己的 HTTP 客户端。
     */
    static void publish(String dataId, String content) {
        Map<String, Object> form = new HashMap<>();
        form.put("dataId", dataId);
        form.put("group", GROUP);
        form.put("type", "properties");
        form.put("content", content);
        String result = HTTP.postRequest("http://" + SERVER_ADDR + "/nacos/v1/cs/configs", form, String.class);
        assertThat(result).as("Nacos 发布配置应返回 true").isEqualTo("true");
    }

    static ConfigService newConfigService() throws Exception {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, SERVER_ADDR);
        return NacosFactory.createConfigService(properties);
    }

    static String newDataId(String prefix) {
        return prefix + UUID.randomUUID() + ".properties";
    }

    /**
     * Nacos 的「写后读」不是强一致的（发布后要等 dump 刷新 JVM 缓存），
     * 这里发布后轮询到可读为止，避免把缓存延迟当成失败。
     */
    static void publishAndAwait(ConfigService configService, String dataId, String content) throws Exception {
        for (int i = 0; i < 20; i++) {
            publish(dataId, content);
            if (content.equals(configService.getConfig(dataId, GROUP, 3000L))) {
                return;
            }
            Thread.sleep(300L);
        }
        throw new AssertionError("发布后 6 秒内仍读不到配置: " + dataId);
    }

    static boolean nacosAvailable() {
        String[] parts = SERVER_ADDR.split(":");
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(parts[0], Integer.parseInt(parts[1])), 500);
            return true;
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }
}
