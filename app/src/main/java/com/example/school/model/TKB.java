package com.example.school.model;

import android.database.Cursor;
import androidx.annotation.Nullable;

/**
 * Model cho thời khóa biểu (TKB)
 *
 * Bảng DB tối thiểu: TKB(id, maLop, maMon, thu, tiet)
 * Thêm các trường tenMon/tenLop/tenGv để hiển thị khi join với MonHoc/LopHoc/NguoiDung.
 */
public class TKB {
    private int id;
    private int maLop;
    private int maMon;
    private int maGv;        // optional: id giáo viên (nếu bạn lưu)
    private int thu;
    private int tiet;

    // Fields for display (may be null if not populated by query)
    private String tenMon;
    private String tenLop;
    private String tenGv;

    public TKB() {}

    public TKB(int id, int maLop, int maMon, int maGv, int thu, int tiet,
               @Nullable String tenMon, @Nullable String tenLop, @Nullable String tenGv) {
        this.id = id;
        this.maLop = maLop;
        this.maMon = maMon;
        this.maGv = maGv;
        this.thu = thu;
        this.tiet = tiet;
        this.tenMon = tenMon;
        this.tenLop = tenLop;
        this.tenGv = tenGv;
    }

    public TKB(int maLop, int maMon, int maGv, int thu, int tiet) {
        this(-1, maLop, maMon, maGv, thu, tiet, null, null, null);
    }

    // ===== Getters / Setters =====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMaLop() { return maLop; }
    public void setMaLop(int maLop) { this.maLop = maLop; }

    public int getMaMon() { return maMon; }
    public void setMaMon(int maMon) { this.maMon = maMon; }

    public int getMaGv() { return maGv; }
    public void setMaGv(int maGv) { this.maGv = maGv; }

    public int getThu() { return thu; }
    public void setThu(int thu) { this.thu = thu; }

    public int getTiet() { return tiet; }
    public void setTiet(int tiet) { this.tiet = tiet; }

    public String getTenMon() { return tenMon; }
    public void setTenMon(String tenMon) { this.tenMon = tenMon; }

    public String getTenLop() { return tenLop; }
    public void setTenLop(String tenLop) { this.tenLop = tenLop; }

    public String getTenGv() { return tenGv; }
    public void setTenGv(String tenGv) { this.tenGv = tenGv; }

    /**
     * Trả về id giáo viên nếu có, hoặc -1 nếu không set
     */
    public int getMaGvOrFallback() {
        return maGv;
    }

    @Override
    public String toString() {
        return "TKB{" +
                "id=" + id +
                ", maLop=" + maLop +
                ", maMon=" + maMon +
                ", maGv=" + maGv +
                ", thu=" + thu +
                ", tiet=" + tiet +
                ", tenMon='" + tenMon + '\'' +
                ", tenLop='" + tenLop + '\'' +
                ", tenGv='" + tenGv + '\'' +
                '}';
    }

    // ===== Helper: tạo từ Cursor (nếu query trả về các cột tương ứng) =====
    // Cursor có thể chứa: id, maLop, maMon, maGv, thu, tiet, tenMon, tenLop, tenGv
    public static TKB fromCursor(Cursor c) {
        TKB t = new TKB();
        try {
            // c may or may not contain each column — check by name existence
            if (hasColumn(c, "id")) t.setId(c.getInt(c.getColumnIndexOrThrow("id")));
            if (hasColumn(c, "maLop")) t.setMaLop(c.getInt(c.getColumnIndexOrThrow("maLop")));
            if (hasColumn(c, "maMon")) t.setMaMon(c.getInt(c.getColumnIndexOrThrow("maMon")));
            if (hasColumn(c, "maGv")) t.setMaGv(c.getInt(c.getColumnIndexOrThrow("maGv")));
            if (hasColumn(c, "thu")) t.setThu(c.getInt(c.getColumnIndexOrThrow("thu")));
            if (hasColumn(c, "tiet")) t.setTiet(c.getInt(c.getColumnIndexOrThrow("tiet")));
            if (hasColumn(c, "tenMon")) t.setTenMon(c.getString(c.getColumnIndexOrThrow("tenMon")));
            if (hasColumn(c, "tenLop")) t.setTenLop(c.getString(c.getColumnIndexOrThrow("tenLop")));
            if (hasColumn(c, "tenGv")) t.setTenGv(c.getString(c.getColumnIndexOrThrow("tenGv")));
        } catch (Exception ex) {
            // ignore, trả object partial nếu thiếu cột
        }
        return t;
    }

    // Kiểm tra cursor có column hay không
    private static boolean hasColumn(Cursor c, String columnName) {
        try {
            return c.getColumnIndex(columnName) != -1;
        } catch (Exception ex) {
            return false;
        }
    }
}
