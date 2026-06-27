package com.electro.sales.dao;

import com.electro.sales.model.Movie;
import com.electro.sales.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovieDao {


    public List<Movie> findAll() {
        List<Movie> list = new ArrayList<>();

        String sql = "SELECT * FROM movie";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Movie m = new Movie();
                m.setId(rs.getInt("id"));
                m.setName(rs.getString("name"));
                m.setDuration(rs.getInt("duration"));
                m.setPrice(rs.getDouble("price"));
                m.setType(rs.getString("type"));

                list.add(m);
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
                    Movie m = new Movie();
                    m.setId(rs.getInt("id"));
                    m.setName(rs.getString("name"));
                    m.setDuration(rs.getInt("duration"));
                    m.setPrice(rs.getDouble("price"));
                    m.setType(rs.getString("type"));
                    return m;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= 添加 =================
    public void add(Movie m) {
        String sql = "INSERT INTO movie(name, duration, price, type) VALUES (?,?,?,?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, m.getName());
            ps.setInt(2, m.getDuration());
            ps.setDouble(3, m.getPrice());
            ps.setString(4, m.getType());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= 更新 =================
    public void update(Movie m) {
        String sql = "UPDATE movie SET name=?, duration=?, price=?, type=? WHERE id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, m.getName());
            ps.setInt(2, m.getDuration());
            ps.setDouble(3, m.getPrice());
            ps.setString(4, m.getType());
            ps.setInt(5, m.getId());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= 删除 =================
    public void delete(int id) {
        String sql = "DELETE FROM movie WHERE id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}