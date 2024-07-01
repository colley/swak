
package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.common.util.StringPool;

import java.util.Objects;

/**
 * ID NOT (select ID from user)
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/6 15:16
 **/
public class NotSegment extends AbstractSqlSegment {
    private final SqlSegment sqlSegment;

    public NotSegment(SqlSegment sqlSegment) {
        super("NOT", SqlKeyword.NOT);
        this.sqlSegment = sqlSegment;
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs paramNameValuePairs) {
        if(Objects.nonNull(sqlSegment)){
            return "not (" + sqlSegment.getSqlSegment(paramNameValuePairs) + ')';
        }
        return StringPool.EMPTY;
    }
}
