package com.swak.operatelog.annotation;

import com.swak.common.dto.base.BaseOperation;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OperateLogOperation implements BaseOperation {
    /**
     * 模块
     *
     */
    private String module;

    /**
     * 操作类型
     *
     */
    private String operateType;

    /**
     * 指定日志内容
     *
     */
    private String content;

    /**
     * 过滤的字段
     *
     */
    private String[] excludeField;

    /**
     * 是否记录方法参数
     */
    private boolean logArgs;
    /**
     * 是否记录方法结果的数据
     */
    private boolean logResult;

    /**
     * 记录范围
     */
    private LogScopeEnum logScope;
}
