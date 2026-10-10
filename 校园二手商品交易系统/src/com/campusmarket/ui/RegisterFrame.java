package com.campusmarket.ui;

import com.campusmarket.dao.UserDAO;
import com.campusmarket.model.User;
import com.campusmarket.util.PasswordUtil;

import javax.swing.*;
import java.awt.*;

/**
 * 注册界面
 */
public class RegisterFrame extends JFrame {

    private final UserDAO userDAO = new UserDAO();

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField realNameField;
    private JTextField phoneField;

    public RegisterFrame() {
        setTitle("校园二手交易系统 - 注册");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 400);
        setResizable(false);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("用户注册", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 22));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("用户名：", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("密  码：", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("确认密码：", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(15);
        panel.add(confirmPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("真实姓名：", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        realNameField = new JTextField(15);
        panel.add(realNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("手机号码：", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        phoneField = new JTextField(15);
        panel.add(phoneField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        JButton registerButton = new JButton("注册");
        registerButton.setPreferredSize(new Dimension(100, 32));
        registerButton.addActionListener(e -> doRegister());
        buttonPanel.add(registerButton);

        JButton cancelButton = new JButton("取消");
        cancelButton.setPreferredSize(new Dimension(100, 32));
        cancelButton.addActionListener(e -> returnToLogin());
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 8, 8, 8);
        panel.add(buttonPanel, gbc);

        add(panel);
        getRootPane().setDefaultButton(registerButton);
    }

    private void doRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String realName = realNameField.getText().trim();
        String phone = phoneField.getText().trim();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名不能为空", "注册失败", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "密码长度不能少于6位", "注册失败", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "两次密码输入不一致", "注册失败", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (realName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "真实姓名不能为空", "注册失败", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (userDAO.findByUsername(username) != null) {
            JOptionPane.showMessageDialog(this, "用户名已存在", "注册失败", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.encrypt(password));
        user.setRealName(realName);
        user.setPhone(phone);
        user.setRole(0);
        user.setStatus(0);

        int id = userDAO.insert(user);
        if (id > 0) {
            JOptionPane.showMessageDialog(this, "注册成功！请返回登录。", "提示", JOptionPane.INFORMATION_MESSAGE);
            returnToLogin();
        } else {
            JOptionPane.showMessageDialog(this, "注册失败，请稍后重试", "注册失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void returnToLogin() {
        new LoginFrame().setVisible(true);
        dispose();
    }
}
