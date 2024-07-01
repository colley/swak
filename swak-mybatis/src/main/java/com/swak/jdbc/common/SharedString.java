package com.swak.jdbc.common;

import com.swak.common.util.GetterUtil;
import com.swak.common.util.StringPool;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
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


    public void toNull() {
        value = null;
    }

    @Override
    public String toString() {
        return GetterUtil.getString(value);
    }

    @Override
    public String getValue() {
        return super.getValue();
    }
}