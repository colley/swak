package com.swak.jdbc.dialects;


import com.swak.common.exception.SwakAssert;
import com.swak.jdbc.parser.ParameterMapping;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/8 14:56
 */
public class DialectModel {
    private static final String FIRST_PARAM_NAME = "swak_first";
    private static final String SECOND_PARAM_NAME = "swak_second";

    /**
     * 分页方言 sql
     */
    @Getter
    private final String dialectSql;

    /**
     * 用 List<ParameterMapping> 消费第一个值
     */
    private Consumer<List<ParameterMapping>> firstParamConsumer = i -> {
    };
    /**
     * 用 Map<String, Object> 消费第一个值
     */
    private Consumer<Map<String, Object>> firstParamMapConsumer = i -> {
    };
    /**
     * 用 List<ParameterMapping> 消费第二个值
     */
    private Consumer<List<ParameterMapping>> secondParamConsumer = i -> {
    };
    /**
     * 用 Map<String, Object> 消费第二个值
     */
    private Consumer<Map<String, Object>> secondParamMapConsumer = i -> {
    };
    /**
     * 提供 第一个值
     */
    private final long firstParam;
    /**
     * 提供 第二个值
     */
    private final long secondParam;

    public DialectModel(String dialectSql) {
        this(dialectSql, 0, 0);
    }

    public DialectModel(String dialectSql, long firstParam) {
        this(dialectSql, firstParam, 0);
    }

    public DialectModel(String dialectSql, long firstParam, long secondParam) {
        this.dialectSql = dialectSql;
        this.firstParam = firstParam;
        this.secondParam = secondParam;
    }

    /**
     * 设置消费 List<ParameterMapping> 的方式
     * <p>带下标的</p>
     * <p>mark: 标记一下,暂时没看到哪个数据库的分页方言会存在使用该方法</p>
     *
     * @return this
     */
    @SuppressWarnings("unused")
    public DialectModel setConsumer(boolean isFirstParam, Function<List<ParameterMapping>, Integer> function) {
        if (isFirstParam) {
            this.firstParamConsumer = i -> {
                Integer index = function.apply(i);
                i.add(function.apply(i), new ParameterMapping(FIRST_PARAM_NAME,index));
            };
        } else {
            this.secondParamConsumer = i -> {
                Integer index = function.apply(i);
                i.add(function.apply(i), new ParameterMapping(SECOND_PARAM_NAME, function.apply(i)));
            };
        }
        this.setParamMapConsumer(isFirstParam);
        return this;
    }

    /**
     * 设置消费 List<ParameterMapping> 的方式
     * <p>不带下标的</p>
     *
     * @return this
     */
    public DialectModel setConsumer(boolean isFirstParam) {
        if (isFirstParam) {
            this.firstParamConsumer = i -> {
                int index = i.size();
                i.add(new ParameterMapping(FIRST_PARAM_NAME,index));
            };
        } else {
            this.secondParamConsumer = i -> {
                int index = i.size();
                i.add(new ParameterMapping(SECOND_PARAM_NAME,index));
            };
        }
        this.setParamMapConsumer(isFirstParam);
        return this;
    }

    /**
     * 设置消费 List<ParameterMapping> 的方式
     * <p>不带下标的,两个值都有</p>
     *
     * @return this
     */
    public DialectModel setConsumerChain() {
        return setConsumer(true).setConsumer(false);
    }

    /**
     * 把内部所有需要消费的都消费掉
     *
     * @param parameterMappings    ParameterMapping 集合
     * @param additionalParameters additionalParameters map
     */
    public void consumers(List<ParameterMapping> parameterMappings,Map<String, Object> additionalParameters) {
        SwakAssert.notNull(parameterMappings, "parameterMappings must notNull !");
        SwakAssert.notNull(additionalParameters, "additionalParameters must notNull !");
        this.firstParamConsumer.accept(parameterMappings);
        this.secondParamConsumer.accept(parameterMappings);
        this.firstParamMapConsumer.accept(additionalParameters);
        this.secondParamMapConsumer.accept(additionalParameters);
    }

    /**
     * 设置消费 Map<String, Object> 的方式
     */
    private void setParamMapConsumer(boolean isFirstParam) {
        if (isFirstParam) {
            this.firstParamMapConsumer = i -> i.put(FIRST_PARAM_NAME, firstParam);
        } else {
            this.secondParamMapConsumer = i -> i.put(SECOND_PARAM_NAME, secondParam);
        }
    }
}
