package com.swak.test.common.util;

import com.swak.common.util.GetterUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link GetterUtil} 的静态工具方法回归。
 * <p>
 * 这个类的特点是一堆同名重载分别处理 String / Object / 包装类型 / Map，
 * 每个重载的 null、空白串、非法数字、缺失 key 分支都要单独钉住，否则很容易
 * 在「看起来等价」的重载之间出现行为差异。
 */
@DisplayName("GetterUtil 取值工具")
class GetterUtilTest {

    static class CloneableBean implements Cloneable {

        String name;

        CloneableBean(String name) {
            this.name = name;
        }

        @Override
        public CloneableBean clone() {
            try {
                return (CloneableBean) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    static class SerializableBean implements Serializable {

        private static final long serialVersionUID = 1L;

        String name;

        SerializableBean(String name) {
            this.name = name;
        }
    }

    static class PlainBean {
    }

    // ---------------------------------------------------------------- 字符串

    @Test
    @DisplayName("getString：null 返回默认值，Object 走 toString，String 会 trim")
    void getStringVariants() {
        assertThat(GetterUtil.getString((Object) null)).isNull();
        assertThat(GetterUtil.getString((Object) 12)).isEqualTo("12");
        assertThat(GetterUtil.getString((Object) null, "fallback")).isEqualTo("fallback");
        assertThat(GetterUtil.getString((String) null, "fallback")).isEqualTo("fallback");
        assertThat(GetterUtil.getString("  swak  ")).isEqualTo("swak");
        assertThat(GetterUtil.getString("   ")).isEmpty();
    }

    @Test
    @DisplayName("get(String,String)：空白串与字面量 \"null\" 都视为缺失")
    void getStringTreatsNullLiteralAsAbsent() {
        assertThat(GetterUtil.get("null", "def")).isEqualTo("def");
        assertThat(GetterUtil.get(" NULL ", "def")).isEqualTo("NULL");
        assertThat(GetterUtil.get("", "def")).isEqualTo("def");
        assertThat(GetterUtil.get(" value ", "def")).isEqualTo("value");
    }

    @Test
    @DisplayName("valueOf：null 转空串")
    void valueOfNull() {
        assertThat(GetterUtil.valueOf(null)).isEmpty();
        assertThat(GetterUtil.valueOf(42)).isEqualTo("42");
    }

    // ---------------------------------------------------------------- 布尔

    @Test
    @DisplayName("getBoolean：支持 true/t/y/on/1/yes 及 false/off/0/no，忽略大小写与空白")
    void getBooleanVariants() {
        assertThat(GetterUtil.getBoolean(" TRUE ")).isTrue();
        assertThat(GetterUtil.getBoolean("t")).isTrue();
        assertThat(GetterUtil.getBoolean("Y")).isTrue();
        assertThat(GetterUtil.getBoolean("on")).isTrue();
        assertThat(GetterUtil.getBoolean("1")).isTrue();
        assertThat(GetterUtil.getBoolean("yes")).isTrue();
        assertThat(GetterUtil.getBoolean("off")).isFalse();
        assertThat(GetterUtil.getBoolean("0")).isFalse();
        assertThat(GetterUtil.getBoolean("no")).isFalse();
        assertThat(GetterUtil.getBoolean("unknown")).isFalse();
        assertThat(GetterUtil.getBoolean((String) null)).isFalse();
    }

    @Test
    @DisplayName("getBoolean：可指定默认值，包装类型 null 时返回默认值")
    void getBooleanWithDefault() {
        assertThat(GetterUtil.getBoolean("unknown", true)).isTrue();
        assertThat(GetterUtil.getBoolean((String) null, true)).isTrue();
        assertThat(GetterUtil.getBoolean(Boolean.FALSE, true)).isFalse();
        assertThat(GetterUtil.getBoolean((Boolean) null, true)).isTrue();
    }

    // ---------------------------------------------------------------- 数值

    @Test
    @DisplayName("getDouble：String / Object / Double 三个重载的边界")
    void getDoubleVariants() {
        assertThat(GetterUtil.getDouble("1.5")).isEqualTo(1.5);
        assertThat(GetterUtil.getDouble("abc")).isZero();
        assertThat(GetterUtil.getDouble("  ")).isZero();
        assertThat(GetterUtil.getDouble("abc", 9.9)).isEqualTo(9.9);
        assertThat(GetterUtil.getDouble((Object) "2.5")).isEqualTo(2.5);
        assertThat(GetterUtil.getDouble((Object) 3)).isEqualTo(3.0);
        assertThat(GetterUtil.getDouble((Object) new Object(), 7.7)).isEqualTo(7.7);
        assertThat(GetterUtil.getDouble((Object) null, 7.7)).isEqualTo(7.7);
        assertThat(GetterUtil.getDouble((Double) null, 1.1)).isEqualTo(1.1);
        assertThat(GetterUtil.getDouble(Double.valueOf(2.2))).isEqualTo(2.2);
        assertThat(GetterUtil.getDouble((Double) null)).isZero();
    }

    @Test
    @DisplayName("getFloat：合法与非法输入")
    void getFloatVariants() {
        assertThat(GetterUtil.getFloat("1.25")).isEqualTo(1.25f);
        assertThat(GetterUtil.getFloat("x")).isZero();
        assertThat(GetterUtil.getFloat("x", 3.5f)).isEqualTo(3.5f);
    }

    @Test
    @DisplayName("getInteger：字符串、数字对象、包装类型三条路径")
    void getIntegerVariants() {
        assertThat(GetterUtil.getInteger("12")).isEqualTo(12);
        assertThat(GetterUtil.getInteger("1.5")).isZero();
        assertThat(GetterUtil.getInteger("bad", 5)).isEqualTo(5);
        assertThat(GetterUtil.getInteger((Object) "12")).isEqualTo(12);
        assertThat(GetterUtil.getInteger((Object) 3.9)).isEqualTo(3);
        assertThat(GetterUtil.getInteger((Object) new Object(), 6)).isEqualTo(6);
        assertThat(GetterUtil.getInteger((Integer) null, 8)).isEqualTo(8);
        assertThat(GetterUtil.getInteger(Integer.valueOf(9))).isEqualTo(9);
        assertThat(GetterUtil.getInteger((Integer) null)).isZero();
    }

    @Test
    @DisplayName("getLong：字符串、数字对象、包装类型三条路径")
    void getLongVariants() {
        assertThat(GetterUtil.getLong("123456789012")).isEqualTo(123456789012L);
        assertThat(GetterUtil.getLong("bad", 3L)).isEqualTo(3L);
        assertThat(GetterUtil.getLong((Object) "42")).isEqualTo(42L);
        assertThat(GetterUtil.getLong((Object) 42)).isEqualTo(42L);
        assertThat(GetterUtil.getLong((Object) new Object(), 4L)).isEqualTo(4L);
        assertThat(GetterUtil.getLong((Long) null, 5L)).isEqualTo(5L);
        assertThat(GetterUtil.getLong((Long) null)).isZero();
        assertThat(GetterUtil.getLong((Object) null)).isZero();
    }

    @Test
    @DisplayName("getShort：正常值、越界与空白")
    void getShortVariants() {
        assertThat(GetterUtil.getShort("300")).isEqualTo((short) 300);
        assertThat(GetterUtil.getShort("70000")).isZero();
        assertThat(GetterUtil.getShort("70000", (short) 1)).isEqualTo((short) 1);
        assertThat(GetterUtil.getShort(" ")).isZero();
    }

    // ---------------------------------------------------------------- 日期

    @Test
    @DisplayName("getDate：按 DateFormat 解析，空值与非法格式回退默认值")
    void getDateVariants() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date expected = Date.from(LocalDate.of(2024, 1, 2).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date fallback = new Date(0L);

        assertThat(GetterUtil.getDate("2024-01-02", format, fallback)).isEqualTo(expected);
        assertThat(GetterUtil.getDate("", format, fallback)).isEqualTo(fallback);
        assertThat(GetterUtil.getDate("not-a-date", format, fallback)).isEqualTo(fallback);
    }

    // ---------------------------------------------------------------- Map

    @Test
    @DisplayName("Map 重载：存在、缺失、空白、布尔各种情况")
    void mapVariants() {
        Map<String, String> data = new HashMap<>();
        data.put("str", " swak ");
        data.put("int", "12");
        data.put("bool", "YES");
        data.put("blank", " ");

        assertThat(GetterUtil.getString(data, "str")).isEqualTo("swak");
        assertThat(GetterUtil.getString(data, "missing")).isEmpty();
        assertThat(GetterUtil.getString(data, "missing", "def")).isEqualTo("def");
        assertThat(GetterUtil.getInteger(data, "int")).isEqualTo(12);
        assertThat(GetterUtil.getInteger(data, "missing")).isZero();
        assertThat(GetterUtil.getInteger(data, "missing", 3)).isEqualTo(3);
        assertThat(GetterUtil.getBoolean(data, "bool")).isTrue();
        assertThat(GetterUtil.getBoolean(data, "missing")).isFalse();
        assertThat(GetterUtil.getBoolean(data, "blank", true)).isTrue();
        assertThat(GetterUtil.getBoolean(data, "missing", true)).isTrue();
        assertThat(GetterUtil.get(data, "str", "def")).isEqualTo("swak");
        assertThat(GetterUtil.get(data, "missing", "def")).isEqualTo("def");
        assertThat(GetterUtil.get(data, "int", 0)).isEqualTo(12);
        assertThat(GetterUtil.get(data, "missing", 7)).isEqualTo(7);
        assertThat(GetterUtil.get(data, "bool", Boolean.FALSE)).isTrue();
        assertThat(GetterUtil.get(data, "missing", Boolean.TRUE)).isTrue();
    }

    // ---------------------------------------------------------------- 集合修复

    @Test
    @DisplayName("getFixData*：null 返回空容器，非 null 原样返回")
    void fixDataContainers() {
        assertThat(GetterUtil.getFixDataMap(null)).isEmpty();
        assertThat(GetterUtil.getFixDatalist(null)).isEmpty();
        assertThat(GetterUtil.getFixDataset(null)).isEmpty();

        Map<String, String> map = new HashMap<>();
        List<String> list = new ArrayList<>();
        Set<String> set = new HashSet<>();
        assertThat(GetterUtil.getFixDataMap(map)).isSameAs(map);
        assertThat(GetterUtil.getFixDatalist(list)).isSameAs(list);
        assertThat(GetterUtil.getFixDataset(set)).isSameAs(set);
    }

    // ---------------------------------------------------------------- 拆分

    @Test
    @DisplayName("getSplit2IntegerArray / getSplit2List / getSplit2Long / getSplitStr")
    void splitVariants() {
        assertThat(GetterUtil.getSplit2IntegerArray("1,2,x")).containsExactly(1, 2, 0);
        assertThat(GetterUtil.getSplit2IntegerArray("")).isNull();
        assertThat(GetterUtil.getSplit2IntegerArray("1|2", "|")).containsExactly(1, 2);

        assertThat(GetterUtil.getSplit2List("a,b")).containsExactly("a", "b");
        assertThat(GetterUtil.getSplit2List(null)).isEmpty();
        assertThat(GetterUtil.getSplit2List("a|b", "|")).containsExactly("a", "b");

        assertThat(GetterUtil.getSplit2Long("1,2")).containsExactly(1L, 2L);
        assertThat(GetterUtil.getSplit2Long(" ")).isEmpty();
        assertThat(GetterUtil.getSplit2Long((String[]) null)).isEmpty();
        assertThat(GetterUtil.getSplit2Long(new String[]{"1", "x"})).containsExactly(1L, 0L);

        assertThat(GetterUtil.getSplit2Integer("3,4")).containsExactly(3, 4);
        assertThat(GetterUtil.getSplit2Integer(null)).isEmpty();

        assertThat(GetterUtil.getSplitStr("a,b")).containsExactly("a", "b");
        assertThat(GetterUtil.getSplitStr("")).isEmpty();
        assertThat(GetterUtil.getSplitStr("a|b", "|")).containsExactly("a", "b");
    }

    @Test
    @DisplayName("split2Map：key-value 与只有 key 的两种写法")
    void split2Map() {
        Map<String, String> map = GetterUtil.split2Map("a-1,b");
        assertThat(map).containsEntry("a", "1").containsKey("b");
        assertThat(map.get("b")).isNull();
        assertThat(GetterUtil.split2Map(null)).isEmpty();
    }

    @Test
    @DisplayName("getSplitList：按桶大小切分，空集合返回 null")
    void getSplitList() {
        assertThat(GetterUtil.getSplitList(Collections.emptyList(), 3)).isNull();
        assertThat(GetterUtil.getSplitList(null, 3)).isNull();

        List<Integer> ten = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<List<Integer>> parts = GetterUtil.getSplitList(ten, 3);
        assertThat(parts).hasSize(4);
        assertThat(parts.get(0)).containsExactly(1, 2, 3);
        assertThat(parts.get(3)).containsExactly(10);

        // 整除时不会多出一个空桶
        assertThat(GetterUtil.getSplitList(Arrays.asList(1, 2, 3, 4, 5, 6), 3))
                .hasSize(2)
                .allSatisfy(part -> assertThat(part).hasSize(3));
    }

    @Test
    @DisplayName("convertListToString：null/空集合返回 null，元素用分隔符连接")
    void convertListToString() {
        assertThat(GetterUtil.convertListToString(null, ",")).isNull();
        assertThat(GetterUtil.convertListToString(Collections.emptyList(), ",")).isNull();
        assertThat(GetterUtil.convertListToString(Arrays.asList(1L, 2L, 3L), ",")).isEqualTo("1,2,3");
    }

    @Test
    @DisplayName("splitSubList：越界截断与非法区间返回 null")
    void splitSubList() {
        List<Integer> list = Arrays.asList(1, 2, 3);

        assertThat(GetterUtil.splitSubList(list, 1, 10)).containsExactly(2, 3);
        assertThat(GetterUtil.splitSubList(list, 3, 1)).isNull();
        assertThat(GetterUtil.splitSubList(list, 5, 6)).isNull();
        assertThat(GetterUtil.splitSubList(list, 0, 0)).isEmpty();
    }

    @Test
    @DisplayName("split：List 与 Set 版本的非法区间都返回空集合")
    void splitCollections() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        assertThat(GetterUtil.split(list, 1, 3)).containsExactly(2, 3);
        assertThat(GetterUtil.split(list, 2, 1)).isEmpty();
        assertThat(GetterUtil.split(list, 9, 10)).isEmpty();

        Set<String> set = new HashSet<>(Collections.singletonList("only"));
        assertThat(GetterUtil.split(set, 0, 5)).containsExactly("only");
        assertThat(GetterUtil.split(set, 2, 1)).isEmpty();
        assertThat(GetterUtil.split(set, 9, 10)).isEmpty();
    }

    @Test
    @DisplayName("strContains：忽略大小写，空集合/空目标返回 false")
    void strContains() {
        List<String> sources = Arrays.asList("Admin", "user");
        assertThat(GetterUtil.strContains(sources, "admin")).isTrue();
        assertThat(GetterUtil.strContains(sources, "guest")).isFalse();
        assertThat(GetterUtil.strContains(Collections.emptyList(), "admin")).isFalse();
        assertThat(GetterUtil.strContains(sources, "")).isFalse();
        assertThat(GetterUtil.strContains(sources, null)).isFalse();
    }

    // ---------------------------------------------------------------- 脱敏与格式化

    @Test
    @DisplayName("hideAccount：默认两位星号、可指定星号数量、太短或空白原样返回")
    void hideAccount() {
        assertThat(GetterUtil.hideAccount("13812345678", 3, 4)).isEqualTo("138**5678");
        assertThat(GetterUtil.hideAccount("13812345678", 3, 4, 4)).isEqualTo("138****5678");
        assertThat(GetterUtil.hideAccount("123", 3, 4)).isEqualTo("123");
        assertThat(GetterUtil.hideAccount("", 3, 4)).isEmpty();
        assertThat(GetterUtil.hideAccount(null, 3, 4)).isNull();
    }

    @Test
    @DisplayName("formatNumber：去掉无意义的小数零")
    void formatNumber() {
        assertThat(GetterUtil.formatNumber(1.5d)).isEqualTo("1.5");
        assertThat(GetterUtil.formatNumber(2.0d)).isEqualTo("2");
        assertThat(GetterUtil.formatNumber(3.25f)).isEqualTo("3.25");
        assertThat(GetterUtil.formatNumber("4.75")).isEqualTo("4.75");
        assertThat(GetterUtil.formatNumber("bad")).isEqualTo("0");
    }

    @Test
    @DisplayName("getList：只接受 JSON 数组字面量")
    void getList() {
        assertThat(GetterUtil.getList("[1,2,3]", Integer.class)).containsExactly(1, 2, 3);
        assertThat(GetterUtil.getList("[1,2]", Long.class)).containsExactly(1L, 2L);
        assertThat(GetterUtil.getList("1,2", Integer.class)).isEmpty();
        assertThat(GetterUtil.getList("", Integer.class)).isEmpty();
        assertThat(GetterUtil.getList(null, Integer.class)).isEmpty();
    }

    // ---------------------------------------------------------------- 克隆

    @Test
    @DisplayName("cloneArray：null、基本类型数组、对象数组、非数组")
    void cloneArray() {
        assertThat(GetterUtil.<Object>cloneArray(null)).isNull();
        assertThat(GetterUtil.cloneArray("not-an-array")).isNull();

        int[] ints = {1, 2, 3};
        int[] intsClone = GetterUtil.cloneArray(ints);
        assertThat(intsClone).containsExactly(1, 2, 3).isNotSameAs(ints);

        String[] strings = {"a", "b"};
        String[] stringsClone = GetterUtil.cloneArray(strings);
        assertThat(stringsClone).containsExactly("a", "b").isNotSameAs(strings);
    }

    @Test
    @DisplayName("clone(T[])：null 返回 null，非 null 返回副本")
    void cloneArrayReference() {
        assertThat(GetterUtil.clone((String[]) null)).isNull();
        String[] source = {"a"};
        String[] copy = GetterUtil.clone(source);
        assertThat(copy).containsExactly("a").isNotSameAs(source);
    }

    @Test
    @DisplayName("clone：Cloneable 走 clone()，Serializable 走序列化，两者都不是返回 null")
    void cloneObject() throws Exception {
        CloneableBean bean = new CloneableBean("swak");
        CloneableBean beanClone = GetterUtil.clone(bean);
        assertThat(beanClone).isNotSameAs(bean);
        assertThat(beanClone.name).isEqualTo("swak");

        SerializableBean serializable = new SerializableBean("deep");
        SerializableBean serializableClone = GetterUtil.clone(serializable);
        assertThat(serializableClone).isNotSameAs(serializable);
        assertThat(serializableClone.name).isEqualTo("deep");

        assertThat(GetterUtil.clone(new PlainBean())).isNull();
    }

    @Test
    @DisplayName("cloneIfPossible / cloneByStream：失败时回退原对象")
    void cloneIfPossibleAndStream() {
        PlainBean plain = new PlainBean();
        assertThat(GetterUtil.cloneIfPossible(plain)).isSameAs(plain);

        assertThat(GetterUtil.cloneByStream("abc")).isEqualTo("abc");
        assertThat(GetterUtil.cloneByStream(new PlainBean())).isNull();
    }

    // ---------------------------------------------------------------- 其它

    @Test
    @DisplayName("equalsIgnoreCase / toLowerCase / toUpperCase：null 安全")
    void caseHelpers() {
        assertThat(GetterUtil.equalsIgnoreCase(null, null)).isTrue();
        assertThat(GetterUtil.equalsIgnoreCase(null, "a")).isFalse();
        assertThat(GetterUtil.equalsIgnoreCase("a", null)).isFalse();
        assertThat(GetterUtil.equalsIgnoreCase("AbC", "aBc")).isTrue();

        assertThat(GetterUtil.toLowerCase(null, Locale.ROOT)).isNull();
        assertThat(GetterUtil.toLowerCase("AbC", Locale.ROOT)).isEqualTo("abc");
        assertThat(GetterUtil.toUpperCase(null, Locale.ROOT)).isNull();
        assertThat(GetterUtil.toUpperCase("AbC", Locale.ROOT)).isEqualTo("ABC");
    }

    @Test
    @DisplayName("requireNonEmpty：空串抛 IllegalArgumentException")
    void requireNonEmpty() {
        assertThat(GetterUtil.requireNonEmpty("x")).isEqualTo("x");
        assertThatThrownBy(() -> GetterUtil.requireNonEmpty(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("defaultIfNull：值、Supplier、Function 三种重载")
    void defaultIfNullVariants() {
        assertThat(GetterUtil.defaultIfNull(null, "def")).isEqualTo("def");
        assertThat(GetterUtil.defaultIfNull("v", "def")).isEqualTo("v");
        assertThat(GetterUtil.defaultIfNull(null, () -> "supplied")).isEqualTo("supplied");
        assertThat(GetterUtil.defaultIfNull("v", () -> "supplied")).isEqualTo("v");
        assertThat(GetterUtil.defaultIfNull(null, (String s) -> "mapped")).isEqualTo("mapped");
        assertThat(GetterUtil.defaultIfNull("v", (String s) -> "mapped")).isEqualTo("v");
    }

    @Test
    @DisplayName("大小写/下划线驼峰互转")
    void namingConversions() {
        assertThat(GetterUtil.firstToUpperCase("abc")).isEqualTo("Abc");
        assertThat(GetterUtil.firstToUpperCase("")).isEmpty();
        assertThat(GetterUtil.firstToUpperCase(null)).isNull();

        assertThat(GetterUtil.firstToLowerCase("Abc")).isEqualTo("abc");
        assertThat(GetterUtil.firstToLowerCase(" ")).isEmpty();
        assertThat(GetterUtil.firstToLowerCase(null)).isEmpty();

        assertThat(GetterUtil.camelToUnderline("userName")).isEqualTo("user_name");
        assertThat(GetterUtil.camelToUnderline("Name")).isEqualTo("name");
        assertThat(GetterUtil.camelToUnderline("")).isEmpty();
        assertThat(GetterUtil.camelToUnderline(null)).isEmpty();

        assertThat(GetterUtil.underlineToCamel("user_name")).isEqualTo("userName");
        assertThat(GetterUtil.underlineToCamel("USER_NAME")).isEqualTo("userName");
        assertThat(GetterUtil.underlineToCamel("user_")).isEqualTo("user");
        assertThat(GetterUtil.underlineToCamel("")).isEmpty();

        assertThat(GetterUtil.concatCapitalize("set", "name")).isEqualTo("setName");
        assertThat(GetterUtil.concatCapitalize(null, "name")).isEqualTo("Name");
        assertThat(GetterUtil.concatCapitalize("set", "")).isEmpty();
        assertThat(GetterUtil.concatCapitalize("set", null)).isNull();
    }

    @Test
    @DisplayName("isChineseChar：命中中文返回 true")
    void isChineseChar() {
        assertThat(GetterUtil.isChineseChar("abc")).isFalse();
        assertThat(GetterUtil.isChineseChar("abc中")).isTrue();
        assertThat(GetterUtil.isChineseChar("")).isFalse();
    }

    @Test
    @DisplayName("concat / newArray：数组拼接")
    void concatAndNewArray() {
        String[] first = {"a"};
        String[] second = {"b", "c"};

        assertThat(GetterUtil.concat(first, second, String.class)).containsExactly("a", "b", "c");
        assertThat(GetterUtil.newArray(String.class, 2)).hasSize(2).containsOnlyNulls();
    }

    @Test
    @DisplayName("getDouble(Object, Double)：BigInteger 也走 Number 分支")
    void getDoubleFromBigInteger() {
        assertThat(GetterUtil.getDouble((Object) new BigInteger("7"))).isEqualTo(7.0);
    }
}
