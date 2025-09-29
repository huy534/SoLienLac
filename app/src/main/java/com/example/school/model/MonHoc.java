package com.example.school.model;

public class MonHoc {
    private int id;
    private String tenMon;
    private int soTiet;
    private int teacherId; // id user (NguoiDung) của giáo viên phụ trách

    public MonHoc() {}

    public MonHoc(int id, String tenMon, int soTiet, int teacherId) {
        this.id = id;
        this.tenMon = tenMon;
        this.soTiet = soTiet;
        this.teacherId = teacherId;
    }

    public MonHoc(int id, String tenMon, int soTiet) {
        this(id, tenMon, soTiet, -1);
    }

    // getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTenMon() { return tenMon; }
    public void setTenMon(String tenMon) { this.tenMon = tenMon; }

    public int getSoTiet() { return soTiet; }
    public void setSoTiet(int soTiet) { this.soTiet = soTiet; }

    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }
}
