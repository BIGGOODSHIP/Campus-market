package com.campusmarket.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 商品实体类
 */
public class Product {
    private int productId;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private int categoryId;
    private String categoryName;
    private int sellerId;
    private String sellerName;
    private String sellerPhone;
    private int status;        // 0=待审核, 1=已上架, 2=已售出, 3=已下架, 4=审核不通过
    private String imagePath;
    private Timestamp createTime;

    public Product() {}

    // 用于视图查询的完整构造
    public Product(int productId, String name, String description, BigDecimal price,
                   BigDecimal originalPrice, int categoryId, String categoryName,
                   int sellerId, String sellerName, String sellerPhone,
                   int status, String imagePath, Timestamp createTime) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.originalPrice = originalPrice;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.sellerPhone = sellerPhone;
        this.status = status;
        this.imagePath = imagePath;
        this.createTime = createTime;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getSellerPhone() { return sellerPhone; }
    public void setSellerPhone(String sellerPhone) { this.sellerPhone = sellerPhone; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getStatusName() {
        switch (status) {
            case 0: return "待审核";
            case 1: return "已上架";
            case 2: return "已售出";
            case 3: return "已下架";
            case 4: return "审核不通过";
            default: return "未知";
        }
    }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }
}
