package com.electro.sales.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class InitDB {

    public static void init() {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("数据库连接为空，无法初始化");
                return;
            }

            // 检查 user 表是否已存在（用 SQL 查询更可靠）
            if (isTableExists(conn)) {
                System.out.println("数据库已初始化，跳过");
                return;
            }

            // 查找 install.sql
            File sqlFile = new File("install.sql");
            if (!sqlFile.exists()) {
                System.out.println("未找到 install.sql，使用内置建表语句");
                createTablesWithBuiltInSql(conn);
                return;
            }

            // 从文件读取并执行
            try (Statement stmt = conn.createStatement();
                 BufferedReader br = new BufferedReader(new FileReader(sqlFile))) {

                StringBuilder sql = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    sql.append(line).append("\n");

                    if (line.trim().endsWith(";")) {
                        stmt.execute(sql.toString());
                        sql.setLength(0);
                    }
                }

                System.out.println("数据库初始化完成");
            }

        } catch (Exception e) {
            System.err.println("数据库初始化失败：" + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 用 SQL 查询判断表是否存在
    private static boolean isTableExists(Connection conn) {
        String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = DATABASE() AND table_name = 'user'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            System.err.println("检查表存在失败：" + e.getMessage());
        }
        return false;
    }

    private static void createTablesWithBuiltInSql(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // 用户表
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `user` (" +
                    "`id` int NOT NULL AUTO_INCREMENT, " +
                    "`username` varchar(50) NOT NULL, " +
                    "`password` varchar(50) NOT NULL, " +
                    "`role` varchar(20) NOT NULL DEFAULT 'user', " +
                    "PRIMARY KEY (`id`), " +
                    "UNIQUE KEY `uk_username` (`username`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // 插入管理员
            stmt.executeUpdate("INSERT IGNORE INTO `user` (`username`, `password`, `role`) VALUES ('admin', '123456', 'admin')");

            // 电影表
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `movie` (" +
                    "`id` int NOT NULL AUTO_INCREMENT, " +
                    "`name` varchar(100) NOT NULL, " +
                    "`duration` int NOT NULL, " +
                    "`price` decimal(10,2) NOT NULL, " +
                    "`type` varchar(50) DEFAULT NULL, " +
                    "PRIMARY KEY (`id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // 场次表
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `showtime` (" +
                    "`id` int NOT NULL AUTO_INCREMENT, " +
                    "`movie_id` int NOT NULL, " +
                    "`hall` varchar(20) NOT NULL, " +
                    "`show_time` datetime NOT NULL, " +
                    "PRIMARY KEY (`id`), " +
                    "KEY `movie_id` (`movie_id`), " +
                    "CONSTRAINT `showtime_ibfk_1` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // 订单表
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `ticket_order` (" +
                    "`id` int NOT NULL AUTO_INCREMENT, " +
                    "`user_id` int NOT NULL, " +
                    "`showtime_id` int NOT NULL, " +
                    "`seat` varchar(20) NOT NULL, " +
                    "`total_price` decimal(10,2) NOT NULL, " +
                    "`status` varchar(20) NOT NULL DEFAULT '待支付', " +
                    "`create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "PRIMARY KEY (`id`), " +
                    "KEY `user_id` (`user_id`), " +
                    "KEY `showtime_id` (`showtime_id`), " +
                    "CONSTRAINT `ticket_order_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`), " +
                    "CONSTRAINT `ticket_order_ibfk_2` FOREIGN KEY (`showtime_id`) REFERENCES `showtime` (`id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            System.out.println("数据库初始化完成（内置SQL）");
        } catch (Exception e) {
            System.err.println("建表失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}