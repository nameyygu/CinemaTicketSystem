package com.electro.sales.dao;

import com.electro.sales.model.ShowtimeSeat;
import com.electro.sales.util.DBUtil;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShowtimeSeatDao {

    // ================= 公共映射方法 =================
    private @NotNull ShowtimeSeat mapSeat(@NotNull ResultSet rs) throws SQLException {
        ShowtimeSeat seat = new ShowtimeSeat();

        seat.setId(rs.getInt("id"));
        seat.setShowtime_id(rs.getInt("showtime_id"));
        seat.setSeat(rs.getString("seat"));
        seat.setStatus(rs.getInt("status"));

        int orderId = rs.getInt("order_id");
        if (rs.wasNull()) {
            seat.setOrder_id(null);
        } else {
            seat.setOrder_id(orderId);
        }

        Timestamp lockTime = rs.getTimestamp("lock_time");
        if (lockTime != null) {
            seat.setLock_time(lockTime.toLocalDateTime());
        }

        seat.setVersion(rs.getInt("version"));

        Timestamp createTime = rs.getTimestamp("create_time");
        if (createTime != null) {
            seat.setCreate_time(createTime.toLocalDateTime());
        }

        Timestamp updateTime = rs.getTimestamp("update_time");
        if (updateTime != null) {
            seat.setUpdate_time(updateTime.toLocalDateTime());
        }

        return seat;
    }

    // ================= 根据场次查询所有座位 =================
    public List<ShowtimeSeat> findByShowtimeId(int showtimeId) {
        List<ShowtimeSeat> list = new ArrayList<>();

        String sql = "SELECT * FROM showtime_seat WHERE showtime_id=? ORDER BY seat ASC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, showtimeId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSeat(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= 根据场次和座位号查询 =================
    public ShowtimeSeat findByShowtimeIdAndSeat(int showtimeId, String seatNo) {
        String sql = "SELECT * FROM showtime_seat WHERE showtime_id=? AND seat=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, showtimeId);
            ps.setString(2, seatNo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapSeat(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= 新增单个座位 =================
    public boolean addSeat(int showtimeId, String seatNo) {
        String sql = "INSERT INTO showtime_seat(showtime_id, seat, status) VALUES (?, ?, 0)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, showtimeId);
            ps.setString(2, seatNo);

            return ps.executeUpdate() == 1;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("座位已存在：" + showtimeId + " - " + seatNo);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= 新增单个座位，支持外部事务 =================
    public int addSeat(Connection conn, int showtimeId, String seatNo) throws SQLException {
        String sql = "INSERT INTO showtime_seat(showtime_id, seat, status) VALUES (?, ?, 0)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            ps.setString(2, seatNo);
            return ps.executeUpdate();
        }
    }

    // ================= 批量生成座位，支持外部事务 =================
    public int batchAddSeats(Connection conn, int showtimeId, @NotNull List<String> seatList) throws SQLException {
        String sql = "INSERT INTO showtime_seat(showtime_id, seat, status) VALUES (?, ?, 0)";

        int count = 0;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String seatNo : seatList) {
                ps.setInt(1, showtimeId);
                ps.setString(2, seatNo);
                ps.addBatch();
            }

            int[] results = ps.executeBatch();

            for (int r : results) {
                if (r >= 0 || r == Statement.SUCCESS_NO_INFO) {
                    count++;
                }
            }
        }

        return count;
    }

    // ================= 抢座：可售 -> 锁定 =================
    public int lockSeat(Connection conn, int showtimeId, String seatNo) throws SQLException {
        String sql = "UPDATE showtime_seat " +
                "SET status = 1, lock_time = NOW(), version = version + 1 " +
                "WHERE showtime_id = ? AND seat = ? AND status = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            ps.setString(2, seatNo);

            return ps.executeUpdate();
        }
    }

    // ================= 订单创建后绑定订单ID =================
    public int bindOrder(Connection conn, int showtimeId, String seatNo, int orderId) throws SQLException {
        String sql = "UPDATE showtime_seat " +
                "SET order_id = ?, version = version + 1 " +
                "WHERE showtime_id = ? AND seat = ? AND status = 1 AND order_id IS NULL";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, showtimeId);
            ps.setString(3, seatNo);

            return ps.executeUpdate();
        }
    }

    // ================= 支付成功：锁定 -> 已售 =================
    public int markSold(Connection conn, int orderId) throws SQLException {
        String sql = "UPDATE showtime_seat " +
                "SET status = 2, version = version + 1 " +
                "WHERE order_id = ? AND status = 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            return ps.executeUpdate();
        }
    }

    // ================= 取消订单：锁定 -> 可售 =================
    public int releaseSeat(Connection conn, int orderId) throws SQLException {
        String sql = "UPDATE showtime_seat " +
                "SET status = 0, order_id = NULL, lock_time = NULL, version = version + 1 " +
                "WHERE order_id = ? AND status = 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            return ps.executeUpdate();
        }
    }

    // ================= 根据场次和座位释放，用于异常修复 =================
    public int releaseSeat(Connection conn, int showtimeId, String seatNo) throws SQLException {
        String sql = "UPDATE showtime_seat " +
                "SET status = 0, order_id = NULL, lock_time = NULL, version = version + 1 " +
                "WHERE showtime_id = ? AND seat = ? AND status = 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            ps.setString(2, seatNo);

            return ps.executeUpdate();
        }
    }

    // ================= 查询某个座位状态，支持外部事务 =================
    public Integer getSeatStatus(Connection conn, int showtimeId, String seatNo) throws SQLException {
        String sql = "SELECT status FROM showtime_seat WHERE showtime_id=? AND seat=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            ps.setString(2, seatNo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("status");
                }
            }
        }

        return null;
    }

    // ================= 删除某场次的所有座位，慎用 =================
    public int deleteByShowtimeId(Connection conn, int showtimeId) throws SQLException {
        String sql = "DELETE FROM showtime_seat WHERE showtime_id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            return ps.executeUpdate();
        }
    }
}