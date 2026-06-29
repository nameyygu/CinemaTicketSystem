package com.electro.sales.view.user;

import com.electro.sales.service.UserService;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JPasswordField confirmField = new JPasswordField();

    private final UserService userService = new UserService();

    public RegisterFrame() {

        setTitle("用户注册");
        setSize(380, 260);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // ================= 标题 =================
        JLabel title = new JLabel("用户注册", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        add(title, BorderLayout.NORTH);

        // ================= 表单 =================
        JPanel form = new JPanel(new GridLayout(3, 2, 8, 12));
        form.setBorder(BorderFactory.createEmptyBorder(15, 35, 10, 35));

        JLabel usernameLabel = new JLabel("用户名：");
        JLabel passwordLabel = new JLabel("密码：");
        JLabel confirmLabel = new JLabel("确认密码：");

        usernameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        confirmLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        form.add(usernameLabel);
        form.add(usernameField);

        form.add(passwordLabel);
        form.add(passwordField);

        form.add(confirmLabel);
        form.add(confirmField);

        add(form, BorderLayout.CENTER);

        // ================= 按钮 =================
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton registerBtn = new JButton("注册");
        JButton cancelBtn = new JButton("取消");

        registerBtn.setPreferredSize(new Dimension(90, 30));
        cancelBtn.setPreferredSize(new Dimension(90, 30));

        btnPanel.add(registerBtn);
        btnPanel.add(cancelBtn);

        add(btnPanel, BorderLayout.SOUTH);

        // ================= 事件 =================
        registerBtn.addActionListener(e -> register());
        cancelBtn.addActionListener(e -> dispose());

        // 回车注册
        confirmField.addActionListener(e -> register());
    }

    // ================= 注册逻辑 =================
    private void register() {

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String confirm = new String(confirmField.getPassword()).trim();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入用户名");
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入密码");
            return;
        }

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "两次密码不一致");
            return;
        }

        boolean success = userService.register(username, password);

        if (success) {
            JOptionPane.showMessageDialog(this, "注册成功，请登录");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "注册失败，用户名可能已存在");
        }
    }
}