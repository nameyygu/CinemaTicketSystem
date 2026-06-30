package com.electro.sales.view.admin;

import com.electro.sales.model.Showtime;
import com.electro.sales.service.AdminService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UpdateShowtimeFrame extends JFrame {

    private final JTextField idField = new JTextField();
    private final JTextField movieIdField = new JTextField();
    private final JTextField hallField = new JTextField();
    private final JTextField showTimeField = new JTextField();
    private final JTextField priceField = new JTextField();
    private final JTextField totalSeatField = new JTextField();
    private final JTextField availableSeatField = new JTextField();
    private final JComboBox<String> statusBox = new JComboBox<>(new String[]{"正常", "取消"});

    private final AdminService adminService = new AdminService();

    private Showtime currentShowtime;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public UpdateShowtimeFrame() {
        setTitle("修改场次信息");
        setSize(480, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
    }

    public UpdateShowtimeFrame(int showtimeId) {
        this();
        idField.setText(String.valueOf(showtimeId));
        loadShowtime();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("修改场次信息", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        add(title, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        JPanel idPanel = new JPanel(new BorderLayout(8, 8));
        JButton loadBtn = new JButton("加载");

        idPanel.add(new JLabel("场次ID："), BorderLayout.WEST);
        idPanel.add(idField, BorderLayout.CENTER);
        idPanel.add(loadBtn, BorderLayout.EAST);

        mainPanel.add(idPanel, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(7, 2, 8, 10));

        form.add(new JLabel("电影ID："));
        form.add(movieIdField);

        form.add(new JLabel("影厅："));
        form.add(hallField);

        form.add(new JLabel("放映时间："));
        form.add(showTimeField);

        form.add(new JLabel("票价："));
        form.add(priceField);

        form.add(new JLabel("总座位："));
        form.add(totalSeatField);

        form.add(new JLabel("剩余座位："));
        form.add(availableSeatField);

        form.add(new JLabel("状态："));
        form.add(statusBox);

        mainPanel.add(form, BorderLayout.CENTER);

        JLabel tips = new JLabel("时间格式：yyyy-MM-dd HH:mm，例如 2026-01-01 19:30");
        tips.setForeground(Color.GRAY);
        tips.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(tips, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 12));

        JButton saveBtn = new JButton("保存修改");
        JButton resetBtn = new JButton("重置");
        JButton closeBtn = new JButton("关闭");

        btnPanel.add(saveBtn);
        btnPanel.add(resetBtn);
        btnPanel.add(closeBtn);

        add(btnPanel, BorderLayout.SOUTH);

        loadBtn.addActionListener(e -> loadShowtime());
        saveBtn.addActionListener(e -> saveShowtime());
        resetBtn.addActionListener(e -> fillForm());
        closeBtn.addActionListener(e -> dispose());
    }

    private void loadShowtime() {
        try {
            int showtimeId = Integer.parseInt(idField.getText().trim());

            currentShowtime = adminService.findShowtimeById(showtimeId);

            if (currentShowtime == null) {
                JOptionPane.showMessageDialog(this, "场次不存在");
                clearForm();
                return;
            }

            fillForm();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "场次ID必须是数字");
        }
    }

    private void fillForm() {
        if (currentShowtime == null) {
            return;
        }

        movieIdField.setText(String.valueOf(currentShowtime.getMovie_id()));
        hallField.setText(currentShowtime.getHall());

        if (currentShowtime.getShow_time() != null) {
            showTimeField.setText(currentShowtime.getShow_time().format(formatter));
        } else {
            showTimeField.setText("");
        }

        priceField.setText(String.valueOf(currentShowtime.getPrice()));
        totalSeatField.setText(String.valueOf(currentShowtime.getTotal_seat()));
        availableSeatField.setText(String.valueOf(currentShowtime.getAvailable_seat()));

        statusBox.setSelectedIndex(currentShowtime.getStatus() == 1 ? 0 : 1);
    }

    private void clearForm() {
        movieIdField.setText("");
        hallField.setText("");
        showTimeField.setText("");
        priceField.setText("");
        totalSeatField.setText("");
        availableSeatField.setText("");
        statusBox.setSelectedIndex(0);
        currentShowtime = null;
    }

    private void saveShowtime() {
        try {
            if (currentShowtime == null) {
                JOptionPane.showMessageDialog(this, "请先加载场次信息");
                return;
            }

            int movieId = Integer.parseInt(movieIdField.getText().trim());
            String hall = hallField.getText().trim();
            LocalDateTime showTime = LocalDateTime.parse(showTimeField.getText().trim(), formatter);
            BigDecimal price = new BigDecimal(priceField.getText().trim());

            int totalSeat = Integer.parseInt(totalSeatField.getText().trim());
            int availableSeat = Integer.parseInt(availableSeatField.getText().trim());

            if (movieId <= 0) {
                JOptionPane.showMessageDialog(this, "电影ID必须大于0");
                return;
            }

            if (hall.isEmpty()) {
                JOptionPane.showMessageDialog(this, "影厅不能为空");
                return;
            }

            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "票价必须大于0");
                return;
            }

            if (totalSeat <= 0) {
                JOptionPane.showMessageDialog(this, "总座位数必须大于0");
                return;
            }

            if (availableSeat < 0 || availableSeat > totalSeat) {
                JOptionPane.showMessageDialog(this, "剩余座位数不合法");
                return;
            }

            Showtime s = new Showtime();

            s.setId(currentShowtime.getId());
            s.setMovie_id(movieId);
            s.setHall(hall);
            s.setShow_time(showTime);
            s.setPrice(price);
            s.setTotal_seat(totalSeat);
            s.setAvailable_seat(availableSeat);

            s.setStatus(statusBox.getSelectedIndex() == 0 ? 1 : 0);

            // 关键：带上原 version，用于乐观锁
            s.setVersion(currentShowtime.getVersion());

            boolean success = adminService.updateShowtime(s);

            if (success) {
                JOptionPane.showMessageDialog(this, "修改成功");
                currentShowtime = adminService.findShowtimeById(s.getId());
                fillForm();
            } else {
                JOptionPane.showMessageDialog(this, "修改失败，数据可能已被其他管理员修改，请刷新后重试");
                currentShowtime = adminService.findShowtimeById(s.getId());
                fillForm();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "电影ID、座位数或价格格式错误");
        } catch (java.time.format.DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "时间格式错误，请使用 yyyy-MM-dd HH:mm");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "修改失败");
        }
    }
}