package com.electro.sales.dao;

import com.electro.sales.model.User;
import com.electro.sales.util.DBUtil;
import org.jetbrains.annotations.NotNull;

import java.sql.*;

public class UserDao {

    public User login(String username, String password) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM user WHERE username=? AND password=?";
            PreparedStatement ps = null;
            if (conn != null) {
                ps = conn.prepareStatement(sql);
            }
            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setRole(rs.getString("role"));
                    return u;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean register(String username, String password, String role) {

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "INSERT INTO user(username, password, role) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);

            return ps.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException e){
            System.out.println("用户名已存在"+username);
            return false;
        }

        catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    public boolean register(@NotNull User user) {
        return register(user.getUsername(), user.getPassword(), user.getRole());
    }
}