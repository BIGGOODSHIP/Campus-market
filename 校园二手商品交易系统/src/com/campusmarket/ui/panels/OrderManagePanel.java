package com.campusmarket.ui.panels;

import com.campusmarket.dao.OrderDAO;
import com.campusmarket.model.Order;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 订单管理面板 — 管理员查看/操作所有订单
 */
public class OrderManagePanel extends JPanel {

    private final OrderDAO orderDAO = new OrderDAO();
    private JTable orderTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilter;
    private JButton searchBtn, resetBtn;
    private JButton completeBtn, cancelBtn, deleteBtn;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public OrderManagePanel() {
        setLayout(new BorderLayout());
        initTopPanel();
        initCenterPanel();
        initBottomPanel();
        loadOrders();
    }

    // ===================== Top Panel =====================

    private void initTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        topPanel.add(new JLabel("订单状态:"));

        statusFilter = new JComboBox<>(new String[]{"全部", "待交易", "已完成", "已取消"});
        statusFilter.setFont(new Font("宋体", Font.PLAIN, 14));
        topPanel.add(statusFilter);

        searchBtn = new JButton("查询");
        searchBtn.setFont(new Font("宋体", Font.PLAIN, 14));
        searchBtn.addActionListener(e -> searchOrders());
        topPanel.add(searchBtn);

        resetBtn = new JButton("重置");
        resetBtn.setFont(new Font("宋体", Font.PLAIN, 14));
        resetBtn.addActionListener(e -> {
            statusFilter.setSelectedIndex(0);
            loadOrders();
        });
        topPanel.add(resetBtn);

        add(topPanel, BorderLayout.NORTH);
    }

    // ===================== Center Panel =====================

    private void initCenterPanel() {
        String[] columns = {"ID", "商品名称", "成交价格", "买家", "卖家", "状态", "下单时间", "完成时间"};
        tableModel = new DefaultTableModel(columns, 0) {
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

        // 右键菜单
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem completeItem = new JMenuItem("完成订单");
        completeItem.addActionListener(e -> completeOrder());
        JMenuItem cancelItem = new JMenuItem("取消订单");
        cancelItem.addActionListener(e -> cancelOrder());
        JMenuItem deleteItem = new JMenuItem("删除订单");
        deleteItem.addActionListener(e -> deleteOrder());
        popupMenu.add(completeItem);
        popupMenu.add(cancelItem);
        popupMenu.addSeparator();
        popupMenu.add(deleteItem);

        orderTable.setComponentPopupMenu(popupMenu);

        // 选择变化时更新按钮状态
        orderTable.getSelectionModel().addListSelectionListener(e -> updateButtonStates());

        // 双击查看详情
        orderTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showOrderDetail();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(orderTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    // ===================== Bottom Panel =====================

    private void initBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        completeBtn = new JButton("完成订单");
        completeBtn.setFont(new Font("宋体", Font.PLAIN, 14));
        completeBtn.addActionListener(e -> completeOrder());

        cancelBtn = new JButton("取消订单");
        cancelBtn.setFont(new Font("宋体", Font.PLAIN, 14));
        cancelBtn.addActionListener(e -> cancelOrder());

        deleteBtn = new JButton("删除订单");
        deleteBtn.setFont(new Font("宋体", Font.PLAIN, 14));
        deleteBtn.addActionListener(e -> deleteOrder());

        bottomPanel.add(completeBtn);
        bottomPanel.add(cancelBtn);
        bottomPanel.add(deleteBtn);

        updateButtonStates();

        add(bottomPanel, BorderLayout.SOUTH);
    }

    // ===================== Data Loading =====================

    private void loadOrders() {
        try {
            List<Order> orders = orderDAO.findAll();
            loadOrders(orders);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "加载订单数据失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadOrders(List<Order> orders) {
        tableModel.setRowCount(0);
        for (Order order : orders) {
            tableModel.addRow(new Object[]{
                    order.getOrderId(),
                    order.getProductName(),
                    order.getPrice(),
                    order.getBuyerName(),
                    order.getSellerName(),
                    order.getStatusName(),
                    formatTimestamp(order.getCreateTime()),
                    formatTimestamp(order.getCompleteTime())
            });
        }
    }

    private void searchOrders() {
        try {
            int selected = statusFilter.getSelectedIndex();
            Integer status = null;
            switch (selected) {
                case 1: status = 0; break; // 待交易
                case 2: status = 1; break; // 已完成
                case 3: status = 2; break; // 已取消
                // default: status = null → 全部
            }
            List<Order> orders = orderDAO.search(status, null, null);
            loadOrders(orders);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "查询订单失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===================== Selection Helpers =====================

    private int getSelectedOrderId() {
        int row = orderTable.getSelectedRow();
        if (row >= 0) {
            Object value = tableModel.getValueAt(row, 0);
            return value instanceof Integer ? (Integer) value : Integer.parseInt(value.toString());
        }
        return -1;
    }

    private String getSelectedOrderStatus() {
        int row = orderTable.getSelectedRow();
        if (row >= 0) {
            Object value = tableModel.getValueAt(row, 5);
            return value != null ? value.toString() : null;
        }
        return null;
    }

    private void updateButtonStates() {
        if (orderTable.getSelectedRow() < 0) {
            completeBtn.setEnabled(false);
            cancelBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
        } else {
            String status = getSelectedOrderStatus();
            boolean isPending = "待交易".equals(status);
            completeBtn.setEnabled(isPending);
            cancelBtn.setEnabled(isPending);
            deleteBtn.setEnabled(true);
        }
    }

    // ===================== Actions =====================

    private void completeOrder() {
        int orderId = getSelectedOrderId();
        if (orderId <= 0) {
            JOptionPane.showMessageDialog(this, "请先选择一个订单", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要将订单 #" + orderId + " 标记为已完成吗？",
                "确认完成", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = orderDAO.updateStatus(orderId, 1);
            if (ok) {
                JOptionPane.showMessageDialog(this, "订单已完成", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadOrders();
            } else {
                JOptionPane.showMessageDialog(this, "操作失败", "操作失败", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "操作失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelOrder() {
        int orderId = getSelectedOrderId();
        if (orderId <= 0) {
            JOptionPane.showMessageDialog(this, "请先选择一个订单", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要取消订单 #" + orderId + " 吗？",
                "确认取消", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = orderDAO.updateStatus(orderId, 2);
            if (ok) {
                JOptionPane.showMessageDialog(this, "订单已取消", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadOrders();
            } else {
                JOptionPane.showMessageDialog(this, "操作失败", "操作失败", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "操作失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteOrder() {
        int orderId = getSelectedOrderId();
        if (orderId <= 0) {
            JOptionPane.showMessageDialog(this, "请先选择一个订单", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除订单 #" + orderId + " 吗？\n此操作不可恢复！",
                "确认删除", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            if (orderDAO.delete(orderId)) {
                JOptionPane.showMessageDialog(this, "订单已删除", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadOrders();
            } else {
                JOptionPane.showMessageDialog(this, "删除失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "删除失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===================== Order Detail Dialog =====================

    private void showOrderDetail() {
        int row = orderTable.getSelectedRow();
        if (row < 0) return;

        StringBuilder detail = new StringBuilder();
        detail.append("订单ID: ").append(tableModel.getValueAt(row, 0)).append("\n");
        detail.append("商品名称: ").append(tableModel.getValueAt(row, 1)).append("\n");
        detail.append("成交价格: ").append(tableModel.getValueAt(row, 2)).append("\n");
        detail.append("买家: ").append(tableModel.getValueAt(row, 3)).append("\n");
        detail.append("卖家: ").append(tableModel.getValueAt(row, 4)).append("\n");
        detail.append("状态: ").append(tableModel.getValueAt(row, 5)).append("\n");
        detail.append("下单时间: ").append(tableModel.getValueAt(row, 6)).append("\n");
        detail.append("完成时间: ").append(tableModel.getValueAt(row, 7));

        JTextArea textArea = new JTextArea(detail.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("宋体", Font.PLAIN, 14));
        textArea.setBackground(new Color(245, 245, 245));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 280));

        JOptionPane.showMessageDialog(this, scrollPane, "订单详情",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ===================== Utility =====================

    private String formatTimestamp(Timestamp ts) {
        return ts != null ? SDF.format(ts) : "";
    }
}
