package com.campusmarket.ui.panels;

import com.campusmarket.dao.CategoryDAO;
import com.campusmarket.dao.ProductDAO;
import com.campusmarket.model.Category;
import com.campusmarket.model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 商品管理面板（管理员端）
 * 提供商品搜索、审核、下架、删除等功能
 */
public class ProductManagePanel extends JPanel {

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private JTextField keywordField;
    private JComboBox<Object> categoryCombo;
    private JComboBox<String> statusCombo;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    private List<Product> currentProducts;

    // 状态映射常量
    private static final String[] STATUS_ITEMS = {"全部", "待审核", "已上架", "已售出", "已下架", "审核不通过"};
    private static final int[] STATUS_VALUES = {-1, 0, 1, 2, 3, 4};

    public ProductManagePanel() {
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

        searchPanel.add(new JLabel("状态："));
        statusCombo = new JComboBox<>(STATUS_ITEMS);
        statusCombo.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        searchPanel.add(statusCombo);

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
     * 从数据库加载分类到下拉框
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
            JOptionPane.showMessageDialog(this, "加载分类失败：" + e.getMessage(), "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 初始化中央表格
     */
    private void initTable() {
        String[] columns = {"ID", "名称", "分类", "价格", "原价", "卖家", "状态", "发布时间"};
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
        productTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        productTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        productTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        productTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        productTable.getColumnModel().getColumn(7).setPreferredWidth(150);

        // 双击查看详情
        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = productTable.rowAtPoint(e.getPoint());
                if (row >= 0 && e.getClickCount() == 2) {
                    showProductDetail(row);
                }
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    showPopupMenu(e, row);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = productTable.rowAtPoint(e.getPoint());
                    showPopupMenu(e, row);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = productTable.rowAtPoint(e.getPoint());
                    showPopupMenu(e, row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * 显示右键菜单
     */
    private void showPopupMenu(MouseEvent e, int row) {
        if (row < 0 || currentProducts == null || row >= currentProducts.size()) {
            return;
        }
        productTable.setRowSelectionInterval(row, row);

        Product product = currentProducts.get(row);
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        int status = product.getStatus();
        if (status == 0) { // 待审核
            JMenuItem approveItem = new JMenuItem("审核通过");
            approveItem.addActionListener(ev -> auditProduct(product.getProductId(), 1));
            popupMenu.add(approveItem);

            JMenuItem rejectItem = new JMenuItem("审核不通过");
            rejectItem.addActionListener(ev -> auditProduct(product.getProductId(), 4));
            popupMenu.add(rejectItem);
        }

        if (status == 1) { // 已上架
            JMenuItem delistItem = new JMenuItem("下架");
            delistItem.addActionListener(ev -> delistProduct(product.getProductId()));
            popupMenu.add(delistItem);
        }

        if (popupMenu.getComponentCount() > 0) {
            popupMenu.addSeparator();
        }

        JMenuItem deleteItem = new JMenuItem("删除商品");
        deleteItem.addActionListener(ev -> deleteProduct(product.getProductId()));
        popupMenu.add(deleteItem);

        popupMenu.show(productTable, e.getX(), e.getY());
    }

    /**
     * 初始化底部面板
     */
    private void initBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        statusLabel = new JLabel("共 0 条记录");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        bottomPanel.add(statusLabel, BorderLayout.WEST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * 刷新数据（查询全部商品）
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

            int statusIndex = statusCombo != null ? statusCombo.getSelectedIndex() : 0;
            Integer status = null;
            if (statusIndex > 0) {
                status = STATUS_VALUES[statusIndex];
            }

            if (!keyword.isEmpty() || categoryId != null || status != null) {
                currentProducts = productDAO.search(keyword, categoryId,
                        null, null, status);
            } else {
                currentProducts = productDAO.findAll();
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

            int statusIndex = statusCombo.getSelectedIndex();
            Integer status = null;
            if (statusIndex > 0) {
                status = STATUS_VALUES[statusIndex];
            }

            currentProducts = productDAO.search(keyword, categoryId,
                    null, null, status);

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
        statusCombo.setSelectedIndex(0);
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
                    p.getStatusName(),
                    p.getCreateTime() != null ? dateFormat.format(p.getCreateTime()) : ""
            };
            tableModel.addRow(row);
        }
        statusLabel.setText("共 " + currentProducts.size() + " 条记录");
    }

    /**
     * 显示商品详情对话框
     */
    private void showProductDetail(int row) {
        if (currentProducts == null || row >= currentProducts.size()) return;
        Product product = currentProducts.get(row);

        StringBuilder sb = new StringBuilder();
        sb.append("商品ID：").append(product.getProductId()).append("\n");
        sb.append("商品名称：").append(product.getName()).append("\n");
        sb.append("商品分类：").append(product.getCategoryName()).append("\n");
        sb.append("售价：").append(product.getPrice()).append("\n");
        sb.append("原价：").append(product.getOriginalPrice()).append("\n");
        sb.append("描述：").append(product.getDescription()).append("\n");
        sb.append("卖家：").append(product.getSellerName()).append("\n");
        sb.append("电话：").append(product.getSellerPhone()).append("\n");
        sb.append("状态：").append(product.getStatusName()).append("\n");
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
     * 审核商品
     */
    private void auditProduct(int productId, int targetStatus) {
        String action = targetStatus == 1 ? "审核通过" : "审核不通过";
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要" + action + "该商品吗？",
                "审核确认",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = productDAO.updateStatus(productId, targetStatus);
            if (ok) {
                JOptionPane.showMessageDialog(this, action + "成功！", "提示",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "操作失败", "操作失败",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "审核操作失败：" + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 下架商品
     */
    private void delistProduct(int productId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要下架该商品吗？",
                "下架确认",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = productDAO.updateStatus(productId, 3);
            if (ok) {
                JOptionPane.showMessageDialog(this, "下架成功！", "提示",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "操作失败", "操作失败",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "下架操作失败：" + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 删除商品
     */
    private void deleteProduct(int productId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除该商品吗？此操作不可恢复！",
                "删除确认",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = productDAO.delete(productId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "删除成功！", "提示",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "删除失败！", "操作失败",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "删除操作失败：" + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

}
