package com.swak.test.license.api;

import com.swak.license.api.x500.GenX500PrincipalBuilder;
import com.swak.license.api.x500.X500AttributeTypeKeyword;
import com.swak.license.api.x500.X500PrincipalBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.security.auth.x500.X500Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * X.500 主体构造器回归：关键字属性、自定义 OID 映射与非法输入。
 */
@DisplayName("X500 主体构造器")
class X500PrincipalBuilderTest {

    @Test
    @DisplayName("按关键字添加属性后可直接构造主体")
    void buildsPrincipalFromKeywords() {
        X500Principal principal = new X500PrincipalBuilder()
                .addCN("Duke")
                .addO("ACME")
                .addC("DE")
                .addOU("Research")
                .addEMAILADDRESS("duke@example.com")
                .build();

        assertThat(principal).isNotNull();
        assertThat(principal.getName())
                .contains("CN=Duke")
                .contains("O=ACME")
                .contains("C=DE")
                .contains("OU=Research");
    }

    @Test
    @DisplayName("标准关键字快捷方法链式可用且返回同一实例")
    void allKeywordShortcutsChain() {
        X500PrincipalBuilder builder = new X500PrincipalBuilder();

        assertThat(builder.addC("DE")).isSameAs(builder);
        builder.addCN("Duke")
                .addDC("example")
                .addL("Berlin")
                .addO("ACME")
                .addOU("Dev")
                .addST("BE")
                .addSTREET("Main Street 1")
                .addUID("duke");

        assertThat(builder.build().getName())
                .contains("CN=Duke")
                .contains("O=ACME")
                .contains("C=DE");
    }

    @Test
    @DisplayName("其余关键字快捷方法均可参与构造")
    void extendedKeywordShortcutsAccepted() {
        X500PrincipalBuilder builder = new X500PrincipalBuilder()
                .addDNQ("dnq")
                .addDNQUALIFIER("dnq2")
                .addEMAILADDRESS("duke@example.com")
                .addGENERATION("Jr")
                .addGIVENNAME("Duke")
                .addINITIALS("D")
                .addSERIALNUMBER("42")
                .addSURNAME("Duke")
                .addT("Dr");

        assertThat(builder.build().getName()).isNotBlank();
        for (X500AttributeTypeKeyword keyword : X500AttributeTypeKeyword.values()) {
            assertThat(keyword.name()).isNotBlank();
        }
    }

    @Test
    @DisplayName("值为 null 时移除已添加的属性")
    void nullValueRemovesAttribute() {
        X500Principal principal = new X500PrincipalBuilder()
                .addCN("Duke")
                .addO("ACME")
                .addAttribute("CN", null)
                .build();

        assertThat(principal.getName()).isEqualTo("O=ACME");
    }

    @Test
    @DisplayName("含逗号、首尾空格与反斜杠的值会被正确引用与转义")
    void quotesAndEscapesSpecialCharacters() {
        assertThat(new X500PrincipalBuilder().addCN("Duke, Jr.").build().getName()).contains("Duke");
        assertThat(new X500PrincipalBuilder().addCN(" padded ").build().getName()).contains("padded");
        assertThat(new X500PrincipalBuilder().addCN("back\\slash").build().getName()).contains("slash");
        assertThat(new X500PrincipalBuilder().addCN("#hash").build().getName()).contains("hash");
    }

    @Test
    @DisplayName("未知属性类型或已移除的 OID 映射导致构造失败")
    void unknownAttributeTypeFails() {
        assertThatThrownBy(() -> new X500PrincipalBuilder().addAttribute("NOPE", "x").build())
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new X500PrincipalBuilder()
                .addKeyword("FOO", "1.2.3.4")
                .addKeyword("FOO", null)
                .addAttribute("FOO", "x")
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("自定义 OID 映射可用于构造主体")
    void customOidMapping() {
        X500Principal principal = new X500PrincipalBuilder()
                .addKeyword("FOO", "1.2.3.4")
                .addAttribute("FOO", "bar")
                .build();

        assertThat(principal.getName()).contains("1.2.3.4");
    }

    @Test
    @DisplayName("类型与关键字参数为 null 时抛 NullPointerException")
    void nullTypeAndKeywordRejected() {
        assertThatThrownBy(() -> new X500PrincipalBuilder().addAttribute((String) null, "x"))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new X500PrincipalBuilder().addKeyword(null, "1.2.3"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("泛型子类可保持自身返回类型")
    void genericSubclassKeepsType() {
        CustomBuilder builder = new CustomBuilder().addCN("Duke");

        assertThat(builder).isInstanceOf(CustomBuilder.class);
        assertThat(builder.build().getName()).isEqualTo("CN=Duke");
    }

    private static final class CustomBuilder extends GenX500PrincipalBuilder<CustomBuilder> {
    }
}
