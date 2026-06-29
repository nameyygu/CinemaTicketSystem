package com.electro.sales.dao;

import com.electro.sales.model.Order;
import com.electro.sales.util.DBUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {

    // ================= 订单状态常量 =================
    public static final int STATUS_WAIT_PAY = 0;   // 待支付
    public static final int STATUS_PAID = 1;       // 已支付
    public static final int STATUS_CANCEL = 2;     // 已取消
    public static final int STATUS_REFUND = 3;     // 已退款


    // ================= 公共映射方法 =================
    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();

        o.setId(rs.getInt("id"));
        o.setOrder_no(rs.getString("order_no"));
        o.setUser_id(rs.getInt("user_id"));
        o.setShowtime_id(rs.getInt("showtime_id"));
        o.setSeat(rs.getString("seat"));
        o.setTotal_price(rs.getBigDecimal("total_price"));
        o.setStatus(rs.getInt("status"));
        o.setVersion(rs.getInt("version"));

        Timestamp expireTime = rs.getTimestamp("expire_time");
        if (expireTime != null) {
            o.setExpire_time(expireTime.toLocalDateTime());
        }

        Timestamp payTime = rs.getTimestamp("pay_time");
        if (payTime != null) {
            o.setPay_time(payTime.toLocalDateTime());
        }

        Timestamp createTime = rs.getTimestamp("create_time");
        if (createTime != null) {
            o.setCreate_time(createTime.toLocalDateTime());
        }

        Timestamp updateTime = rs.getTimestamp("update_time");
        if (updateTime != null) {
            o.setUpdate_time(updateTime.toLocalDateTime());
        }

        return o;
    }


    // ================= 查询所有订单 =================
    public List<Order> findAll() {
        List<Order> list = new ArrayList<>();

        String sql = "SELECT * FROM ticket_order ORDER BY create_time DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapOrder(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    // ================= 按用户查询订单 =================
    public List<Order> findByUserId(int userId) {
        List<Order> list = new ArrayList<>();

        String sql = "SELECT * FROM ticket_order WHERE user_id=? ORDER BY create_time DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrder(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    // ================= 根据ID查询 =================
    public Order findById(int id) {
        String sql = "SELECT * FROM ticket_order WHERE id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapOrder(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    // ================= 根据ID查询，支持外部事务 =================
    public Order findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM ticket_order WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapOrder(rs);
                }
            }
        }

        return null;
    }


    // ================= 根据ID查询并加锁，支付/取消时可用 =================
    public Order findByIdForUpdate(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM ticket_order WHERE id=? FOR UPDATE";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapOrder(rs);
                }
            }
        }

        return null;
    }


    // ================= 创建订单，支持事务 =================
    public int createOrder(Connection conn,
                           String orderNo,
                           int userId,
                           int showtimeId,
                           String seat,
                           BigDecimal totalPrice) throws SQLException {

        String sql = "INSERT INTO ticket_order " +
                "(order_no, user_id, showtime_id, seat, total_price, status, expire_time) " +
                "VALUES (?, ?, ?, ?, ?, 0, DATE_ADD(NOW(), INTERVAL 15 MINUTE))";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, orderNo);
            ps.setInt(2, userId);
            ps.setInt(3, showtimeId);
            ps.setString(4, seat);
            ps.setBigDecimal(5, totalPrice);

            int count = ps.executeUpdate();

            if (count > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }

        return 0;
    }


    // ================= 兼容普通添加订单，不推荐买票时直接用 =================
    public boolean add(Order o) {
        String sql = "INSERT INTO ticket_order " +
                "(order_no, user_id, showtime_id, seat, total_price, status, expire_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, o.getOrder_no());
            ps.setInt(2, o.getUser_id());
            ps.setInt(3, o.getShowtime_id());
            ps.setString(4, o.getSeat());
            ps.setBigDecimal(5, o.getTotal_price());
            ps.setInt(6, o.getStatus());

            if (o.getExpire_time() != null) {
                ps.setTimestamp(7, Timestamp.valueOf(o.getExpire_time()));
            } else {
                ps.setTimestamp(7, null);
            }

            return ps.executeUpdate() == 1;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("订单号重复或约束冲突：" + e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    // ================= 支付订单：待支付 -> 已支付 =================
    public int payOrder(Connection conn, int orderId) throws SQLException {
        String sql = "UPDATE ticket_order " +
                "SET status = 1, pay_time = NOW(), version = version + 1 " +
                "WHERE id = ? " +
                "AND status = 0 " +
                "AND expire_time > NOW()";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            return ps.executeUpdate();
        }
    }


    // ================= 取消订单：待支付 -> 已取消 =================
    public int cancelOrder(Connection conn, int orderId) throws SQLException {
        String sql = "UPDATE ticket_order " +
                "SET status = 2, version = version + 1 " +
                "WHERE id = ? " +
                "AND status = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            return ps.executeUpdate();
        }
    }


    // ================= 超时取消订单：待支付并且已过期 -> 已取消 =================
    public int cancelExpiredOrder(Connection conn, int orderId) throws SQLException {
        String sql = "UPDATE ticket_order " +
                "SET status = 2, version = version + 1 " +
                "WHERE id = ? " +
                "AND status = 0 " +
                "AND expire_time < NOW()";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            return ps.executeUpdate();
        }
    }


    // ================= 查询已超时待支付订单 =================
    public List<Order> findExpiredUnpaidOrders() {
        List<Order> list = new ArrayList<>();

        String sql = "SELECT * FROM ticket_order " +
                "WHERE status = 0 AND expire_time < NOW() " +
                "ORDER BY expire_time ASC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapOrder(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    // ================= 根据状态查询订单 =================
    public List<Order> findByStatus(int status) {
        List<Order> list = new ArrayList<>();

        String sql = "SELECT * FROM ticket_order WHERE status=? ORDER BY create_time DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, status);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrder(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    // ================= 不推荐物理删除订单 =================
    // 如果一定要保留，可以只给管理员清理测试数据用
    public boolean deletePhysical(int id) {
        String sql = "DELETE FROM ticket_order WHERE id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    // ================= 普通更新状态，不推荐用于支付/取消核心流程 =================
    // 核心支付、取消建议用 payOrder/cancelOrder，并且放在 Service 事务中
    public boolean updateStatus(int id, int status) {
        String sql = "UPDATE ticket_order SET status=?, version=version+1 WHERE id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, status);
            ps.setInt(2, id);

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}