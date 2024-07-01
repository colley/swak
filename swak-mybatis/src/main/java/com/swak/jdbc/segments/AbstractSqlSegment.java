package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.MatchSegment;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.common.util.StringPool;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static com.swak.common.util.StringPool.SPACE;

public abstract class AbstractSqlSegment  implements SqlSegment {

    protected String property;

    @Getter
    protected SqlKeyword sqlKeyword;

    public AbstractSqlSegment(String property, SqlKeyword sqlKeyword) {
        this.property = property;
        this.sqlKeyword = sqlKeyword;
    }

    public AbstractSqlSegment(SqlKeyword sqlKeyword) {
        this(StringPool.EMPTY, sqlKeyword);
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getProperty() {
        return StringUtils.trim(property);
    }

    public void where(List<SqlSegment> whereScope, SqlSegment... sqlSegments) {
        if(ArrayUtils.isNotEmpty(sqlSegments)){
            SqlSegment lastValue = CollectionUtils.isEmpty(whereScope)? null :whereScope.get(whereScope.size()-1);
            for (SqlSegment sqlSegment : sqlSegments) {
                whereScope.add(sqlSegment);
                if(Objects.isNull(lastValue) ||
                        !MatchSegment.AND_OR.match(lastValue)) {
                    whereScope.add(SqlKeyword.AND);
                }
            }
        }
    }

    public void where(List<SqlSegment> whereScope, SqlSegment sqlSegment) {
        if(sqlSegment!=null){
            where(whereScope,new SqlSegment[] {sqlSegment});
        }
    }

    protected String whereSqlString(List<SqlSegment> whereScope,ParamNameValuePairs paramNameValuePairs) {
        StringBuilder buffer = new StringBuilder();
        if (CollectionUtils.isNotEmpty(whereScope)) {
            if (MatchSegment.AND_OR.match(whereScope.get(whereScope.size()-1))) {
                whereScope.remove(whereScope.size()-1);
            }
            Iterator<SqlSegment> iter = whereScope.iterator();
            while (iter.hasNext()) {
                buffer.append((iter.next()).getSqlSegment(paramNameValuePairs)).append(SPACE);
            }
        }
        return buffer.toString();
    }
}
