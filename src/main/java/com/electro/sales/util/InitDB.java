package com.electro.sales.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

            // 如果 user 表已存在，认为数据库已初始化
            if (isTableExists(conn, "user")) {
                System.out.println("数据库表已存在，跳过 install.sql 初始化");
                return;
            }

            InputStream in = findInstallSql();

            if (in == null) {
                System.err.println("未找到 install.sql，无法初始化数据库表");
                return;
            }

            executeSqlFile(conn, in);

            System.out.println("数据库表初始化完成");

        } catch (Exception e) {
            System.err.println("数据库初始化失败：" + e.getMessage());
            e.printStackTrace();

        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ================= 查找 install.sql =================
    private static InputStream findInstallSql() throws Exception {
        // 1. 优先查找项目根目录
        File file = new File("install.sql");

        if (file.exists()) {
            System.out.println("从项目根目录加载 install.sql：" + file.getAbsolutePath());
            return new FileInputStream(file);
        }

        // 2. 再从 classpath 查找
        InputStream in = InitDB.class.getClassLoader().getResourceAsStream("install.sql");

        if (in != null) {
            System.out.println("从 classpath 加载 install.sql");
            return in;
        }

        return null;
    }

    // ================= 执行 SQL 文件 =================
    private static void executeSqlFile(Connection conn, InputStream in) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
             Statement stmt = conn.createStatement()) {

            StringBuilder sql = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                String trimLine = line.trim();

                // 跳过空行
                if (trimLine.isEmpty()) {
                    continue;
                }

                // 跳过单行注释
                if (trimLine.startsWith("--")) {
                    continue;
                }

                // 跳过 MySQL 注释
                if (trimLine.startsWith("#")) {
                    continue;
                }

                sql.append(line).append("\n");

                // 遇到分号执行
                if (trimLine.endsWith(";")) {
                    String realSql = sql.toString();

                    // 去掉最后的分号
                    realSql = realSql.substring(0, realSql.lastIndexOf(";")).trim();

                    if (!realSql.isEmpty()) {
                        System.out.println("执行SQL：\n" + realSql);
                        stmt.execute(realSql);
                    }

                    sql.setLength(0);
                }
            }

            // 防止最后一条 SQL 没有分号
            String lastSql = sql.toString().trim();
            if (!lastSql.isEmpty()) {
                System.out.println("执行SQL：\n" + lastSql);
                stmt.execute(lastSql);
            }
        }
    }

    // ================= 判断表是否存在 =================
    private static boolean isTableExists(Connection conn, String tableName) {
        String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = DATABASE() AND table_name = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (Exception e) {
            System.err.println("检查表是否存在失败：" + e.getMessage());
        }

        return false;
    }
}