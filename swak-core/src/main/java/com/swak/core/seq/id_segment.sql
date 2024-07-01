CREATE TABLE `id_segment` (
 `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
 `biz_tag` varchar(20) DEFAULT NULL COMMENT '业务标识',
 `max_id` bigint(20) DEFAULT NULL COMMENT '分配的id号段的最大值',
 `p_step` bigint(20) DEFAULT NULL COMMENT '步长',
 `last_update_time` datetime DEFAULT NULL,
 `current_update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uniq_biz_tag` (`biz_tag`)
 ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='号段存储表';