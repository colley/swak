
package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.common.IbsStringHelper;
import com.swak.jdbc.enums.SqlKeyword;
import lombok.Getter;


/**
 * between start and end
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/6 15:03
 **/
@Getter
public class BetweenSegment extends AbstractSqlSegment {
    private final Object low;
    private final Object high;

    public BetweenSegment(String column, Object low, Object high) {
        this(column, low, high,SqlKeyword.BETWEEN);
    }

    public BetweenSegment(String column, Object low, Object high, SqlKeyword sqlKeyword) {
       super(column,sqlKeyword);
        this.low = low;
        this.high = high;
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs paramNameValuePairs) {
    	String parameterNameLow = paramNameValuePairs.addParameter(getProperty()+"low", low);
		String  parameterNameHigh = paramNameValuePairs.addParameter(getProperty() + "high", high);
    	StringBuilder buffer = new StringBuilder();
    	buffer.append("(");
    	buffer.append(getProperty());
    	buffer.append(sqlKeyword.getSqlSegment(paramNameValuePairs));
    	buffer.append(IbsStringHelper.repeatParamFormat(parameterNameLow));
    	buffer.append(" AND ");
    	buffer.append(IbsStringHelper.repeatParamFormat(parameterNameHigh));
    	buffer.append(")");
        return buffer.toString();
    }

    @Override
	public String toString(){
    	return getProperty() +sqlKeyword.getKeyword()+low+" AND "+high;
    }
}
