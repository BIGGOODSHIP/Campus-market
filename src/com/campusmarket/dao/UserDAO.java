package com.campusmarket.dao;

import com.campusmarket.config.DatabaseConfig;
import com.campusmarket.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据访问对象
 * 所有SQL操作使用PreparedStatement防止SQL注入
 */
public class UserDAO {

    /**
     * 根据用户名和密码查找用户（登录认证）
     */
    public User findByUsernameAndPassword(String username, String password) {
        String sql = "SELECT * FROM tb_user WHERE username = ? AND password = ? AND status = 0";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据ID查找用户
     */
    public User findById(int userId) {
        String sql = "SELECT * FROM tb_user WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据用户名查找用户（检查重复）
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM tb_user WHERE username = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询所有用户
     */
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM tb_user ORDER BY role, user_id";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 条件查询用户（支持分页）
     */
    public List<User> search(String keyword, Integer role, Integer status) {
        List<User> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM tb_user WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (username LIKE ? OR real_name LIKE ? OR phone LIKE ?)");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (role != null && role != -1) {
            sql.append(" AND role = ?");
            params.add(role);
        }
        if (status != null && status != -1) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY role, user_id");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 新增用户
     */
    public int insert(User user) {
        String sql = "INSERT INTO tb_user (username, password, real_name, phone, role, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRealName());
            ps.setString(4, user.getPhone());
            ps.setInt(5, user.getRole());
            ps.setInt(6, user.getStatus());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 更新用户
     */
    public boolean update(User user) {
        String sql = "UPDATE tb_user SET real_name=?, phone=?, role=?, status=? WHERE user_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getRealName());
            ps.setString(2, user.getPhone());
            ps.setInt(3, user.getRole());
            ps.setInt(4, user.getStatus());
            ps.setInt(5, user.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新用户状态（启用/禁用）
     */
    public boolean updateStatus(int userId, int status) {
        String sql = "UPDATE tb_user SET status=? WHERE user_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 修改密码
     */
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE tb_user SET password=? WHERE user_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除用户
     */
    public boolean delete(int userId) {
        String sql = "DELETE FROM tb_user WHERE user_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取用户总数
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM tb_user";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setRealName(rs.getString("real_name"));
        u.setPhone(rs.getString("phone"));
        u.setRole(rs.getInt("role"));
        u.setStatus(rs.getInt("status"));
        u.setCreateTime(rs.getTimestamp("create_time"));
        return u;
    }
}
