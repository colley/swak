package com.swak.easyjob.mapper;

import com.swak.common.util.GetterUtil;
import com.swak.easyjob.EasyJobConfig;
import com.swak.easyjob.annotation.EasyJobInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * The type Easy job mapper.
 *
 * @author colley.ma
 * @since  2022 /8/25 17:35
 */
public class EasyJobMapperImpl implements EasyJobMapper {

    private final static String REPLACE_INSERT_SQL = "REPLACE INTO %s ( app_name,job_name, schedule_type,schedule_conf, executor_handler, executor_param, sharding_num, sharding_count,schedule_enabled, trigger_last_time, trigger_next_time ) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
    private final EasyJobConfig easyJobConfig;
    private JdbcTemplate jdbcTemplate;

    /**
     * Instantiates a new Easy job mapper.
     *
     * @param easyJobConfig the easy job config
     */
    public EasyJobMapperImpl(EasyJobConfig easyJobConfig) {
        this.jdbcTemplate = easyJobConfig.getJdbcTemplate();
        this.easyJobConfig = easyJobConfig;
    }

    /**
     * 保存任务信息
     *
     * @param infos
     * @return boolean
     */
    @Override
    public boolean register(List<EasyJobInfo> infos) {
        if (CollectionUtils.isEmpty(infos)) {
            return false;
        }
        int[][] rows = jdbcTemplate.batchUpdate(String.format(REPLACE_INSERT_SQL, easyJobConfig.getJobInfoTableName()),
                infos, infos.size(), (ps, info) -> {
                    int i = 0;
                    ps.setString(++i, info.getAppName());
                    ps.setString(++i, info.getJobName());
                    ps.setString(++i, info.getScheduleType());
                    ps.setString(++i, info.getScheduleConf());
                    ps.setString(++i, info.getExecutorHandler());
                    ps.setString(++i, info.getExecutorParam());
                    ps.setInt(++i, info.getShardingNum());
                    ps.setInt(++i, info.getShardingCount());
                    ps.setString(++i, info.getScheduleEnabled());
                    ps.setLong(++i, info.getTriggerLastTime());
                    ps.setLong(++i, info.getTriggerNextTime());
                });
        return rows.length > 0;
    }

    /**
     * 初始化lock表
     *
     * @param lockName
     * @return boolean
     */
    @Override
    public boolean initJobLockData(String lockName) {
        Integer count = jdbcTemplate.queryForObject(String.format("select count(*) from %s where app_name=? and lock_name=?", easyJobConfig.getJobLockTableName()), Integer.class, easyJobConfig.getAppName(), lockName);
        if (GetterUtil.getInteger(count) <= 0) {
            int rows = jdbcTemplate.update(String.format("REPLACE INTO %s (app_name,lock_name) VALUES (?,?)",
                    easyJobConfig.getJobLockTableName()), easyJobConfig.getAppName(), lockName);
            return rows > 0;
        }
        return true;
    }


    /**
     * @param jobName
     * @return EasyJobInfo
     */
    @Override
    public EasyJobInfo findByJobName(String jobName) {
        return jdbcTemplate.queryForObject(
                String.format("SELECT * FROM  %s where app_name=? and job_name=?", easyJobConfig.getJobInfoTableName()),
                new JobRowMapper(),
                new Object[]{easyJobConfig.getAppName(), jobName});
    }

    /**
     * 保存任务调度信息
     *
     * @param jobInfo
     * @return int
     */
    @Override
    public int scheduleUpdate(EasyJobInfo jobInfo) {
        return jdbcTemplate.update(
                String.format("UPDATE %s SET trigger_last_time=?,trigger_next_time=? WHERE id=?", easyJobConfig.getJobInfoTableName()),
                jobInfo.getTriggerLastTime(),
                jobInfo.getTriggerNextTime(),
                jobInfo.getId());
    }

    /**
     * 获取调度任务
     *
     * @param maxNextTime
     * @return List<EasyJobInfo>
     */
    @Override
    public List<EasyJobInfo> scheduleJobQuery(long maxNextTime) {
        return jdbcTemplate.query(
                String.format("SELECT * FROM  %s where app_name=? and trigger_next_time<=?", easyJobConfig.getJobInfoTableName()),
                new JobRowMapper(),
                new Object[]{easyJobConfig.getAppName(), maxNextTime});
    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return easyJobConfig.getJdbcTemplate();
    }

    /**
     * The type Job row mapper.
     */
    class JobRowMapper implements RowMapper<EasyJobInfo> {
        @Override
        public EasyJobInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            EasyJobInfo jobInfo = EasyJobInfo.builder().build();
            jobInfo.setId(rs.getInt("id"));
            jobInfo.setAppName(rs.getString("app_name"));
            jobInfo.setJobName(rs.getString("job_name"));
            jobInfo.setShardingNum(rs.getInt("sharding_num"));
            jobInfo.setShardingCount(rs.getInt("sharding_count"));
            jobInfo.setScheduleType(rs.getString("schedule_type"));
            jobInfo.setScheduleConf(rs.getString("schedule_conf"));
            jobInfo.setExecutorHandler(rs.getString("executor_handler"));
            jobInfo.setExecutorParam(rs.getString("executor_param"));
            jobInfo.setScheduleEnabled(rs.getString("schedule_enabled"));
            jobInfo.setTriggerLastTime(rs.getLong("trigger_last_time"));
            jobInfo.setTriggerNextTime(rs.getLong("trigger_next_time"));
            return jobInfo;
        }
    }
}
