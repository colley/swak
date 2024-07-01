
package com.swak.jdbc.segments;


import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.common.ConfigProperties;
import com.swak.jdbc.common.SharedBool;
import com.swak.jdbc.common.SharedString;
import com.swak.jdbc.conditions.WhereWrapper;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;

/**
 * 合并 SQL WHERE 片段
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/6 15:23
 **/
public abstract class WhereSegmentWrapper<Children extends WhereSegmentWrapper<Children>>
        extends WhereWrapper<Children> implements WhereSegment<Children> {
    @Getter
    protected SharedBool hasAlias = new SharedBool(false);

    protected Children typedThis = getChildren();

    @Override
    protected void initNeed() {
        expression = new MergeSegments();
        lastSql = SharedString.emptyString();
        sqlComment = SharedString.emptyString();
        sqlFirst = SharedString.emptyString();
        this.alias = new SharedString(ConfigProperties.tableAlias);
    }

    public WhereSegmentWrapper() {
        super();
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        return expression.getSqlSegment(valuePairs);
    }

    @Override
    public Children where(SqlSegment... sqlSegments) {
        if (ArrayUtils.isNotEmpty(sqlSegments)) {
            expression.add(sqlSegments);
        }
        return getChildren();
    }

    @Override
    public void clear() {
        expression.clear();
        lastSql.toEmpty();
        sqlComment.toEmpty();
        sqlFirst.toEmpty();
    }
}
