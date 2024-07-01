package com.swak.easyjob.mapper;

import com.swak.easyjob.annotation.EasyJobInfo;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;


/**
 * The interface Easy job mapper.
 * @author yuanchao.ma
 * @since 2022/9/13 17:03
*/
public interface EasyJobMapper {

    /**
     * 保存任务信息
     *
     * @param info the info
     * @return boolean
     */
     boolean register(List<EasyJobInfo> info);

    /**
     * 初始化lock表
     *
     * @param lockName the lock name
     * @return boolean
     */
     boolean initJobLockData(String lockName);


    /**
     * Find by job name easy job info.
     *
     * @param jobName the job name
     * @return easy job info
     */
     EasyJobInfo findByJobName(String jobName) ;

    /**
     * 保存任务调度信息
     *
     * @param jobInfo the job info
     * @return int
     */
     int scheduleUpdate(EasyJobInfo jobInfo);

    /**
     * 获取调度任务
     *
     * @param maxNextTime the max next time
     * @return list
     */
     List<EasyJobInfo> scheduleJobQuery(long maxNextTime) ;

    /**
     * Gets jdbc template.
     *
     * @return the jdbc template
     */
     JdbcTemplate getJdbcTemplate();
}
