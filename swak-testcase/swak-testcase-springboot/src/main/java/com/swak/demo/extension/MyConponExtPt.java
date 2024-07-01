/**
 * Copyright (C), 2020-2021 by colley.ma
 * File Name: MyAlwaysBuyExtPt.java
 * Encoding: UTF-8
 * Date: 2021年12月13日 下午7:44:02
 * History:
 */
package com.swak.demo.extension;

import com.swak.core.extension.ExtCaseHandler;
import com.swak.demo.dto.RpcParamRequest;

import java.util.List;


public interface MyConponExtPt extends ExtCaseHandler<List<String>, RpcParamRequest> {

    String BIZ_ID = "MyConponExtPt";

    @Override
    default List<String> exchange(RpcParamRequest request) {
        return invoke(request);
    }
}
