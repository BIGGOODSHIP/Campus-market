package com.campusmarket.dao;

import com.campusmarket.config.DatabaseConfig;
import com.campusmarket.model.Product;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品数据访问对象
 */
public class ProductDAO {

    /**
     * 根据ID查找商品（使用视图获取完整信息）
     */
    public Product findById(int productId) {
        String sql = "SELECT * FROM v_product_list WHERE product_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapProduct(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询所有已上架商品
     */
    public List<Product> findOnSale() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM v_product_list WHERE status = 1 ORDER BY create_time DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 根据卖家ID查询商品
     */
    public List<Product> findBySellerId(int sellerId) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM v_product_list WHERE seller_id = ? ORDER BY create_time DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 查询全部商品（管理员视图）
     */
    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM v_product_list ORDER BY create_time DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 多条件搜索商品
     */
    public List<Product> search(String keyword, Integer categoryId,
                                 BigDecimal minPrice, BigDecimal maxPrice, Integer status) {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM v_product_list WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (name LIKE ? OR description LIKE ?)");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
        }
        if (categoryId != null && categoryId > 0) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }
        if (minPrice != null) {
            sql.append(" AND price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND price <= ?");
            params.add(maxPrice);
        }
        if (status != null && status != -1) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY create_time DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 发布商品
     */
    public int insert(Product product) {
        String sql = "INSERT INTO tb_product (name, description, price, original_price, category_id, seller_id, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setBigDecimal(4, product.getOriginalPrice());
            if (product.getCategoryId() > 0) {
                ps.setInt(5, product.getCategoryId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setInt(6, product.getSellerId());
            ps.setInt(7, product.getStatus());
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
     * 更新商品信息
     */
    public boolean update(Product product) {
        String sql = "UPDATE tb_product SET name=?, description=?, price=?, original_price=?, category_id=? WHERE product_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setBigDecimal(4, product.getOriginalPrice());
            if (product.getCategoryId() > 0) {
                ps.setInt(5, product.getCategoryId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setInt(6, product.getProductId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新商品状态（审核/上下架等）
     */
    public boolean updateStatus(int productId, int status) {
        String sql = "UPDATE tb_product SET status=? WHERE product_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除商品
     */
    public boolean delete(int productId) {
        String sql = "DELETE FROM tb_product WHERE product_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 统计商品数量
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM tb_product";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setOriginalPrice(rs.getBigDecimal("original_price"));
        p.setCategoryId(rs.getInt("category_id"));
        try { p.setCategoryName(rs.getString("category_name")); } catch (SQLException e) { p.setCategoryName(""); }
        p.setSellerId(rs.getInt("seller_id"));
        try { p.setSellerName(rs.getString("seller_name")); } catch (SQLException e) { p.setSellerName(""); }
        try { p.setSellerPhone(rs.getString("seller_phone")); } catch (SQLException e) { p.setSellerPhone(""); }
        p.setStatus(rs.getInt("status"));
        try { p.setImagePath(rs.getString("image_path")); } catch (SQLException e) { p.setImagePath(""); }
        p.setCreateTime(rs.getTimestamp("create_time"));
        return p;
    }
}
