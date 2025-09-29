package com.example.school.model;

public class LopHoc {
    private int id;
    private String tenLop;
    private String khoi;
    private String namHoc;
    private int siSo;
    private int gvcn;

    public LopHoc(int id, String tenLop, String khoi, String namHoc, int siSo, int gvcn) {
        this.id = id;
        this.tenLop = tenLop;
        this.khoi = khoi;
        this.namHoc = namHoc;
        this.siSo = siSo;
        this.gvcn = gvcn;
    }

    // getters
    public int getId() { return id; }
    public String getTenLop() { return tenLop; }
    public String getKhoi() { return khoi; }
    public String getNamHoc() { return namHoc; }
    public int getSiSo() { return siSo; }
    public int getGvcn() { return gvcn; }
}
