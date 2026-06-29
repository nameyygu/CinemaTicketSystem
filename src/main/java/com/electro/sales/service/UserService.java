package com.electro.sales.service;

import com.electro.sales.dao.*;
import com.electro.sales.model.*;
import com.electro.sales.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

public class UserService {

    private final UserDao userDao = new UserDao();
    private final MovieDao movieDao = new MovieDao();
    private final ShowtimeDao showtimeDao = new ShowtimeDao();
    private final ShowtimeSeatDao showtimeSeatDao = new ShowtimeSeatDao();
    private final OrderDao orderDao = new OrderDao();

    // ================= 用户登录 =================
    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        if (password == null || password.trim().isEmpty()) {
            return null;
        }

        return userDao.login(username, password);
    }

    // ================= 用户注册 =================
    public boolean register(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        if (password == null || password.trim().isEmpty()) {
            return false;
        }

        return userDao.register(username, password, "user");
    }

    // ================= 查询正在上映电影 =================
    public List<Movie> findAllMovies() {
        return movieDao.findAll();
    }

    // ================= 根据电影ID查询电影 =================
    public Movie findMovieById(int movieId) {
        return movieDao.findById(movieId);
    }

    // ================= 按电影查询场次 =================
    public List<Showtime> findShowtimeByMovieId(int movieId) {
        return showtimeDao.findByMovieId(movieId);
    }

    // ================= 根据场次ID查询场次 =================
    public Showtime findShowtimeById(int showtimeId) {
        return showtimeDao.findById(showtimeId);
    }

    public List<Showtime> findAllShowtimes() {
        return showtimeDao.findAll();
    }

    // ================= 查询某场次所有座位 =================
    public List<ShowtimeSeat> findSeatsByShowtimeId(int showtimeId) {
        return showtimeSeatDao.findByShowtimeId(showtimeId);
    }

    // ================= 查询我的订单 =================
    public List<Order> findMyOrders(int userId) {
        return orderDao.findByUserId(userId);
    }

    // ================= 根据订单ID查询订单 =================
    public Order findOrderById(int orderId) {
        return orderDao.findById(orderId);
    }

    // ================= 用户购票/抢座/创建订单 =================
    /**
     * 返回值：
     * > 0 表示订单ID，创建成功
     * 0 表示失败，座位已被锁定/售出，或者其他错误
     */
    public int createOrder(int userId, int showtimeId, String seatNo) {
        Connection conn = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 1. 抢座：showtime_seat status 0 -> 1
            int lockCount = showtimeSeatDao.lockSeat(conn, showtimeId, seatNo);

            if (lockCount == 0) {
                conn.rollback();
                return 0;
            }

            // 2. 查询场次价格
            BigDecimal price = showtimeDao.getPriceById(conn, showtimeId);

            if (price == null) {
                conn.rollback();
                return 0;
            }

            // 3. 扣减剩余座位数
            int decreaseCount = showtimeDao.decreaseAvailableSeats(conn, showtimeId);

            if (decreaseCount == 0) {
                conn.rollback();
                return 0;
            }

            // 4. 生成订单号
            String orderNo = generateOrderNo(userId);

            // 5. 创建待支付订单
            int orderId = orderDao.createOrder(
                    conn,
                    orderNo,
                    userId,
                    showtimeId,
                    seatNo,
                    price
            );

            if (orderId == 0) {
                conn.rollback();
                return 0;
            }

            // 6. 绑定座位和订单
            int bindCount = showtimeSeatDao.bindOrder(conn, showtimeId, seatNo, orderId);

            if (bindCount == 0) {
                conn.rollback();
                return 0;
            }

            conn.commit();
            return orderId;

        } catch (Exception e) {
            e.printStackTrace();

            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return 0;

        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ================= 用户支付订单 =================
    public boolean payOrder(int userId, int orderId) {
        Connection conn = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 1. 查询订单并加锁，防止同一订单重复支付/取消
            Order order = orderDao.findByIdForUpdate(conn, orderId);

            if (order == null) {
                conn.rollback();
                return false;
            }

            // 2. 校验订单归属
            if (order.getUser_id() != userId) {
                conn.rollback();
                return false;
            }

            // 3. 只有待支付订单才能支付
            if (order.getStatus() != OrderDao.STATUS_WAIT_PAY) {
                conn.rollback();
                return false;
            }

            // 4. 更新订单状态：待支付 -> 已支付
            int payCount = orderDao.payOrder(conn, orderId);

            if (payCount == 0) {
                conn.rollback();
                return false;
            }

            // 5. 更新座位状态：锁定 -> 已售
            int seatCount = showtimeSeatDao.markSold(conn, orderId);

            if (seatCount == 0) {
                conn.rollback();
                return false;
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();

            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return false;

        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ================= 用户取消订单 =================
    public boolean cancelOrder(int userId, int orderId) {
        Connection conn = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 1. 查询订单并加锁
            Order order = orderDao.findByIdForUpdate(conn, orderId);

            if (order == null) {
                conn.rollback();
                return false;
            }

            // 2. 校验订单归属
            if (order.getUser_id() != userId) {
                conn.rollback();
                return false;
            }

            // 3. 只有待支付订单可以取消
            if (order.getStatus() != OrderDao.STATUS_WAIT_PAY) {
                conn.rollback();
                return false;
            }

            // 4. 订单状态：待支付 -> 已取消
            int cancelCount = orderDao.cancelOrder(conn, orderId);

            if (cancelCount == 0) {
                conn.rollback();
                return false;
            }

            // 5. 释放座位：锁定 -> 可售
            int releaseCount = showtimeSeatDao.releaseSeat(conn, orderId);

            if (releaseCount == 0) {
                conn.rollback();
                return false;
            }

            // 6. 恢复剩余座位数
            int increaseCount = showtimeDao.increaseAvailableSeats(conn, order.getShowtime_id());

            if (increaseCount == 0) {
                conn.rollback();
                return false;
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();

            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return false;

        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ================= 订单号生成 =================
    private String generateOrderNo(int userId) {
        return "NO" + System.currentTimeMillis() + userId + (int) (Math.random() * 10000);
    }
}