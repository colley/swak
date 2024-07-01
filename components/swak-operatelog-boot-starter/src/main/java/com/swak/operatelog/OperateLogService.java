package com.swak.operatelog;

import com.swak.common.dto.Response;
import com.swak.operatelog.annotation.OperateDataLog;
import com.swak.operatelog.annotation.OperateLogContext;

import java.util.List;

/**
 * 日志的处理服务
 */
public interface OperateLogService {

    /**
     * 预处理
     */
    default void preHandle(OperateLogContext contextHolder){
    }
    /**
     * 操作日志
     * @param operateDataLogs
     */
    Response<Void> addOperationLogs(List<OperateDataLog> operateDataLogs);


    /**
     * 操作日志是否开启，可以根据用户的角色来开启
     * 比如操作员等
     */
    default boolean isOperateLogEnabled() {
        return true;
    }
}
