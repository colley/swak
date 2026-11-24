package io.gitee.swak.fixture;

import com.swak.core.extension.ExtensionPoint;
import com.swak.core.extension.annotation.Extension;

/**
 * 扩展点测试夹具。
 * <p>
 * 刻意放在 {@code com.swak} 包之外：{@code SwakAutoConfiguration} 会
 * {@code @ComponentScan("com.swak")}，夹具若留在测试包内会被当成真实扩展点注册成 bean，
 * 进而触发「扩展点名称必须以 ExtPt 结尾」的校验，把无关的上下文测试打挂。
 */
public final class ExtensionFixtures {

    private ExtensionFixtures() {
    }

    @Extension(bizId = "order", useCase = "create", scenario = "vip", desc = "下单扩展")
    public static class AnnotatedExtension implements ExtensionPoint {
    }

    public static class PlainExtension implements ExtensionPoint {
    }
}
