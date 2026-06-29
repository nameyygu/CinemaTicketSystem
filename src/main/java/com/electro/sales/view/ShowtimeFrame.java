package com.electro.sales.view;

import com.electro.sales.model.Showtime;
import com.electro.sales.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ShowtimeFrame extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    private final UserService userService = new UserService();

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ShowtimeFrame() {

        setTitle("场次列表");
        setSize(900, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // ================= 标题 =================
        JLabel title = new JLabel("场次列表", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        add(title, BorderLayout.NORTH);

        // ================= 表格 =================
        model = new DefaultTableModel(
                new Object[]{
                        "场次ID",
                        "电影ID",
                        "影厅",
                        "放映时间",
                        "票价",
                        "总座位",
                        "剩余座位",
                        "状态"
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
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ================= 按钮区 =================
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 12));

        JButton refreshBtn = new JButton("刷新");
        JButton detailBtn = new JButton("查看详情");
        JButton seatBtn = new JButton("查看座位");
        JButton closeBtn = new JButton("关闭");

        btnPanel.add(refreshBtn);
        btnPanel.add(detailBtn);
        btnPanel.add(seatBtn);
        btnPanel.add(closeBtn);

        add(btnPanel, BorderLayout.SOUTH);

        // ================= 事件 =================
        refreshBtn.addActionListener(e -> loadData());
        detailBtn.addActionListener(e -> showDetail());
        seatBtn.addActionListener(e -> openSeatFrame());
        closeBtn.addActionListener(e -> dispose());

        loadData();
    }

    // ================= 加载数据 =================
    private void loadData() {

        model.setRowCount(0);

        List<Showtime> list = userService.findAllShowtimes();

        for (Showtime s : list) {
            model.addRow(new Object[]{
                    s.getId(),
                    s.getMovie_id(),
                    s.getHall(),
                    s.getShow_time() == null ? "" : s.getShow_time().format(fmt),
                    s.getPrice(),
                    s.getTotal_seat(),
                    s.getAvailable_seat(),
                    statusText(s.getStatus())
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

        int showtimeId = (int) model.getValueAt(row, 0);

        Showtime s = userService.findShowtimeById(showtimeId);

        if (s == null) {
            JOptionPane.showMessageDialog(this, "场次不存在或已取消");
            loadData();
            return;
        }

        String info =
                "场次ID: " + s.getId() + "\n" +
                        "电影ID: " + s.getMovie_id() + "\n" +
                        "影厅: " + s.getHall() + "\n" +
                        "时间: " + (s.getShow_time() == null ? "" : s.getShow_time().format(fmt)) + "\n" +
                        "票价: " + s.getPrice() + " 元\n" +
                        "总座位: " + s.getTotal_seat() + "\n" +
                        "剩余座位: " + s.getAvailable_seat() + "\n" +
                        "状态: " + statusText(s.getStatus());

        JOptionPane.showMessageDialog(this, info);
    }

    // ================= 打开座位窗口 =================
    private void openSeatFrame() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请选择一条场次");
            return;
        }

        int showtimeId = (int) model.getValueAt(row, 0);

        new ShowtimeSeatFrame(showtimeId).setVisible(true);
    }

    private String statusText(int status) {
        return status == 1 ? "正常" : "已取消";
    }
}