package com.campusmarket.config;

import java.sql.*;

/**
 * 数据库连接配置与管理类
 * 提供数据库连接获取、关闭等基础操作
 */
public class DatabaseConfig {

    private static final String URL = "jdbc:mysql://localhost:3306/campus_market?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    // 单例连接（简化实现，实际生产环境应使用连接池）
    private static Connection connection;

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC驱动未找到，请将mysql-connector-java.jar放入lib目录", e);
            }
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    /**
     * 关闭连接
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭 ResultSet, Statement, Connection
     */
    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

}
