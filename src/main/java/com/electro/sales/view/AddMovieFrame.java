package com.electro.sales.view;

import com.electro.sales.dao.MovieDao;
import com.electro.sales.model.Movie;

import javax.swing.*;
import java.awt.*;

public class AddMovieFrame extends JFrame {

    private final JTextField nameField = new JTextField();
    private final JTextField durationField = new JTextField();
    private final JTextField priceField = new JTextField();
    private final JTextField typeField = new JTextField();

    private final MovieDao dao = new MovieDao();

    public AddMovieFrame() {

        setTitle("新增电影");
        setSize(350, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());

        // ================= 表单 =================
        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));

        form.add(new JLabel("电影名称："));
        form.add(nameField);

        form.add(new JLabel("时长（分钟）："));
        form.add(durationField);

        form.add(new JLabel("价格："));
        form.add(priceField);

        form.add(new JLabel("类型："));
        form.add(typeField);

        add(form, BorderLayout.CENTER);

        // ================= 按钮 =================
        JPanel btnPanel = new JPanel();

        JButton addBtn = new JButton("添加");
        JButton cancelBtn = new JButton("取消");

        btnPanel.add(addBtn);
        btnPanel.add(cancelBtn);

        add(btnPanel, BorderLayout.SOUTH);

        // ================= 事件 =================

        addBtn.addActionListener(e -> addMovie());

        cancelBtn.addActionListener(e -> dispose());
    }

    // ================= 添加电影 =================
    private void addMovie() {

        try {
            String name = nameField.getText();
            int duration = Integer.parseInt(durationField.getText());
            double price = Double.parseDouble(priceField.getText());
            String type = typeField.getText();

            if (name.isEmpty() || type.isEmpty()) {
                JOptionPane.showMessageDialog(this, "名称或类型不能为空");
                return;
            }

            Movie m = new Movie();
            m.setName(name);
            m.setDuration(duration);
            m.setPrice(price);
            m.setType(type);

            dao.add(m);

            JOptionPane.showMessageDialog(this, "添加成功");

            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "时长或价格格式错误");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "添加失败");
        }
    }
}