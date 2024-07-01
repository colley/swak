
package com.swak.jdbc.segments;

import com.swak.common.util.StringPool;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.SqlKeyword;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * group by name,age
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/6 15:07
 **/
public class GroupBySegment extends AbstractSqlSegment {
    private final List<SqlSegment> groupByColumnList = new ArrayList<>();
    private final List<SqlSegment> having = new ArrayList<>();

    public GroupBySegment() {
        super(SqlKeyword.GROUP_BY);
    }

    public GroupBySegment(String... groupColumn) {
       this();
        groupBy(groupColumn);
    }

    public GroupBySegment having(SqlSegment sqlSegment) {
        if (sqlSegment != null) {
            having.add(sqlSegment);
        }
        return this;
    }

    public GroupBySegment having(List<SqlSegment> sqlSegments) {
        if (sqlSegments != null) {
            having.addAll(sqlSegments);
        }
        return this;
    }

    public  GroupBySegment groupBy(String... groupColumn) {
        if (ArrayUtils.isNotEmpty(groupColumn)) {
            for (String columnName : groupColumn) {
                if (StringUtils.isNotEmpty(columnName)) {
                    this.groupByColumnList.add(StringSqlSegment.apply(columnName));
                }
            }
        }
        return this;
    }

    public  GroupBySegment groupBy(List<SqlSegment> sqlSegments) {
        if (CollectionUtils.isNotEmpty(sqlSegments)) {
            this.groupByColumnList.addAll(sqlSegments);
        }
        return this;
    }

    @Override
    public String toString() {
        return getSqlKeyword().getKeyword() + " " + StringUtils.join(groupByColumnList, ",") +
                (CollectionUtils.isNotEmpty(having) ? SqlKeyword.HAVING.getKeyword() + having.iterator() : "");
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        if(CollectionUtils.isEmpty(groupByColumnList)){
            return StringPool.EMPTY;
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append(getSqlKeyword().getSqlSegment(valuePairs))
                .append(StringUtils.join(groupByColumnList, ","));
        if (CollectionUtils.isNotEmpty(having)) {
            buffer.append(SqlKeyword.HAVING.getKeyword());
            buffer.append(whereSqlString(having,valuePairs));
        }
        return buffer.toString();
    }
}
