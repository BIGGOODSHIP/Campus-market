package com.campusmarket.ui.panels;

import com.campusmarket.dao.OrderDAO;
import com.campusmarket.model.Order;
import com.campusmarket.model.User;
import com.campusmarket.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 我的订单面板 — 学生查看/操作自己的订单
 */
public class MyOrderPanel extends JPanel {

    private final OrderDAO orderDAO = new OrderDAO();
    private JTable orderTable;
    private DefaultTableModel tableModel;
    private JRadioButton buyerRadio, sellerRadio;
    private JButton confirmBtn, cancelBtn;
    private List<Order> currentOrders = new ArrayList<>();
    private boolean isBuyerView = true;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public MyOrderPanel() {
        setLayout(new BorderLayout());
        initTopPanel();
        initCenterPanel();
        initBottomPanel();
        loadBuyerOrders();
    }

    // ===================== Top Panel (View Switcher) =====================

    private void initTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));

        buyerRadio = new JRadioButton("我购买的", true);
        buyerRadio.setFont(new Font("宋体", Font.PLAIN, 14));
        buyerRadio.addActionListener(e -> {
            if (buyerRadio.isSelected()) {
                isBuyerView = true;
                loadBuyerOrders();
            }
        });

        sellerRadio = new JRadioButton("我卖出的");
        sellerRadio.setFont(new Font("宋体", Font.PLAIN, 14));
        sellerRadio.addActionListener(e -> {
            if (sellerRadio.isSelected()) {
                isBuyerView = false;
                loadSellerOrders();
            }
        });

        ButtonGroup group = new ButtonGroup();
        group.add(buyerRadio);
        group.add(sellerRadio);

        topPanel.add(new JLabel("查看方式:"));
        topPanel.add(buyerRadio);
        topPanel.add(sellerRadio);

        add(topPanel, BorderLayout.NORTH);
    }

    // ===================== Center Panel (Table) =====================

    private void initCenterPanel() {
        // 默认以买家视图列初始化
        String[] buyerColumns = {"ID", "商品名称", "价格", "卖家", "状态", "下单时间", "完成时间"};
        tableModel = new DefaultTableModel(buyerColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        orderTable = new JTable(tableModel);
        orderTable.setFont(new Font("宋体", Font.PLAIN, 14));
        orderTable.setRowHeight(24);
        orderTable.getTableHeader().setFont(new Font("宋体", Font.BOLD, 14));
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 选择变化时更新按钮状态
        orderTable.getSelectionModel().addListSelectionListener(e -> updateButtonStates());

        JScrollPane scrollPane = new JScrollPane(orderTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    // ===================== Bottom Panel (Action Buttons) =====================

    private void initBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        confirmBtn = new JButton("确认收货");
        confirmBtn.setFont(new Font("宋体", Font.PLAIN, 14));
        confirmBtn.addActionListener(e -> confirmReceipt());

        cancelBtn = new JButton("取消订单");
        cancelBtn.setFont(new Font("宋体", Font.PLAIN, 14));
        cancelBtn.addActionListener(e -> cancelOrder());

        bottomPanel.add(confirmBtn);
        bottomPanel.add(cancelBtn);

        updateButtonStates();

        add(bottomPanel, BorderLayout.SOUTH);
    }

    // ===================== Data Loading =====================

    private void loadBuyerOrders() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return;

        try {
            currentOrders = orderDAO.findByBuyerId(currentUser.getUserId());
            setupTableColumns(true);
            populateTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "加载购买订单失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSellerOrders() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return;

        try {
            currentOrders = orderDAO.findBySellerId(currentUser.getUserId());
            setupTableColumns(false);
            populateTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "加载出售订单失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupTableColumns(boolean isBuyer) {
        tableModel.setRowCount(0);
        String[] columns;
        if (isBuyer) {
            columns = new String[]{"ID", "商品名称", "价格", "卖家", "状态", "下单时间", "完成时间"};
        } else {
            columns = new String[]{"ID", "商品名称", "价格", "买家", "状态", "下单时间", "完成时间"};
        }
        tableModel.setColumnIdentifiers(columns);

        // 触发表头刷新
        orderTable.getTableHeader().repaint();
    }

    private void populateTable() {
        tableModel.setRowCount(0);
        for (Order order : currentOrders) {
            tableModel.addRow(new Object[]{
                    order.getOrderId(),
                    order.getProductName(),
                    order.getPrice(),
                    isBuyerView ? order.getSellerName() : order.getBuyerName(),
                    order.getStatusName(),
                    formatTimestamp(order.getCreateTime()),
                    formatTimestamp(order.getCompleteTime())
            });
        }
    }

    // ===================== Selection Helpers =====================

    private Order getSelectedOrder() {
        int row = orderTable.getSelectedRow();
        if (row >= 0 && row < currentOrders.size()) {
            return currentOrders.get(row);
        }
        return null;
    }

    private void updateButtonStates() {
        Order order = getSelectedOrder();
        if (order == null) {
            confirmBtn.setEnabled(false);
            cancelBtn.setEnabled(false);
        } else {
            boolean isPending = order.getStatus() == 0;

            // 买家视图
            confirmBtn.setVisible(isBuyerView);

            // 启用条件
            confirmBtn.setEnabled(isBuyerView && isPending);
            cancelBtn.setEnabled(isPending);
        }
    }

    // ===================== Actions =====================

    private void confirmReceipt() {
        Order order = getSelectedOrder();
        if (order == null) {
            JOptionPane.showMessageDialog(this, "请先选择一个待交易订单", "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "确认收到商品 \"" + order.getProductName() + "\" 吗？\n确认后将完成订单。",
                "确认收货", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = orderDAO.updateStatus(order.getOrderId(), 1);
            if (ok) {
                JOptionPane.showMessageDialog(this, "确认收货成功，订单已完成！",
                        "成功", JOptionPane.INFORMATION_MESSAGE);
                refreshCurrentView();
            } else {
                JOptionPane.showMessageDialog(this, "操作失败", "操作失败", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "操作失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelOrder() {
        Order order = getSelectedOrder();
        if (order == null) {
            JOptionPane.showMessageDialog(this, "请先选择一个待交易订单", "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要取消订单 #" + order.getOrderId() + " 吗？\n商品: " + order.getProductName(),
                "确认取消", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = orderDAO.updateStatus(order.getOrderId(), 2);
            if (ok) {
                JOptionPane.showMessageDialog(this, "订单已取消",
                        "成功", JOptionPane.INFORMATION_MESSAGE);
                refreshCurrentView();
            } else {
                JOptionPane.showMessageDialog(this, "操作失败", "操作失败", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "操作失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshCurrentView() {
        if (isBuyerView) {
            loadBuyerOrders();
        } else {
            loadSellerOrders();
        }
    }

    // ===================== Utility =====================

    private String formatTimestamp(Timestamp ts) {
        return ts != null ? SDF.format(ts) : "";
    }
}
