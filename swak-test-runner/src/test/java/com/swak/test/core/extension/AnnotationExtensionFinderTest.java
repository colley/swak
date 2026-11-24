package com.swak.test.core.extension;

import com.swak.core.extension.annotation.ExtensionOperation;
import com.swak.core.extension.repository.AnnotationExtensionFinder;
import io.gitee.swak.fixture.ExtensionFixtures.AnnotatedExtension;
import io.gitee.swak.fixture.ExtensionFixtures.PlainExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link AnnotationExtensionFinder} 的注解解析与容器扫描。
 * <p>
 * 重点：@Extension 的四个属性要原样带出；结果按 ClassKey 缓存；
 * 容器扫描时未标注的 ExtensionPoint 只告警不返回。
 */
@DisplayName("注解式扩展查找器")
class AnnotationExtensionFinderTest {

    private final AnnotationExtensionFinder finder = new AnnotationExtensionFinder();

    @Test
    @DisplayName("find(Class)：@Extension 的属性完整映射成 ExtensionOperation")
    void findByClass() {
        Collection<ExtensionOperation> operations = finder.find(AnnotatedExtension.class);

        assertThat(operations).hasSize(1);
        ExtensionOperation operation = operations.iterator().next();
        assertThat(operation.getExtensionClass()).isEqualTo(AnnotatedExtension.class);
        assertThat(operation.getBizId()).isEqualTo("order");
        assertThat(operation.getUseCase()).isEqualTo("create");
        assertThat(operation.getScenario()).isEqualTo("vip");
        assertThat(operation.getDesc()).isEqualTo("下单扩展");
        assertThat(operation.getExtension()).isNull();
    }

    @Test
    @DisplayName("find(Class)：同一 Class 命中缓存，返回同一集合")
    void findByClassIsCached() {
        Collection<ExtensionOperation> first = finder.find(AnnotatedExtension.class);
        Collection<ExtensionOperation> second = finder.find(AnnotatedExtension.class);

        assertThat(second).isSameAs(first);
    }

    @Test
    @DisplayName("find(Class)：未标注 @Extension 时两次都返回空集合（缓存哨兵不再返回 null）")
    void missingAnnotationIsConsistent() {
        Collection<ExtensionOperation> first = finder.find(PlainExtension.class);
        Collection<ExtensionOperation> second = finder.find(PlainExtension.class);

        assertThat(first).isEmpty();
        assertThat(second).isEmpty();
    }

    @Test
    @DisplayName("find()：从容器挑出标注 @Extension 的实现，未标注的跳过")
    void findAllFromApplicationContext() {
        try (GenericApplicationContext context = new GenericApplicationContext()) {
            context.registerBean("annotatedExtension", AnnotatedExtension.class);
            context.registerBean("plainExtension", PlainExtension.class);
            context.refresh();
            finder.setApplicationContext(context);

            List<ExtensionOperation> operations = finder.find();

            assertThat(operations).hasSize(1);
            assertThat(operations.get(0).getBizId()).isEqualTo("order");
            assertThat(operations.get(0).getExtension()).isInstanceOf(AnnotatedExtension.class);
        }
    }

    @Test
    @DisplayName("find()：容器里没有 ExtensionPoint 时返回空列表")
    void findAllWithNoBeans() {
        try (GenericApplicationContext context = new GenericApplicationContext()) {
            context.refresh();
            finder.setApplicationContext(context);

            assertThat(finder.find()).isEmpty();
        }
    }

    @Test
    @DisplayName("未注入 ApplicationContext 时 find() 抛出带说明的 IllegalStateException")
    void findAllWithoutContextFails() {
        assertThatThrownBy(finder::find)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ApplicationContext");
    }
}
