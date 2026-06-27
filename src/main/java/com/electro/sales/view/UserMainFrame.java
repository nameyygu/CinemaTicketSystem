package com.electro.sales.view;

import com.electro.sales.model.User;

import javax.swing.*;
import java.awt.*;

public class UserMainFrame extends JFrame {

    private User user;

    public UserMainFrame(User user) {

        this.user = user;

        setTitle("用户端 - 影院票务系统");
        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        // ================= 菜单栏 =================
        JMenuBar bar = new JMenuBar();

        JMenu movieMenu = new JMenu("电影浏览");
        JMenuItem movieList = new JMenuItem("电影列表");
        JMenuItem showList = new JMenuItem("场次信息");

        movieMenu.add(movieList);
        movieMenu.add(showList);

        JMenu ticketMenu = new JMenu("购票");
        JMenuItem buyTicket = new JMenuItem("购买电影票");

        ticketMenu.add(buyTicket);

        JMenu orderMenu = new JMenu("订单");
        JMenuItem myOrders = new JMenuItem("我的订单");

        orderMenu.add(myOrders);

        JMenu systemMenu = new JMenu("系统");
        JMenuItem logout = new JMenuItem("退出登录");

        systemMenu.add(logout);

        bar.add(movieMenu);
        bar.add(ticketMenu);
        bar.add(orderMenu);
        bar.add(systemMenu);

        setJMenuBar(bar);

        // ================= 中间欢迎页 =================
        JPanel center = new JPanel(new GridLayout(3, 1));

        JLabel title = new JLabel("影院票务系统", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));

        JLabel welcome = new JLabel("欢迎您：" + user.getUsername(), SwingConstants.CENTER);

        JLabel tips = new JLabel("请选择上方功能进行操作", SwingConstants.CENTER);

        center.add(title);
        center.add(welcome);
        center.add(tips);

        add(center, BorderLayout.CENTER);

        // ================= 事件 =================

        movieList.addActionListener(e -> new MovieFrame().setVisible(true));

        showList.addActionListener(e -> new ShowtimeFrame().setVisible(true));

        buyTicket.addActionListener(e -> new BuyFrame(user).setVisible(true));

        myOrders.addActionListener(e -> new OrderUserFrame(user.getId()).setVisible(true));

        logout.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(
                    this,
                    "确定退出登录吗？",
                    "提示",
                    JOptionPane.YES_NO_OPTION
            );

            if (res == JOptionPane.YES_OPTION) {
                this.dispose();
                new MainFrame().setVisible(true);
            }
        });
    }
}