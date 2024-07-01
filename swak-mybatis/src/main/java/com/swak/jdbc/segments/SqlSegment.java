package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.SqlKeyword;

import java.io.Serializable;

public interface SqlSegment extends Serializable {
    /**
     * SQL片段
     */
    String getSqlSegment(ParamNameValuePairs valuePairs);

     SqlKeyword getSqlKeyword();
}
