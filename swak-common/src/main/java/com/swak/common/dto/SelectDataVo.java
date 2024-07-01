package com.swak.common.dto;

import com.swak.common.dto.base.VO;
import lombok.Data;

@Data
public class SelectDataVo implements VO {
    private static final long serialVersionUID = -1248936650708948671L;

    private String value;
    private String name;
    private String label;

    private String category;
    private String categoryName;
}
