
package com.swak.jdbc.segments;

import com.swak.common.util.StringPool;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.MatchSegment;
import com.swak.jdbc.enums.SqlKeyword;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author colley
 */
public  class MergeSegments implements SqlSegment{
    @Getter
    protected NormalSegmentList normal = new NormalSegmentList();

    @Getter
    protected List<SqlSegment> orderBys = new ArrayList<>();

    @Getter
    protected GroupBySegment groupBy = new GroupBySegment();

    public void add(SqlSegment... sqlSegments) {
        List<SqlSegment> list = Arrays.asList(sqlSegments);
        SqlSegment firstSqlSegment = list.get(0);
        if (MatchSegment.ORDER_BY.match(firstSqlSegment)) {
            list.remove(0);
            this.orderBys.addAll(list);
        } else if (MatchSegment.GROUP_BY.match(firstSqlSegment)) {
            list.remove(0);
            this.groupBy.groupBy(list);
        } else if (MatchSegment.HAVING.match(firstSqlSegment)) {
            list.remove(0);
            groupBy.having(list);
        } else {
            this.normal.addAll(list);
        }
    }

    @Override
    public SqlKeyword getSqlKeyword() {
        return SqlKeyword.WHERE;
    }

    public MergeSegments clear() {
        normal.clear();
        orderBys.clear();
        groupBy = new GroupBySegment();
        return this;
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        StringBuilder buf = new StringBuilder();
        if (CollectionUtils.isNotEmpty(normal)) {
            buf.append(normal.getSqlSegment(valuePairs));
        }
        //增加groupBy和order
        if (groupBy != null) {
            buf.append(groupBy.getSqlSegment(valuePairs));
        }
        if (CollectionUtils.isNotEmpty(orderBys)) {
            buf.append(orderBySql(valuePairs));
        }
        return buf.toString();
    }

    protected String orderBySql(ParamNameValuePairs paramNameValuePairs) {
        if (CollectionUtils.isNotEmpty(orderBys)) {
            List<String> orderByList = orderBys.stream().map(orderSegment -> orderSegment.getSqlSegment(paramNameValuePairs))
                    .collect(Collectors.toList());
            return StringUtils.join(orderByList, ",");
        }
        return StringPool.EMPTY;
    }
}
