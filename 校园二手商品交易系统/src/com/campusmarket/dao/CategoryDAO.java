package com.campusmarket.dao;

import com.campusmarket.config.DatabaseConfig;
import com.campusmarket.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品分类数据访问对象
 */
public class CategoryDAO {

    /**
     * 查询全部分类
     */
    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM tb_category ORDER BY category_id";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapCategory(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 根据ID查找分类
     */
    public Category findById(int categoryId) {
        String sql = "SELECT * FROM tb_category WHERE category_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCategory(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 新增分类
     */
    public int insert(Category category) {
        String sql = "INSERT INTO tb_category (name, description) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 更新分类
     */
    public boolean update(Category category) {
        String sql = "UPDATE tb_category SET name=?, description=? WHERE category_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setInt(3, category.getCategoryId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除分类
     */
    public boolean delete(int categoryId) {
        String sql = "DELETE FROM tb_category WHERE category_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Category mapCategory(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setCategoryId(rs.getInt("category_id"));
        c.setName(rs.getString("name"));
        c.setDescription(rs.getString("description"));
        return c;
    }
}
