package com.swak.demo.extension;

import com.google.common.collect.Lists;
import com.swak.core.extension.annotation.Extension;
import com.swak.demo.dto.RpcParamRequest;

import java.util.List;

@Extension(bizId = MyConponExtPt.BIZ_ID, useCase = "my", desc = "我的专属优惠券")
public class AssetsMyConpon implements MyConponExtPt {


    @Override
    public List<String> invoke(RpcParamRequest request) {
        return Lists.newArrayList("My");
    }

}
