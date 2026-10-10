package com.campusmarket.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 订单实体类
 */
public class Order {
    private int orderId;
    private int buyerId;
    private String buyerName;
    private String buyerPhone;
    private int productId;
    private String productName;
    private int sellerId;
    private String sellerName;
    private String sellerPhone;
    private BigDecimal price;
    private int status;        // 0=待交易, 1=已完成, 2=已取消
    private Timestamp createTime;
    private Timestamp completeTime;

    public Order() {}

    // 用于视图查询的完整构造
    public Order(int orderId, BigDecimal price, int status, Timestamp createTime,
                 Timestamp completeTime, int buyerId, String buyerName, String buyerPhone,
                 int productId, String productName, int sellerId, String sellerName, String sellerPhone) {
        this.orderId = orderId;
        this.price = price;
        this.status = status;
        this.createTime = createTime;
        this.completeTime = completeTime;
        this.buyerId = buyerId;
        this.buyerName = buyerName;
        this.buyerPhone = buyerPhone;
        this.productId = productId;
        this.productName = productName;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.sellerPhone = sellerPhone;
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getBuyerId() { return buyerId; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getBuyerPhone() { return buyerPhone; }
    public void setBuyerPhone(String buyerPhone) { this.buyerPhone = buyerPhone; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getSellerPhone() { return sellerPhone; }
    public void setSellerPhone(String sellerPhone) { this.sellerPhone = sellerPhone; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getStatusName() {
        switch (status) {
            case 0: return "待交易";
            case 1: return "已完成";
            case 2: return "已取消";
            default: return "未知";
        }
    }

    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }

    public Timestamp getCompleteTime() { return completeTime; }
    public void setCompleteTime(Timestamp completeTime) { this.completeTime = completeTime; }
}
