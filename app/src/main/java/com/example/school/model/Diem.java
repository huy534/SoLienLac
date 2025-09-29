package com.example.school.model;

public class Diem {
    private int hocSinhId;
    private int monId;
    private String tenHocSinh;
    private String tenMon;

    private float diemHS1;
    private float diemHS2;
    private float diemThi;
    private float diemTB;
    private String nhanXet;
    private String diemDanh;

    public Diem(int hocSinhId, int monId, float diemHS1, float diemHS2, float diemThi, float diemTB, String nhanXet) {
        this.hocSinhId = hocSinhId;
        this.monId = monId;
        this.diemHS1 = diemHS1;
        this.diemHS2 = diemHS2;
        this.diemThi = diemThi;
        this.diemTB = diemTB;
        this.nhanXet = nhanXet;
    }
    public int getHocSinhId() { return hocSinhId; }
    public void setHocSinhId(int hocSinhId) { this.hocSinhId = hocSinhId; }

    public int getMonId() { return monId; }
    public void setMonId(int monId) { this.monId = monId; }

    public String getTenHocSinh() { return tenHocSinh; }
    public void setTenHocSinh(String tenHocSinh) { this.tenHocSinh = tenHocSinh; }

    public String getTenMon() { return tenMon; }
    public void setTenMon(String tenMon) { this.tenMon = tenMon; }

    public float getDiemHS1() { return diemHS1; }
    public void setDiemHS1(float diemHS1) { this.diemHS1 = diemHS1; }

    public float getDiemHS2() { return diemHS2; }
    public void setDiemHS2(float diemHS2) { this.diemHS2 = diemHS2; }

    public float getDiemThi() { return diemThi; }
    public void setDiemThi(float diemThi) { this.diemThi = diemThi; }

    public float getDiemTB() { return diemTB; }
    public void setDiemTB(float diemTB) { this.diemTB = diemTB; }

    public String getNhanXet() { return nhanXet; }
    public void setNhanXet(String nhanXet) { this.nhanXet = nhanXet; }

    public String getDiemDanh() { return diemDanh; }
    public void setDiemDanh(String diemDanh) { this.diemDanh = diemDanh; }
}
