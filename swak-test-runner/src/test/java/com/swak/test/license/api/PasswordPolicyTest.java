package com.swak.test.license.api;

import com.swak.common.util.ObfuscatedString;
import com.swak.license.api.passwd.Password;
import com.swak.license.api.passwd.PasswordProtection;
import com.swak.license.api.passwd.PasswordUsage;
import com.swak.license.api.passwd.WeakPasswordException;
import com.swak.license.core.passwd.MinimumPasswordPolicy;
import com.swak.license.core.passwd.ObfuscatedPasswordProtection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 密码保护与最小强度策略回归。
 */
@DisplayName("密码策略")
class PasswordPolicyTest {

    private final MinimumPasswordPolicy policy = new MinimumPasswordPolicy();

    @Test
    @DisplayName("长度不少于 8 且同时含字母与数字时通过")
    void acceptsStrongPassword() {
        assertThatCode(() -> policy.check(protection("abcd1234"))).doesNotThrowAnyException();
        assertThatCode(() -> policy.check(protection("Passw0rdLong"))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("短于 8 位、纯字母、纯数字都被拒绝")
    void rejectsWeakPasswords() {
        assertThatThrownBy(() -> policy.check(protection("abc123")))
                .isInstanceOf(WeakPasswordException.class);
        assertThatThrownBy(() -> policy.check(protection("abcdefgh")))
                .isInstanceOf(WeakPasswordException.class);
        assertThatThrownBy(() -> policy.check(protection("12345678")))
                .isInstanceOf(WeakPasswordException.class);
    }

    @Test
    @DisplayName("混淆密码保护：字符内容正确，关闭后缓冲区被清零")
    void obfuscatedProtectionErasesCharacters() throws Exception {
        PasswordProtection protection = protection("abcd1234");

        Password password = protection.password(PasswordUsage.ENCRYPTION);
        char[] characters = password.characters();
        assertThat(new String(characters)).isEqualTo("abcd1234");

        password.close();
        assertThat(characters).containsOnly('\0');

        // 每次调用都返回独立的密码对象。
        assertThat(protection.password(PasswordUsage.DECRYPTION).characters())
                .isNotSameAs(protection.password(PasswordUsage.DECRYPTION).characters());
    }

    @Test
    @DisplayName("混淆密码保护拒绝 null")
    void rejectsNull() {
        assertThatThrownBy(() -> new ObfuscatedPasswordProtection(null))
                .isInstanceOf(NullPointerException.class);
    }

    private static PasswordProtection protection(String plain) {
        return new ObfuscatedPasswordProtection(new ObfuscatedString(ObfuscatedString.array(plain)));
    }
}
