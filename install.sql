-- 影院售票系统

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
                                      `id` int NOT NULL AUTO_INCREMENT,
                                      `username` varchar(50) NOT NULL COMMENT '用户名',
                                      `password` varchar(100) NOT NULL COMMENT '密码，建议存储加密后的密码',
                                      `role` varchar(20) NOT NULL DEFAULT 'user' COMMENT '角色：admin/user',
                                      `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
                                      `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入默认管理员
INSERT IGNORE INTO `user` (`username`, `password`, `role`) VALUES ('admin', '123456', 'admin');

-- 电影表
CREATE TABLE IF NOT EXISTS `movie` (
                                       `id` int NOT NULL AUTO_INCREMENT,
                                       `name` varchar(100) NOT NULL COMMENT '电影名称',
                                       `duration` int NOT NULL COMMENT '时长，单位分钟',
                                       `price` decimal(10,2) NOT NULL COMMENT '默认票价',
                                       `type` varchar(50) DEFAULT NULL COMMENT '类型',
                                       `description` varchar(500) DEFAULT NULL COMMENT '简介',
                                       `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1上映，0下架',
                                       `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
                                       `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       PRIMARY KEY (`id`),
                                       KEY `idx_movie_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 场次表
CREATE TABLE IF NOT EXISTS `showtime` (
                                          `id` int NOT NULL AUTO_INCREMENT,
                                          `movie_id` int NOT NULL,
                                          `hall` varchar(20) NOT NULL COMMENT '影厅',
                                          `show_time` datetime NOT NULL COMMENT '放映时间',
                                          `price` decimal(10,2) NOT NULL COMMENT '本场次票价',
                                          `total_seats` int NOT NULL DEFAULT 100 COMMENT '总座位数',
                                          `available_seats` int NOT NULL DEFAULT 100 COMMENT '剩余座位数',
                                          `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1正常，0取消',
                                          `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
                                          `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                          PRIMARY KEY (`id`),
                                          KEY `idx_movie_id` (`movie_id`),
                                          KEY `idx_show_time` (`show_time`),
                                          KEY `idx_hall_time` (`hall`, `show_time`),
                                          CONSTRAINT `fk_showtime_movie`
                                              FOREIGN KEY (`movie_id`) REFERENCES `movie` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 场次座位表
CREATE TABLE IF NOT EXISTS `showtime_seat` (
                                               `id` int NOT NULL AUTO_INCREMENT,
                                               `showtime_id` int NOT NULL COMMENT '场次ID',
                                               `seat` varchar(20) NOT NULL COMMENT '座位号',
                                               `status` tinyint NOT NULL DEFAULT 0 COMMENT '座位状态：0可售，1锁定，2已售',
                                               `order_id` int DEFAULT NULL COMMENT '关联订单ID',
                                               `lock_time` datetime DEFAULT NULL COMMENT '锁定时间',
                                               `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
                                               `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                               `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                               PRIMARY KEY (`id`),
                                               UNIQUE KEY `uk_showtime_seat` (`showtime_id`, `seat`),
                                               KEY `idx_showtime_id` (`showtime_id`),
                                               KEY `idx_status` (`status`),
                                               CONSTRAINT `fk_seat_showtime`
                                                   FOREIGN KEY (`showtime_id`) REFERENCES `showtime` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单表
CREATE TABLE IF NOT EXISTS `ticket_order` (
                                              `id` int NOT NULL AUTO_INCREMENT,
                                              `order_no` varchar(50) NOT NULL COMMENT '订单号',
                                              `user_id` int NOT NULL,
                                              `showtime_id` int NOT NULL,
                                              `seat` varchar(20) NOT NULL COMMENT '座位号',
                                              `total_price` decimal(10,2) NOT NULL COMMENT '总价',
                                              `status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态：0待支付，1已支付，2已取消，3已退款',
                                              `expire_time` datetime DEFAULT NULL COMMENT '支付过期时间',
                                              `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
                                              `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
                                              `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                              `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                              PRIMARY KEY (`id`),
                                              UNIQUE KEY `uk_order_no` (`order_no`),
                                              UNIQUE KEY `uk_showtime_seat` (`showtime_id`, `seat`),
                                              KEY `idx_user_id` (`user_id`),
                                              KEY `idx_showtime_id` (`showtime_id`),
                                              KEY `idx_status` (`status`),
                                              CONSTRAINT `fk_order_user`
                                                  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
                                              CONSTRAINT `fk_order_showtime`
                                                  FOREIGN KEY (`showtime_id`) REFERENCES `showtime` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

