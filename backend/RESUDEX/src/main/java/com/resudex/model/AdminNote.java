package com.resudex.model;

public class AdminNote {
    private int id;
    private int userId;
    private String note;
    private String createdAt;

    public AdminNote() {}

    public AdminNote(int id, int userId, String note, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.note = note;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
