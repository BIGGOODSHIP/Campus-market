package com.campusmarket.ui;

import com.campusmarket.model.User;
import com.campusmarket.ui.panels.UserManagePanel;
import com.campusmarket.ui.panels.ProductManagePanel;
import com.campusmarket.ui.panels.OrderManagePanel;
import com.campusmarket.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 管理员主界面
 */
public class AdminFrame extends JFrame {

    private JTabbedPane tabbedPane;
    private JLabel statusLabel;

    // 面板引用
    private UserManagePanel userManagePanel;
    private ProductManagePanel productManagePanel;
    private OrderManagePanel orderManagePanel;

    public AdminFrame() {
        initUI();
        initMenuBar();
        initTabs();
        initStatusBar();

        // 显示欢迎信息
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            statusLabel.setText("欢迎，管理员 " + currentUser.getUsername());
        }

        // 窗口关闭事件
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(
                        AdminFrame.this,
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
        setTitle("校园二手交易系统 - 管理员端");
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
        setJMenuBar(menuBar);
    }

    private void initTabs() {
        tabbedPane = new JTabbedPane();

        userManagePanel = new UserManagePanel();
        productManagePanel = new ProductManagePanel();
        orderManagePanel = new OrderManagePanel();

        tabbedPane.addTab("用户管理", userManagePanel);
        tabbedPane.addTab("商品管理", productManagePanel);
        tabbedPane.addTab("订单管理", orderManagePanel);

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
}
