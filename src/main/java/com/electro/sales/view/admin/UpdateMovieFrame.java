package com.electro.sales.view.admin;

import com.electro.sales.model.Movie;
import com.electro.sales.service.AdminService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class UpdateMovieFrame extends JFrame {

    private final JTextField idField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField durationField = new JTextField();
    private final JTextField priceField = new JTextField();
    private final JTextField typeField = new JTextField();
    private final JTextArea descriptionArea = new JTextArea();
    private final JComboBox<String> statusBox = new JComboBox<>(new String[]{"上映", "下架"});

    private final AdminService adminService = new AdminService();

    private Movie currentMovie;

    public UpdateMovieFrame() {
        setTitle("修改电影信息");
        setSize(460, 430);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
    }

    public UpdateMovieFrame(int movieId) {
        this();
        idField.setText(String.valueOf(movieId));
        loadMovie();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("修改电影信息", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        add(title, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        JPanel idPanel = new JPanel(new BorderLayout(8, 8));
        JButton loadBtn = new JButton("加载");

        idPanel.add(new JLabel("电影ID："), BorderLayout.WEST);
        idPanel.add(idField, BorderLayout.CENTER);
        idPanel.add(loadBtn, BorderLayout.EAST);

        mainPanel.add(idPanel, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, gbc, 0, "电影名称：", nameField);
        addRow(form, gbc, 1, "时长分钟：", durationField);
        addRow(form, gbc, 2, "价格：", priceField);
        addRow(form, gbc, 3, "类型：", typeField);
        addRow(form, gbc, 4, "状态：", statusBox);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(new JLabel("简介："), gbc);

        descriptionArea.setRows(4);
        descriptionArea.setLineWrap(true);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        form.add(new JScrollPane(descriptionArea), gbc);

        mainPanel.add(form, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 12));

        JButton saveBtn = new JButton("保存修改");
        JButton resetBtn = new JButton("重置");
        JButton closeBtn = new JButton("关闭");

        btnPanel.add(saveBtn);
        btnPanel.add(resetBtn);
        btnPanel.add(closeBtn);

        add(btnPanel, BorderLayout.SOUTH);

        loadBtn.addActionListener(e -> loadMovie());
        saveBtn.addActionListener(e -> saveMovie());
        resetBtn.addActionListener(e -> fillForm());
        closeBtn.addActionListener(e -> dispose());
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }

    private void loadMovie() {
        try {
            int movieId = Integer.parseInt(idField.getText().trim());

            currentMovie = adminService.findMovieById(movieId);

            if (currentMovie == null) {
                JOptionPane.showMessageDialog(this, "电影不存在");
                clearForm();
                return;
            }

            fillForm();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "电影ID必须是数字");
        }
    }

    private void fillForm() {
        if (currentMovie == null) {
            return;
        }

        nameField.setText(currentMovie.getName());
        durationField.setText(String.valueOf(currentMovie.getDuration()));
        priceField.setText(String.valueOf(currentMovie.getPrice()));
        typeField.setText(currentMovie.getType() == null ? "" : currentMovie.getType());
        descriptionArea.setText(currentMovie.getDescription() == null ? "" : currentMovie.getDescription());
        statusBox.setSelectedIndex(currentMovie.getStatus() == 1 ? 0 : 1);
    }

    private void clearForm() {
        nameField.setText("");
        durationField.setText("");
        priceField.setText("");
        typeField.setText("");
        descriptionArea.setText("");
        statusBox.setSelectedIndex(0);
        currentMovie = null;
    }

    private void saveMovie() {
        try {
            if (currentMovie == null) {
                JOptionPane.showMessageDialog(this, "请先加载电影信息");
                return;
            }

            String name = nameField.getText().trim();
            String durationText = durationField.getText().trim();
            String priceText = priceField.getText().trim();
            String type = typeField.getText().trim();
            String description = descriptionArea.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "电影名称不能为空");
                return;
            }

            int duration = Integer.parseInt(durationText);
            BigDecimal price = new BigDecimal(priceText);

            if (duration <= 0) {
                JOptionPane.showMessageDialog(this, "时长必须大于0");
                return;
            }

            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "价格必须大于0");
                return;
            }

            Movie m = new Movie();
            m.setId(currentMovie.getId());
            m.setName(name);
            m.setDuration(duration);
            m.setPrice(price);
            m.setType(type);
            m.setDescription(description);
            m.setStatus(statusBox.getSelectedIndex() == 0 ? 1 : 0);

            // 关键：带上原 version，用于乐观锁
            m.setVersion(currentMovie.getVersion());

            boolean success = adminService.updateMovie(m);

            if (success) {
                JOptionPane.showMessageDialog(this, "修改成功");
                currentMovie = adminService.findMovieById(m.getId());
                fillForm();
            } else {
                JOptionPane.showMessageDialog(this, "修改失败，数据可能已被其他管理员修改，请刷新后重试");
                currentMovie = adminService.findMovieById(m.getId());
                fillForm();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "时长或价格格式错误");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "修改失败");
        }
    }
}