package com.electro.sales.view;

import com.electro.sales.dao.ShowtimeDao;
import com.electro.sales.model.Showtime;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

public class AddShowtimeFrame extends JFrame {

    private final JTextField movieIdField = new JTextField();
    private final JTextField hallField = new JTextField();
    private final JTextField timeField = new JTextField();
    // 格式：2026-06-25T19:30

    private final ShowtimeDao dao = new ShowtimeDao();

    public AddShowtimeFrame() {

        setTitle("新增场次");
        setSize(350, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());

        // ================= 表单 =================
        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));

        form.add(new JLabel("电影ID："));
        form.add(movieIdField);

        form.add(new JLabel("影厅："));
        form.add(hallField);

        form.add(new JLabel("放映时间："));
        form.add(timeField);

        add(form, BorderLayout.CENTER);

        // ================= 按钮 =================
        JPanel btnPanel = new JPanel();

        JButton addBtn = new JButton("添加");
        JButton cancelBtn = new JButton("取消");

        btnPanel.add(addBtn);
        btnPanel.add(cancelBtn);

        add(btnPanel, BorderLayout.SOUTH);

        // ================= 事件 =================

        addBtn.addActionListener(e -> addShowtime());

        cancelBtn.addActionListener(e -> dispose());
    }

    // ================= 添加场次 =================
    private void addShowtime() {

        try {
            int movieId = Integer.parseInt(movieIdField.getText());
            String hall = hallField.getText();
            String timeStr = timeField.getText();

            if (hall.isEmpty() || timeStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "信息不能为空");
                return;
            }

            LocalDateTime time = LocalDateTime.parse(timeStr);

            Showtime s = new Showtime();
            s.setMovie_id(movieId);
            s.setHall(hall);
            s.setShow_time(time);

            dao.add(s);

            JOptionPane.showMessageDialog(this, "场次添加成功");

            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "电影ID必须是数字");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "时间格式错误（例：2026-06-25T19:30）");
        }
    }
}