package com.swak.jdbc.common;

import com.swak.common.util.GetterUtil;
import com.swak.common.util.StringPool;


public class SharedString extends SharedValue<String> {

    public SharedString(){
        super();
    }

    public SharedString(String value){
        super(value);
    }

    /**
     * SharedString 里是 ""
     */
    public static SharedString emptyString() {
        return new SharedString(StringPool.EMPTY);
    }

    public void toEmpty() {
        value = StringPool.EMPTY;
    }

    @Override
    public String toString() {
        return GetterUtil.getString(value);
    }
}