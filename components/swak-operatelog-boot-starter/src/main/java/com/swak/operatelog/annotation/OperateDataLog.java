package com.swak.operatelog.annotation;

import com.swak.common.dto.base.DTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;


@Data
@Accessors(chain = true)
public class OperateDataLog implements DTO {

    public static final String TRACE_ID = "TRACE_ID";

    /**
     * 链路追踪编号
     */
    private String traceId;

    /**
     * 用户Id,不同的业务场景不一样，定义为字符串类型
     */
    private String userId;

    /**
     * 模块类型
     */
    private String module;

    /**
     * 操作类型
     */
    private String operateType;

    /**
     * 操作明细
     */
    private String content;

    /**
     * 请求方法名
     */
    private String requestMethod;

    /**
     * 请求地址
     */
    private String requestUrl;

    /**
     * 用户 IP
     */
    private String userIp;

    /**
     * 浏览器 UserAgent
     */
    private String userAgent;

    /**
     * Java 方法名
     */
    private String javaMethod;

    /**
     * Java 方法的参数
     */
    private String javaMethodArgs;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 执行时长，单位：毫秒
     */
    private Long costTime;

    /**
     * 结果码
     */
    private Integer resultCode;

    /**
     * 结果提示
     */
    private String resultMsg;

    /**
     * 结果数据
     */
    private String resultData;

    private Date createTime;

    /**
     * 上下文
     */
    private OperateLogContext contextHolder;
}
