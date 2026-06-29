package com.electro.sales.view.admin;

import com.electro.sales.model.Showtime;
import com.electro.sales.service.AdminService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AddShowtimeFrame extends JFrame {

    private final JTextField movieIdField = new JTextField();
    private final JTextField hallField = new JTextField();
    private final JTextField showTimeField = new JTextField();
    private final JTextField priceField = new JTextField();
    private final JTextField totalSeatsField = new JTextField("100");

    private final AdminService adminService = new AdminService();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AddShowtimeFrame() {

        setTitle("新增场次");
        setSize(430, 330);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("新增场次", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 12));
        form.setBorder(BorderFactory.createEmptyBorder(15, 35, 10, 35));

        form.add(new JLabel("电影ID："));
        form.add(movieIdField);

        form.add(new JLabel("影厅："));
        form.add(hallField);

        form.add(new JLabel("放映时间："));
        form.add(showTimeField);

        form.add(new JLabel("票价："));
        form.add(priceField);

        form.add(new JLabel("总座位数："));
        form.add(totalSeatsField);

        add(form, BorderLayout.CENTER);

        JLabel tip = new JLabel("时间格式：yyyy-MM-dd HH:mm，例如 2026-07-01 19:30", SwingConstants.CENTER);
        tip.setForeground(Color.GRAY);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(tip, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton addBtn = new JButton("添加");
        JButton resetBtn = new JButton("重置");
        JButton cancelBtn = new JButton("取消");

        btnPanel.add(addBtn);
        btnPanel.add(resetBtn);
        btnPanel.add(cancelBtn);

        southPanel.add(btnPanel, BorderLayout.CENTER);

        add(southPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> addShowtime());
        resetBtn.addActionListener(e -> resetForm());
        cancelBtn.addActionListener(e -> dispose());
    }

    private void addShowtime() {
        try {
            String movieIdText = movieIdField.getText().trim();
            String hall = hallField.getText().trim();
            String showTimeText = showTimeField.getText().trim();
            String priceText = priceField.getText().trim();
            String totalSeatsText = totalSeatsField.getText().trim();

            if (movieIdText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入电影ID");
                return;
            }

            if (hall.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入影厅");
                return;
            }

            if (showTimeText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入放映时间");
                return;
            }

            if (priceText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入票价");
                return;
            }

            int movieId = Integer.parseInt(movieIdText);
            LocalDateTime showTime = LocalDateTime.parse(showTimeText, formatter);
            BigDecimal price = new BigDecimal(priceText);

            int totalSeats = 100;
            if (!totalSeatsText.isEmpty()) {
                totalSeats = Integer.parseInt(totalSeatsText);
            }

            if (movieId <= 0) {
                JOptionPane.showMessageDialog(this, "电影ID必须大于0");
                return;
            }

            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "票价必须大于0");
                return;
            }

            if (totalSeats <= 0) {
                JOptionPane.showMessageDialog(this, "总座位数必须大于0");
                return;
            }

            Showtime s = new Showtime();
            s.setMovie_id(movieId);
            s.setHall(hall);
            s.setShow_time(showTime);
            s.setPrice(price);
            s.setTotal_seat(totalSeats);
            s.setAvailable_seat(totalSeats);
            s.setStatus(1);

            boolean success = adminService.addShowtimeWithSeats(s);

            if (success) {
                JOptionPane.showMessageDialog(this, "场次添加成功，座位已自动生成");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "场次添加失败，请检查电影ID是否存在");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "电影ID、票价或座位数格式错误");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "添加失败，时间格式应为：yyyy-MM-dd HH:mm");
        }
    }

    private void resetForm() {
        movieIdField.setText("");
        hallField.setText("");
        showTimeField.setText("");
        priceField.setText("");
        totalSeatsField.setText("100");
    }
}