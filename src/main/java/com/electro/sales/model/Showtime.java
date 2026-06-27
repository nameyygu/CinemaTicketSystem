package com.electro.sales.model;

import java.time.LocalDateTime;

public class Showtime {
    private int id;
    private int movie_id;
    private String hall;
    private LocalDateTime show_time;

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
}
