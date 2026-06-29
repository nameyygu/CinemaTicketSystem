package com.electro.sales.view.user;

import com.electro.sales.model.User;
import com.electro.sales.view.MainFrame;
import com.electro.sales.view.MovieFrame;
import com.electro.sales.view.ShowtimeFrame;

import javax.swing.*;
import java.awt.*;

public class UserMainFrame extends JFrame {

    private final User user;

    public UserMainFrame(User user) {

        this.user = user;

        setTitle("用户端 - 影院票务系统");
        setSize(760, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ================= 菜单栏 =================
        JMenuBar bar = new JMenuBar();
        bar.setFont(new Font("微软雅黑", Font.PLAIN, 14));

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

        // ================= 顶部标题 =================
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel title = new JLabel("影院票务系统", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 26));

        topPanel.add(title, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ================= 中间欢迎页 =================
        JPanel center = new JPanel();
        center.setLayout(new GridLayout(4, 1, 10, 10));
        center.setBorder(BorderFactory.createEmptyBorder(30, 80, 30, 80));

        JLabel welcome = new JLabel("欢迎您：" + user.getUsername(), SwingConstants.CENTER);
        welcome.setFont(new Font("微软雅黑", Font.PLAIN, 18));

        JLabel role = new JLabel("当前身份：普通用户", SwingConstants.CENTER);
        role.setFont(new Font("微软雅黑", Font.PLAIN, 15));

        JLabel tips = new JLabel("您可以浏览电影、查看场次、购买电影票和管理订单", SwingConstants.CENTER);
        tips.setFont(new Font("微软雅黑", Font.PLAIN, 15));

        JLabel safeTips = new JLabel("提示：下单后请及时支付，超时订单可能会自动取消", SwingConstants.CENTER);
        safeTips.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        safeTips.setForeground(new Color(120, 120, 120));

        center.add(welcome);
        center.add(role);
        center.add(tips);
        center.add(safeTips);

        add(center, BorderLayout.CENTER);

        // ================= 底部快捷按钮 =================
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));

        JButton movieBtn = new JButton("查看电影");
        JButton buyBtn = new JButton("立即购票");
        JButton orderBtn = new JButton("我的订单");

        bottomPanel.add(movieBtn);
        bottomPanel.add(buyBtn);
        bottomPanel.add(orderBtn);

        add(bottomPanel, BorderLayout.SOUTH);

        // ================= 事件 =================
        movieList.addActionListener(e -> new MovieFrame().setVisible(true));
        movieBtn.addActionListener(e -> new MovieFrame().setVisible(true));

        showList.addActionListener(e -> new ShowtimeFrame().setVisible(true));

        buyTicket.addActionListener(e -> new BuyFrame(user).setVisible(true));
        buyBtn.addActionListener(e -> new BuyFrame(user).setVisible(true));

        myOrders.addActionListener(e -> new OrderUserFrame(user.getId()).setVisible(true));
        orderBtn.addActionListener(e -> new OrderUserFrame(user.getId()).setVisible(true));

        logout.addActionListener(e -> logout());
    }

    private void logout() {
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
    }
}