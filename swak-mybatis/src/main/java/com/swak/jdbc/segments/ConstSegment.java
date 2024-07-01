
package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.SqlKeyword;

/**
 * const e.g name=name
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/6 15:04
 **/
public class ConstSegment extends AbstractSqlSegment {
    private final String constValue;

    public ConstSegment(String constValue) {
        super(SqlKeyword.APPLY);
        this.constValue = constValue;
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs paramNameValuePairs) {
        return constValue;
    }
}
