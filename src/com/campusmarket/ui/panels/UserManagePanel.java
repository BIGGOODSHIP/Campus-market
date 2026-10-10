package com.campusmarket.ui.panels;

import com.campusmarket.dao.UserDAO;
import com.campusmarket.model.User;
import com.campusmarket.util.PasswordUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * 用户管理面板（管理员）
 * 提供用户搜索、新增、启用/禁用、重置密码、删除等功能
 */
public class UserManagePanel extends JPanel {

    private final UserDAO userDAO = new UserDAO();

    private DefaultTableModel tableModel;
    private JTable userTable;
    private JTextField keywordField;
    private JComboBox<String> roleCombo;
    private JComboBox<String> statusCombo;

    public UserManagePanel() {
        setLayout(new BorderLayout());
        initComponents();
        refreshTable();
    }

    // ==================== 初始化组件 ====================

    private void initComponents() {
        // top panel — search + actions
        add(buildTopPanel(), BorderLayout.NORTH);

        // center — table
        add(buildTablePanel(), BorderLayout.CENTER);

        // bottom — action buttons
        add(buildBottomPanel(), BorderLayout.SOUTH);
    }

    /**
     * 构建顶部搜索栏
     */
    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        panel.add(new JLabel("关键词："));
        keywordField = new JTextField(12);
        panel.add(keywordField);

        panel.add(new JLabel("角色："));
        roleCombo = new JComboBox<>(new String[]{"全部", "学生", "管理员"});
        panel.add(roleCombo);

        panel.add(new JLabel("状态："));
        statusCombo = new JComboBox<>(new String[]{"全部", "正常", "禁用"});
        panel.add(statusCombo);

        JButton searchBtn = new JButton("搜索");
        searchBtn.addActionListener(e -> doSearch());
        panel.add(searchBtn);

        JButton resetBtn = new JButton("重置");
        resetBtn.addActionListener(e -> doReset());
        panel.add(resetBtn);

        panel.add(Box.createHorizontalStrut(20));

        JButton addUserBtn = new JButton("添加用户");
        addUserBtn.addActionListener(e -> showAddUserDialog());
        panel.add(addUserBtn);

        return panel;
    }

    /**
     * 构建中央表格
     */
    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"ID", "用户名", "姓名", "手机", "角色", "状态", "创建时间"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setRowHeight(24);
        userTable.getTableHeader().setReorderingAllowed(false);

        // 设置列宽
        userTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        userTable.getColumnModel().getColumn(1).setPreferredWidth(100);  // 用户名
        userTable.getColumnModel().getColumn(2).setPreferredWidth(80);   // 姓名
        userTable.getColumnModel().getColumn(3).setPreferredWidth(120);  // 手机
        userTable.getColumnModel().getColumn(4).setPreferredWidth(60);   // 角色
        userTable.getColumnModel().getColumn(5).setPreferredWidth(60);   // 状态
        userTable.getColumnModel().getColumn(6).setPreferredWidth(160);  // 创建时间

        JScrollPane scrollPane = new JScrollPane(userTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 右键菜单
        JPopupMenu popupMenu = buildPopupMenu();
        userTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            private void showPopup(MouseEvent e) {
                int row = userTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    userTable.setRowSelectionInterval(row, row);
                    popupMenu.show(userTable, e.getX(), e.getY());
                }
            }
        });

        return panel;
    }

    /**
     * 构建右键弹出菜单
     */
    private JPopupMenu buildPopupMenu() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem enableItem = new JMenuItem("启用");
        enableItem.addActionListener(e -> doEnableSelected());
        menu.add(enableItem);

        JMenuItem disableItem = new JMenuItem("禁用");
        disableItem.addActionListener(e -> doDisableSelected());
        menu.add(disableItem);

        menu.addSeparator();

        JMenuItem resetPwdItem = new JMenuItem("重置密码");
        resetPwdItem.addActionListener(e -> doResetPasswordSelected());
        menu.add(resetPwdItem);

        menu.addSeparator();

        JMenuItem deleteItem = new JMenuItem("删除选中用户");
        deleteItem.addActionListener(e -> doDeleteSelected());
        menu.add(deleteItem);

        return menu;
    }

    /**
     * 构建底部操作按钮栏
     */
    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JButton enableBtn = new JButton("启用");
        enableBtn.addActionListener(e -> doEnableSelected());
        panel.add(enableBtn);

        JButton disableBtn = new JButton("禁用");
        disableBtn.addActionListener(e -> doDisableSelected());
        panel.add(disableBtn);

        JButton resetPwdBtn = new JButton("重置密码");
        resetPwdBtn.addActionListener(e -> doResetPasswordSelected());
        panel.add(resetPwdBtn);

        JButton deleteBtn = new JButton("删除选中用户");
        deleteBtn.setForeground(Color.RED);
        deleteBtn.addActionListener(e -> doDeleteSelected());
        panel.add(deleteBtn);

        return panel;
    }

    // ==================== 表格刷新 ====================

    /**
     * 刷新表格数据（显示全部用户）
     */
    private void refreshTable() {
        tableModel.setRowCount(0);
        List<User> users = userDAO.findAll();
        for (User u : users) {
            tableModel.addRow(new Object[]{
                    u.getUserId(),
                    u.getUsername(),
                    u.getRealName(),
                    u.getPhone(),
                    u.getRoleName(),
                    u.getStatusName(),
                    u.getCreateTime()
            });
        }
    }

    // ==================== 搜索与重置 ====================

    /**
     * 执行条件搜索
     */
    private void doSearch() {
        String keyword = keywordField.getText().trim();
        Integer role = comboValueToInt(roleCombo.getSelectedIndex());
        Integer status = comboValueToInt(statusCombo.getSelectedIndex());

        List<User> users = userDAO.search(keyword, role, status);

        tableModel.setRowCount(0);
        for (User u : users) {
            tableModel.addRow(new Object[]{
                    u.getUserId(),
                    u.getUsername(),
                    u.getRealName(),
                    u.getPhone(),
                    u.getRoleName(),
                    u.getStatusName(),
                    u.getCreateTime()
            });
        }
    }

    /**
     * 重置搜索条件，重新加载全部数据
     */
    private void doReset() {
        keywordField.setText("");
        roleCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);
        refreshTable();
    }

    /**
     * 将 ComboBox 索引转为查询参数
     * 0 → -1（全部），其余 → 实际值
     */
    private Integer comboValueToInt(int index) {
        return index <= 0 ? -1 : index - 1;
    }

    // ==================== 获取选中用户ID ====================

    /**
     * 获取当前选中行的用户ID
     */
    private int getSelectedUserId() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一条用户记录", "提示", JOptionPane.WARNING_MESSAGE);
            return -1;
        }
        return (int) tableModel.getValueAt(row, 0);
    }

    // ==================== 操作逻辑 ====================

    /**
     * 启用选中用户
     */
    private void doEnableSelected() {
        int userId = getSelectedUserId();
        if (userId < 0) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要启用该用户吗？", "确认操作", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = userDAO.updateStatus(userId, 0);
        if (ok) {
            JOptionPane.showMessageDialog(this, "用户已启用", "成功", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "操作失败，请稍后重试", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 禁用选中用户
     */
    private void doDisableSelected() {
        int userId = getSelectedUserId();
        if (userId < 0) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要禁用该用户吗？", "确认操作", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = userDAO.updateStatus(userId, 1);
        if (ok) {
            JOptionPane.showMessageDialog(this, "用户已禁用", "成功", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "操作失败，请稍后重试", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 重置选中用户的密码
     */
    private void doResetPasswordSelected() {
        int userId = getSelectedUserId();
        if (userId < 0) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要将该用户密码重置为默认密码（123456）吗？", "确认操作", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = userDAO.updatePassword(userId, PasswordUtil.encrypt("123456"));
        if (ok) {
            JOptionPane.showMessageDialog(this, "密码已重置为 123456", "成功", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "操作失败，请稍后重试", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 删除选中用户
     */
    private void doDeleteSelected() {
        int userId = getSelectedUserId();
        if (userId < 0) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除该用户吗？此操作不可恢复！", "确认删除", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = userDAO.delete(userId);
        if (ok) {
            JOptionPane.showMessageDialog(this, "用户已删除", "成功", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "删除失败，请稍后重试", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== 添加用户对话框 ====================

    /**
     * 显示添加用户对话框
     */
    private void showAddUserDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "添加用户", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(380, 280);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 用户名
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("用户名：", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        JTextField usernameField = new JTextField(15);
        panel.add(usernameField, gbc);

        // 真实姓名
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("真实姓名：", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        JTextField realNameField = new JTextField(15);
        panel.add(realNameField, gbc);

        // 手机号
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("手机号：", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        JTextField phoneField = new JTextField(15);
        panel.add(phoneField, gbc);

        // 角色
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("角色：", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        JComboBox<String> roleField = new JComboBox<>(new String[]{"学生", "管理员"});
        panel.add(roleField, gbc);

        // 按钮
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 6, 6, 6);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));

        JButton okBtn = new JButton("确定");
        okBtn.setPreferredSize(new Dimension(90, 30));

        JButton cancelBtn = new JButton("取消");
        cancelBtn.setPreferredSize(new Dimension(90, 30));

        okBtn.addActionListener((ActionEvent e) -> {
            String username = usernameField.getText().trim();
            String realName = realNameField.getText().trim();
            String phone = phoneField.getText().trim();
            int role = roleField.getSelectedIndex(); // 0=学生, 1=管理员

            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "用户名不能为空", "添加失败", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (realName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "真实姓名不能为空", "添加失败", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (userDAO.findByUsername(username) != null) {
                JOptionPane.showMessageDialog(dialog, "用户名已存在", "添加失败", JOptionPane.ERROR_MESSAGE);
                return;
            }
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(PasswordUtil.encrypt("123456"));
            newUser.setRealName(realName);
            newUser.setPhone(phone);
            newUser.setRole(role);
            newUser.setStatus(0);

            int id = userDAO.insert(newUser);
            if (id > 0) {
                JOptionPane.showMessageDialog(dialog, "用户添加成功，默认密码：123456", "成功", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(dialog, "添加失败，请稍后重试", "添加失败", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);
        panel.add(btnPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }
}
