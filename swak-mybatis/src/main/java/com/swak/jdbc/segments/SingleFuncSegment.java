
package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairTranslator;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.common.IbsStringHelper;
import com.swak.jdbc.enums.SqlKeyword;


public class SingleFuncSegment extends AbstractSqlSegment  {
    private final Object value;
    private final String functionName;
    private final boolean left;

    public SingleFuncSegment(String property, String functionName, Object value, boolean isLeft) {
        super(property, SqlKeyword.APPLY);
        this.functionName = functionName;
        this.property = property;
        this.value = value;
        this.left = isLeft;
    }

    @Override
	public String toString() {
        if(left){
            return functionName + "("+property +",'"+ value.toString()+"')";
        }else{
            return functionName + "('"+value.toString() +"',"+property+")";
        }
    }
    
    public static void main(String[] args) {
        ParamNameValuePairs paramNameValuePairs = new ParamNameValuePairTranslator();
        SingleFuncSegment func = new SingleFuncSegment("FIND_IN_SET", "task_list", "2", true);
        System.out.println(func.getSqlSegment(paramNameValuePairs));
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs paramNameValuePairs) {
        String parameterName = paramNameValuePairs.addParameter(property, value);
        StringBuilder fragment = new StringBuilder();
        fragment.append(functionName).append("(");
        if(left){
            fragment.append(getProperty()).append(",");
            fragment.append(IbsStringHelper.repeatParamFormat(parameterName)).append(")");
        }else{
            fragment.append(IbsStringHelper.repeatParamFormat(parameterName)).append(",");
            fragment.append(getProperty()).append(")");
        }
        return fragment.toString();
    }
}
