package com.campusmarket.ui.panels;

import com.campusmarket.dao.CategoryDAO;
import com.campusmarket.dao.ProductDAO;
import com.campusmarket.model.Category;
import com.campusmarket.model.Product;
import com.campusmarket.util.SessionManager;

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
 * 我的商品面板（学生端）
 * 学生可查看、编辑、下架、删除自己发布的商品
 */
public class MyProductPanel extends JPanel {

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private JComboBox<String> statusFilterCombo;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    private List<Product> currentProducts;

    // 状态映射
    private static final String[] STATUS_ITEMS = {"全部", "待审核", "已上架", "已售出", "已下架", "审核不通过"};
    private static final int[] STATUS_VALUES = {-1, 0, 1, 2, 3, 4};

    public MyProductPanel() {
        setLayout(new BorderLayout());
        initTopPanel();
        initTable();
        initBottomPanel();
        refreshData();
    }

    /**
     * 初始化顶部过滤面板
     */
    private void initTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topPanel.setBorder(BorderFactory.createTitledBorder("筛选条件"));

        topPanel.add(new JLabel("商品状态："));
        statusFilterCombo = new JComboBox<>(STATUS_ITEMS);
        statusFilterCombo.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        topPanel.add(statusFilterCombo);

        JButton searchButton = new JButton("查询");
        searchButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        searchButton.addActionListener(e -> filterProducts());
        topPanel.add(searchButton);

        JButton refreshButton = new JButton("刷新");
        refreshButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        refreshButton.addActionListener(e -> refreshData());
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);
    }

    /**
     * 初始化中央表格
     */
    private void initTable() {
        String[] columns = {"ID", "名称", "价格", "原价", "分类", "状态", "发布时间"};
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
        productTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        productTable.getColumnModel().getColumn(6).setPreferredWidth(150);

        // 鼠标事件：双击编辑，右键菜单
        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = productTable.rowAtPoint(e.getPoint());
                if (row >= 0 && e.getClickCount() == 2) {
                    editProduct(row);
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

        JMenuItem editItem = new JMenuItem("编辑");
        editItem.addActionListener(ev -> editProduct(row));
        popupMenu.add(editItem);

        if (product.getStatus() == 1) { // 已上架
            JMenuItem delistItem = new JMenuItem("下架");
            delistItem.addActionListener(ev -> delistProduct(product.getProductId()));
            popupMenu.add(delistItem);
        }

        popupMenu.addSeparator();

        JMenuItem deleteItem = new JMenuItem("删除");
        deleteItem.addActionListener(ev -> deleteProduct(product.getProductId()));
        popupMenu.add(deleteItem);

        popupMenu.show(productTable, e.getX(), e.getY());
    }

    /**
     * 初始化底部按钮面板和状态标签
     */
    private void initBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // 操作按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));

        JButton editButton = new JButton("编辑");
        editButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        editButton.addActionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "请先选择一个商品！", "提示",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            editProduct(row);
        });
        buttonPanel.add(editButton);

        JButton delistButton = new JButton("下架");
        delistButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        delistButton.addActionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "请先选择一个商品！", "提示",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (currentProducts == null || row >= currentProducts.size()) return;
            Product product = currentProducts.get(row);
            if (product.getStatus() != 1) {
                JOptionPane.showMessageDialog(this,
                        "只能下架「已上架」状态的商品！", "提示",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            delistProduct(product.getProductId());
        });
        buttonPanel.add(delistButton);

        JButton deleteButton = new JButton("删除");
        deleteButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        deleteButton.addActionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "请先选择一个商品！", "提示",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (currentProducts == null || row >= currentProducts.size()) return;
            deleteProduct(currentProducts.get(row).getProductId());
        });
        buttonPanel.add(deleteButton);

        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        // 状态标签
        statusLabel = new JLabel("共 0 条记录");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        bottomPanel.add(statusLabel, BorderLayout.WEST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * 刷新数据（加载当前用户的所有商品）
     */
    public void refreshData() {
        int userId = SessionManager.getCurrentUserId();
        if (userId == 0) {
            tableModel.setRowCount(0);
            statusLabel.setText("共 0 条记录");
            return;
        }

        try {
            int statusIndex = statusFilterCombo.getSelectedIndex();
            Integer statusFilter = null;
            if (statusIndex > 0) {
                statusFilter = STATUS_VALUES[statusIndex];
            }

            if (statusFilter != null) {
                // 使用搜索接口按状态过滤当前用户商品
                currentProducts = productDAO.search(
                        null, null, null, null, statusFilter);
                // 进一步过滤：只保留当前用户的
                currentProducts.removeIf(p -> p.getSellerId() != userId);
            } else {
                currentProducts = productDAO.findBySellerId(userId);
            }

            updateTableData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载商品数据失败：" + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 按状态过滤
     */
    private void filterProducts() {
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
                    p.getPrice() != null ? p.getPrice().toString() : "",
                    p.getOriginalPrice() != null ? p.getOriginalPrice().toString() : "",
                    p.getCategoryName(),
                    p.getStatusName(),
                    p.getCreateTime() != null ? dateFormat.format(p.getCreateTime()) : ""
            };
            tableModel.addRow(row);
        }
        statusLabel.setText("共 " + currentProducts.size() + " 条记录");
    }

    /**
     * 编辑商品 — 打开编辑对话框
     */
    private void editProduct(int row) {
        if (currentProducts == null || row >= currentProducts.size()) return;
        Product product = currentProducts.get(row);

        showEditDialog(product);
    }

    /**
     * 显示编辑商品对话框
     */
    private void showEditDialog(Product product) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                "编辑商品", true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 14);

        // 商品名称
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("商品名称："), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JTextField nameField = new JTextField(product.getName(), 20);
        nameField.setFont(fieldFont);
        formPanel.add(nameField, gbc);

        // 商品分类
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("商品分类："), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JComboBox<Category> catCombo = new JComboBox<>();
        catCombo.setFont(fieldFont);
        try {
            List<Category> categories = categoryDAO.findAll();
            int selectIndex = 0;
            for (int i = 0; i < categories.size(); i++) {
                Category c = categories.get(i);
                catCombo.addItem(c);
                if (c.getCategoryId() == product.getCategoryId()) {
                    selectIndex = i;
                }
            }
            catCombo.setSelectedIndex(selectIndex);
        } catch (Exception ex) {
            // 加载失败则使用已有分类名
            Category fallback = new Category();
            fallback.setCategoryId(product.getCategoryId());
            fallback.setName(product.getCategoryName());
            catCombo.addItem(fallback);
        }
        formPanel.add(catCombo, gbc);

        // 售价
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("售  价："), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JTextField priceField = new JTextField(
                product.getPrice() != null ? product.getPrice().toString() : "", 20);
        priceField.setFont(fieldFont);
        formPanel.add(priceField, gbc);

        // 原价
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("原  价："), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JTextField origPriceField = new JTextField(
                product.getOriginalPrice() != null ? product.getOriginalPrice().toString() : "", 20);
        origPriceField.setFont(fieldFont);
        formPanel.add(origPriceField, gbc);

        // 商品描述
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        formPanel.add(new JLabel("商品描述："), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JTextArea descArea = new JTextArea(
                product.getDescription() != null ? product.getDescription() : "", 4, 20);
        descArea.setFont(fieldFont);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(280, 80));
        formPanel.add(descScroll, gbc);

        dialog.add(formPanel, BorderLayout.CENTER);

        // 底部按钮
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton saveButton = new JButton("保存");
        saveButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        saveButton.setPreferredSize(new Dimension(90, 32));
        saveButton.addActionListener(e -> {
            // 校验输入
            String name = nameField.getText().trim();
            String priceText = priceField.getText().trim();
            String origPriceText = origPriceField.getText().trim();
            String desc = descArea.getText().trim();
            Category selectedCat = (Category) catCombo.getSelectedItem();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "商品名称不能为空！", "输入错误",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (selectedCat == null) {
                JOptionPane.showMessageDialog(dialog, "请选择商品分类！", "输入错误",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            BigDecimal price;
            try {
                price = new BigDecimal(priceText);
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(dialog, "售价必须大于0！", "输入错误",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "请输入有效的售价！", "输入错误",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            BigDecimal origPrice = null;
            if (!origPriceText.isEmpty()) {
                try {
                    origPrice = new BigDecimal(origPriceText);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "请输入有效的原价！", "输入错误",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // 更新商品
            try {
                product.setName(name);
                product.setDescription(desc);
                product.setPrice(price);
                product.setOriginalPrice(origPrice);
                product.setCategoryId(selectedCat.getCategoryId());
                boolean ok = productDAO.update(product);

                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "修改成功！", "提示",
                            JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    refreshData();
                } else {
                    JOptionPane.showMessageDialog(dialog, "修改失败", "修改失败",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "修改失败：" + ex.getMessage(), "错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        btnPanel.add(saveButton);

        JButton cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        cancelButton.setPreferredSize(new Dimension(90, 32));
        cancelButton.addActionListener(e2 -> dialog.dispose());
        btnPanel.add(cancelButton);

        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
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
                JOptionPane.showMessageDialog(this, "下架失败", "操作失败",
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
            boolean success = productDAO.delete(productId);
            if (success) {
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
