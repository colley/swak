package com.swak.excel.metadata;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class IncludeField {

    /**
     * 需要导出的字段
     */
    private String fieldName;

    /**
     * 顺序
     */
    private  Integer index;
}
