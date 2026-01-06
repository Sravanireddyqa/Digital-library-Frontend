package com.simats.digitallibrary;

/**
 * User model class
 */
public class User {
    private int id;
    private String name;
    private String email;
    private String type; // "user" or "admin"
    private String status; // "active" or "blocked"
    private String joinedDate;
    private int reservationsCount;

    public User() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(String joinedDate) {
        this.joinedDate = joinedDate;
    }

    public int getReservationsCount() {
        return reservationsCount;
    }

    public void setReservationsCount(int reservationsCount) {
        this.reservationsCount = reservationsCount;
    }
}
