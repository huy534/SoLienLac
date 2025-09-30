package com.example.school.model;

public class NguoiDung {
    private int id;
    private String tenDangNhap;
    private String matKhau;
    private String vaiTro;
    private String email;
    private String sdt;

    public NguoiDung() {}

    public NguoiDung(int id, String tenDangNhap, String matKhau, String vaiTro, String email, String sdt) {
        this.id = id; this.tenDangNhap = tenDangNhap; this.matKhau = matKhau;
        this.vaiTro = vaiTro; this.email = email; this.sdt = sdt;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }
}
