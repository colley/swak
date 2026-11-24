package com.swak.test.license.api;

import com.swak.license.Messages;
import com.swak.license.api.i18n.Message;
import com.swak.license.core.spi.DefaultMessage;
import com.swak.license.core.spi.FormattedMessage;
import com.swak.license.core.spi.FormattedResourceBundle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 许可证消息与资源包回归。
 */
@DisplayName("许可证消息")
class LicenseMessageTest {

    @Test
    @DisplayName("无参数消息直接从资源包取值")
    void looksUpPlainMessage() {
        Message unknown = Messages.message("unknown");

        assertThat(unknown.toString(Locale.ENGLISH)).isEqualTo("Unknown");
        assertThat(unknown.toString()).isEqualTo(Messages.message("unknown").toString(Locale.getDefault()));
    }

    @Test
    @DisplayName("带参数消息按 MessageFormat 占位符替换")
    void formatsParameterizedMessage() {
        Message expired = Messages.message("licenseHasExpired", "2026-01-01");

        assertThat(expired.toString(Locale.SIMPLIFIED_CHINESE)).contains("2026-01-01");
        assertThat(expired.toString(Locale.ENGLISH)).contains("2026-01-01");
    }

    @Test
    @DisplayName("FormattedMessage 基于类的简单名定位资源包")
    void formattedMessageUsesClassSimpleName() {
        Message message = new FormattedMessage(Messages.class, "issuerIsNull");

        assertThat(message.toString(Locale.SIMPLIFIED_CHINESE)).isNotBlank();
    }

    @Test
    @DisplayName("资源包包装：lookup 与 format 均可直接使用")
    void formattedResourceBundleLookupAndFormat() {
        FormattedResourceBundle bundle = FormattedResourceBundle.bundle("Messages", Locale.ENGLISH);

        assertThat(bundle.lookup("unknown")).isEqualTo("Unknown");
        assertThat(bundle.format("licenseHasExpired", "2030-12-31")).contains("2030-12-31");
        assertThat(FormattedResourceBundle.bundle("Messages")).isNotNull();
    }

    @Test
    @DisplayName("资源包包装：可包装任意 ResourceBundle")
    void wrapsAnyResourceBundle() {
        ResourceBundle raw = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][]{{"greeting", "Hello {0}"}};
            }
        };

        FormattedResourceBundle bundle = FormattedResourceBundle.wrap(raw);

        assertThat(bundle.lookup("greeting")).isEqualTo("Hello {0}");
        assertThat(bundle.format("greeting", "Duke")).isEqualTo("Hello Duke");
    }

    @Test
    @DisplayName("DefaultMessage 原样返回文本")
    void defaultMessageReturnsText() {
        Message message = new DefaultMessage("plain-text");

        assertThat(message.toString(Locale.JAPAN)).isEqualTo("plain-text");
        assertThat(message.toString()).isEqualTo("plain-text");
    }
}
