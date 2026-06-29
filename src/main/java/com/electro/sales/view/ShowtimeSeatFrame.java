package com.electro.sales.view;

import com.electro.sales.model.Showtime;
import com.electro.sales.model.ShowtimeSeat;
import com.electro.sales.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ShowtimeSeatFrame extends JFrame {

    private final JTextField showtimeIdField = new JTextField();

    private final JTable table;
    private final DefaultTableModel model;
    private final JTextArea seatArea = new JTextArea();

    private final JLabel showtimeInfoLabel = new JLabel("场次信息：-");

    private final UserService userService = new UserService();

    public ShowtimeSeatFrame() {
        this(0);
    }

    public ShowtimeSeatFrame(int showtimeId) {

        setTitle("场次座位状态");
        setSize(900, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        model = new DefaultTableModel(
                new Object[]{
                        "ID",
                        "场次ID",
                        "座位号",
                        "状态",
                        "订单ID",
                        "锁定时间",
                        "版本号"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);

        initUI();

        if (showtimeId > 0) {
            showtimeIdField.setText(String.valueOf(showtimeId));
            loadSeats();
        }
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // ================= 顶部 =================
        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("场次座位状态", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));

        topPanel.add(title, BorderLayout.NORTH);

        JPanel queryPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));

        showtimeIdField.setPreferredSize(new Dimension(120, 28));

        JButton queryBtn = new JButton("查询");
        JButton refreshBtn = new JButton("刷新");

        queryPanel.add(new JLabel("场次ID："));
        queryPanel.add(showtimeIdField);
        queryPanel.add(queryBtn);
        queryPanel.add(refreshBtn);

        topPanel.add(queryPanel, BorderLayout.CENTER);

        showtimeInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        showtimeInfoLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 8, 0));
        topPanel.add(showtimeInfoLabel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // ================= 中间区域 =================
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.45);

        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("座位列表"));

        seatArea.setEditable(false);
        seatArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        seatArea.setText("请输入场次ID后查询座位状态。");
        seatArea.setLineWrap(true);

        JScrollPane seatScrollPane = new JScrollPane(seatArea);
        seatScrollPane.setBorder(BorderFactory.createTitledBorder("座位布局"));

        splitPane.setTopComponent(tableScrollPane);
        splitPane.setBottomComponent(seatScrollPane);

        add(splitPane, BorderLayout.CENTER);

        // ================= 底部 =================
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 12));

        JLabel tips = new JLabel("状态说明：可售 / 锁定 / 已售");
        JButton closeBtn = new JButton("关闭");

        bottomPanel.add(tips);
        bottomPanel.add(closeBtn);

        add(bottomPanel, BorderLayout.SOUTH);

        // ================= 事件 =================
        queryBtn.addActionListener(e -> loadSeats());
        refreshBtn.addActionListener(e -> loadSeats());
        closeBtn.addActionListener(e -> dispose());

        showtimeIdField.addActionListener(e -> loadSeats());
    }

    // ================= 加载座位 =================
    private void loadSeats() {

        try {
            String text = showtimeIdField.getText().trim();

            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入场次ID");
                return;
            }

            int showtimeId = Integer.parseInt(text);

            Showtime showtime = userService.findShowtimeById(showtimeId);

            if (showtime == null) {
                JOptionPane.showMessageDialog(this, "场次不存在");
                model.setRowCount(0);
                seatArea.setText("");
                showtimeInfoLabel.setText("场次信息：-");
                return;
            }

            showtimeInfoLabel.setText(
                    "场次信息：影厅 " + showtime.getHall()
                            + " | 票价 " + showtime.getPrice()
                            + " | 剩余 " + showtime.getAvailable_seat()
                            + "/" + showtime.getTotal_seat()
                            + " | 状态 " + statusTextShowtime(showtime.getStatus())
            );

            List<ShowtimeSeat> list = userService.findSeatsByShowtimeId(showtimeId);

            model.setRowCount(0);

            for (ShowtimeSeat s : list) {
                model.addRow(new Object[]{
                        s.getId(),
                        s.getShowtime_id(),
                        s.getSeat(),
                        statusTextSeat(s.getStatus()),
                        s.getOrder_id() == null ? "" : s.getOrder_id(),
                        s.getLock_time() == null ? "" : s.getLock_time(),
                        s.getVersion()
                });
            }

            seatArea.setText(buildSeatLayout(list));

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "场次ID必须是数字");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "查询失败");
        }
    }

    // ================= 构建座位布局 =================
    private String buildSeatLayout(List<ShowtimeSeat> list) {

        if (list == null || list.isEmpty()) {
            return "该场次暂无座位数据。";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("座位状态：\n");
        sb.append("可售：A1    锁定：A1[锁]    已售：A1[售]\n\n");

        int count = 0;

        for (ShowtimeSeat s : list) {
            String text = s.getSeat();

            if (s.getStatus() == 1) {
                text += "[锁]";
            } else if (s.getStatus() == 2) {
                text += "[售]";
            }

            sb.append(String.format("%-10s", text));

            count++;

            if (count % 10 == 0) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    private String statusTextSeat(int status) {
        switch (status) {
            case 0:
                return "可售";
            case 1:
                return "锁定";
            case 2:
                return "已售";
            default:
                return "未知";
        }
    }

    private String statusTextShowtime(int status) {
        return status == 1 ? "正常" : "已取消";
    }
}