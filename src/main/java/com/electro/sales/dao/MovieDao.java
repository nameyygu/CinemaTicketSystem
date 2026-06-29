package com.electro.sales.dao;

import com.electro.sales.model.Movie;
import com.electro.sales.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovieDao {

    // ================= 公共映射方法 =================
    private Movie mapMovie(ResultSet rs) throws SQLException {
        Movie m = new Movie();

        m.setId(rs.getInt("id"));
        m.setName(rs.getString("name"));
        m.setDuration(rs.getInt("duration"));
        m.setPrice(rs.getBigDecimal("price"));
        m.setType(rs.getString("type"));
        m.setDescription(rs.getString("description"));
        m.setStatus(rs.getInt("status"));
        m.setVersion(rs.getInt("version"));

        Timestamp createTime = rs.getTimestamp("create_time");
        if (createTime != null) {
            m.setCreate_time(createTime.toLocalDateTime());
        }

        Timestamp updateTime = rs.getTimestamp("update_time");
        if (updateTime != null) {
            m.setUpdate_time(updateTime.toLocalDateTime());
        }

        return m;
    }

    // ================= 查询所有上映电影 =================
    public List<Movie> findAll() {
        List<Movie> list = new ArrayList<>();

        String sql = "SELECT * FROM movie WHERE status = 1 ORDER BY id DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapMovie(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    // ================= 管理员查询所有电影，包括下架 =================
    public List<Movie> findAllForAdmin() {
        List<Movie> list = new ArrayList<>();

        String sql = "SELECT * FROM movie ORDER BY id DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapMovie(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    // ================= 根据ID查询 =================
    public Movie findById(int id) {
        String sql = "SELECT * FROM movie WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapMovie(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    // ================= 添加电影 =================
    public boolean add(Movie m) {
        String sql = "INSERT INTO movie(name, duration, price, type, description, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, m.getName());
            ps.setInt(2, m.getDuration());
            ps.setBigDecimal(3, m.getPrice());
            ps.setString(4, m.getType());
            ps.setString(5, m.getDescription());
            ps.setInt(6, m.getStatus());

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    // ================= 更新电影，乐观锁控制 =================
    public boolean update(Movie m) {
        String sql = "UPDATE movie " +
                "SET name = ?, " +
                "duration = ?, " +
                "price = ?, " +
                "type = ?, " +
                "description = ?, " +
                "status = ?, " +
                "version = version + 1 " +
                "WHERE id = ? AND version = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, m.getName());
            ps.setInt(2, m.getDuration());
            ps.setBigDecimal(3, m.getPrice());
            ps.setString(4, m.getType());
            ps.setString(5, m.getDescription());
            ps.setInt(6, m.getStatus());
            ps.setInt(7, m.getId());
            ps.setInt(8, m.getVersion());

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    // ================= 下架电影，软删除，乐观锁控制 =================
    public boolean delete(int id, int version) {
        String sql = "UPDATE movie " +
                "SET status = 0, version = version + 1 " +
                "WHERE id = ? AND version = ?";

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


    // ================= 兼容旧代码：只传id删除，不推荐 =================
    public boolean delete(int id) {
        String sql = "UPDATE movie " +
                "SET status = 0, version = version + 1 " +
                "WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    // ================= 上架电影 =================
    public boolean onlineMovie(int id, int version) {
        String sql = "UPDATE movie " +
                "SET status = 1, version = version + 1 " +
                "WHERE id = ? AND version = ?";

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


    // ================= 下架电影 =================
    public boolean offlineMovie(int id, int version) {
        return delete(id, version);
    }


    // ================= 按电影名模糊查询上映电影 =================
    public List<Movie> searchByName(String keyword) {
        List<Movie> list = new ArrayList<>();

        String sql = "SELECT * FROM movie WHERE status = 1 AND name LIKE ? ORDER BY id DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapMovie(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}