package com.example.school.model;

public class ThongBao {
    private int id;
    private String guiTu;
    private String tieuDe;
    private String noiDung;
    private String thoiGian;     // lưu dạng "yyyy-MM-dd HH:mm"
    private String targetRole;   // "teacher", "parent", "all"
    private String senderRole; // optional: gửi đến người cụ thể (nullable)

    public ThongBao() {
    }

    public ThongBao(int id, String guiTu, String tieuDe, String noiDung, String thoiGian, String targetRole, String senderRole) {
        this.id = id;
        this.guiTu = guiTu;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.thoiGian = thoiGian;
        this.targetRole = targetRole;
        this.senderRole = senderRole;
    }

    // getters / setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGuiTu() {
        return guiTu;
    }

    public void setGuiTu(String guiTu) {
        this.guiTu = guiTu;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(String thoiGian) {
        this.thoiGian = thoiGian;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }
}