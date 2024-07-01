
-- ----------------------------
-- Table structure for easy_job_info
-- ----------------------------
DROP TABLE IF EXISTS `easy_job_info`;
CREATE TABLE `easy_job_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `app_name` varchar(50) DEFAULT NULL COMMENT '应用名称',
  `job_name` varchar(80) NOT NULL COMMENT 'job名称',
  `schedule_type` varchar(80) NOT NULL COMMENT '调度类型',
  `schedule_conf` varchar(128) NOT NULL COMMENT '调度配置',
  `executor_handler` varchar(255) NOT NULL COMMENT '执行器任务handler',
  `sharding_num` int(11) DEFAULT NULL COMMENT '分片序号',
  `sharding_count` int(11) DEFAULT NULL COMMENT '分片数',
  `schedule_enabled` varchar(255) DEFAULT NULL COMMENT '开关配置，支持配置中心配置',
  `executor_param` varchar(512) DEFAULT NULL COMMENT '执行器任务参数',
  `trigger_last_time` bigint(13) NOT NULL COMMENT '上次调度时间',
  `trigger_next_time` bigint(13) NOT NULL COMMENT '下次调度时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uniq_job_appname` (`job_name`,`app_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5592 DEFAULT CHARSET=utf8 COMMENT='EasyJob信息表';

-- ----------------------------
-- Table structure for easy_job_lock
-- ----------------------------
DROP TABLE IF EXISTS `easy_job_lock`;
CREATE TABLE `easy_job_lock` (
  `app_name` varchar(50) NOT NULL COMMENT '应用名称',
  `lock_name` varchar(50) NOT NULL COMMENT '锁名称',
  PRIMARY KEY (`lock_name`,`app_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='job锁表';

SET FOREIGN_KEY_CHECKS = 1;
