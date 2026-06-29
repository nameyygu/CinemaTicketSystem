package com.electro.sales.view;

import com.electro.sales.model.User;
import com.electro.sales.service.UserService;
import com.electro.sales.view.admin.AdminMainFrame;
import com.electro.sales.view.user.UserMainFrame;
import com.electro.sales.view.user.RegisterFrame;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    private final UserService userService = new UserService();

    public MainFrame() {

        setTitle("系统登录");
        setSize(350, 220);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));

        // ================= 表单区 =================
        JPanel form = new JPanel(new GridLayout(2, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        form.add(new JLabel("用户名："));
        form.add(usernameField);

        form.add(new JLabel("密码："));
        form.add(passwordField);

        add(form, BorderLayout.CENTER);

        // ================= 按钮区 =================
        JPanel btnPanel = new JPanel();

        JButton loginBtn = new JButton("登录");
        JButton registerBtn = new JButton("注册");

        btnPanel.add(loginBtn);
        btnPanel.add(registerBtn);

        add(btnPanel, BorderLayout.SOUTH);

        // ================= 事件 =================
        loginBtn.addActionListener(e -> login());

        registerBtn.addActionListener(e ->
                new RegisterFrame().setVisible(true)
        );
    }

    // ================= 登录逻辑 =================
    private void login() {

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入用户名");
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入密码");
            return;
        }

        User user = userService.login(username, password);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "用户名或密码错误");
            return;
        }

        JOptionPane.showMessageDialog(this, "登录成功");

        this.dispose();

        // ================= 角色分流 =================
        if ("admin".equals(user.getRole())) {
            new AdminMainFrame(user).setVisible(true);
        } else {
            new UserMainFrame(user).setVisible(true);
        }
    }
}