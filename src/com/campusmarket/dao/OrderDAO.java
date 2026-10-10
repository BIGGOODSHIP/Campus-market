package com.campusmarket.dao;

import com.campusmarket.config.DatabaseConfig;
import com.campusmarket.model.Order;
import com.campusmarket.model.Product;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单数据访问对象
 * 包含事务控制的核心操作
 */
public class OrderDAO {

    /**
     * 根据ID查找订单
     */
    public Order findById(int orderId) {
        String sql = "SELECT * FROM v_order_detail WHERE order_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapOrder(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询全部订单
     */
    public List<Order> findAll() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM v_order_detail ORDER BY order_time DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 根据买家ID查询订单
     */
    public List<Order> findByBuyerId(int buyerId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM v_order_detail WHERE buyer_id = ? ORDER BY order_time DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, buyerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrder(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 根据卖家ID查询订单（卖家的商品被购买）
     */
    public List<Order> findBySellerId(int sellerId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM v_order_detail WHERE seller_id = ? ORDER BY order_time DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrder(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 创建订单（事务控制：检查库存→创建订单→更新商品状态）
     * 调用方需传入同一个 connection 以保证事务
     */
    public int insert(Connection conn, int buyerId, int productId, BigDecimal price) throws SQLException {
        String sql = "INSERT INTO tb_order (buyer_id, product_id, price, status) VALUES (?, ?, ?, 0)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, buyerId);
            ps.setInt(2, productId);
            ps.setBigDecimal(3, price);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    /**
     * 更新订单状态
     */
    public boolean updateStatus(int orderId, int status) {
        String sql;
        if (status == 1 || status == 2) {
            sql = "UPDATE tb_order SET status=?, complete_time=NOW() WHERE order_id=?";
        } else {
            sql = "UPDATE tb_order SET status=? WHERE order_id=?";
        }
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除订单
     */
    public boolean delete(int orderId) {
        String sql = "DELETE FROM tb_order WHERE order_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 按状态统计订单
     */
    public List<Order> search(Integer status, Integer buyerId, Integer sellerId) {
        List<Order> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM v_order_detail WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (status != null && status != -1) {
            sql.append(" AND order_status = ?");
            params.add(status);
        }
        if (buyerId != null && buyerId > 0) {
            sql.append(" AND buyer_id = ?");
            params.add(buyerId);
        }
        if (sellerId != null && sellerId > 0) {
            sql.append(" AND seller_id = ?");
            params.add(sellerId);
        }
        sql.append(" ORDER BY order_time DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrder(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 创建订单（事务控制：校验商品 → 创建订单）
     * @return Map: {success: boolean, message: String, orderId: int}
     */
    public Map<String, Object> createOrder(int buyerId, int productId, BigDecimal price) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("orderId", -1);

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // 校验商品状态
            ProductDAO productDAO = new ProductDAO();
            Product product = productDAO.findById(productId);
            if (product == null) {
                result.put("message", "商品不存在");
                conn.rollback();
                return result;
            }
            if (product.getStatus() != 1) {
                result.put("message", "商品不是已上架状态，无法购买");
                conn.rollback();
                return result;
            }
            if (product.getSellerId() == buyerId) {
                result.put("message", "不能购买自己的商品");
                conn.rollback();
                return result;
            }

            // 创建订单
            int orderId = insert(conn, buyerId, productId, price);
            if (orderId <= 0) {
                result.put("message", "订单创建失败");
                conn.rollback();
                return result;
            }

            conn.commit();
            result.put("success", true);
            result.put("message", "下单成功");
            result.put("orderId", orderId);
        } catch (SQLException e) {
            e.printStackTrace();
            result.put("message", "下单失败: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
        return result;
    }

    /**
     * 获取订单总数
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM tb_order";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setPrice(rs.getBigDecimal("order_price"));
        o.setStatus(rs.getInt("order_status"));
        o.setCreateTime(rs.getTimestamp("order_time"));
        o.setCompleteTime(rs.getTimestamp("complete_time"));
        o.setBuyerId(rs.getInt("buyer_id"));
        try { o.setBuyerName(rs.getString("buyer_name")); } catch (SQLException e) { o.setBuyerName(""); }
        try { o.setBuyerPhone(rs.getString("buyer_phone")); } catch (SQLException e) { o.setBuyerPhone(""); }
        o.setProductId(rs.getInt("product_id"));
        try { o.setProductName(rs.getString("product_name")); } catch (SQLException e) { o.setProductName(""); }
        o.setSellerId(rs.getInt("seller_id"));
        try { o.setSellerName(rs.getString("seller_name")); } catch (SQLException e) { o.setSellerName(""); }
        try { o.setSellerPhone(rs.getString("seller_phone")); } catch (SQLException e) { o.setSellerPhone(""); }
        return o;
    }
}
