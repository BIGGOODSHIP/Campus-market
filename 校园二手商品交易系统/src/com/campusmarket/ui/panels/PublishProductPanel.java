package com.campusmarket.ui.panels;

import com.campusmarket.dao.CategoryDAO;
import com.campusmarket.dao.ProductDAO;
import com.campusmarket.model.Category;
import com.campusmarket.model.Product;
import com.campusmarket.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * 发布商品面板（学生端）
 * 学生可在此发布二手商品信息
 */
public class PublishProductPanel extends JPanel {

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private JTextField nameField;
    private JComboBox<Category> categoryCombo;
    private JTextField priceField;
    private JTextField originalPriceField;
    private JTextArea descriptionArea;

    public PublishProductPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));
        initComponents();
    }

    /**
     * 初始化表单组件
     */
    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("微软雅黑", Font.PLAIN, 15);
        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 14);

        // ---- 标题 ----
        JLabel titleLabel = new JLabel("发布二手商品", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 22));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 25, 10);
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 10);

        // ---- 商品名称 ----
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel nameLabel = new JLabel("商品名称：");
        nameLabel.setFont(labelFont);
        add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        nameField = new JTextField(20);
        nameField.setFont(fieldFont);
        add(nameField, gbc);

        // ---- 商品分类 ----
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel catLabel = new JLabel("商品分类：");
        catLabel.setFont(labelFont);
        add(catLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        categoryCombo = new JComboBox<>();
        categoryCombo.setFont(fieldFont);
        loadCategories();
        add(categoryCombo, gbc);

        // ---- 售价 ----
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel priceLabel = new JLabel("售  价：");
        priceLabel.setFont(labelFont);
        add(priceLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        priceField = new JTextField(20);
        priceField.setFont(fieldFont);
        add(priceField, gbc);

        // ---- 原价 ----
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel origPriceLabel = new JLabel("原  价：");
        origPriceLabel.setFont(labelFont);
        add(origPriceLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        originalPriceField = new JTextField(20);
        originalPriceField.setFont(fieldFont);
        add(originalPriceField, gbc);

        // ---- 商品描述 ----
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        JLabel descLabel = new JLabel("商品描述：");
        descLabel.setFont(labelFont);
        add(descLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setFont(fieldFont);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setPreferredSize(new Dimension(280, 80));
        add(descScroll, gbc);

        // ---- 按钮面板 ----
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(25, 10, 10, 10);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));

        JButton publishButton = new JButton("发  布");
        publishButton.setFont(new Font("微软雅黑", Font.BOLD, 15));
        publishButton.setPreferredSize(new Dimension(110, 36));
        publishButton.addActionListener(e -> publishProduct());
        buttonPanel.add(publishButton);

        JButton resetButton = new JButton("重  置");
        resetButton.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        resetButton.setPreferredSize(new Dimension(110, 36));
        resetButton.addActionListener(e -> clearForm());
        buttonPanel.add(resetButton);

        add(buttonPanel, gbc);
    }

    /**
     * 加载分类下拉框数据
     */
    private void loadCategories() {
        try {
            List<Category> categories = categoryDAO.findAll();
            for (Category cat : categories) {
                categoryCombo.addItem(cat);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载分类失败：" + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 发布商品
     */
    private void publishProduct() {
        // 获取表单数据
        String name = nameField.getText().trim();
        String priceText = priceField.getText().trim();
        String originalPriceText = originalPriceField.getText().trim();
        String description = descriptionArea.getText().trim();
        Category selectedCategory = (Category) categoryCombo.getSelectedItem();

        // 验证商品名称
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入商品名称！", "输入错误",
                    JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        // 验证分类
        if (selectedCategory == null) {
            JOptionPane.showMessageDialog(this, "请选择商品分类！", "输入错误",
                    JOptionPane.WARNING_MESSAGE);
            categoryCombo.requestFocus();
            return;
        }

        // 验证售价
        BigDecimal price;
        try {
            price = new BigDecimal(priceText);
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "售价必须大于0！", "输入错误",
                        JOptionPane.WARNING_MESSAGE);
                priceField.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的售价！", "输入错误",
                    JOptionPane.WARNING_MESSAGE);
            priceField.requestFocus();
            return;
        }

        // 验证原价
        BigDecimal originalPrice = null;
        if (!originalPriceText.isEmpty()) {
            try {
                originalPrice = new BigDecimal(originalPriceText);
                if (originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(this, "原价必须大于0！", "输入错误",
                            JOptionPane.WARNING_MESSAGE);
                    originalPriceField.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "请输入有效的原价！", "输入错误",
                        JOptionPane.WARNING_MESSAGE);
                originalPriceField.requestFocus();
                return;
            }
        }

        // 检查登录状态
        if (!SessionManager.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "请先登录！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int sellerId = SessionManager.getCurrentUserId();
        if (sellerId == 0) {
            JOptionPane.showMessageDialog(this, "无法获取用户信息，请重新登录！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 发布商品
        try {
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setOriginalPrice(originalPrice);
            product.setCategoryId(selectedCategory.getCategoryId());
            product.setSellerId(sellerId);
            product.setStatus(0);

            int id = productDAO.insert(product);
            if (id > 0) {
                JOptionPane.showMessageDialog(this,
                        "商品发布成功！\n商品已提交审核，审核通过后将自动上架。",
                        "发布成功", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "发布失败", "发布失败",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "发布失败：" + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 清空表单
     */
    private void clearForm() {
        nameField.setText("");
        if (categoryCombo.getItemCount() > 0) {
            categoryCombo.setSelectedIndex(0);
        }
        priceField.setText("");
        originalPriceField.setText("");
        descriptionArea.setText("");
        nameField.requestFocus();
    }
}
