package com.example.school.model;

public class AttendanceReportItem {
    private int monId;
    private String tenMon;
    private int present;    // có mặt
    private int excused;    // vắng có phép
    private int unexcused;  // vắng không phép
    private int late;       // muộn

    public AttendanceReportItem() {}

    // getters / setters
    public int getMonId() { return monId; }
    public void setMonId(int monId) { this.monId = monId; }
    public String getTenMon() { return tenMon; }
    public void setTenMon(String tenMon) { this.tenMon = tenMon; }
    public int getPresent() { return present; }
    public void setPresent(int present) { this.present = present; }
    public int getExcused() { return excused; }
    public void setExcused(int excused) { this.excused = excused; }
    public int getUnexcused() { return unexcused; }
    public void setUnexcused(int unexcused) { this.unexcused = unexcused; }
    public int getLate() { return late; }
    public void setLate(int late) { this.late = late; }
}
