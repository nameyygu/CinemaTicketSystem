package com.electro.sales.dao;

import com.electro.sales.model.Showtime;
import com.electro.sales.util.DBUtil;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ShowtimeDao {

    // ================= 公共映射方法 =================
    private @NotNull Showtime mapShowtime(@NotNull ResultSet rs)throws SQLException{
        Showtime s = new Showtime();

        s.setId(rs.getInt("id"));
        s.setMovie_id(rs.getInt("movie_id"));
        s.setHall(rs.getString("hall"));

        Timestamp showTime = rs.getTimestamp("show_time");
        if (showTime != null) {
            s.setShow_time(showTime.toLocalDateTime());
        }

        s.setPrice(rs.getBigDecimal("price"));
        s.setTotal_seat(rs.getInt("total_seats"));
        s.setAvailable_seat(rs.getInt("available_seats"));
        s.setStatus(rs.getInt("status"));
        s.setVersion(rs.getInt("version"));

        Timestamp updateTime = rs.getTimestamp("update_time");
        if (updateTime != null) {
            s.setUpdate_time(updateTime.toLocalDateTime());
        }

        Timestamp createTime = rs.getTimestamp("create_time");
        if (createTime != null) {
            s.setCreate_time(createTime.toLocalDateTime());
        }
        return s;
    }

    // ================= 查询正常场次 =================
    public List<Showtime> findAll() {
        List<Showtime> list = new ArrayList<>();

        String sql = "SELECT * FROM showtime WHERE status=1 ORDER BY show_time ASC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapShowtime(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= 查询全部场次 =================
    public List<Showtime> findAllForAdmin() {
        List<Showtime> list = new ArrayList<>();

        String sql = "SELECT * FROM showtime ORDER BY show_time ASC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapShowtime(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= 根据ID查询 =================
    public Showtime findById(int id) {

        String sql = "SELECT * FROM showtime WHERE id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapShowtime(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= 添加场次 =================
    public int add(Showtime s) {

        String sql = "INSERT INTO showtime(movie_id, hall, show_time, price, total_seats, available_seats, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, s.getMovie_id());
            ps.setString(2, s.getHall());
            ps.setTimestamp(3, Timestamp.valueOf(s.getShow_time()));
            ps.setBigDecimal(4, s.getPrice());
            ps.setInt(5, s.getTotal_seat());
            ps.setInt(6, s.getAvailable_seat());
            ps.setInt(7, s.getStatus());

            int count = ps.executeUpdate();

            if (count > 0) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()){
                        return rs.getInt(1);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ================= 添加场次，支持外部事务 =================
    public int add(Connection conn, @NotNull Showtime s) throws SQLException {
        String sql = "INSERT INTO showtime(movie_id, hall, show_time, price, total_seats, available_seats, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, s.getMovie_id());
            ps.setString(2, s.getHall());
            ps.setTimestamp(3, Timestamp.valueOf(s.getShow_time()));
            ps.setBigDecimal(4, s.getPrice());
            ps.setInt(5, s.getTotal_seat());
            ps.setInt(6, s.getAvailable_seat());
            ps.setInt(7, s.getStatus());

            int count = ps.executeUpdate();

            if (count > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }

            return 0;
        }
    }

    // ================= 更新场次 =================
    public boolean update(Showtime s) throws SQLException {
        String sql = "UPDATE showtime " +
                "SET movie_id=?, hall=?, show_time=?, price=?, total_seats=?, available_seats=?, status=?, version=version+1 " +
                "WHERE id=? AND version=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, s.getMovie_id());
            ps.setString(2, s.getHall());
            ps.setTimestamp(3, Timestamp.valueOf(s.getShow_time()));
            ps.setBigDecimal(4, s.getPrice());
            ps.setInt(5, s.getTotal_seat());
            ps.setInt(6, s.getAvailable_seat());
            ps.setInt(7, s.getStatus());
            ps.setInt(8, s.getId());
            ps.setInt(9, s.getVersion());

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= 取消场次 =================
    public boolean delete(int id, int version) {
        String sql = "UPDATE showtime SET status=0, version=version+1 WHERE id=? AND version=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, version);

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= 按电影查询场次 =================
    public List<Showtime> findByMovieId(int movieId) {
        List<Showtime> list = new ArrayList<>();

        String sql = "SELECT * FROM showtime WHERE movie_id=? AND status=1 ORDER BY show_time ASC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, movieId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapShowtime(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= 根据ID查询票价，支持外部事务 =================
    public BigDecimal getPriceById(Connection conn, int showtimeId) throws SQLException {
        String sql = "SELECT price FROM showtime WHERE id=? AND status=1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("price");
                }
            }
        }

        return null;
    }

    // ================= 扣减剩余座位，买票时使用 =================
    public int decreaseAvailableSeats(Connection conn, int showtimeId) throws SQLException {
        String sql = "UPDATE showtime " +
                "SET available_seats = available_seats - 1, version = version + 1 " +
                "WHERE id = ? AND status = 1 AND available_seats > 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            return ps.executeUpdate();
        }
    }

    // ================= 恢复剩余座位，取消订单时使用 =================
    public int increaseAvailableSeats(Connection conn, int showtimeId) throws SQLException {
        String sql = "UPDATE showtime " +
                "SET available_seats = available_seats + 1, version = version + 1 " +
                "WHERE id = ? AND status = 1 AND available_seats < total_seats";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            return ps.executeUpdate();
        }
    }

    // ================= 根据ID查询场次，支持外部事务 =================
    public Showtime findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM showtime WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapShowtime(rs);
                }
            }
        }

        return null;
    }
}