package com.swak.license.spi.event;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.base.Joiner;
import com.swak.common.util.DateTimeUtils;
import com.swak.common.util.NetUtils;
import com.swak.common.util.StringPool;
import com.swak.license.api.License;
import com.swak.license.api.LicenseValidationException;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author colley
 */

@Slf4j
public class LicensePrinter {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String DECORATION = "-=-=-=-=-=-=-=-=-=-=  -=-=-=-=-=-=-=-=-=-=  -=-=-=-=-=-=-=-=-=-=";

    // 定义字段名的固定宽度（中文字符按2个宽度算，英文按1个。这里设为20是为了兼容中文）
    private static final int LABEL_WIDTH = 20;

    public static String buildLicenseBanner(License license) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(LINE_SEPARATOR);
            sb.append(DECORATION).append(LINE_SEPARATOR);
            sb.append("                    License授权证书安装成功                            ").append(LINE_SEPARATOR);
            sb.append(DECORATION).append(LINE_SEPARATOR);
            sb.append(padRight(":: 证书名称 ")).append("  :: ").append(getObjectName(license.getSubject())).append(LINE_SEPARATOR);
            sb.append(padRight(":: Issuer")).append(" :: ").append(getObjectName(license.getIssuer())).append(LINE_SEPARATOR);
            sb.append(padRight(":: Holder")).append(" :: ").append(getObjectName(license.getHolder())).append(LINE_SEPARATOR);
            sb.append(padRight(":: 证书有效期")).append("   :: ").append(DateTimeUtils.date2String(license.getNotBefore())).append(" - ").append(DateTimeUtils.date2String(license.getNotAfter())).append(LINE_SEPARATOR);
            sb.append(padRight(":: 本服务器IP")).append("   :: ").append(Joiner.on(",").join(NetUtils.getIpAddress())).append(LINE_SEPARATOR);
            sb.append(padRight(":: 本服务器MAC")).append("   :: ").append(Joiner.on(",").join(NetUtils.getMacAddress())).append(LINE_SEPARATOR);
            // 3. 扩展信息 (Extra Map)
            Object extraObj = license.getExtra();
            if (extraObj != null) {
                JSONObject extraObject = JSON.parseObject(JSON.toJSONString(extraObj));
                for (String key : extraObject.keySet()) {
                    String value = formatValue(extraObject.getString(key));
                    sb.append(padRight(":: "+key)).append(" :: ").append(value).append(LINE_SEPARATOR);
                }
            }
            sb.append(DECORATION).append(LINE_SEPARATOR);
            return sb.toString();
        } catch (Exception e) {
            log.error("", e);
        }
        return StringPool.EMPTY;
    }


    public static String buildLicenseError(LicenseValidationException validationException) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(LINE_SEPARATOR);
            sb.append(DECORATION).append(LINE_SEPARATOR);
            sb.append("                    License授权证书验证失败                            ").append(LINE_SEPARATOR);
            sb.append(DECORATION).append(LINE_SEPARATOR);
            License license = validationException.getLicense();
            sb.append(padRight(":: 失败原因")).append(" :: ").append(validationException.getMessage()).append(LINE_SEPARATOR);
            if(license != null){
                sb.append(padRight(":: 证书名称 ")).append("  :: ").append(getObjectName(license.getSubject())).append(LINE_SEPARATOR);
                sb.append(padRight(":: Issuer")).append(" :: ").append(getObjectName(license.getIssuer())).append(LINE_SEPARATOR);
                sb.append(padRight(":: Holder")).append(" :: ").append(getObjectName(license.getHolder())).append(LINE_SEPARATOR);
                sb.append(padRight(":: 证书有效期")).append("   :: ").append(DateTimeUtils.date2String(license.getNotBefore())).append(" - ").append(DateTimeUtils.date2String(license.getNotAfter())).append(LINE_SEPARATOR);
                sb.append(padRight(":: 本服务器IP")).append("   :: ").append(Joiner.on(",").join(NetUtils.getIpAddress())).append(LINE_SEPARATOR);
                sb.append(padRight(":: 本服务器MAC")).append("   :: ").append(Joiner.on(",").join(NetUtils.getMacAddress())).append(LINE_SEPARATOR);
                // 3. 扩展信息 (Extra Map)
                Object extraObj = license.getExtra();
                if (extraObj != null) {
                    JSONObject extraObject = JSON.parseObject(JSON.toJSONString(extraObj));
                    for (String key : extraObject.keySet()) {
                        String value = formatValue(extraObject.getString(key));
                        sb.append(padRight(":: "+key)).append(" :: ").append(value).append(LINE_SEPARATOR);
                    }
                }
            }
            sb.append(DECORATION).append(LINE_SEPARATOR);
            return sb.toString();
        } catch (Exception e) {
            log.error("", e);
        }
        return StringPool.EMPTY;
    }

    private static String padRight(String s) {
        if (s == null) return "";
        int visualLen = getVisualLength(s);
        int padding = LABEL_WIDTH - visualLen;
        if (padding < 0) padding = 0;
        return s + repeatString(padding);
    }


    private static int getVisualLength(String s) {
        if (s == null) return 0;
        int length = 0;
        for (char c : s.toCharArray()) {
            if (c >= '\u4e00' && c <= '\u9fa5') {
                length += 2;
            } else {
                length += 1;
            }
        }
        return length;
    }

    private static String repeatString(int n) {
        if (n <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(" ");
        return sb.toString();
    }

    private static String getObjectName(Object obj) {
        if (obj == null) return "Unknown";
        return obj.toString();
    }

    private static String formatValue(Object value) {
        if (value == null) return "Unknown";
        String str = value.toString();
        return Objects.equals(str, "-1") ? "Unlimited" : str;
    }
}