package com.example.school.model;

public class NguoiDung {
    private int id;
    private String tenDangNhap;
    private String matKhau;
    private String vaiTro;
    private String email;
    private String sdt;

    public NguoiDung(int id, String tenDangNhap, String matKhau, String vaiTro, String email, String sdt) {
        this.id = id;
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
        this.email = email;
        this.sdt = sdt;
    }

    public int getId() {
        return id;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public String getEmail() {
        return email;
    }

    public String getSdt() {
        return sdt;
    }
}
