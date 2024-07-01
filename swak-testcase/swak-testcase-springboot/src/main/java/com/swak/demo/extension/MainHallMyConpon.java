package com.swak.demo.extension;

import com.google.common.collect.Lists;
import com.swak.core.extension.annotation.Extension;
import com.swak.demo.dto.RpcParamRequest;

import java.util.List;


@Extension(bizId = MyConponExtPt.BIZ_ID, useCase = "main", desc = "主会场我的优惠券信息")
public class MainHallMyConpon implements MyConponExtPt {


    @Override
    public List<String> invoke(RpcParamRequest request) {
        return Lists.newArrayList("main");
    }
}
