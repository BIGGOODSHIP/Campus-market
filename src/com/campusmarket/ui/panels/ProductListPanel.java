package com.campusmarket.ui.panels;

import com.campusmarket.dao.CategoryDAO;
import com.campusmarket.dao.OrderDAO;
import com.campusmarket.dao.ProductDAO;
import com.campusmarket.model.Category;
import com.campusmarket.model.Product;
import com.campusmarket.util.SessionManager;

import java.util.Map;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 商品列表面板（学生端）
 * 浏览已上架商品，支持搜索、查看详情和立即购买
 */
public class ProductListPanel extends JPanel {

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private JTextField keywordField;
    private JComboBox<Object> categoryCombo;
    private JTextField minPriceField;
    private JTextField maxPriceField;
    private JTable productTable;
    private DefaultTableModel tableModel;

    private List<Product> currentProducts;

    public ProductListPanel() {
        setLayout(new BorderLayout());
        initSearchPanel();
        initTable();
        initBottomPanel();
        refreshData();
    }

    /**
     * 初始化顶部搜索面板
     */
    private void initSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        searchPanel.setBorder(BorderFactory.createTitledBorder("商品检索"));

        searchPanel.add(new JLabel("关键词："));
        keywordField = new JTextField(12);
        keywordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        searchPanel.add(keywordField);

        searchPanel.add(new JLabel("分类："));
        categoryCombo = new JComboBox<>();
        categoryCombo.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        loadCategoryCombo();
        searchPanel.add(categoryCombo);

        searchPanel.add(new JLabel("最低价："));
        minPriceField = new JTextField(6);
        minPriceField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        searchPanel.add(minPriceField);

        searchPanel.add(new JLabel("最高价："));
        maxPriceField = new JTextField(6);
        maxPriceField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        searchPanel.add(maxPriceField);

        JButton searchButton = new JButton("搜索");
        searchButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        searchButton.addActionListener(e -> searchProducts());
        searchPanel.add(searchButton);

        JButton resetButton = new JButton("重置");
        resetButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        resetButton.addActionListener(e -> resetSearch());
        searchPanel.add(resetButton);

        add(searchPanel, BorderLayout.NORTH);
    }

    /**
     * 加载分类下拉框
     */
    private void loadCategoryCombo() {
        categoryCombo.removeAllItems();
        Category allCat = new Category(); allCat.setCategoryId(0); allCat.setName("全部");
        categoryCombo.addItem(allCat);
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
     * 初始化中央表格
     */
    private void initTable() {
        String[] columns = {"ID", "名称", "分类", "价格", "原价", "卖家", "发布时间"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        productTable = new JTable(tableModel);
        productTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        productTable.setRowHeight(24);
        productTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));

        // 居中渲染
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < columns.length; i++) {
            productTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // 列宽
        productTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        productTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        productTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        productTable.getColumnModel().getColumn(6).setPreferredWidth(150);

        // 双击查看详情
        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = productTable.rowAtPoint(e.getPoint());
                if (row >= 0 && e.getClickCount() == 2) {
                    viewProductDetail(row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * 初始化底部按钮面板
     */
    private void initBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JButton detailButton = new JButton("查看详情");
        detailButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        detailButton.addActionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "请先选择一件商品！", "提示",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            viewProductDetail(row);
        });
        bottomPanel.add(detailButton);

        JButton buyButton = new JButton("立即购买");
        buyButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        buyButton.addActionListener(e -> buyProduct());
        bottomPanel.add(buyButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * 刷新数据（默认只显示已上架商品）
     */
    public void refreshData() {
        try {
            String keyword = keywordField != null ? keywordField.getText().trim() : "";
            Integer categoryId = null;
            Object catObj = categoryCombo != null ? categoryCombo.getSelectedItem() : null;
            if (catObj instanceof Category) {
                Category c = (Category) catObj;
                if (c.getCategoryId() > 0) {
                    categoryId = c.getCategoryId();
                }
            }

            BigDecimal minPrice = null;
            BigDecimal maxPrice = null;
            if (minPriceField != null && !minPriceField.getText().trim().isEmpty()) {
                try {
                    minPrice = new BigDecimal(minPriceField.getText().trim());
                } catch (NumberFormatException ignored) {}
            }
            if (maxPriceField != null && !maxPriceField.getText().trim().isEmpty()) {
                try {
                    maxPrice = new BigDecimal(maxPriceField.getText().trim());
                } catch (NumberFormatException ignored) {}
            }

            if (!keyword.isEmpty() || categoryId != null || minPrice != null || maxPrice != null) {
                currentProducts = productDAO.search(keyword, categoryId,
                        minPrice, maxPrice, 1);
            } else {
                currentProducts = productDAO.findOnSale();
            }

            updateTableData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载商品数据失败：" + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 搜索商品
     */
    private void searchProducts() {
        try {
            String keyword = keywordField.getText().trim();
            Integer categoryId = null;
            Object catObj = categoryCombo.getSelectedItem();
            if (catObj instanceof Category) {
                Category c = (Category) catObj;
                if (c.getCategoryId() > 0) {
                    categoryId = c.getCategoryId();
                }
            }

            BigDecimal minPrice = null;
            BigDecimal maxPrice = null;
            if (!minPriceField.getText().trim().isEmpty()) {
                try { minPrice = new BigDecimal(minPriceField.getText().trim()); }
                catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "最低价格式不正确！", "输入错误",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            if (!maxPriceField.getText().trim().isEmpty()) {
                try { maxPrice = new BigDecimal(maxPriceField.getText().trim()); }
                catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "最高价格式不正确！", "输入错误",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            currentProducts = productDAO.search(keyword, categoryId,
                    minPrice, maxPrice, 1);
            updateTableData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "搜索失败：" + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 重置搜索条件
     */
    private void resetSearch() {
        keywordField.setText("");
        categoryCombo.setSelectedIndex(0);
        minPriceField.setText("");
        maxPriceField.setText("");
        refreshData();
    }

    /**
     * 更新表格数据
     */
    private void updateTableData() {
        tableModel.setRowCount(0);
        if (currentProducts == null) return;

        for (Product p : currentProducts) {
            Object[] row = {
                    p.getProductId(),
                    p.getName(),
                    p.getCategoryName(),
                    p.getPrice() != null ? p.getPrice().toString() : "",
                    p.getOriginalPrice() != null ? p.getOriginalPrice().toString() : "",
                    p.getSellerName(),
                    p.getCreateTime() != null ? dateFormat.format(p.getCreateTime()) : ""
            };
            tableModel.addRow(row);
        }
    }

    /**
     * 查看商品详情
     */
    private void viewProductDetail(int row) {
        if (currentProducts == null || row >= currentProducts.size()) return;
        Product product = currentProducts.get(row);

        StringBuilder sb = new StringBuilder();
        sb.append("商品名称：").append(product.getName()).append("\n");
        sb.append("商品分类：").append(product.getCategoryName()).append("\n");
        sb.append("售价：¥").append(product.getPrice()).append("\n");
        sb.append("原价：¥").append(product.getOriginalPrice()).append("\n");
        sb.append("卖家：").append(product.getSellerName()).append("\n");
        sb.append("描述：").append(product.getDescription()).append("\n");
        sb.append("发布时间：").append(
                product.getCreateTime() != null ? dateFormat.format(product.getCreateTime()) : "");

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        textArea.setEditable(false);
        textArea.setOpaque(false);

        JOptionPane.showMessageDialog(this, textArea, "商品详情",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 立即购买 — 弹出购买确认对话框并创建订单
     */
    private void buyProduct() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一件商品！", "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentProducts == null || row >= currentProducts.size()) return;

        Product product = currentProducts.get(row);

        // 检查是否登录
        if (!SessionManager.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "请先登录！", "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 不能购买自己的商品
        if (product.getSellerId() == SessionManager.getCurrentUserId()) {
            JOptionPane.showMessageDialog(this, "不能购买自己的商品！", "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 购买确认对话框
        StringBuilder msg = new StringBuilder();
        msg.append("确定要购买以下商品吗？\n\n");
        msg.append("商品名称：").append(product.getName()).append("\n");
        msg.append("价格：¥").append(product.getPrice()).append("\n");
        msg.append("卖家：").append(product.getSellerName());

        int confirm = JOptionPane.showConfirmDialog(this, msg.toString(),
                "购买确认", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        // 创建订单
        try {
            Map<String, Object> result = orderDAO.createOrder(
                    SessionManager.getCurrentUserId(),
                    product.getProductId(),
                    product.getPrice());

            boolean success = (boolean) result.get("success");
            String message = (String) result.get("message");

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "购买成功！请在我的订单中查看订单详情。",
                        "提示", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, message,
                        "购买失败", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "购买失败：" + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
