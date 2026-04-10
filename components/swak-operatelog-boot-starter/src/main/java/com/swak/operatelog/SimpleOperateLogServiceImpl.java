package com.swak.operatelog;

import com.alibaba.fastjson2.JSON;
import com.swak.common.dto.Response;
import com.swak.operatelog.annotation.OperateDataLog;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SimpleOperateLogServiceImpl implements OperateLogService {

    @Override
    public Response<Void> addOperationLogs(List<OperateDataLog> operateDataLogs) {
        if (log.isDebugEnabled()) {
            log.debug("[Swak-OpLog] {}", JSON.toJSONString(operateDataLogs));
        }
        return Response.success();
    }
}
