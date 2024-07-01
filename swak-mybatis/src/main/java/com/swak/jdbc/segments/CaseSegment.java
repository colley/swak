
package com.swak.jdbc.segments;

import com.google.common.collect.Lists;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.common.IbsStringHelper;
import com.swak.jdbc.enums.SqlKeyword;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * (case when a=1 then a when a=2 then b end)='值'
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/6 15:04
 **/
public class CaseSegment extends AbstractSqlSegment {

    private final List<SqlSegment> whenExpressions = new ArrayList<>();

    private final Object value;

    public CaseSegment(WhenExpression[] whenExpression, Object value, SqlKeyword sqlKeyword) {
        super(sqlKeyword);
        if (whenExpression != null) {
            this.whenExpressions.addAll(Arrays.asList(whenExpression));
        }
        this.value = value;
    }

    public CaseSegment add(WhenExpression whenExpression) {
        Optional.ofNullable(whenExpression).ifPresent(whenExpressions::add);
        return this;
    }

    @Override
    public void setProperty(String property) {
        throw new UnsupportedOperationException("don't support");
    }

    @Override
    public String getProperty() {
        throw new UnsupportedOperationException("don't support");
    }

    /**
     * (case when a=1 then a when a=2 then b end)='值'
     */
    @Override
    public String getSqlSegment(ParamNameValuePairs paramNameValuePairs) {
        String parameterName = paramNameValuePairs.addParameter("caseExpress", value);
        StringBuffer fragment = new StringBuffer("(CASE ");
        // 设置when parameterName
        for (SqlSegment whenExpression : whenExpressions) {
            fragment.append(whenExpression.getSqlSegment(paramNameValuePairs));
        }
        // 设置end
        fragment.append(" END)");
        fragment.append(getSqlKeyword().getSqlSegment(paramNameValuePairs));
        fragment.append(IbsStringHelper.repeatParamFormat(parameterName));
        return fragment.toString();
    }

    public static class WhenExpression extends AbstractSqlSegment {
        private final List<SqlSegment> sqlSegmentList = new ArrayList<>();
        private final Object value;
        private final String propertyName;

        public WhenExpression(SqlSegment[] sqlSegments, Object value, boolean isProperty) {
            super(SqlKeyword.WHEN);
            if (sqlSegments != null) {
                this.sqlSegmentList.addAll(Arrays.asList(sqlSegments));
            }
            if (isProperty) {
                this.propertyName = value.toString();
                this.value = null;
            } else {
                this.propertyName = null;
                this.value = value;
            }
        }

        public WhenExpression(SqlSegment sqlSegment, Object value, boolean isProperty) {
            this(Lists.newArrayList(sqlSegment).toArray(new SqlSegment[0]), value, isProperty);
        }

        public static WhenExpression whenProperty(SqlSegment[] sqlSegments, Object value) {
            return new WhenExpression(sqlSegments, value, true);
        }

        public static WhenExpression whenProperty(SqlSegment sqlSegment, Object value) {
            return new WhenExpression(sqlSegment, value, true);
        }

        public static WhenExpression whenValue(SqlSegment[] sqlSegments, Object value) {
            return new WhenExpression(sqlSegments, value, false);
        }

        public static WhenExpression whenValue(SqlSegment sqlSegment, Object value) {
            return new WhenExpression(sqlSegment, value, false);
        }

        public WhenExpression addWhen(SqlSegment sqlSegment) {
            if (sqlSegment != null) {
                sqlSegmentList.add(sqlSegment);
            }
            return this;
        }

        @Override
        public String getSqlSegment(ParamNameValuePairs valuePairs) {
            StringBuffer fragment = new StringBuffer();
            fragment.append(getSqlKeyword().getSqlSegment(valuePairs));
            if (sqlSegmentList.size() > 1) {
                fragment.append("(");
            }
            Iterator<SqlSegment> iter = this.sqlSegmentList.iterator();
            while (iter.hasNext()) {
                fragment.append(iter.next().getSqlSegment(valuePairs));
                if (iter.hasNext()) {
                    fragment.append(" AND ");
                }
            }
            if (sqlSegmentList.size() > 1) {
                fragment.append(")");
            }
            fragment.append(" THEN ");
            if (StringUtils.isNotEmpty(propertyName)) {
                fragment.append(propertyName);
            } else {
                String parameterName = valuePairs.addParameter(getSqlKeyword().name(), value);
                fragment.append(IbsStringHelper.repeatParamFormat(parameterName));
            }
            return fragment.toString();
        }
    }
}
