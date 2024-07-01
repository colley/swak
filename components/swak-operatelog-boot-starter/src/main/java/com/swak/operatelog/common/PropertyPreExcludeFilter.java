package com.swak.operatelog.common;

import com.alibaba.fastjson2.filter.SimplePropertyPreFilter;
import org.apache.commons.lang3.ArrayUtils;

public class PropertyPreExcludeFilter extends SimplePropertyPreFilter {
    public PropertyPreExcludeFilter(String... filters){
        super();
        if(ArrayUtils.isNotEmpty(filters)){
            for (int i = 0; i < filters.length; i++) {
                this.getExcludes().add(filters[i]);
            }
        }
    }
}
