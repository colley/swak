
package com.swak.jdbc.segments;

import com.google.common.collect.Lists;
import com.swak.common.util.StringPool;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.SqlKeyword;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class JunctionSqlSegment extends AbstractSqlSegment {
    private final List<SqlSegment> sqlSegmentList = new ArrayList<>();


    public JunctionSqlSegment(SqlKeyword sqlKeyword) {
        super(sqlKeyword);
    }

    public JunctionSqlSegment addSqlSegment(SqlSegment... sqlSegments) {
        if (ArrayUtils.isNotEmpty(sqlSegments)) {
            sqlSegmentList.addAll(Lists.newArrayList(sqlSegments));
        }
        return this;
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs paramNameValuePairs) {
    	if (sqlSegmentList.size()==0 ) {
    		return StringPool.EMPTY;
    	}
		StringBuilder buffer = new StringBuilder()
			.append('(');
		Iterator<SqlSegment> iter = sqlSegmentList.iterator();
		while ( iter.hasNext() ) {
			buffer.append(iter.next().getSqlSegment(paramNameValuePairs) );
			if ( iter.hasNext() ) {
				buffer.append(' ').append(getSqlKeyword().getSqlSegment(paramNameValuePairs)).append(' ');
			}
		}
		return buffer.append(')').toString();
    }
}
