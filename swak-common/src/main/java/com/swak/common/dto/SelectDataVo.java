package com.swak.common.dto;

import com.swak.common.dto.base.VO;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SelectDataVo implements VO {
    private static final long serialVersionUID = -1248936650708948671L;

    private String value;
    private String name;
    private String label;

    private String category;
    private String categoryName;
    /**
     * 显示排序
     */
    private Integer sortOrder;
}
