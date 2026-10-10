package com.campusmarket.ui;

import com.campusmarket.dao.UserDAO;
import com.campusmarket.model.User;
import com.campusmarket.ui.panels.ProductListPanel;
import com.campusmarket.ui.panels.PublishProductPanel;
import com.campusmarket.ui.panels.MyProductPanel;
import com.campusmarket.ui.panels.MyOrderPanel;
import com.campusmarket.util.PasswordUtil;
import com.campusmarket.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 学生主界面
 */
public class StudentFrame extends JFrame {

    private JTabbedPane tabbedPane;
    private JLabel statusLabel;

    // 面板引用
    private ProductListPanel productListPanel;
    private PublishProductPanel publishProductPanel;
    private MyProductPanel myProductPanel;
    private MyOrderPanel myOrderPanel;

    public StudentFrame() {
        initUI();
        initMenuBar();
        initTabs();
        initStatusBar();

        // 显示欢迎信息
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            statusLabel.setText("欢迎，" + currentUser.getRealName());
        }

        // 窗口关闭事件
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(
                        StudentFrame.this,
                        "确定要退出系统吗？",
                        "退出确认",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (result == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });

        setVisible(true);
    }

    private void initUI() {
        setTitle("校园二手交易系统 - 学生端");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 系统菜单
        JMenu systemMenu = new JMenu("系统");

        JMenuItem logoutItem = new JMenuItem("退出登录");
        logoutItem.addActionListener(e -> logout());

        JMenuItem exitItem = new JMenuItem("退出系统");
        exitItem.addActionListener(e -> exitSystem());

        systemMenu.add(logoutItem);
        systemMenu.addSeparator();
        systemMenu.add(exitItem);

        menuBar.add(systemMenu);

        // 个人菜单
        JMenu personalMenu = new JMenu("个人");

        JMenuItem changePasswordItem = new JMenuItem("修改密码");
        changePasswordItem.addActionListener(e -> showChangePasswordDialog());

        personalMenu.add(changePasswordItem);
        menuBar.add(personalMenu);

        setJMenuBar(menuBar);
    }

    private void initTabs() {
        tabbedPane = new JTabbedPane();

        productListPanel = new ProductListPanel();
        publishProductPanel = new PublishProductPanel();
        myProductPanel = new MyProductPanel();
        myOrderPanel = new MyOrderPanel();

        tabbedPane.addTab("商品浏览", productListPanel);
        tabbedPane.addTab("发布商品", publishProductPanel);
        tabbedPane.addTab("我的商品", myProductPanel);
        tabbedPane.addTab("我的订单", myOrderPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void initStatusBar() {
        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusLabel, BorderLayout.SOUTH);
    }

    /**
     * 退出登录 — 返回登录界面
     */
    private void logout() {
        SessionManager.logout();
        dispose();
        new LoginFrame().setVisible(true);
    }

    /**
     * 退出系统
     */
    private void exitSystem() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "确定要退出系统吗？",
                "退出确认",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    /**
     * 显示修改密码对话框
     */
    private void showChangePasswordDialog() {
        JDialog dialog = new JDialog(this, "修改密码", true);
        dialog.setSize(380, 280);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 原密码
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("原密码：", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        JPasswordField oldPwdField = new JPasswordField(15);
        panel.add(oldPwdField, gbc);

        // 新密码
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("新密码：", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        JPasswordField newPwdField = new JPasswordField(15);
        panel.add(newPwdField, gbc);

        // 确认密码
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("确认密码：", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        JPasswordField confirmPwdField = new JPasswordField(15);
        panel.add(confirmPwdField, gbc);

        // 按钮
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 8, 8, 8);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));

        JButton okBtn = new JButton("确定");
        okBtn.setPreferredSize(new Dimension(90, 30));
        JButton cancelBtn = new JButton("取消");
        cancelBtn.setPreferredSize(new Dimension(90, 30));

        okBtn.addActionListener(e -> {
            String oldPwd = new String(oldPwdField.getPassword());
            String newPwd = new String(newPwdField.getPassword());
            String confirmPwd = new String(confirmPwdField.getPassword());

            UserDAO userDAO = new UserDAO();
            int userId = SessionManager.getCurrentUserId();
            User user = userDAO.findById(userId);
            if (user == null) {
                JOptionPane.showMessageDialog(dialog, "用户不存在", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!user.getPassword().equals(PasswordUtil.encrypt(oldPwd))) {
                JOptionPane.showMessageDialog(dialog, "原密码错误", "修改失败", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (newPwd.length() < 6) {
                JOptionPane.showMessageDialog(dialog, "新密码长度不能少于6位", "修改失败", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!newPwd.equals(confirmPwd)) {
                JOptionPane.showMessageDialog(dialog, "两次密码输入不一致", "修改失败", JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean ok = userDAO.updatePassword(userId, PasswordUtil.encrypt(newPwd));
            if (ok) {
                JOptionPane.showMessageDialog(dialog, "密码修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "修改失败，请稍后重试", "修改失败", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);
        panel.add(btnPanel, gbc);

        dialog.add(panel);
        dialog.getRootPane().setDefaultButton(okBtn);
        dialog.setVisible(true);
    }
}
