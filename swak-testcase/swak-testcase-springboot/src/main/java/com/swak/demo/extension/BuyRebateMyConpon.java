package com.swak.demo.extension;

import com.google.common.collect.Lists;
import com.swak.core.extension.annotation.Extension;
import com.swak.demo.dto.RpcParamRequest;

import java.util.List;


@Extension(bizId = MyConponExtPt.BIZ_ID, useCase = "rebate", desc = "38节买返券")
public class BuyRebateMyConpon implements MyConponExtPt {

    @Override
    public List<String> invoke(RpcParamRequest request) {
        return Lists.newArrayList("rebate");
    }
}
