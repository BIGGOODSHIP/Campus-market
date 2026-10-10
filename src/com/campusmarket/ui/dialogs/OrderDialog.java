package com.campusmarket.ui.dialogs;

import com.campusmarket.dao.OrderDAO;
import com.campusmarket.model.Product;
import com.campusmarket.model.User;
import com.campusmarket.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * 下单对话框 — 确认购买商品
 */
public class OrderDialog extends JDialog {

    private final Product product;
    private final OrderDAO orderDAO = new OrderDAO();

    /**
     * @param parent  父窗口
     * @param product 要购买的商品
     */
    public OrderDialog(JFrame parent, Product product) {
        super(parent, "确认下单", true);
        this.product = product;
        initUI();
        pack();
        setSize(400, 350);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("请确认以下订单信息", SwingConstants.CENTER);
        titleLabel.setFont(new Font("宋体", Font.BOLD, 16));
        mainPanel.add(titleLabel, gbc);

        // 分隔
        gbc.gridwidth = 2;
        gbc.gridy = 1;
        mainPanel.add(new JSeparator(), gbc);

        // 商品名称
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel nameLabel = new JLabel("商品名称:");
        nameLabel.setFont(new Font("宋体", Font.PLAIN, 14));
        mainPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        JLabel nameValue = new JLabel(product.getName());
        nameValue.setFont(new Font("宋体", Font.PLAIN, 14));
        mainPanel.add(nameValue, gbc);

        // 价格
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel priceLabel = new JLabel("价格:");
        priceLabel.setFont(new Font("宋体", Font.PLAIN, 14));
        mainPanel.add(priceLabel, gbc);

        gbc.gridx = 1;
        JLabel priceValue = new JLabel("¥" + product.getPrice().toString());
        priceValue.setFont(new Font("宋体", Font.BOLD, 16));
        priceValue.setForeground(new Color(200, 50, 50));
        mainPanel.add(priceValue, gbc);

        // 卖家
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel sellerLabel = new JLabel("卖家:");
        sellerLabel.setFont(new Font("宋体", Font.PLAIN, 14));
        mainPanel.add(sellerLabel, gbc);

        gbc.gridx = 1;
        JLabel sellerValue = new JLabel(product.getSellerName());
        sellerValue.setFont(new Font("宋体", Font.PLAIN, 14));
        mainPanel.add(sellerValue, gbc);

        // 买家（当前用户）
        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel buyerLabel = new JLabel("买家:");
        buyerLabel.setFont(new Font("宋体", Font.PLAIN, 14));
        mainPanel.add(buyerLabel, gbc);

        gbc.gridx = 1;
        User currentUser = SessionManager.getCurrentUser();
        JLabel buyerValue = new JLabel(currentUser != null ? currentUser.getRealName() : "未知");
        buyerValue.setFont(new Font("宋体", Font.PLAIN, 14));
        mainPanel.add(buyerValue, gbc);

        // 按钮面板
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));

        JButton confirmBtn = new JButton("确认下单");
        confirmBtn.setFont(new Font("宋体", Font.PLAIN, 14));
        confirmBtn.setPreferredSize(new Dimension(110, 35));
        confirmBtn.addActionListener(e -> placeOrder());

        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(new Font("宋体", Font.PLAIN, 14));
        cancelBtn.setPreferredSize(new Dimension(110, 35));
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel);
    }

    /**
     * 执行下单操作
     */
    private void placeOrder() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "登录状态异常，请重新登录",
                    "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 二次确认
        int confirm = JOptionPane.showConfirmDialog(this,
                "确认购买 \"" + product.getName() + "\" 吗？\n价格: ¥" + product.getPrice(),
                "确认下单", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Map<String, Object> result = orderDAO.createOrder(
                    currentUser.getUserId(),
                    product.getProductId(),
                    product.getPrice()
            );

            if ((boolean) result.get("success")) {
                int orderId = (int) result.get("orderId");
                JOptionPane.showMessageDialog(this,
                        "下单成功！\n订单号: " + orderId,
                        "成功", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                String message = (String) result.getOrDefault("message", "下单失败");
                JOptionPane.showMessageDialog(this, message,
                        "下单失败", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "下单失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
