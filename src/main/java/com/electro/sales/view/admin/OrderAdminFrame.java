package com.electro.sales.view.admin;

import com.electro.sales.dao.OrderDao;
import com.electro.sales.model.Order;
import com.electro.sales.service.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class OrderAdminFrame extends JFrame {

    private final JTable table;
    private final DefaultTableModel model;

    private final AdminService adminService = new AdminService();

    public OrderAdminFrame() {

        setTitle("订单管理");
        setSize(850, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());

        // ================= 表格 =================
        model = new DefaultTableModel(
                new Object[]{
                        "订单ID",
                        "订单号",
                        "用户ID",
                        "场次ID",
                        "座位",
                        "总价",
                        "状态",
                        "创建时间"
                },
                0
        ) {
            // 禁止编辑表格
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);
        table.getTableHeader().setReorderingAllowed(false);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ================= 按钮区 =================
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

        List<Order> list = adminService.findAllOrders();

        for (Order o : list) {
            model.addRow(new Object[]{
                    o.getId(),
                    o.getOrder_no(),
                    o.getUser_id(),
                    o.getShowtime_id(),
                    o.getSeat(),
                    o.getTotal_price(),
                    statusText(o.getStatus()),
                    o.getCreate_time()
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

        int orderId = (int) model.getValueAt(row, 0);

        Order order = adminService.findOrderById(orderId);

        if (order == null) {
            JOptionPane.showMessageDialog(this, "订单不存在，可能已被删除或更新");
            loadData();
            return;
        }

        String info =
                "订单ID: " + order.getId() + "\n" +
                        "订单号: " + order.getOrder_no() + "\n" +
                        "用户ID: " + order.getUser_id() + "\n" +
                        "场次ID: " + order.getShowtime_id() + "\n" +
                        "座位: " + order.getSeat() + "\n" +
                        "总价: " + order.getTotal_price() + "\n" +
                        "状态: " + statusText(order.getStatus()) + "\n" +
                        "过期时间: " + order.getExpire_time() + "\n" +
                        "支付时间: " + order.getPay_time() + "\n" +
                        "创建时间: " + order.getCreate_time() + "\n" +
                        "更新时间: " + order.getUpdate_time();

        JOptionPane.showMessageDialog(this, info);
    }

    // ================= 管理员取消订单 =================
    private void cancelOrder() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请选择订单");
            return;
        }

        int orderId = (int) model.getValueAt(row, 0);
        String status = String.valueOf(model.getValueAt(row, 6));

        if (!"待支付".equals(status)) {
            JOptionPane.showMessageDialog(this, "只有待支付订单可以取消");
            return;
        }

        int res = JOptionPane.showConfirmDialog(
                this,
                "确定取消该订单吗？\n取消后会释放座位并恢复剩余票数。",
                "提示",
                JOptionPane.YES_NO_OPTION
        );

        if (res != JOptionPane.YES_OPTION) {
            return;
        }

        boolean success = adminService.adminCancelOrder(orderId);

        if (success) {
            JOptionPane.showMessageDialog(this, "订单取消成功");
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "订单取消失败，订单可能已支付、已取消或已被其他操作处理");
            loadData();
        }
    }

    // ================= 状态转换 =================
    private String statusText(int status) {
        switch (status) {
            case OrderDao.STATUS_WAIT_PAY:
                return "待支付";
            case OrderDao.STATUS_PAID:
                return "已支付";
            case OrderDao.STATUS_CANCEL:
                return "已取消";
            case OrderDao.STATUS_REFUND:
                return "已退款";
            default:
                return "未知";
        }
    }
}