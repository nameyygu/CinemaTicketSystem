package com.electro.sales.util;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DBUtil {

    private static String url;
    private static String user;
    private static String password;

    private static boolean initSuccess = false;
    private static String errorMsg = "";

    static {
        initConfigAndDatabase();
    }

    private static void initConfigAndDatabase() {
        try {
            // 1. 加载 MySQL 驱动
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 2. 读取配置文件
            loadProperties();

            System.out.println("数据库配置：");
            System.out.println("URL = " + url);
            System.out.println("USER = " + user);

            if (url == null || url.trim().isEmpty()) {
                throw new RuntimeException("db.properties 中 url 不能为空");
            }

            if (user == null || user.trim().isEmpty()) {
                throw new RuntimeException("db.properties 中 user 不能为空");
            }

            // 3. 创建数据库
            boolean success = createDatabaseIfNotExists();

            if (success) {
                initSuccess = true;
                System.out.println("数据库初始化连接准备完成");
            }

        } catch (Exception e) {
            initSuccess = false;
            errorMsg = e.getMessage();

            System.err.println("数据库初始化失败：" + errorMsg);
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    null,
                    "数据库初始化失败：\n" + errorMsg,
                    "数据库错误",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static void loadProperties() throws Exception {
        Properties props = new Properties();
        InputStream in = null;

        // 1. 优先从项目根目录加载 db.properties
        File file = new File("db.properties");

        if (file.exists()) {
            in = new FileInputStream(file);
            System.out.println("从项目根目录加载配置：" + file.getAbsolutePath());
        } else {
            // 2. 再从 classpath 加载
            in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties");

            if (in != null) {
                System.out.println("从 classpath 加载 db.properties");
            }
        }

        if (in == null) {
            throw new RuntimeException("未找到 db.properties 配置文件");
        }

        try {
            props.load(in);

            url = props.getProperty("url");
            user = props.getProperty("user");
            password = props.getProperty("password");

        } finally {
            in.close();
        }
    }

    private static boolean createDatabaseIfNotExists() {
        String dbName = getDatabaseNameFromUrl(url);
        String baseUrl = getBaseUrl(url);

        System.out.println("数据库名：" + dbName);
        System.out.println("MySQL连接地址：" + baseUrl);

        try (Connection conn = DriverManager.getConnection(baseUrl, user, password);
             Statement stmt = conn.createStatement()) {

            String sql = "CREATE DATABASE IF NOT EXISTS `" + dbName + "` " +
                    "DEFAULT CHARACTER SET utf8mb4 " +
                    "COLLATE utf8mb4_general_ci";

            stmt.executeUpdate(sql);

            System.out.println("数据库 `" + dbName + "` 已就绪");

            return true;

        } catch (SQLException e) {
            errorMsg = e.getMessage();

            System.err.println("创建数据库失败：" + errorMsg);

            JOptionPane.showMessageDialog(
                    null,
                    "无法连接 MySQL 服务器！\n\n" +
                            "请检查：\n" +
                            "1. MySQL 服务是否启动\n" +
                            "2. 用户名和密码是否正确\n" +
                            "3. 端口是否正确\n\n" +
                            "当前连接：" + baseUrl + "\n" +
                            "用户：" + user + "\n\n" +
                            "错误：" + errorMsg,
                    "数据库连接失败",
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }
    }

    private static String getDatabaseNameFromUrl(String jdbcUrl) {
        try {
            String temp = jdbcUrl.substring(jdbcUrl.indexOf("://") + 3);

            if (temp.contains("/")) {
                String dbPart = temp.substring(temp.indexOf("/") + 1);

                if (dbPart.contains("?")) {
                    return dbPart.substring(0, dbPart.indexOf("?"));
                }

                return dbPart;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "electro";
    }

    private static String getBaseUrl(String jdbcUrl) {
        try {
            String temp = jdbcUrl.substring(jdbcUrl.indexOf("://") + 3);

            String hostPort;

            if (temp.contains("/")) {
                hostPort = temp.substring(0, temp.indexOf("/"));
            } else {
                hostPort = temp;
            }

            return "jdbc:mysql://" + hostPort +
                    "?useSSL=false" +
                    "&serverTimezone=Asia/Shanghai" +
                    "&allowPublicKeyRetrieval=true" +
                    "&characterEncoding=utf8";

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "jdbc:mysql://localhost:3306" +
                "?useSSL=false" +
                "&serverTimezone=Asia/Shanghai" +
                "&allowPublicKeyRetrieval=true" +
                "&characterEncoding=utf8";
    }

    public static Connection getConnection() {
        if (!initSuccess) {
            System.err.println("数据库未初始化成功，无法获取连接");
            System.err.println("错误原因：" + errorMsg);
            return null;
        }

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.err.println("获取数据库连接失败：" + e.getMessage());
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    null,
                    "获取数据库连接失败：\n" + e.getMessage(),
                    "数据库错误",
                    JOptionPane.ERROR_MESSAGE
            );

            return null;
        }
    }
}