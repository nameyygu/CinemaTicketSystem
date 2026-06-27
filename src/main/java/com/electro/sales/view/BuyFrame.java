package com.electro.sales.view;

import com.electro.sales.dao.OrderDao;
import com.electro.sales.model.Order;
import com.electro.sales.model.User;

import javax.swing.*;
import java.awt.*;

public class BuyFrame extends JFrame {

    private final JTextField showtimeIdField = new JTextField();
    private final JTextField seatField = new JTextField();
    private final JTextField quantityField = new JTextField("1");
    private final JTextField priceField = new JTextField();

    private final User user;
    private final OrderDao orderDao = new OrderDao();

    public BuyFrame(User user) {

        this.user = user;

        setTitle("电影购票");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());

        // ================= 表单 =================
        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));

        form.add(new JLabel("场次ID："));
        form.add(showtimeIdField);

        form.add(new JLabel("座位（如 A1）："));
        form.add(seatField);

        form.add(new JLabel("数量："));
        form.add(quantityField);

        form.add(new JLabel("总价："));
        form.add(priceField);

        add(form, BorderLayout.CENTER);

        // ================= 按钮 =================
        JPanel btnPanel = new JPanel();

        JButton calcBtn = new JButton("计算价格");
        JButton buyBtn = new JButton("下单");
        JButton closeBtn = new JButton("关闭");

        btnPanel.add(calcBtn);
        btnPanel.add(buyBtn);
        btnPanel.add(closeBtn);

        add(btnPanel, BorderLayout.SOUTH);

        // ================= 事件 =================

        calcBtn.addActionListener(e -> calcPrice());

        buyBtn.addActionListener(e -> createOrder());

        closeBtn.addActionListener(e -> dispose());
    }

    // ================= 计算价格（简化版） =================
    private void calcPrice() {
        try {
            int quantity = Integer.parseInt(quantityField.getText());
            double pricePerTicket = 30.0; // 你可以后面改成查movie/showtime价格

            double total = quantity * pricePerTicket;

            priceField.setText(String.valueOf(total));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "数量输入错误");
        }
    }

    // ================= 下单 =================
    private void createOrder() {

        try {
            Order o = new Order();

            o.setUser_id(user.getId());
            o.setShowtime_id(Integer.parseInt(showtimeIdField.getText()));
            o.setSeat(seatField.getText());
            o.setTotalprice(Double.parseDouble(priceField.getText()));
            o.setStatus("待支付");

            orderDao.add(o);

            JOptionPane.showMessageDialog(this, "下单成功");
            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "下单失败，请检查输入");
        }
    }
}