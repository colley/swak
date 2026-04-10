CREATE TABLE `token_jwt` (
 `id` bigint(20) NOT NULL AUTO_INCREMENT,
 `token` varchar (64) NOT NULL COMMENT 'token',
 `token_jwt` text  COMMENT 'TokenJwt',
 `expire_time` datetime NOT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uniq_token` (`token`)
 ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='token jwt';