package com.electro.sales.view;

import com.electro.sales.model.Movie;
import com.electro.sales.service.UserService;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MovieFrame extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    private final UserService userService = new UserService();

    public MovieFrame() {

        setTitle("电影列表");
        setSize(780, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // ================= 标题 =================
        JLabel title = new JLabel("电影列表", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        add(title, BorderLayout.NORTH);

        // ================= 表格 =================
        model = new DefaultTableModel(
                new Object[]{
                        "ID",
                        "名称",
                        "时长",
                        "价格",
                        "类型",
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
        JButton closeBtn = new JButton("关闭");

        btnPanel.add(refreshBtn);
        btnPanel.add(detailBtn);
        btnPanel.add(closeBtn);

        add(btnPanel, BorderLayout.SOUTH);

        // ================= 事件 =================
        refreshBtn.addActionListener(e -> loadMovies());
        detailBtn.addActionListener(e -> showDetail());
        closeBtn.addActionListener(e -> dispose());

        loadMovies();
    }

    // ================= 加载数据 =================
    private void loadMovies() {

        model.setRowCount(0);

        List<Movie> list = userService.findAllMovies();

        for (Movie m : list) {
            model.addRow(new Object[]{
                    m.getId(),
                    m.getName(),
                    m.getDuration() + " 分钟",
                    m.getPrice(),
                    m.getType(),
                    statusText(m.getStatus())
            });
        }
    }

    // ================= 查看详情 =================
    private void showDetail() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请选择一部电影");
            return;
        }

        int movieId = (int) model.getValueAt(row, 0);

        Movie movie = userService.findMovieById(movieId);

        if (movie == null) {
            JOptionPane.showMessageDialog(this, "电影不存在或已下架");
            loadMovies();
            return;
        }

        String info =
                "ID: " + movie.getId() + "\n" +
                        "名称: " + movie.getName() + "\n" +
                        "时长: " + movie.getDuration() + " 分钟\n" +
                        "价格: " + movie.getPrice() + " 元\n" +
                        "类型: " + movie.getType() + "\n" +
                        "状态: " + statusText(movie.getStatus()) + "\n" +
                        "简介: " + safeText(movie.getDescription());

        JOptionPane.showMessageDialog(this, info);
    }

    @Contract(pure = true)
    private @NotNull String statusText(int status) {
        return status == 1 ? "上映" : "下架";
    }

    private @NotNull String safeText(String text) {
        return text == null || text.trim().isEmpty() ? "暂无" : text;
    }
}