package com.swak.demo.dto;

import com.swak.common.dto.base.BizScenario;
import com.swak.common.dto.base.Query;
import lombok.Data;

@Data
public class RpcParamRequest extends Query {

    protected BizScenario bizScenario;
    private String pin;
}
