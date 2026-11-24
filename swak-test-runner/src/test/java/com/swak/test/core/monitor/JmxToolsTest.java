package com.swak.test.core.monitor;

import com.swak.core.monitor.tools.JmxTools;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.lang.management.ManagementFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link JmxTools} 的取值工具。
 * <p>
 * 真实 MBean 分支用平台 MBeanServer（java.lang 域是 JDK 必带的），
 * CompositeData 的「类型不匹配」分支用 Mockito 构造，
 * 所有断言只依赖 JDK 自身实现的稳定属性。
 */
@DisplayName("JMX 取值工具")
class JmxToolsTest {

    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    private final ObjectName threading = new ObjectName("java.lang:type=Threading");

    private final ObjectName classLoading = new ObjectName("java.lang:type=ClassLoading");

    private final ObjectName runtime = new ObjectName("java.lang:type=Runtime");

    JmxToolsTest() throws Exception {
    }

    @Test
    @DisplayName("真实 MBean：long / int / String 属性都能读出；缺失属性按约定回退")
    void readRealMBeanAttributes() throws Exception {
        assertThat(JmxTools.getLongAttr(server, threading, "TotalStartedThreadCount")).isPositive();
        assertThat(JmxTools.getLongAttr(server, threading, "NotExists", -1L)).isEqualTo(-1L);
        assertThat(JmxTools.getLongAttr(server, threading, "TotalStartedThreadCount")).isPositive();

        assertThat(JmxTools.getIntAttr(server, threading, "ThreadCount")).isPositive();
        assertThat(JmxTools.getIntAttr(server, classLoading, "LoadedClassCount")).isPositive();
        assertThat(JmxTools.getStringAttr(server, runtime, "VmName")).isNotBlank();

        // 缺失属性走 AttributeNotFoundException -> null
        assertThat(JmxTools.getAttribute(server, threading, "NotExists")).isNull();
        assertThat(JmxTools.getStringAttr(server, threading, "NotExists")).isNull();

        assertThat(JmxTools.hasAttribute(server, threading, "ThreadCount")).isTrue();
        assertThat(JmxTools.hasAttribute(server, threading, "NotExists")).isFalse();

        // 直接转 long 的重载不吞异常，缺失属性会抛出
        assertThatThrownBy(() -> JmxTools.getLongAttr(server, threading, "NotExists"))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("getArrayAttr：只有真正的 String[] 才返回，其它类型返回 null")
    void getArrayAttr() throws Exception {
        MBeanServer mbs = mock(MBeanServer.class);
        ObjectName name = new ObjectName("swak:type=Test");
        when(mbs.getAttribute(name, "array")).thenReturn(new String[]{"a", "b"});
        when(mbs.getAttribute(name, "string")).thenReturn("not-an-array");

        assertThat(JmxTools.getArrayAttr(mbs, name, "array")).containsExactly("a", "b");
        assertThat(JmxTools.getArrayAttr(mbs, name, "string")).isNull();
        assertThat(JmxTools.getArrayAttr(mbs, name, "missing")).isNull();
    }

    @Test
    @DisplayName("CompositeData：正确类型按值返回，缺失或类型不符时返回默认值")
    void compositeDataAccessors() throws Exception {
        CompositeData cds = mock(CompositeData.class);
        when(cds.get("long")).thenReturn(42L);
        when(cds.get("int")).thenReturn(7);
        when(cds.get("string")).thenReturn("value");
        when(cds.get("bool")).thenReturn(Boolean.TRUE);
        when(cds.get("wrongType")).thenReturn("not-a-number");

        assertThat(JmxTools.getLongAttr(cds, "long")).isEqualTo(42L);
        assertThat(JmxTools.getLongAttr(cds, "missing")).isZero();
        assertThat(JmxTools.getLongAttr(cds, "wrongType")).isZero();

        assertThat(JmxTools.getIntAttr(cds, "int", -1)).isEqualTo(7);
        assertThat(JmxTools.getIntAttr(cds, "missing", -1)).isEqualTo(-1);
        assertThat(JmxTools.getIntAttr(cds, "wrongType", -1)).isEqualTo(-1);

        assertThat(JmxTools.getStringAttr(cds, "string")).isEqualTo("value");
        assertThat(JmxTools.getStringAttr(cds, "missing")).isNull();

        assertThat(JmxTools.getBooleanAttr(cds, "bool")).isTrue();
        assertThat(JmxTools.getBooleanAttr(cds, "missing")).isFalse();
        assertThat(JmxTools.getBooleanAttr(cds, "wrongType")).isFalse();
    }
}
