
package com.swak.jdbc.segments;


import com.google.common.collect.Lists;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.common.IbsStringHelper;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.common.util.StringPool;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * ID IN(1,2,3)
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/6 15:08
 **/
public class InSqlSegment extends AbstractSqlSegment {

    private final List<Object> valueList = Lists.newArrayList();

    public InSqlSegment(String property, SqlKeyword sqlKeyword, Object[] values) {
        super(property, sqlKeyword);
        if(ArrayUtils.isNotEmpty(values)){
            valueList.addAll(Lists.newArrayList(values));
        }
    }

    public InSqlSegment(String property, SqlKeyword sqlKeyword, List<Object> values) {
        super(property, sqlKeyword);
        if(CollectionUtils.isNotEmpty(values)){
            valueList.addAll(values);
        }
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs paramNameValuePairs) {
        if(CollectionUtils.isEmpty(valueList)){
            return StringPool.EMPTY;
        }
        List<String> paramNameList = Lists.newArrayList();
        valueList.forEach(value->{
            String parameterName = paramNameValuePairs.addParameter(property, value);
            paramNameList.add(IbsStringHelper.repeatParamFormat(parameterName));
        });
        String[] columns = new String[]{property};
        String cols = IbsStringHelper.join(", ", columns);
        return cols + getSqlKeyword().getSqlSegment(paramNameValuePairs) +
                "(" + StringUtils.join(paramNameList,",") + ')';
    }
}
