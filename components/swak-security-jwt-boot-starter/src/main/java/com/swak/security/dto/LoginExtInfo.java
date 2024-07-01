package com.swak.security.dto;

import com.swak.common.dto.base.VO;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LoginExtInfo implements VO {
   private String ipaddr;

    private String loginLocation;

    private String browser;
    private  String os;
}
