package com.electro.sales.view;

import com.electro.sales.dao.UserDao;
import com.electro.sales.model.User;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JPasswordField confirmField = new JPasswordField();

    private final UserDao userDao = new UserDao();

    public RegisterFrame() {

        setTitle("用户注册");
        setSize(350, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout(10, 10));

        // ================= 表单 =================
        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));

        form.add(new JLabel("用户名："));
        form.add(usernameField);

        form.add(new JLabel("密码："));
        form.add(passwordField);

        form.add(new JLabel("确认密码："));
        form.add(confirmField);

        add(form, BorderLayout.CENTER);

        // ================= 按钮 =================
        JPanel btnPanel = new JPanel();

        JButton registerBtn = new JButton("注册");
        JButton cancelBtn = new JButton("取消");

        btnPanel.add(registerBtn);
        btnPanel.add(cancelBtn);

        add(btnPanel, BorderLayout.SOUTH);

        // ================= 事件 =================

        registerBtn.addActionListener(e -> register());

        cancelBtn.addActionListener(e -> dispose());
    }

    // ================= 注册逻辑 =================
    private void register() {

        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名或密码不能为空");
            return;
        }

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "两次密码不一致");
            return;
        }

        // ================= 封装用户 =================
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("user"); // 默认普通用户

        boolean success = userDao.register(user);

        if (success) {
            JOptionPane.showMessageDialog(this, "注册成功");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "注册失败（用户名可能已存在）");
        }
    }
}