CREATE TABLE `canonical_field_mapping` (
                                           `id` BIGINT NOT NULL AUTO_INCREMENT,
                                           `canonical_field` VARCHAR(255) NOT NULL,
                                           `description` VARCHAR(255) DEFAULT NULL,
                                           `aliases` JSON DEFAULT NULL,
                                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `kb_info` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT,
                           `canonical_field` varchar(255) NOT NULL,
                           `description` varchar(255) DEFAULT NULL,
                           `aliases` varchar(1000) DEFAULT NULL COMMENT '逗号分隔别名字符串，如 custNo,cust_no',
                           `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                           `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `canonical_field` (`canonical_field`),
                           UNIQUE KEY `ux_kb_info_canonical_field` (`canonical_field`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4;


CREATE TABLE `kb_pending` (
                              `id` bigint(20) NOT NULL AUTO_INCREMENT,
                              `original_field` varchar(255) DEFAULT NULL,
                              `ai_canonical_field` varchar(255) DEFAULT NULL,
                              `canonical_field_description` varchar(500) DEFAULT NULL,
                              `reason` varchar(500) DEFAULT NULL,
                              `confidence` double DEFAULT NULL,
                              `aliases` json DEFAULT NULL,
                              `status` varchar(20) DEFAULT 'PENDING',
                              `create_time` datetime DEFAULT NULL,
                              `update_time` datetime DEFAULT NULL,
                              `auto_approved` tinyint(1) DEFAULT '0' COMMENT '是否自动通过(0/1)',
                              `approve_time` datetime DEFAULT NULL COMMENT '自动/人工通过时间',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 ;