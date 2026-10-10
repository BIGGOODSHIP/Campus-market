package com.campusmarket.model;

/**
 * 商品分类实体类
 */
public class Category {
    private int categoryId;
    private String name;
    private String description;

    public Category() {}

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return name;
    }
}
