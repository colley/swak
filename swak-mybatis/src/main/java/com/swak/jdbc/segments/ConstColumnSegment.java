package com.swak.jdbc.segments;

import com.swak.common.convert.ConvertUtil;
import com.swak.common.util.StringPool;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.SqlKeyword;

import java.util.Objects;

/**
 * ConstColumnSegment.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public class ConstColumnSegment implements ColumnSegment {

    private final Object value;

    public ConstColumnSegment(Object value) {
        this.value = value;
    }

    public ConstColumnSegment() {
        this(null);
    }

    public static ConstColumnSegment constant(Object value) {
        return new ConstColumnSegment(value);
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        if (Objects.isNull(value)) {
            return "NULL";
        }
        String valueStr = ConvertUtil.toStr(value, StringPool.EMPTY);
        return String.format("'%s'", valueStr);
    }

    @Override
    public SqlKeyword getSqlKeyword() {
        return SqlKeyword.APPLY;
    }
}
