package com.electro.sales.view;

import com.electro.sales.dao.OrderDao;
import com.electro.sales.model.Order;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class OrderUserFrame extends JFrame {

    private final JTable table;
    private final DefaultTableModel model;

    private final OrderDao dao = new OrderDao();
    private final int userId;

    public OrderUserFrame(int userId) {

        this.userId = userId;

        setTitle("我的订单");
        setSize(700, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());

        // ================= 表格 =================
        model = new DefaultTableModel(
                new Object[]{"订单ID", "场次ID", "座位", "总价", "状态"},
                0
        );

        table = new JTable(model);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ================= 按钮 =================
        JPanel btnPanel = new JPanel();

        JButton refreshBtn = new JButton("刷新");
        JButton detailBtn = new JButton("查看详情");
        JButton cancelBtn = new JButton("取消订单");
        JButton closeBtn = new JButton("关闭");

        btnPanel.add(refreshBtn);
        btnPanel.add(detailBtn);
        btnPanel.add(cancelBtn);
        btnPanel.add(closeBtn);

        add(btnPanel, BorderLayout.SOUTH);

        // ================= 事件 =================

        refreshBtn.addActionListener(e -> loadData());

        detailBtn.addActionListener(e -> showDetail());

        cancelBtn.addActionListener(e -> cancelOrder());

        closeBtn.addActionListener(e -> dispose());

        // 初始化加载
        loadData();
    }

    // ================= 加载数据 =================
    private void loadData() {

        model.setRowCount(0);

        List<Order> list = dao.findByUserId(userId);

        for (Order o : list) {
            model.addRow(new Object[]{
                    o.getId(),
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
                        "场次ID: " + model.getValueAt(row, 1) + "\n" +
                        "座位: " + model.getValueAt(row, 2) + "\n" +
                        "总价: " + model.getValueAt(row, 3) + "\n" +
                        "状态: " + model.getValueAt(row, 4);

        JOptionPane.showMessageDialog(this, info);
    }

    // ================= 取消订单 =================
    private void cancelOrder() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请选择订单");
            return;
        }

        int orderId = (int) model.getValueAt(row, 0);
        String status = (String) model.getValueAt(row, 4);

        if ("已支付".equals(status)) {
            JOptionPane.showMessageDialog(this, "已支付订单不能取消");
            return;
        }

        int res = JOptionPane.showConfirmDialog(
                this,
                "确定取消订单吗？",
                "提示",
                JOptionPane.YES_NO_OPTION
        );

        if (res == JOptionPane.YES_OPTION) {
            dao.delete(orderId);
            loadData();
        }
    }
}