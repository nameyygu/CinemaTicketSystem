package com.electro.sales.view;

import com.electro.sales.dao.OrderDao;
import com.electro.sales.model.Order;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class OrderAdminFrame extends JFrame {

    private final JTable table;
    private final DefaultTableModel model;

    private final OrderDao dao = new OrderDao();

    public OrderAdminFrame() {

        setTitle("订单管理");
        setSize(750, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());

        // ================= 表格 =================
        model = new DefaultTableModel(
                new Object[]{"订单ID", "用户ID", "场次ID", "座位", "总价", "状态"},
                0
        );

        table = new JTable(model);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ================= 按钮区 =================
        JPanel btnPanel = new JPanel();

        JButton refreshBtn = new JButton("刷新");
        JButton detailBtn = new JButton("查看详情");
        JButton deleteBtn = new JButton("删除订单");
        JButton closeBtn = new JButton("关闭");

        btnPanel.add(refreshBtn);
        btnPanel.add(detailBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(closeBtn);

        add(btnPanel, BorderLayout.SOUTH);

        // ================= 事件 =================

        refreshBtn.addActionListener(e -> loadData());

        detailBtn.addActionListener(e -> showDetail());

        deleteBtn.addActionListener(e -> deleteOrder());

        closeBtn.addActionListener(e -> dispose());

        // 初始化加载
        loadData();
    }

    // ================= 加载数据 =================
    private void loadData() {

        model.setRowCount(0);

        List<Order> list = dao.findAll();

        for (Order o : list) {
            model.addRow(new Object[]{
                    o.getId(),
                    o.getUser_id(),
                    o.getShowtime_id(),
                    o.getSeat(),
                    o.getTotalprice(),
                    o.getStatus()
            });
        }
    }

    // ================= 查看详情 =================
    private void showDetail() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请选择订单");
            return;
        }

        String info =
                "订单ID: " + model.getValueAt(row, 0) + "\n" +
                        "用户ID: " + model.getValueAt(row, 1) + "\n" +
                        "场次ID: " + model.getValueAt(row, 2) + "\n" +
                        "座位: " + model.getValueAt(row, 3) + "\n" +
                        "总价: " + model.getValueAt(row, 4) + "\n" +
                        "状态: " + model.getValueAt(row, 5);

        JOptionPane.showMessageDialog(this, info);
    }

    // ================= 删除订单 =================
    private void deleteOrder() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请选择订单");
            return;
        }

        int id = (int) model.getValueAt(row, 0);

        int res = JOptionPane.showConfirmDialog(
                this,
                "确定删除该订单吗？",
                "提示",
                JOptionPane.YES_NO_OPTION
        );

        if (res == JOptionPane.YES_OPTION) {
            dao.delete(id);
            loadData();
        }
    }
}