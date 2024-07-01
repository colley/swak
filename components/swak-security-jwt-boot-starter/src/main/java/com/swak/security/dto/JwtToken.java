package com.swak.security.dto;

import com.swak.common.dto.base.VO;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class JwtToken implements VO {
    private Long loginTime;

    private String access_token;

    private Long expires_in;
}
