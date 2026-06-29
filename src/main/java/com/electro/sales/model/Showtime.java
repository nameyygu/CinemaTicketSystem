package com.electro.sales.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Showtime {
    private int id;
    private int movie_id;
    private String hall;
    private LocalDateTime show_time;
    private BigDecimal price;
    private int total_seat;
    private int available_seat;
    private int status;
    private int version;
    private LocalDateTime create_time;
    private LocalDateTime update_time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMovie_id() {
        return movie_id;
    }

    public void setMovie_id(int movie_id) {
        this.movie_id = movie_id;
    }

    public String getHall() {
        return hall;
    }

    public void setHall(String hall) {
        this.hall = hall;
    }

    public LocalDateTime getShow_time() {
        return show_time;
    }

    public void setShow_time(LocalDateTime show_time) {
        this.show_time = show_time;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getTotal_seat() {
        return total_seat;
    }

    public void setTotal_seat(int total_seat) {
        this.total_seat = total_seat;
    }

    public int getAvailable_seat() {
        return available_seat;
    }

    public void setAvailable_seat(int available_seat) {
        this.available_seat = available_seat;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public LocalDateTime getCreate_time() {
        return create_time;
    }

    public void setCreate_time(LocalDateTime create_time) {
        this.create_time = create_time;
    }

    public LocalDateTime getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(LocalDateTime update_time) {
        this.update_time = update_time;
    }


}
