package com.electro.sales.view;

import com.electro.sales.dao.MovieDao;
import com.electro.sales.model.Movie;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MovieFrame extends JFrame {

    private final JTable table;
    private final DefaultTableModel model;
    private final MovieDao dao = new MovieDao();

    public MovieFrame() {

        setTitle("电影列表");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());

        // ================= 表格 =================
        model = new DefaultTableModel(
                new Object[]{"ID", "名称", "时长", "价格", "类型"},
                0
        );

        table = new JTable(model);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ================= 按钮区 =================
        JPanel btnPanel = new JPanel();

        JButton refreshBtn = new JButton("刷新");
        JButton detailBtn = new JButton("查看详情");
        JButton closeBtn = new JButton("关闭");

        btnPanel.add(refreshBtn);
        btnPanel.add(detailBtn);
        btnPanel.add(closeBtn);

        add(btnPanel, BorderLayout.SOUTH);

        // ================= 事件 =================

        refreshBtn.addActionListener(e -> loadMovies());

        detailBtn.addActionListener(e -> showDetail());

        closeBtn.addActionListener(e -> dispose());

        // 初始化加载
        loadMovies();
    }

    // ================= 加载数据 =================
    private void loadMovies() {

        model.setRowCount(0);

        List<Movie> list = dao.findAll();

        for (Movie m : list) {
            model.addRow(new Object[]{
                    m.getId(),
                    m.getName(),
                    m.getDuration(),
                    m.getPrice(),
                    m.getType()
            });
        }
    }

    // ================= 查看详情 =================
    private void showDetail() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请选择一部电影");
            return;
        }

        String info =
                "ID: " + model.getValueAt(row, 0) + "\n" +
                        "名称: " + model.getValueAt(row, 1) + "\n" +
                        "时长: " + model.getValueAt(row, 2) + " 分钟\n" +
                        "价格: " + model.getValueAt(row, 3) + " 元\n" +
                        "类型: " + model.getValueAt(row, 4);

        JOptionPane.showMessageDialog(this, info);
    }
}