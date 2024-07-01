
package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.enums.SqlKeyword;

/**
 * <p>
 *     name is null
 *     name is not null
 * </p>
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/6 15:17
 **/
public class NullSegment extends AbstractSqlSegment {

    public NullSegment(String property, SqlKeyword sqlKeyword) {
       super(property,sqlKeyword);
    }

	@Override
	public String toString(){
		return property+getSqlKeyword().getKeyword();
	}

	@Override
	public String getSqlSegment(ParamNameValuePairs paramNameValuePairs) {
		String fragment = getProperty() + " " +
				getSqlKeyword().getSqlSegment(paramNameValuePairs);
		return fragment;
	}
}
