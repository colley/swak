package com.swak.excel.metadata;

import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class ExcelWriteDynamicData extends ExcelWriteData<List<Object>> {
    /**
     * 头部信息
     */
    private List<List<String>> heads = new ArrayList<>();

    public void head(String... heads) {
        head(Arrays.asList(heads));
    }

    public void head(List<String> headList) {
        if (CollectionUtils.isNotEmpty(headList)) {
            headList.forEach(head -> {
                this.heads.add(Lists.newArrayList(head));
            });
        }
    }
}
