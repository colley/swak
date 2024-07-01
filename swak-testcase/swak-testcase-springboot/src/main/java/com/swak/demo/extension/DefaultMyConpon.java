package com.swak.demo.extension;

import com.google.common.collect.Lists;
import com.swak.core.extension.annotation.Extension;
import com.swak.demo.dto.RpcParamRequest;

import java.util.List;


@Extension(desc = "我的优惠券默认我的专属")
public class DefaultMyConpon implements MyConponExtPt {


    @Override
    public List<String> invoke(RpcParamRequest request) {
        return Lists.newArrayList("default");
    }


}
