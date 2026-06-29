package com.electro.sales.view.user;

import com.electro.sales.model.Showtime;
import com.electro.sales.model.ShowtimeSeat;
import com.electro.sales.model.User;
import com.electro.sales.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BuyFrame extends JFrame {

    private final JTextField showtimeIdField = new JTextField();
    private final JTextField seatField = new JTextField();

    private final JLabel priceLabel = new JLabel("请先输入场次ID查询");
    private final JLabel availableLabel = new JLabel("-");
    private final JTextArea seatArea = new JTextArea();

    private final User user;
    private final UserService userService = new UserService();

    public BuyFrame(User user) {

        this.user = user;

        setTitle("电影购票");
        setSize(560, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // ================= 标题 =================
        JLabel title = new JLabel("购买电影票", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        add(title, BorderLayout.NORTH);

        // ================= 表单区域 =================
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 10));

        form.add(new JLabel("场次ID："));
        form.add(showtimeIdField);

        form.add(new JLabel("座位号："));
        form.add(seatField);

        form.add(new JLabel("票价："));
        form.add(priceLabel);

        form.add(new JLabel("剩余座位："));
        form.add(availableLabel);

        mainPanel.add(form, BorderLayout.NORTH);

        // ================= 座位显示 =================
        seatArea.setEditable(false);
        seatArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        seatArea.setLineWrap(true);
        seatArea.setText("输入场次ID后点击【查询场次/座位】查看座位状态。\n\n状态说明：\n可售：A1\n锁定：A1[锁]\n已售：A1[售]");

        JScrollPane scrollPane = new JScrollPane(seatArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("座位状态"));

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // ================= 按钮 =================
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 12));

        JButton queryBtn = new JButton("查询场次/座位");
        JButton buyBtn = new JButton("下单");
        JButton closeBtn = new JButton("关闭");

        btnPanel.add(queryBtn);
        btnPanel.add(buyBtn);
        btnPanel.add(closeBtn);

        add(btnPanel, BorderLayout.SOUTH);

        // ================= 事件 =================
        queryBtn.addActionListener(e -> queryShowtimeAndSeats());
        buyBtn.addActionListener(e -> createOrder());
        closeBtn.addActionListener(e -> dispose());
    }

    // ================= 查询场次和座位 =================
    private void queryShowtimeAndSeats() {
        try {
            int showtimeId = Integer.parseInt(showtimeIdField.getText().trim());

            Showtime showtime = userService.findShowtimeById(showtimeId);

            if (showtime == null) {
                JOptionPane.showMessageDialog(this, "场次不存在");
                return;
            }

            if (showtime.getStatus() != 1) {
                JOptionPane.showMessageDialog(this, "该场次已取消或不可购买");
                return;
            }

            priceLabel.setText(String.valueOf(showtime.getPrice()));
            availableLabel.setText(showtime.getAvailable_seat() + " / " + showtime.getTotal_seat());

            List<ShowtimeSeat> seats = userService.findSeatsByShowtimeId(showtimeId);

            if (seats == null || seats.isEmpty()) {
                seatArea.setText("该场次暂无座位数据");
                return;
            }

            seatArea.setText(buildSeatText(seats));

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "场次ID必须是数字");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "查询失败");
        }
    }

    // ================= 下单 =================
    private void createOrder() {

        try {
            int showtimeId = Integer.parseInt(showtimeIdField.getText().trim());
            String seatNo = seatField.getText().trim().toUpperCase();

            if (seatNo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入座位号，例如 A1");
                return;
            }

            int res = JOptionPane.showConfirmDialog(
                    this,
                    "确定购买该座位吗？\n场次ID：" + showtimeId + "\n座位：" + seatNo,
                    "确认下单",
                    JOptionPane.YES_NO_OPTION
            );

            if (res != JOptionPane.YES_OPTION) {
                return;
            }

            int orderId = userService.createOrder(user.getId(), showtimeId, seatNo);

            if (orderId > 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "下单成功！\n订单ID：" + orderId + "\n请在订单页面尽快支付。"
                );

                queryShowtimeAndSeats();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "下单失败，该座位可能已被购买或锁定"
                );

                queryShowtimeAndSeats();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "场次ID必须是数字");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "下单失败，请检查输入");
        }
    }

    // ================= 构造座位显示文本 =================
    private String buildSeatText(List<ShowtimeSeat> seats) {
        StringBuilder sb = new StringBuilder();

        int count = 0;

        for (ShowtimeSeat s : seats) {
            String text = s.getSeat();

            if (s.getStatus() == 1) {
                text += "[锁]";
            } else if (s.getStatus() == 2) {
                text += "[售]";
            }

            sb.append(String.format("%-8s", text));

            count++;

            if (count % 10 == 0) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}