package com.electro.sales.view.admin;

import com.electro.sales.model.User;
import com.electro.sales.view.MainFrame;
import com.electro.sales.view.MovieFrame;
import com.electro.sales.view.ShowtimeFrame;
import com.electro.sales.view.ShowtimeSeatFrame;


import javax.swing.*;
import java.awt.*;

public class AdminMainFrame extends JFrame {

    public AdminMainFrame(User user) {

        setTitle("管理员后台 - " + user.getUsername());
        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        // ================= 顶部菜单 =================
        JMenuBar bar = new JMenuBar();

        JMenu movieMenu = new JMenu("电影管理");
        JMenuItem movieList = new JMenuItem("电影列表");
        JMenuItem addMovie = new JMenuItem("新增电影");

        movieMenu.add(movieList);
        movieMenu.add(addMovie);

        JMenu showMenu = new JMenu("场次管理");
        JMenuItem showList = new JMenuItem("场次列表");
        JMenuItem seatStatus = new JMenuItem("座位状态");
        JMenuItem addShow = new JMenuItem("新增场次");

        showMenu.add(showList);
        showMenu.add(seatStatus);
        showMenu.add(addShow);

        JMenu orderMenu = new JMenu("订单管理");
        JMenuItem orderList = new JMenuItem("所有订单");

        orderMenu.add(orderList);

        JMenu systemMenu = new JMenu("系统");
        JMenuItem logout = new JMenuItem("退出登录");

        systemMenu.add(logout);

        bar.add(movieMenu);
        bar.add(showMenu);
        bar.add(orderMenu);
        bar.add(systemMenu);

        setJMenuBar(bar);

        // ================= 中间欢迎面板 =================
        JPanel centerPanel = new JPanel(new GridLayout(3, 1));

        JLabel title = new JLabel("影院票务管理系统", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));

        JLabel welcome = new JLabel("欢迎您，管理员：" + user.getUsername(), SwingConstants.CENTER);

        JLabel tips = new JLabel("请选择上方功能进行操作", SwingConstants.CENTER);

        centerPanel.add(title);
        centerPanel.add(welcome);
        centerPanel.add(tips);

        add(centerPanel, BorderLayout.CENTER);

        // ================= 事件绑定 =================

        movieList.addActionListener(e -> new MovieFrame().setVisible(true));

        addMovie.addActionListener(e -> new AddMovieFrame().setVisible(true));

        showList.addActionListener(e -> new ShowtimeFrame().setVisible(true));

        addShow.addActionListener(e -> new AddShowtimeFrame().setVisible(true));

        orderList.addActionListener(e -> new OrderAdminFrame().setVisible(true));

        seatStatus.addActionListener(e -> new ShowtimeSeatFrame().setVisible(true));

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