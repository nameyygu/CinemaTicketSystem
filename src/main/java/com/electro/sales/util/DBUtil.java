package com.electro.sales.util;

import javax.swing.*;
import java.io.*;
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
        try {
            Properties props = new Properties();
            InputStream in = null;

            // 1. 尝试从多个位置加载 db.properties
            File file = new File("db.properties");
            if (file.exists()) {
                in = new FileInputStream(file);
                System.out.println("从文件加载配置: " + file.getAbsolutePath());
            } else {
                // 从类路径加载
                in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties");
                if (in != null) {
                    System.out.println("从类路径加载配置");
                }
            }

            // 2. 加载配置或使用默认值
            if (in != null) {
                props.load(in);
                in.close();
                url = props.getProperty("url");
                user = props.getProperty("user");
                password = props.getProperty("password");
            }

            // 3. 默认值兜底
            if (url == null || url.isEmpty()) {
                url = "jdbc:mysql://localhost:3306/electro?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
            }
            if (user == null || user.isEmpty()) {
                user = "root";
            }
            if (password == null) {
                password = "123456";
            }

            System.out.println("数据库配置 - URL: " + url + ", 用户: " + user);

            // 4. 先创建数据库
            if (initDatabase()) {
                initSuccess = true;
            }

        } catch (Exception e) {
            errorMsg = e.getMessage();
            System.err.println("初始化失败: " + errorMsg);
            e.printStackTrace();
        }
    }

    private static boolean initDatabase() {
        // 构建基础连接URL（不指定数据库）
        String baseUrl = "jdbc:mysql://localhost:3306?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";

        try {
            // 从配置的URL中提取主机和端口
            if (url != null && url.contains("://")) {
                String temp = url.substring(url.indexOf("://") + 3);
                if (temp.contains("/")) {
                    String hostPort = temp.substring(0, temp.indexOf("/"));
                    baseUrl = "jdbc:mysql://" + hostPort + "?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
                }
            }
        } catch (Exception e) {
            // 使用默认的 baseUrl
        }

        // 提取数据库名
        String dbName = "electro";
        try {
            if (url != null && url.contains("/")) {
                String temp = url.substring(url.lastIndexOf("/") + 1);
                if (temp.contains("?")) {
                    dbName = temp.substring(0, temp.indexOf("?"));
                } else {
                    dbName = temp;
                }
            }
        } catch (Exception e) {
            // 使用默认数据库名
        }

        System.out.println("尝试创建数据库: " + dbName);

        try (Connection conn = DriverManager.getConnection(baseUrl, user, password);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbName + "`");
            System.out.println("数据库 " + dbName + " 已就绪");
            return true;

        } catch (SQLException e) {
            errorMsg = e.getMessage();
            System.err.println("创建数据库失败: " + errorMsg);

            // 弹窗提示
            JOptionPane.showMessageDialog(null,
                    "无法连接 MySQL 服务器！\n\n" +
                            "请检查以下内容：\n" +
                            "1. MySQL 服务是否已启动\n" +
                            "2. 用户名和密码是否正确\n" +
                            "3. MySQL 端口是否为 3306\n\n" +
                            "当前配置：\n" +
                            "用户：" + user + "\n" +
                            "地址：" + baseUrl + "\n\n" +
                            "错误详情：" + errorMsg,
                    "数据库连接失败",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public static Connection getConnection() {
        if (!initSuccess) {
            System.err.println("数据库未初始化成功，无法获取连接");
            System.err.println("错误原因: " + errorMsg);
            return null;
        }

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            if (conn == null) {
                System.err.println("获取连接失败：连接为空");
            }
            return conn;
        } catch (SQLException e) {
            System.err.println("获取连接失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}