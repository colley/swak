
package com.swak.jdbc.segments;

import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.common.IbsStringHelper;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.common.util.StringPool;

import java.text.MessageFormat;


public class FunctionSqlSegment extends AbstractSqlSegment {
    private final FunctionBody funcEntry;
    private final Object value;
    private final FunctionBody  secondFuncEntry;

    public FunctionSqlSegment(FunctionBody funcEntry, Object value, SqlKeyword sqlKeyword) {
        this(funcEntry, value, null, sqlKeyword);
    }

    public FunctionSqlSegment(FunctionBody funcEntry, Object value, FunctionBody secondFuncEntry, SqlKeyword sqlKeyword) {
        super(StringPool.EMPTY,sqlKeyword);
        this.funcEntry = funcEntry;
        this.value = value;
        this.secondFuncEntry = secondFuncEntry;
    }

    @Override
	public void setProperty(String property) {
        throw new UnsupportedOperationException("don't support");
    }

    @Override
	public String getProperty() {
        throw new UnsupportedOperationException("don't support");
    }




    @Override
	public String toString() {
        StringBuilder fragment = new StringBuilder();
        fragment.append(funcEntry.toString());
        fragment.append(getSqlKeyword().getKeyword());
        if (secondFuncEntry == null) {
            fragment.append(value);
        } else {
            fragment.append(secondFuncEntry);
        }

        return fragment.toString();
    }

    /**
     *特殊情况 函数中带参数，用?代替
     */
    @Override
    public String getSqlSegment(ParamNameValuePairs paramNameValuePairs) {
        StringBuilder fragment = new StringBuilder();
        fragment.append(funcEntry.getSqlSegment(paramNameValuePairs));
        fragment.append(getSqlKeyword().getSqlSegment(paramNameValuePairs) );
        if (secondFuncEntry == null) {
            String parameterName = paramNameValuePairs.addParameter("functionMode", value);
            fragment.append(IbsStringHelper.repeatParamFormat(parameterName));
        } else {
            fragment.append(secondFuncEntry.getSqlSegment(paramNameValuePairs));
        }

        return fragment.toString();
    }

    /**
     * 单个函数实现
     */
    public static class FunctionBody extends AbstractSqlSegment {
        private static final long serialVersionUID = 4136372599913962873L;
        private final String functionName;
        private final String functionValue;
        private final Object[] values;

        public FunctionBody(String functionName, String functionValue) {
            this(functionName, functionValue, new Object[0]);
        }

        public FunctionBody(String functionName, String functionValue, Object[] values) {
            super(SqlKeyword.APPLY);
            this.functionName = functionName;
            this.functionValue = functionValue;
            this.values = values;
        }
        
        
        public static FunctionBody neFunc(String functionName, String functionValue, Object[] values) {
            return new FunctionBody(functionName, functionValue, values);
        }

        public static FunctionBody neFunc(String functionName, String functionValue) {
            return new FunctionBody(functionName, functionValue);
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
         *特殊情况 函数中带参数，用?代替
         */
        @Override
        public String getSqlSegment(ParamNameValuePairs paramNameValuePairs) {
            String[] propertyNames = new String[values.length];

            for (int i = 0; i < values.length; i++) {
                String parameterName = paramNameValuePairs.addParameter(getSqlKeyword().name(), values[i]);
                propertyNames[i] = IbsStringHelper.repeatParamFormat(parameterName);
            }

            return functionName + "(" + formatFunctionValue(propertyNames) + ")";
        }

        @Override
		public String toString() {
            return functionName + "(" + functionValue + ")";
        }

        private String formatFunctionValue(Object[] propertyNames) {
            StringBuilder newPropertyStr = new StringBuilder(functionValue.length());
            int index = 0;

            for (int i = 0; i < functionValue.length(); i++) {
                char ch = functionValue.charAt(i);

                if (functionValue.charAt(i) == '?') {
                    newPropertyStr.append("{").append(index).append("}");
                    index++;
                } else {
                    newPropertyStr.append(ch);
                }
            }
            return MessageFormat.format(newPropertyStr.toString(), propertyNames);
        }
    }
}
