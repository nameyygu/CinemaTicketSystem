-- 影院售票系统

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
                                      `id` int NOT NULL AUTO_INCREMENT,
                                      `username` varchar(50) NOT NULL,
                                      `password` varchar(50) NOT NULL,
                                      `role` varchar(20) NOT NULL DEFAULT 'user',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入默认管理员
INSERT IGNORE INTO `user` (`username`, `password`, `role`) VALUES ('admin', '123456', 'admin');

-- 电影表
CREATE TABLE IF NOT EXISTS `movie` (
                                       `id` int NOT NULL AUTO_INCREMENT,
                                       `name` varchar(100) NOT NULL COMMENT '电影名称',
                                       `duration` int NOT NULL COMMENT '时长(分钟)',
                                       `price` decimal(10,2) NOT NULL COMMENT '票价',
                                       `type` varchar(50) DEFAULT NULL COMMENT '类型',
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 场次表
CREATE TABLE IF NOT EXISTS `showtime` (
                                          `id` int NOT NULL AUTO_INCREMENT,
                                          `movie_id` int NOT NULL,
                                          `hall` varchar(20) NOT NULL COMMENT '影厅',
                                          `show_time` datetime NOT NULL COMMENT '放映时间',
                                          PRIMARY KEY (`id`),
                                          KEY `movie_id` (`movie_id`),
                                          CONSTRAINT `showtime_ibfk_1` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单表
CREATE TABLE IF NOT EXISTS `ticket_order` (
                                              `id` int NOT NULL AUTO_INCREMENT,
                                              `user_id` int NOT NULL,
                                              `showtime_id` int NOT NULL,
                                              `seat` varchar(20) NOT NULL COMMENT '座位号',
                                              `total_price` decimal(10,2) NOT NULL COMMENT '总价',
                                              `status` varchar(20) NOT NULL DEFAULT '待支付' COMMENT '订单状态',
                                              `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                              PRIMARY KEY (`id`),
                                              KEY `user_id` (`user_id`),
                                              KEY `showtime_id` (`showtime_id`),
                                              CONSTRAINT `ticket_order_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
                                              CONSTRAINT `ticket_order_ibfk_2` FOREIGN KEY (`showtime_id`) REFERENCES `showtime` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;