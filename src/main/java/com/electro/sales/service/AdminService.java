package com.electro.sales.service;

import com.electro.sales.dao.*;
import com.electro.sales.model.*;
import com.electro.sales.util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminService {

    private final MovieDao movieDao = new MovieDao();
    private final ShowtimeDao showtimeDao = new ShowtimeDao();
    private final ShowtimeSeatDao showtimeSeatDao = new ShowtimeSeatDao();
    private final OrderDao orderDao = new OrderDao();

    // ================= 查询所有电影，管理员用 =================
    public List<Movie> findAllMoviesForAdmin() {
        return movieDao.findAllForAdmin();
    }

    // ================= 查询上映电影 =================
    public List<Movie> findOnlineMovies() {
        return movieDao.findAll();
    }

    // ================= 根据ID查询电影 =================
    public Movie findMovieById(int movieId) {
        return movieDao.findById(movieId);
    }

    // ================= 添加电影 =================
    public boolean addMovie(Movie movie) {
        if (movie == null) {
            return false;
        }

        if (movie.getName() == null || movie.getName().trim().isEmpty()) {
            return false;
        }

        if (movie.getDuration() <= 0) {
            return false;
        }

        if (movie.getPrice() == null) {
            return false;
        }

        // 新增电影默认上映
        if (movie.getStatus() != 0 && movie.getStatus() != 1) {
            movie.setStatus(1);
        }

        return movieDao.add(movie);
    }

    // ================= 修改电影，乐观锁控制 =================
    public boolean updateMovie(Movie movie) {
        if (movie == null) {
            return false;
        }

        if (movie.getId() <= 0) {
            return false;
        }

        if (movie.getName() == null || movie.getName().trim().isEmpty()) {
            return false;
        }

        if (movie.getDuration() <= 0) {
            return false;
        }

        if (movie.getPrice() == null) {
            return false;
        }

        return movieDao.update(movie);
    }
    // ================= 下架电影 =================
    public boolean offlineMovie(int movieId, int version) {
        if (movieId <= 0) {
            return false;
        }

        return movieDao.offlineMovie(movieId, version);
    }

    // ================= 上架电影 =================
    public boolean onlineMovie(int movieId, int version) {
        if (movieId <= 0) {
            return false;
        }

        return movieDao.onlineMovie(movieId, version);
    }

    // ================= 查询所有场次，管理员用 =================
    public List<Showtime> findAllShowtimesForAdmin() {
        return showtimeDao.findAllForAdmin();
    }

    // ================= 查询正常场次 =================
    public List<Showtime> findAllShowtimes() {
        return showtimeDao.findAll();
    }

    // ================= 根据ID查询场次 =================
    public Showtime findShowtimeById(int showtimeId) {
        return showtimeDao.findById(showtimeId);
    }

    // ================= 添加场次，并自动生成座位 =================
    public boolean addShowtimeWithSeats(Showtime showtime) {
        Connection conn = null;

        try {
            if (showtime == null) {
                return false;
            }

            if (showtime.getMovie_id() <= 0) {
                return false;
            }

            if (showtime.getHall() == null || showtime.getHall().trim().isEmpty()) {
                return false;
            }

            if (showtime.getShow_time() == null) {
                return false;
            }

            if (showtime.getPrice() == null) {
                return false;
            }

            if (showtime.getTotal_seat() <= 0) {
                showtime.setTotal_seat(100);
            }

            showtime.setAvailable_seat(showtime.getTotal_seat());

            // 新增场次默认正常
            showtime.setStatus(1);

            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 1. 添加场次
            int showtimeId = showtimeDao.add(conn, showtime);

            if (showtimeId == 0) {
                conn.rollback();
                return false;
            }

            // 2. 根据总座位数生成座位
            List<String> seats = generateSeats(showtime.getTotal_seat());

            // 3. 批量插入座位
            int seatCount = showtimeSeatDao.batchAddSeats(conn, showtimeId, seats);

            if (seatCount != seats.size()) {
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

    // ================= 修改场次，乐观锁控制 =================
    public boolean updateShowtime(Showtime showtime) {
        if (showtime == null) {
            return false;
        }

        if (showtime.getId() <= 0) {
            return false;
        }

        if (showtime.getMovie_id() <= 0) {
            return false;
        }

        if (showtime.getHall() == null || showtime.getHall().trim().isEmpty()) {
            return false;
        }

        if (showtime.getShow_time() == null) {
            return false;
        }

        if (showtime.getPrice() == null) {
            return false;
        }

        try {
            return showtimeDao.update(showtime);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    // ================= 取消场次，软删除 =================
    public boolean cancelShowtime(int showtimeId, int version) {
        if (showtimeId <= 0) {
            return false;
        }

        return showtimeDao.delete(showtimeId, version);
    }

    // ================= 查询某场次座位 =================
    public List<ShowtimeSeat> findSeatsByShowtimeId(int showtimeId) {
        return showtimeSeatDao.findByShowtimeId(showtimeId);
    }

    // ================= 查询所有订单 =================
    public List<Order> findAllOrders() {
        return orderDao.findAll();
    }

    // ================= 根据用户查询订单 =================
    public List<Order> findOrdersByUserId(int userId) {
        return orderDao.findByUserId(userId);
    }

    // ================= 根据状态查询订单 =================
    public List<Order> findOrdersByStatus(int status) {
        return orderDao.findByStatus(status);
    }

    // ================= 根据订单ID查询订单 =================
    public Order findOrderById(int orderId) {
        return orderDao.findById(orderId);
    }

    // ================= 管理员取消订单 =================
    public boolean adminCancelOrder(int orderId) {
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

            // 2. 只有待支付订单可以直接释放座位
            if (order.getStatus() != OrderDao.STATUS_WAIT_PAY) {
                conn.rollback();
                return false;
            }

            // 3. 订单改为已取消
            int cancelCount = orderDao.cancelOrder(conn, orderId);

            if (cancelCount == 0) {
                conn.rollback();
                return false;
            }

            // 4. 释放座位
            int releaseCount = showtimeSeatDao.releaseSeat(conn, orderId);

            if (releaseCount == 0) {
                conn.rollback();
                return false;
            }

            // 5. 恢复剩余座位
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

    // ================= 处理超时未支付订单 =================
    public int cancelExpiredOrders() {
        List<Order> expiredOrders = orderDao.findExpiredUnpaidOrders();

        int successCount = 0;

        for (Order order : expiredOrders) {
            boolean success = cancelExpiredOrder(order.getId());

            if (success) {
                successCount++;
            }
        }

        return successCount;
    }

    // ================= 取消单个超时订单 =================
    private boolean cancelExpiredOrder(int orderId) {
        Connection conn = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            Order order = orderDao.findByIdForUpdate(conn, orderId);

            if (order == null) {
                conn.rollback();
                return false;
            }

            if (order.getStatus() != OrderDao.STATUS_WAIT_PAY) {
                conn.rollback();
                return false;
            }

            int cancelCount = orderDao.cancelExpiredOrder(conn, orderId);

            if (cancelCount == 0) {
                conn.rollback();
                return false;
            }

            int releaseCount = showtimeSeatDao.releaseSeat(conn, orderId);

            if (releaseCount == 0) {
                conn.rollback();
                return false;
            }

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

    // ================= 根据总座位数生成座位号 =================
    private List<String> generateSeats(int totalSeats) {
        List<String> seats = new ArrayList<>();

        int colCount = 10;
        int count = 0;
        int rowIndex = 0;

        while (count < totalSeats) {
            char row = (char) ('A' + rowIndex);

            for (int col = 1; col <= colCount && count < totalSeats; col++) {
                seats.add(row + String.valueOf(col));
                count++;
            }

            rowIndex++;
        }

        return seats;
    }
}