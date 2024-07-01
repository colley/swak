package com.swak.easyjob;

import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The type Easy job config.
 */
@Data
public class EasyJobConfig {
    private String appName;
    private JdbcTemplate jdbcTemplate;
    /**
     * job info 表名
     */
    private String jobInfoTableName = "easy_job_info";
    /**
     * job lock 表名
     */
    private String jobLockTableName = "easy_job_lock";
}
