package com.electro.sales.dao;

import com.electro.sales.model.Showtime;
import com.electro.sales.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ShowtimeDao {

    // ================= 查询全部场次 =================
    public List<Showtime> findAll() {
        List<Showtime> list = new ArrayList<>();

        String sql = "SELECT * FROM showtime";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Showtime s = new Showtime();
                s.setId(rs.getInt("id"));
                s.setMovie_id(rs.getInt("movie_id"));
                s.setHall(rs.getString("hall"));

                Timestamp ts = rs.getTimestamp("show_time");
                if (ts != null) {
                    s.setShow_time(ts.toLocalDateTime());
                }

                list.add(s);
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
                    Showtime s = new Showtime();
                    s.setId(rs.getInt("id"));
                    s.setMovie_id(rs.getInt("movie_id"));
                    s.setHall(rs.getString("hall"));

                    Timestamp ts = rs.getTimestamp("show_time");
                    if (ts != null) {
                        s.setShow_time(ts.toLocalDateTime());
                    }

                    return s;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= 添加场次 =================
    public void add(Showtime s) {

        String sql = "INSERT INTO showtime(movie_id, hall, show_time) VALUES (?,?,?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, s.getMovie_id());
            ps.setString(2, s.getHall());
            ps.setTimestamp(3, Timestamp.valueOf(s.getShow_time()));

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= 更新场次 =================
    public void update(Showtime s) {

        String sql = "UPDATE showtime SET movie_id=?, hall=?, show_time=? WHERE id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, s.getMovie_id());
            ps.setString(2, s.getHall());
            ps.setTimestamp(3, Timestamp.valueOf(s.getShow_time()));
            ps.setInt(4, s.getId());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= 删除场次 =================
    public void delete(int id) {

        String sql = "DELETE FROM showtime WHERE id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= 按电影查询场次（很重要🔥） =================
    public List<Showtime> findByMovieId(int movieId) {

        List<Showtime> list = new ArrayList<>();

        String sql = "SELECT * FROM showtime WHERE movie_id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, movieId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Showtime s = new Showtime();
                    s.setId(rs.getInt("id"));
                    s.setMovie_id(rs.getInt("movie_id"));
                    s.setHall(rs.getString("hall"));

                    Timestamp ts = rs.getTimestamp("show_time");
                    if (ts != null) {
                        s.setShow_time(ts.toLocalDateTime());
                    }

                    list.add(s);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}