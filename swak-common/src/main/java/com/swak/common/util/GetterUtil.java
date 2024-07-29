
package com.swak.common.util;

import com.alibaba.fastjson2.JSONArray;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.swak.common.util.stream.FastByteArrayOutputStream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.swak.common.util.StringPool.*;

/**
 * 公共方法 处理 字符串转数据，日期等
 *
 * @author colley.ma
 * @since 2.2.1
 */
public final class GetterUtil {

    public static String getString(Object obj) {
        return getString(obj, null);
    }

    public static String getString(Object obj, String defaultValue) {
        return (obj == null) ? defaultValue : obj.toString();
    }

    public static boolean getBoolean(String value) {
        return getBoolean(value, DEFAULT_BOOLEAN);
    }

    public static Boolean getBoolean(String value, Boolean defaultValue) {
        return get(value, defaultValue);
    }

    public static Boolean getBoolean(Boolean value, Boolean defaultValue) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        return value;
    }

    public static Date getDate(String value, DateFormat df) {
        return getDate(value, df, new Date());
    }

    public static Date getDate(String value, DateFormat df, Date defaultValue) {
        return get(value, df, defaultValue);
    }


    public static Double getDouble(String value) {
        return getDouble(value, DEFAULT_DOUBLE);
    }

    public static Double getDouble(String value, Double defaultValue) {
        return get(value, defaultValue);
    }

    public static Double getDouble(Object value) {
        return getDouble(value, DEFAULT_DOUBLE);
    }

    public static Double getDouble(Object value, Double defaultValue) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        if (value instanceof String) {
            return getDouble((String) value, defaultValue);
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    public static Double getDouble(Double value, Double defaultValue) {
        return Optional.ofNullable(value).orElse(defaultValue);
    }

    public static Double getDouble(Double value) {
        return getDouble(value, DEFAULT_DOUBLE);
    }


    public static Float getFloat(String value) {
        return getFloat(value, DEFAULT_FLOAT);
    }

    public static Float getFloat(String value, Float defaultValue) {
        return get(value, defaultValue);
    }


    public static Integer getInteger(String value) {
        return getInteger(value, DEFAULT_INTEGER);
    }

    public static Integer getInteger(String value, Integer defaultValue) {
        return get(value, defaultValue);
    }

    public static Integer getInteger(Integer value, Integer defaultValue) {
        return Optional.ofNullable(value).orElse(defaultValue);
    }

    public static Integer getInteger(Integer value) {
        return getInteger(value, DEFAULT_INTEGER);
    }

    public static Integer getInteger(Object value) {
        return getInteger(value, DEFAULT_INTEGER);
    }

    public static Integer getInteger(Object value, Integer defaultValue) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        if (value instanceof String) {
            return getInteger((String) value, defaultValue);
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }


    public static Long getLong(String value) {
        return getLong(value, DEFAULT_LONG);
    }

    public static Long getLong(String value, Long defaultValue) {
        return get(value, defaultValue);
    }

    public static Long getLong(Object value) {
        return getLong(value, DEFAULT_LONG);
    }

    public static Long getLong(Object value, Long defaultValue) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        if (value instanceof String) {
            return getLong((String) value, defaultValue);
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return defaultValue;
    }

    public static Long getLong(Long value) {
        return getLong(value, DEFAULT_LONG);
    }

    public static Long getLong(Long value, Long defaultValue) {
        return Optional.ofNullable(value).orElse(defaultValue);
    }


    public static Short getShort(String value) {
        return getShort(value, DEFAULT_SHORT);
    }

    public static Short getShort(String value, Short defaultValue) {
        return get(value, defaultValue);
    }

    public static String getString(String value) {
        return getString(value, EMPTY);
    }

    public static String getString(String value, String defaultValue) {
        return get(value, defaultValue);
    }

    public static Boolean get(String value, Boolean defaultValue) {
        if (value != null) {
            String temValue = value.trim();
            Boolean result = BOOLEANS.get(temValue.toLowerCase());
            if (Objects.nonNull(result)) {
                return result;
            }
        }
        return defaultValue;
    }

    public static Date get(String value, DateFormat df, Date defaultValue) {
        if (StringUtils.isEmpty(getString(value))) {
            return defaultValue;
        }
        try {
            Date date = df.parse(value.trim());
            if (date != null) {
                return date;
            }
        } catch (Exception ignored) {
        }
        return defaultValue;
    }

    public static Double get(String value, Double defaultValue) {
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Float get(String value, Float defaultValue) {
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Integer get(String value, Integer defaultValue) {
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Long get(String value, Long defaultValue) {
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Short get(String value, Short defaultValue) {
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String get(String value, String defaultValue) {
        String lastValue = StringUtils.trimToEmpty(value);
        if (StringUtils.isBlank(lastValue) ||
                Objects.equals("null", lastValue)) {
            return defaultValue;
        }
        return lastValue;
    }

    public static String getString(Map<String, String> data, String key) {
        return getString(data, key, EMPTY);
    }

    public static String getString(Map<String, String> data, String key, String defaultValue) {
        return get(data, key, defaultValue);
    }

    public static Integer getInteger(Map<String, String> data, String key) {
        return getInteger(data, key, DEFAULT_INTEGER);
    }

    public static Integer getInteger(Map<String, String> data, String key, Integer defaultValue) {
        return get(data, key, defaultValue);
    }

    public static String get(Map<String, String> data, String key, String defaultValue) {
        String value = data.get(key);

        return get(value, defaultValue);
    }

    public static Integer get(Map<String, String> data, String key, Integer defaultValue) {
        String value = data.get(key);

        return get(value, defaultValue);
    }

    public static Boolean get(Map<String, String> data, String key, Boolean defaultValue) {
        String value = StringUtils.trimToEmpty(data.get(key));
        if (StringUtils.isNotEmpty(value)) {
            Boolean result = BOOLEANS.get(value.toLowerCase());
            if (Objects.nonNull(result)) {
                return result;
            }
        }
        return defaultValue;
    }

    public static Boolean getBoolean(Map<String, String> data, String key) {
        return get(data, key, DEFAULT_BOOLEAN);
    }

    public static Boolean getBoolean(Map<String, String> data, String key, Boolean defaultValue) {
        return get(data, key, defaultValue);
    }

    public static boolean isChineseChar(String str) {
        for (int i = 0; i < str.length(); i++) {
            String bb = str.substring(i, i + 1);
            boolean cc = java.util.regex.Pattern.matches("[\u4E00-\u9FA5]", bb);

            if (cc) {
                return true;
            }
        }
        return false;
    }

    public static <K, V> Map<K, V> getFixDataMap(Map<K, V> data) {
        return (data == null) ? Maps.newHashMap() : data;
    }

    public static <T> List<T> getFixDatalist(List<T> dataList) {
        return (dataList == null) ? new ArrayList<>() : dataList;
    }

    public static <T> Set<T> getFixDataset(Set<T> dataSet) {
        return (dataSet == null) ? new HashSet<>() : dataSet;
    }

    public static Integer[] getSplit2IntegerArray(String codes, String splitChar) {
        if (StringUtils.isBlank(codes)) {
            return null;
        }

        String[] stringArr = StringUtils.split(codes, splitChar);
        Integer[] intList = new Integer[stringArr.length];

        for (int i = 0; i < stringArr.length; i++) {
            intList[i] = getInteger(stringArr[i]);
        }

        return intList;
    }

    public static Integer[] getSplit2IntegerArray(String codes) {
        return getSplit2IntegerArray(codes, COMMA);
    }

    public static List<String> getSplit2List(String codes, String splitChar) {
        if (StringUtils.isBlank(codes)) {
            return new ArrayList<>(0);
        }

        return Arrays.asList(StringUtils.split(codes, splitChar));
    }

    public static List<String> getSplit2List(String codes) {
        return getSplit2List(codes, COMMA);
    }

    public static List<Long> getSplit2Long(String codes, String splitChar) {
        if (StringUtils.isBlank(codes)) {
            return new ArrayList<>(0);
        }

        String[] stringArr = StringUtils.split(codes, splitChar);

        return getSplit2Long(stringArr);
    }

    public static List<Long> getSplit2Long(String[] stringArray) {
        List<Long> longList = new ArrayList<>();

        if (ArrayUtils.isNotEmpty(stringArray)) {
            for (String s : stringArray) {
                longList.add(getLong(s));
            }
        }

        return longList;
    }

    public static List<Long> getSplit2Long(String codes) {
        return getSplit2Long(codes, COMMA);
    }

    public static List<Integer> getSplit2Integer(String codes, String splitChar) {
        if (StringUtils.isBlank(codes)) {
            return new ArrayList<>(0);
        }

        String[] stringArr = StringUtils.split(codes, splitChar);
        List<Integer> intList = new ArrayList<>(stringArr.length);

        for (String s : stringArr) {
            intList.add(getInteger(s));
        }

        return intList;
    }

    public static String[] getSplitStr(String codes, String splitChar) {
        if (StringUtils.isBlank(codes)) {
            return new String[0];
        }

        return StringUtils.split(codes, splitChar);
    }

    public static String[] getSplitStr(String codes) {
        return getSplitStr(codes, COMMA);
    }

    public static List<Integer> getSplit2Integer(String codes) {
        return getSplit2Integer(codes, COMMA);
    }

    public static <T> T[] concat(T[] first, T[] second, Class<T> type) {
        T[] result = newArray(type, first.length + second.length);
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);

        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] newArray(Class<T> type, int length) {
        return (T[]) Array.newInstance(type, length);
    }

    public static <T> List<T> splitSubList(List<T> targetList, int start, int end) {
        int size = targetList.size();

        if ((start > end) || (start > size)) {
            return null;
        }

        if (end > size) {
            end = size;
        }

        return targetList.subList(start, end);
    }

    public static Map<String, String> split2Map(String codes) {
        Map<String, String> result = Maps.newHashMap();
        List<String> list = getSplit2List(codes, COMMA);

        for (String str : list) {
            String[] stringArr = StringUtils.split(str, DASH);

            if (ArrayUtils.isNotEmpty(stringArr)) {
                if (stringArr.length >= 2) {
                    result.put(stringArr[0], stringArr[1]);
                } else {
                    result.put(stringArr[0], null);
                }
            }
        }

        return result;
    }

    public static <T> List<List<T>> getSplitList(List<T> baseList, int reqSplitSize) {
        if (CollectionUtils.isEmpty(baseList)) {
            return null;
        }

        int totalSize = baseList.size();
        int loopCount = Math.min(totalSize, reqSplitSize);
        int loop = totalSize % loopCount;

        if (loop == 0) {
            loop = totalSize / loopCount;
        } else {
            loop = (totalSize / loopCount) + 1;
        }

        List<List<T>> resultList = new ArrayList<>();

        for (int i = 0; i < loop; i++) {
            int startIndex = i * loopCount;
            int endIndex = Math.min(startIndex + loopCount, totalSize);
            resultList.add(baseList.subList(startIndex, endIndex));
        }

        return resultList;
    }

    public static String convertListToString(List<Long> altos, String signStr) {
        if (altos == null) {
            return null;
        }
        String str = null;
        StringBuilder sb = new StringBuilder();
        for (Long stops : altos) {
            sb.append(signStr).append(stops);
        }
        if (sb.length() > 0) {
            str = sb.substring(1);
        }
        return str;
    }

    public static <T> Set<T> split(Set<T> sourceSet, int start, int end) {
        int size = sourceSet.size();
        List<T> source = Lists.newArrayList(sourceSet);

        if ((start > end) || (start > size)) {
            return Sets.newHashSet();
        }

        if (end > size) {
            end = size;
        }

        return Sets.newHashSet(source.subList(start, end));
    }

    public static <T> List<T> split(List<T> source, int start, int end) {
        int size = source.size();
        if ((start > end) || (start > size)) {
            return Lists.newArrayList();
        }
        if (end > size) {
            end = size;
        }
        return Lists.newArrayList(source.subList(start, end));
    }

    public static boolean strContains(List<String> sources, String target) {
        if (CollectionUtils.isEmpty(sources) || StringUtils.isEmpty(target)) {
            return false;
        }

        for (String sourceStr : sources) {
            if (target.equalsIgnoreCase(sourceStr)) {
                return true;
            }
        }

        return false;
    }

    public static String hideAccount(String account, int beforeLen, int endLen) {
        return hideAccount(account, beforeLen, endLen, 2);
    }

    public static String hideAccount(String account, int beforeLen, int endLen, int star) {
        if (StringUtils.isBlank(account)) {
            return account;
        }
        int length = account.length();
        if (length < (beforeLen + endLen)) {
            return account;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(account, 0, beforeLen);
        for (int i = 0; i < star; i++) {
            sb.append(STAR);
        }
        // 替换字符串，当前使用“*”
        sb.append(account, (length - endLen), length);
        return sb.toString();
    }

    public static String formatNumber(double number) {
        DecimalFormat decimalFormat = new DecimalFormat("###################.###########");

        return decimalFormat.format(number);
    }

    public static String formatNumber(String numberStr) {
        return formatNumber(getDouble(numberStr));
    }

    public static String formatNumber(float number) {
        DecimalFormat decimalFormat = new DecimalFormat("###################.###########");

        return decimalFormat.format(number);
    }

    public static <T> List<T> getList(String value, Class<T> clazz) {
        value = getString(value);
        if (StringUtils.isBlank(value) || !(value.startsWith("[") && value.endsWith("]"))) {
            return Collections.emptyList();
        }
        JSONArray array = JSONArray.parseArray(value);
        return array.toJavaList(clazz);
    }

    /**
     * 克隆对象<br>
     * 如果对象实现Cloneable接口，调用其clone方法<br>
     * 如果实现Serializable接口，执行深度克隆<br>
     * 否则返回<code>null</code>
     *
     * @param <T> 对象类型
     * @param obj 被克隆对象
     * @return 克隆后的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(T obj) throws Exception {
        T result = cloneArray(obj);
        if (null == result) {
            if (obj instanceof Cloneable) {
                Method method = getCloneMethod(obj.getClass());
                if (method != null) {
                    method.setAccessible(true);
                    result = (T) method.invoke(obj);
                }
            } else {
                result = cloneByStream(obj);
            }
        }
        return result;
    }

    private static Method getCloneMethod(Class<?> clazz) {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod("clone");
        } catch (NoSuchMethodException e) {
            try {
                method = clazz.getMethod("clone");
            } catch (Exception ignored) {
            }
        } catch (Exception ignored) {
        }
        return method;
    }

    /**
     * 克隆数组
     *
     * @param <T>   数组元素类型
     * @param array 被克隆的数组
     * @return 新数组
     */
    public static <T> T[] clone(T[] array) {
        if (array == null) {
            return null;
        }
        return array.clone();
    }

    /**
     * 克隆数组，如果非数组返回<code>null</code>
     *
     * @param <T> 数组元素类型
     * @param obj 数组对象
     * @return 克隆后的数组对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T cloneArray(final T obj) {
        if (null == obj) {
            return null;
        }
        if (obj.getClass().isArray()) {
            final Object result;
            final Class<?> componentType = obj.getClass().getComponentType();
            if (componentType.isPrimitive()) {// 原始类型
                int length = Array.getLength(obj);
                result = Array.newInstance(componentType, length);
                while (length-- > 0) {
                    Array.set(result, length, Array.get(obj, length));
                }
            } else {
                result = ((Object[]) obj).clone();
            }
            return (T) result;
        }
        return null;
    }

    /**
     * 返回克隆后的对象，如果克隆失败，返回原对象
     *
     * @param <T> 对象类型
     * @param obj 对象
     * @return 克隆后或原对象
     */
    public static <T> T cloneIfPossible(final T obj) {
        T clone = null;
        try {
            clone = clone(obj);
        } catch (Exception e) {
            // pass
        }
        return clone == null ? obj : clone;
    }

    /**
     * 序列化后拷贝流的方式克隆<br>
     * 对象必须实现Serializable接口
     *
     * @param <T> 对象类型
     * @param obj 被克隆对象
     * @return 克隆后的对象
     * @throws RuntimeException IO异常和ClassNotFoundException封装
     */
    @SuppressWarnings("unchecked")
    public static <T> T cloneByStream(T obj) {
        if (!(obj instanceof Serializable)) {
            return null;
        }
        final FastByteArrayOutputStream byteOut = new FastByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(byteOut)) {
            out.writeObject(obj);
            out.flush();
            return (T) new ObjectInputStream(new ByteArrayInputStream(byteOut.toByteArray())).readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String valueOf(Object obj) {
        return (obj == null) ? EMPTY : obj.toString();
    }

    public static boolean equalsIgnoreCase(String a, String b) {
        return null == a ? null == b : a.equalsIgnoreCase(b);
    }


    public static String toLowerCase(String s, Locale l) {
        return null == s ? null : s.toLowerCase(l);
    }

    public static String toUpperCase(String s, Locale l) {
        return null == s ? null : s.toUpperCase(l);
    }

    public static String requireNonEmpty(final String s) {
        if (s.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return s;
    }

    public static <T> T defaultIfNull(final T object, final T defaultValue) {
        return Objects.isNull(object) ? defaultValue : object;
    }

    public static <T> T defaultIfNull(T source, Supplier<? extends T> defaultValueSupplier) {
        if (Objects.isNull(source)) {
            return defaultValueSupplier.get();
        }
        return source;
    }

    public static <T> T defaultIfNull(T source, Function<T, ? extends T> defaultValueSupplier) {
        if (Objects.isNull(source)) {
            return defaultValueSupplier.apply(null);
        }
        return source;
    }

    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String lowerFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }
}