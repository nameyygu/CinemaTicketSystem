package com.electro.sales.view.admin;

import com.electro.sales.model.Movie;
import com.electro.sales.service.AdminService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class AddMovieFrame extends JFrame {

    private final JTextField nameField = new JTextField();
    private final JTextField durationField = new JTextField();
    private final JTextField priceField = new JTextField();
    private final JTextField typeField = new JTextField();
    private final JTextArea descriptionArea = new JTextArea();

    private final AdminService adminService = new AdminService();

    public AddMovieFrame() {

        setTitle("新增电影");
        setSize(420, 380);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // ================= 标题 =================
        JLabel title = new JLabel("新增电影", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        add(title, BorderLayout.NORTH);

        // ================= 表单 =================
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addFormRow(form, gbc, 0, "电影名称：", nameField);
        addFormRow(form, gbc, 1, "时长（分钟）：", durationField);
        addFormRow(form, gbc, 2, "价格：", priceField);
        addFormRow(form, gbc, 3, "类型：", typeField);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        form.add(new JLabel("简介："), gbc);

        descriptionArea.setLineWrap(true);
        descriptionArea.setRows(4);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        form.add(new JScrollPane(descriptionArea), gbc);

        add(form, BorderLayout.CENTER);

        // ================= 按钮 =================
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 12));

        JButton addBtn = new JButton("添加");
        JButton resetBtn = new JButton("重置");
        JButton cancelBtn = new JButton("取消");

        addBtn.setPreferredSize(new Dimension(80, 30));
        resetBtn.setPreferredSize(new Dimension(80, 30));
        cancelBtn.setPreferredSize(new Dimension(80, 30));

        btnPanel.add(addBtn);
        btnPanel.add(resetBtn);
        btnPanel.add(cancelBtn);

        add(btnPanel, BorderLayout.SOUTH);

        // ================= 事件 =================
        addBtn.addActionListener(e -> addMovie());
        resetBtn.addActionListener(e -> resetForm());
        cancelBtn.addActionListener(e -> dispose());
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1;
        form.add(field, gbc);
    }

    // ================= 添加电影 =================
    private void addMovie() {

        try {
            String name = nameField.getText().trim();
            String durationText = durationField.getText().trim();
            String priceText = priceField.getText().trim();
            String type = typeField.getText().trim();
            String description = descriptionArea.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "电影名称不能为空");
                return;
            }

            if (durationText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "时长不能为空");
                return;
            }

            if (priceText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "价格不能为空");
                return;
            }

            if (type.isEmpty()) {
                JOptionPane.showMessageDialog(this, "类型不能为空");
                return;
            }

            int duration = Integer.parseInt(durationText);
            BigDecimal price = new BigDecimal(priceText);

            if (duration <= 0) {
                JOptionPane.showMessageDialog(this, "电影时长必须大于0");
                return;
            }

            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "价格必须大于0");
                return;
            }

            Movie m = new Movie();
            m.setName(name);
            m.setDuration(duration);
            m.setPrice(price);
            m.setType(type);
            m.setDescription(description);
            m.setStatus(1);

            boolean success = adminService.addMovie(m);

            if (success) {
                JOptionPane.showMessageDialog(this, "添加成功");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "添加失败");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "时长或价格格式错误");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "添加失败");
        }
    }

    private void resetForm() {
        nameField.setText("");
        durationField.setText("");
        priceField.setText("");
        typeField.setText("");
        descriptionArea.setText("");
    }
}