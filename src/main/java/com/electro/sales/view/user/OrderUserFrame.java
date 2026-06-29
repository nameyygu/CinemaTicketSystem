package com.electro.sales.view.user;

import com.electro.sales.dao.OrderDao;
import com.electro.sales.model.Order;
import com.electro.sales.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class OrderUserFrame extends JFrame {

    private final JTable table;
    private final DefaultTableModel model;

    private final UserService userService = new UserService();
    private final int userId;

    public OrderUserFrame(int userId) {

        this.userId = userId;

        setTitle("我的订单");
        setSize(860, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());

        // ================= 标题 =================
        JLabel title = new JLabel("我的订单", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // ================= 表格 =================
        model = new DefaultTableModel(
                new Object[]{
                        "订单ID",
                        "订单号",
                        "场次ID",
                        "座位",
                        "总价",
                        "状态",
                        "创建时间"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(26);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ================= 按钮 =================
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 12));

        JButton refreshBtn = new JButton("刷新");
        JButton detailBtn = new JButton("查看详情");
        JButton payBtn = new JButton("支付订单");
        JButton cancelBtn = new JButton("取消订单");
        JButton closeBtn = new JButton("关闭");

        btnPanel.add(refreshBtn);
        btnPanel.add(detailBtn);
        btnPanel.add(payBtn);
        btnPanel.add(cancelBtn);
        btnPanel.add(closeBtn);

        add(btnPanel, BorderLayout.SOUTH);

        // ================= 事件 =================
        refreshBtn.addActionListener(e -> loadData());
        detailBtn.addActionListener(e -> showDetail());
        payBtn.addActionListener(e -> payOrder());
        cancelBtn.addActionListener(e -> cancelOrder());
        closeBtn.addActionListener(e -> dispose());

        loadData();
    }

    // ================= 加载数据 =================
    private void loadData() {

        model.setRowCount(0);

        List<Order> list = userService.findMyOrders(userId);

        for (Order o : list) {
            model.addRow(new Object[]{
                    o.getId(),
                    o.getOrder_no(),
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

        Order order = userService.findOrderById(orderId);

        if (order == null || order.getUser_id() != userId) {
            JOptionPane.showMessageDialog(this, "订单不存在或无权限查看");
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

    // ================= 支付订单 =================
    private void payOrder() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请选择订单");
            return;
        }

        int orderId = (int) model.getValueAt(row, 0);
        String status = String.valueOf(model.getValueAt(row, 5));

        if (!"待支付".equals(status)) {
            JOptionPane.showMessageDialog(this, "只有待支付订单可以支付");
            return;
        }

        int res = JOptionPane.showConfirmDialog(
                this,
                "确定支付该订单吗？",
                "支付确认",
                JOptionPane.YES_NO_OPTION
        );

        if (res != JOptionPane.YES_OPTION) {
            return;
        }

        boolean success = userService.payOrder(userId, orderId);

        if (success) {
            JOptionPane.showMessageDialog(this, "支付成功");
        } else {
            JOptionPane.showMessageDialog(this, "支付失败，订单可能已支付、已取消或已超时");
        }

        loadData();
    }

    // ================= 取消订单 =================
    private void cancelOrder() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请选择订单");
            return;
        }

        int orderId = (int) model.getValueAt(row, 0);
        String status = String.valueOf(model.getValueAt(row, 5));

        if (!"待支付".equals(status)) {
            JOptionPane.showMessageDialog(this, "只有待支付订单可以取消");
            return;
        }

        int res = JOptionPane.showConfirmDialog(
                this,
                "确定取消订单吗？\n取消后座位会被释放。",
                "提示",
                JOptionPane.YES_NO_OPTION
        );

        if (res != JOptionPane.YES_OPTION) {
            return;
        }

        boolean success = userService.cancelOrder(userId, orderId);

        if (success) {
            JOptionPane.showMessageDialog(this, "订单取消成功");
        } else {
            JOptionPane.showMessageDialog(this, "取消失败，订单可能已支付、已取消或被其他操作处理");
        }

        loadData();
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