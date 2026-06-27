package com.electro.sales.dao;

import com.electro.sales.model.Order;
import com.electro.sales.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {

    // ================= 查询所有订单 =================
    public List<Order> findAll() {
        List<Order> list = new ArrayList<>();

        String sql = "SELECT * FROM ticket_order";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Order o = new Order();
                o.setId(rs.getInt("id"));
                o.setUser_id(rs.getInt("user_id"));
                o.setShowtime_id(rs.getInt("showtime_id"));
                o.setSeat(rs.getString("seat"));
                o.setTotalprice(rs.getDouble("total_price"));
                o.setStatus(rs.getString("status"));

                list.add(o);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= 按用户查询订单 =================
    public List<Order> findByUserId(int userId) {
        List<Order> list = new ArrayList<>();

        String sql = "SELECT * FROM ticket_order WHERE user_id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = new Order();
                    o.setId(rs.getInt("id"));
                    o.setUser_id(rs.getInt("user_id"));
                    o.setShowtime_id(rs.getInt("showtime_id"));
                    o.setSeat(rs.getString("seat"));
                    o.setTotalprice(rs.getDouble("total_price"));
                    o.setStatus(rs.getString("status"));

                    list.add(o);
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
                    Order o = new Order();
                    o.setId(rs.getInt("id"));
                    o.setUser_id(rs.getInt("user_id"));
                    o.setShowtime_id(rs.getInt("showtime_id"));
                    o.setSeat(rs.getString("seat"));
                    o.setTotalprice(rs.getDouble("total_price"));
                    o.setStatus(rs.getString("status"));
                    return o;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= 添加订单 =================
    public void add(Order o) {

        String sql = "INSERT INTO ticket_order(user_id, showtime_id, seat, total_price, status) VALUES (?,?,?,?,?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, o.getUser_id());
            ps.setInt(2, o.getShowtime_id());
            ps.setString(3, o.getSeat());
            ps.setDouble(4, o.getTotalprice());
            ps.setString(5, o.getStatus());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= 更新状态（支付/取消） =================
    public void updateStatus(int id, String status) {

        String sql = "UPDATE ticket_order SET status=? WHERE id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, id);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= 删除订单 =================
    public void delete(int id) {

        String sql = "DELETE FROM ticket_order WHERE id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}