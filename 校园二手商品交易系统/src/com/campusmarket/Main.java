package com.campusmarket;

import com.campusmarket.ui.LoginFrame;

import javax.swing.*;

/**
 * 校园二手物品置换交易管理系统 — 程序入口
 */
public class Main {

    public static void main(String[] args) {
        // 设置 Swing 外观为系统默认
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // 忽略，使用默认外观
        }

        // 确保字体渲染清晰
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // 在 EDT 线程启动 GUI
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
