package com.swak.core.web;


import com.alibaba.fastjson2.JSONObject;
import com.swak.core.http.RestTemplateBuilder;
import com.swak.core.http.SwakRestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 获取地址类
 */
@Slf4j
public class AddressUtils {

    // IP地址查询
    public static final String IP_URL = "http://whois.pconline.com.cn/ipJson.jsp";

    // 未知地址
    public static final String UNKNOWN = "XX XX";

    public static SwakRestTemplate restTemplate = RestTemplateBuilder.restTemplate();

    public static String getRealAddressByIP(String ip, Boolean addressEnabled) {
        addressEnabled = Optional.ofNullable(addressEnabled).orElse(false);
        if (!addressEnabled) {
            return UNKNOWN;
        }
        return getRealAddressByIP(ip);
    }

    public static String getRealAddressByIP(String ip) {
        // 内网不查询
        if (ServletUtils.internalIp(ip)) {
            return "内网IP";
        }
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("ip", ip);
            JSONObject response = restTemplate.getRequest(IP_URL, params, JSONObject.class);
            if (Objects.isNull(response)) {
                log.error("获取地理位置异常 {}", ip);
                return UNKNOWN;
            }
            String region = response.getString("pro");
            String city = response.getString("city");
            return String.format("%s %s", region, city);
        } catch (Exception e) {
            log.error("获取地理位置异常 {}", ip);
        }

        return UNKNOWN;
    }
}
