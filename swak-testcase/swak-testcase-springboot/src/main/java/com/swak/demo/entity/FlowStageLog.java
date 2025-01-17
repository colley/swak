package com.swak.demo.entity;

import com.swak.jdbc.annotation.SColumn;
import com.swak.jdbc.annotation.STable;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * FlowStageLog.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@Data
@Accessors(chain = true)
@STable("flow_stage_log")
public class FlowStageLog {

    @SColumn("chamber_id")
    private Long chamberId;

    @SColumn("workflow_id")
    private String workflowId;

    @SColumn("logic_id")
    private Long logicId;
}
