package com.electro.sales.view;

import com.electro.sales.dao.ShowtimeDao;
import com.electro.sales.model.Showtime;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ShowtimeFrame extends JFrame {

    private final JTable table;
    private final DefaultTableModel model;
    private final ShowtimeDao dao = new ShowtimeDao();

    public ShowtimeFrame() {

        setTitle("场次列表");
        setSize(650, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());

        // ================= 表格 =================
        model = new DefaultTableModel(
                new Object[]{"ID", "电影ID", "影厅", "放映时间"},
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

        refreshBtn.addActionListener(e -> loadData());

        detailBtn.addActionListener(e -> showDetail());

        closeBtn.addActionListener(e -> dispose());

        // 初始化加载
        loadData();
    }

    // ================= 加载数据 =================
    private void loadData() {

        model.setRowCount(0);

        List<Showtime> list = dao.findAll();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Showtime s : list) {
            model.addRow(new Object[]{
                    s.getId(),
                    s.getMovie_id(),
                    s.getHall(),
                    s.getShow_time() == null ? "" : s.getShow_time().format(fmt)
            });
        }
    }

    // ================= 查看详情 =================
    private void showDetail() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请选择一条场次");
            return;
        }

        String info =
                "场次ID: " + model.getValueAt(row, 0) + "\n" +
                        "电影ID: " + model.getValueAt(row, 1) + "\n" +
                        "影厅: " + model.getValueAt(row, 2) + "\n" +
                        "时间: " + model.getValueAt(row, 3);

        JOptionPane.showMessageDialog(this, info);
    }
}