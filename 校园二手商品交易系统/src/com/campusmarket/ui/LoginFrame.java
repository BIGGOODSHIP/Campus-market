package com.campusmarket.ui;

import com.campusmarket.dao.UserDAO;
import com.campusmarket.model.User;
import com.campusmarket.util.PasswordUtil;
import com.campusmarket.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * 登录界面
 */
public class LoginFrame extends JFrame {

    private final UserDAO userDAO = new UserDAO();

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField captchaField;
    private JLabel captchaLabel;
    private String captchaCode;

    public LoginFrame() {
        setTitle("校园二手交易系统 - 登录");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 440);
        setResizable(false);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("校园二手交易系统", SwingConstants.CENTER);
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
        panel.add(new JLabel("验证码：", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        captchaLabel = new JLabel();
        captchaLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        captchaLabel.setToolTipText("点击刷新验证码");
        refreshCaptcha();
        panel.add(captchaLabel, gbc);

        captchaLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { refreshCaptcha(); }
        });

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("输入验证码：", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        captchaField = new JTextField(15);
        panel.add(captchaField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        JButton loginButton = new JButton("登录");
        loginButton.setPreferredSize(new Dimension(100, 32));
        loginButton.addActionListener(e -> doLogin());
        buttonPanel.add(loginButton);

        JButton registerButton = new JButton("注册");
        registerButton.setPreferredSize(new Dimension(100, 32));
        registerButton.addActionListener(e -> openRegister());
        buttonPanel.add(registerButton);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 8, 8, 8);
        panel.add(buttonPanel, gbc);

        add(panel);
        getRootPane().setDefaultButton(loginButton);
    }

    /**
     * 生成图形验证码
     */
    private void refreshCaptcha() {
        int width = 120, height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, width, height);

        Random random = new Random();
        g.setColor(new Color(180, 180, 180));
        for (int i = 0; i < 5; i++) {
            g.drawLine(random.nextInt(width), random.nextInt(height),
                       random.nextInt(width), random.nextInt(height));
        }

        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        captchaCode = code.toString();

        g.setFont(new Font("Arial", Font.BOLD, 24));
        for (int i = 0; i < captchaCode.length(); i++) {
            g.setColor(new Color(random.nextInt(100), random.nextInt(100), random.nextInt(100)));
            g.drawString(String.valueOf(captchaCode.charAt(i)), 20 + i * 22, 28 + random.nextInt(5));
        }

        for (int i = 0; i < 30; i++) {
            g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            g.fillOval(random.nextInt(width), random.nextInt(height), 2, 2);
        }

        g.dispose();
        captchaLabel.setIcon(new ImageIcon(image));
        if (captchaField != null) captchaField.setText("");
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String captcha = captchaField.getText().trim();

        if (captcha.isEmpty() || !captcha.equalsIgnoreCase(captchaCode)) {
            JOptionPane.showMessageDialog(this, "验证码错误", "登录失败", JOptionPane.ERROR_MESSAGE);
            refreshCaptcha();
            return;
        }
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入用户名", "登录失败", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入密码", "登录失败", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = userDAO.findByUsernameAndPassword(username, PasswordUtil.encrypt(password));
        if (user == null) {
            JOptionPane.showMessageDialog(this, "用户名或密码错误，或账号已被禁用", "登录失败", JOptionPane.ERROR_MESSAGE);
            refreshCaptcha();
            return;
        }

        SessionManager.login(user);
        JOptionPane.showMessageDialog(this, "登录成功！欢迎 " + user.getRealName(), "提示", JOptionPane.INFORMATION_MESSAGE);

        if (SessionManager.isAdmin()) {
            new AdminFrame().setVisible(true);
        } else {
            new StudentFrame().setVisible(true);
        }
        dispose();
    }

    private void openRegister() {
        new RegisterFrame().setVisible(true);
        dispose();
    }
}
