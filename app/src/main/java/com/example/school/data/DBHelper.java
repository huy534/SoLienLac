package com.example.school.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.school.auth.HashUtils;
import com.example.school.model.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";
    private static final String DB_NAME = "school.db";
    private static final int DB_VERSION = 49; 
    private Diem buildDiemFromCursor(Cursor c) {
        Diem d = new Diem(
                c.getInt(c.getColumnIndexOrThrow("hocSinhId")),
                c.getInt(c.getColumnIndexOrThrow("monId")),
                c.getFloat(c.getColumnIndexOrThrow("diemHS1")),
                c.getFloat(c.getColumnIndexOrThrow("diemHS2")),
                c.getFloat(c.getColumnIndexOrThrow("diemThi")),
                c.getFloat(c.getColumnIndexOrThrow("diemTB")),
                c.getString(c.getColumnIndexOrThrow("nhanXet"))
        );
        if (hasColumn(c, "tenHocSinh")) d.setTenHocSinh(c.getString(c.getColumnIndexOrThrow("tenHocSinh")));
        if (hasColumn(c, "tenMon")) d.setTenMon(c.getString(c.getColumnIndexOrThrow("tenMon")));
        if (hasColumn(c, "tenLop")) d.setTenLop(c.getString(c.getColumnIndexOrThrow("tenLop")));
        return d;
    }

    // helper an toàn để tránh exception nếu cột không t��n tại
    private boolean hasColumn(Cursor cursor, String columnName) {
        try {
            return cursor.getColumnIndex(columnName) != -1;
        } catch (Exception e) {
            return false;
        }
    }

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    public static class SubjectAttendance {
        public int monId;
        public String tenMon;
        public int countPresent;     // status == 0
        public int countPermitLeave; // status == 1
        public int countAbsent;      // status == 2
        public int countLate;        // status == 3

        public SubjectAttendance(int monId, String tenMon) {
            this.monId = monId;
            this.tenMon = tenMon == null ? "Môn " + monId : tenMon;
        }

        public int total() {
            return countPresent + countPermitLeave + countAbsent + countLate;
        }
        public void addPresent(int c) { this.countPresent += c; }
        public void addPermitLeave(int c) { this.countPermitLeave += c; }
        public void addAbsent(int c) { this.countAbsent += c; }
        public void addLate(int c) { this.countLate += c; }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // NguoiDung
        db.execSQL("CREATE TABLE IF NOT EXISTS NguoiDung (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "hoTen TEXT," +
                "tenDangNhap TEXT UNIQUE," +
                "matKhau TEXT," +
                "vaiTro TEXT," +
                "email TEXT," +
                "sdt TEXT)");

        // LopHoc
        db.execSQL("CREATE TABLE IF NOT EXISTS LopHoc (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tenLop TEXT," +
                "khoi TEXT," +
                "namHoc TEXT," +
                "siSo INTEGER," +
                "gvcn INTEGER)");

        // HocSinh
        db.execSQL("CREATE TABLE IF NOT EXISTS HocSinh (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "hoTen TEXT," +
                "ngaySinh TEXT," +
                "gioiTinh TEXT," +
                "queQuan TEXT," +
                "maLop INTEGER)");

        // MonHoc
        db.execSQL("CREATE TABLE IF NOT EXISTS MonHoc (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tenMon TEXT," +
                "soTiet INTEGER," +
                "teacherId INTEGER)"); 

        // Diem (expanded)
        db.execSQL("CREATE TABLE IF NOT EXISTS Diem (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "hocSinhId INTEGER," +
                "monId INTEGER," +
                "diemHS1 REAL," +
                "diemHS2 REAL," +
                "diemThi REAL," +
                "diemTB REAL," +
                "nhanXet TEXT)");

        // Attendance
        db.execSQL("CREATE TABLE IF NOT EXISTS Attendance (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "hocSinhId INTEGER," +
                "monId INTEGER," +
                "ngay TEXT," +    // yyyy-MM-dd
                "status INTEGER)"); // 0: Có mặt,1: Vắng có phép,2: Vắng không phép,3: Muộn

        // TKB
        db.execSQL("CREATE TABLE IF NOT EXISTS TKB (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "maLop INTEGER," +
                "maMon INTEGER," +
                "thu INTEGER," +
                "tiet INTEGER)");

        // ParentStudent mapping
        db.execSQL("CREATE TABLE IF NOT EXISTS ParentStudent (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "userId INTEGER," +
                "studentId INTEGER," +
                "UNIQUE(userId, studentId))");
        db.execSQL("CREATE TABLE IF NOT EXISTS ThongBao (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "guiTu TEXT," +
                "tieuDe TEXT," +
                "noiDung TEXT," +
                "thoiGian TEXT," +
                "targetRole TEXT," +
                "senderRole TEXT," +
                "targetUserId INTEGER"
                + ")");
        seedData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading DB from " + oldVersion + " to " + newVersion + " - dropping all");
        db.execSQL("DROP TABLE IF EXISTS NguoiDung");
        db.execSQL("DROP TABLE IF EXISTS LopHoc");
        db.execSQL("DROP TABLE IF EXISTS HocSinh");
        db.execSQL("DROP TABLE IF EXISTS MonHoc");
        db.execSQL("DROP TABLE IF EXISTS Diem");
        db.execSQL("DROP TABLE IF EXISTS Attendance");
        db.execSQL("DROP TABLE IF EXISTS TKB");
        db.execSQL("DROP TABLE IF EXISTS ParentStudent");
        db.execSQL("DROP TABLE IF EXISTS ThongBao"); // <- ensure ThongBao is dropped so new schema is created
        onCreate(db);
    }
    // ----------------- Seed helpers -----------------
    private boolean exists(SQLiteDatabase db, String table, String whereClause, String[] whereArgs) {
        Cursor c = db.rawQuery("SELECT 1 FROM " + table + " WHERE " + whereClause + " LIMIT 1", whereArgs);
        boolean ok = c.moveToFirst();
        c.close();
        return ok;
    }

    private long safeInsertUser(SQLiteDatabase db, String username, String password, String role, String email, String sdt) {
        if (exists(db, "NguoiDung", "tenDangNhap=?", new String[]{username})) return -1;
        String hashed = HashUtils.sha256(password);
        db.execSQL("INSERT INTO NguoiDung (tenDangNhap, matKhau, vaiTro, email, sdt) VALUES (?,?,?,?,?)",
                new Object[]{username, hashed, role, email, sdt});
        Cursor c = db.rawQuery("SELECT id FROM NguoiDung WHERE tenDangNhap=?", new String[]{username});
        long id = -1;
        if (c.moveToFirst()) {
            id = c.getLong(0);
            c.close();
        }
        return id;
    }

    private int getUserIdByUsername(SQLiteDatabase db, String username) {
        Cursor c = db.rawQuery("SELECT id FROM NguoiDung WHERE tenDangNhap=?", new String[]{username});
        int id = -1;
        if (c.moveToFirst()) {
            id = c.getInt(0);
            c.close();
        }
        return id;
    }

    private void seedData(SQLiteDatabase db) {
        // ---------- USERS ----------
        safeInsertUser(db, "admin", "123", "admin", "admin@gmail.com", "0900000001");

        // 9 giáo viên
        String[] gvNames = {"gvtoan1", "gvvan1", "gvanh1", "gvly1", "gvhoa1", "gvsinh1", "gvtin1", "gvls1", "gvdia1"};
        for (int i = 0; i < gvNames.length; i++) {
            safeInsertUser(db, gvNames[i], "123", "giaovien", gvNames[i] + "@gmail.com", "09100000" + (i + 1));
        }

        // 50 phụ huynh
        for (int i = 1; i <= 50; i++) {
            safeInsertUser(db, "ph" + i, "123", "phuhuynh", "ph" + i + "@gmail.com", "09200000" + i);
        }
        // Lấy id GV
        int idGvToan = getUserIdByUsername(db, "gvtoan1");
        int idGvVan = getUserIdByUsername(db, "gvvan1");
        int idGvAnh = getUserIdByUsername(db, "gvanh1");
        int idGvLy = getUserIdByUsername(db, "gvly1");
        int idGvHoa = getUserIdByUsername(db, "gvhoa1");
        int idGvSinh = getUserIdByUsername(db, "gvsinh1");
        int idGvTin = getUserIdByUsername(db, "gvtin1");
        int idGvLs = getUserIdByUsername(db, "gvls1");
        int idGvDia = getUserIdByUsername(db, "gvdia1");

        // ---------- LOP HOC ----------
        String[] tenLop = {"10A1", "10A2", "10A3", "10A4", "11A1", "11A2", "11A3", "12A1", "12A2", "12A3"};
        int[] gvcnArr = {idGvToan, idGvVan, idGvAnh, idGvLy, idGvHoa, idGvSinh, idGvTin, idGvLs, idGvDia, idGvToan};

        for (int i = 0; i < tenLop.length; i++) {
            if (!exists(db, "LopHoc", "tenLop=?", new String[]{tenLop[i]})) {
                db.execSQL("INSERT INTO LopHoc (tenLop,khoi,namHoc,siSo,gvcn) VALUES (?,?,?,?,?)",
                        new Object[]{tenLop[i], tenLop[i].substring(0, 2), "2024-2025", 30, gvcnArr[i]});
            }
        }

        // ---------- HOC SINH ----------
        String[][] hocSinhs = {
                {"Nguyen Van A", "2008-01-05", "Nam", "Ha Noi", "1"},
                {"Tran Thi B", "2008-02-14", "Nu", "Hai Phong", "1"},
                {"Le Van C", "2008-03-22", "Nam", "Nam Dinh", "2"},
                {"Pham Thi D", "2008-04-10", "Nu", "Ha Noi", "2"},
                {"Hoang Van E", "2008-05-18", "Nam", "Thai Binh", "3"},
                {"Do Thi F", "2008-06-21", "Nu", "Thanh Hoa", "3"},
                {"Ngo Van G", "2008-07-12", "Nam", "Ha Nam", "4"},
                {"Bui Thi H", "2008-08-15", "Nu", "Hai Duong", "4"},
                {"Nguyen Van I", "2008-09-09", "Nam", "Ha Noi", "5"},
                {"Tran Thi K", "2008-10-30", "Nu", "Nam Dinh", "5"},
                {"Pham Van L", "2008-11-20", "Nam", "Hai Phong", "6"},
                {"Le Thi M", "2008-12-25", "Nu", "Ha Noi", "6"},
                {"Hoang Van N", "2009-01-15", "Nam", "Thanh Hoa", "7"},
                {"Do Thi O", "2009-02-18", "Nu", "Nghe An", "7"},
                {"Ngo Van P", "2009-03-12", "Nam", "Ha Tinh", "8"},
                {"Bui Thi Q", "2009-04-22", "Nu", "Hai Duong", "8"},
                {"Nguyen Van R", "2009-05-11", "Nam", "Ha Noi", "9"},
                {"Tran Thi S", "2009-06-07", "Nu", "Nam Dinh", "9"},
                {"Pham Van T", "2009-07-09", "Nam", "Hai Phong", "10"},
                {"Le Thi U", "2009-08-19", "Nu", "Ha Noi", "10"},
                {"Hoang Van V", "2009-09-14", "Nam", "Ha Nam", "1"},
                {"Do Thi W", "2009-10-29", "Nu", "Thanh Hoa", "2"},
                {"Ngo Van X", "2009-11-03", "Nam", "Nghe An", "3"},
                {"Bui Thi Y", "2009-12-08", "Nu", "Ha Noi", "4"},
                {"Nguyen Van Z", "2010-01-17", "Nam", "Hai Phong", "5"},
                {"Tran Thi AA", "2010-02-23", "Nu", "Nam Dinh", "6"},
                {"Pham Van BB", "2010-03-15", "Nam", "Ha Noi", "7"}
        };

        for (String[] hs : hocSinhs) {
            if (!exists(db, "HocSinh", "hoTen=?", new String[]{hs[0]})) {
                db.execSQL("INSERT INTO HocSinh (hoTen,ngaySinh,gioiTinh,queQuan,maLop) VALUES (?,?,?,?,?)",
                        new Object[]{hs[0], hs[1], hs[2], hs[3], Integer.parseInt(hs[4])});
            }
        }
        // ---------- TKB ----------
        if (!exists(db, "TKB", "1=1", new String[]{})) {
            // mapping môn -> id (theo seed MonHoc bên trên)
            int idToan = 1, idVan = 2, idAnh = 3, idLy = 4, idHoa = 5, idSinh = 6;

            // Danh sách lớp (id = 1..10 theo seed LopHoc)
            for (int lopId = 1; lopId <= 10; lopId++) {
                // mỗi lớp có 5 ngày học (thứ 2 -> thứ 6), mỗi ngày 3 tiết
                for (int thu = 2; thu <= 6; thu++) {
                    for (int tiet = 1; tiet <= 3; tiet++) {
                        int maMon;
                        switch ((lopId + thu + tiet) % 6) {
                            case 0:
                                maMon = idToan;
                                break;
                            case 1:
                                maMon = idVan;
                                break;
                            case 2:
                                maMon = idAnh;
                                break;
                            case 3:
                                maMon = idLy;
                                break;
                            case 4:
                                maMon = idHoa;
                                break;
                            default:
                                maMon = idSinh;
                                break;
                        }
                        db.execSQL("INSERT INTO TKB (maLop, maMon, thu, tiet) VALUES (?,?,?,?)",
                                new Object[]{lopId, maMon, thu, tiet});
                    }
                }
            }
        }
        db.execSQL("INSERT INTO ThongBao(guiTu, tieuDe, noiDung, thoiGian, senderRole, targetRole, targetUserId) VALUES " +
                "('admin', 'Lịch họp', 'Tất cả giáo viên họp chiều thứ 2.', '2025-10-02 09:00:00', 'admin', 'giaovien', NULL)," +
                "('admin', 'Thông báo nghỉ học', 'Toàn trường nghỉ học ngày 7/10.', '2025-10-06 10:00:00', 'admin', 'all', NULL)," +
                "('admin', 'Phân công coi thi', 'Danh sách phân công coi thi đã được gửi.', '2025-10-01 14:30:00', 'admin', 'giaovien', NULL)," +
                "('admin', 'Hoạt động', 'Các học sinh sẽ tham gia hoạt động ngoại khóa vào ngày 10/10.', '2025-10-01 08:45:00', 'admin', 'phuhuynh', NULL);");

        // ---------- MON HOC ----------
        Object[][] monHocArr = {
                {"Toán", 4, idGvToan},
                {"Văn", 3, idGvVan},
                {"Anh", 3, idGvAnh},
                {"Vật Lý", 3, idGvLy},
                {"Hóa Học", 3, idGvHoa},
                {"Sinh Học", 2, idGvSinh}
        };

        for (Object[] mh : monHocArr) {
            if (!exists(db, "MonHoc", "tenMon=?", new String[]{(String) mh[0]})) {
                db.execSQL("INSERT INTO MonHoc (tenMon,soTiet,teacherId) VALUES (?,?,?)",
                        new Object[]{mh[0], mh[1], mh[2]});
            }
        }

        // ---------- DIEM (50 bản ghi ngẫu nhiên) ----------
        Random rnd = new Random();
        int inserted = 0;
        outer:
        for (int hsId = 1; hsId <= 27; hsId++) {
            for (int monId = 1; monId <= 6; monId++) {
                if (inserted >= 200) break outer;
                if (!exists(db, "Diem", "hocSinhId=? AND monId=?", new String[]{String.valueOf(hsId), String.valueOf(monId)})) {
                    float hs1 = 5 + rnd.nextFloat() * 5;
                    float hs2 = 5 + rnd.nextFloat() * 5;
                    float thi = 5 + rnd.nextFloat() * 5;
                    float tb = computeTB(hs1, hs2, thi);
                    db.execSQL("INSERT INTO Diem (hocSinhId,monId,diemHS1,diemHS2,diemThi,diemTB,nhanXet) VALUES (?,?,?,?,?,?,?)",
                            new Object[]{hsId, monId, hs1, hs2, thi, tb, "OK"});
                    inserted++;
                }
            }
        }
        String[] dates = {"2025-09-01", "2025-09-02", "2025-09-03"}; // danh sách ngày
        int[] monIds = {1, 2, 3, 4, 5, 6}; // danh sách môn
        int[] studentIds = new int[27];
        for (int i = 0; i < 27; i++) studentIds[i] = i + 1; // id học sinh từ 1 đến 27

        for (int studentId : studentIds) {
            for (int monId : monIds) {
                for (String date : dates) {
                    int status = (int) (Math.random() * 4); // 0,1,2,3 ví dụ trạng thái
                    // kiểm tra nếu chưa tồn tại
                    if (!exists(db, "Attendance", "hocSinhId=? AND monId=? AND ngay=?",
                            new String[]{String.valueOf(studentId), String.valueOf(monId), date})) {
                        db.execSQL(
                                "INSERT INTO Attendance (hocSinhId, monId, ngay, status) VALUES (?,?,?,?)",
                                new Object[]{studentId, monId, date, status}
                        );
                    }
                }
            }
        }



        // ---------- Parent-Student mapping ----------
        for (int i = 1; i <= 50; i++) {
            int studentId = (i % 27) + 1; // gán tuần tự cho 27 HS
            int parentId = getUserIdByUsername(db, "ph" + i);
            if (parentId != -1 && !exists(db, "ParentStudent", "userId=? AND studentId=?",
                    new String[]{String.valueOf(parentId), String.valueOf(studentId)})) {
                db.execSQL("INSERT INTO ParentStudent (userId, studentId) VALUES (?,?)",
                        new Object[]{parentId, studentId});
            }
        }
    }

    public int getLopIdByGVCN(int gvId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM LopHoc WHERE gvcn=? LIMIT 1", new String[]{String.valueOf(gvId)});
        int id = -1;
        if (c.moveToFirst()) id = c.getInt(0);
        c.close();
        db.close();
        return id;
    }

    public List<HocSinh> getHocSinhByLop(int lopId) {
        List<HocSinh> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM HocSinh WHERE maLop=?", new String[]{String.valueOf(lopId)});
        while (c.moveToNext()) {
            list.add(new HocSinh(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("hoTen")),
                    c.getString(c.getColumnIndexOrThrow("ngaySinh")),
                    c.getString(c.getColumnIndexOrThrow("gioiTinh")),
                    c.getString(c.getColumnIndexOrThrow("queQuan")),
                    c.getInt(c.getColumnIndexOrThrow("maLop"))
            ));
        }
        c.close();
        db.close();
        return list;
    }

    // ----------------- AUTH -----------------
    public NguoiDung login(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashed = HashUtils.sha256(password);
        Cursor c = db.rawQuery("SELECT * FROM NguoiDung WHERE tenDangNhap=? AND matKhau=?", new String[]{username, hashed});
        NguoiDung u = null;
        if (c.moveToFirst()) {
            u = new NguoiDung(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("hoTen")),
                    c.getString(c.getColumnIndexOrThrow("tenDangNhap")),
                    c.getString(c.getColumnIndexOrThrow("matKhau")),
                    c.getString(c.getColumnIndexOrThrow("vaiTro")),
                    c.getString(c.getColumnIndexOrThrow("email")),
                    c.getString(c.getColumnIndexOrThrow("sdt"))
            );
            c.close();
        } else c.close();
        db.close();
        return u;
    }

    public boolean registerUser(String username, String password, String role, String email, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("tenDangNhap", username);
        cv.put("matKhau", HashUtils.sha256(password)); // Hash SHA-256
        cv.put("vaiTro", role);
        cv.put("email", email);
        cv.put("sdt", phone);
        long result = db.insert("NguoiDung", null, cv);
        return result != -1;
    }

    public List<NguoiDung> getAllUsers() {
        List<NguoiDung> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM NguoiDung", null);
        while (c.moveToNext()) {
            NguoiDung u = new NguoiDung(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("hoTen")),
                    c.getString(c.getColumnIndexOrThrow("tenDangNhap")),
                    c.getString(c.getColumnIndexOrThrow("matKhau")),
                    c.getString(c.getColumnIndexOrThrow("vaiTro")),
                    c.getString(c.getColumnIndexOrThrow("email")),
                    c.getString(c.getColumnIndexOrThrow("sdt"))
            );
            list.add(u);
        }
        c.close();
        db.close();
        return list;
    }
    // Lấy người dùng theo vai trò
    public List<NguoiDung> getUsersByRole(String role) {
        List<NguoiDung> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM NguoiDung WHERE vaiTro=?", new String[]{role});
        while (c.moveToNext()) {
            list.add(new NguoiDung(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("hoTen")),
                    c.getString(c.getColumnIndexOrThrow("tenDangNhap")),
                    c.getString(c.getColumnIndexOrThrow("matKhau")),
                    c.getString(c.getColumnIndexOrThrow("vaiTro")),
                    c.getString(c.getColumnIndexOrThrow("email")),
                    c.getString(c.getColumnIndexOrThrow("sdt"))
            ));
        }
        c.close();
        db.close();
        return list;
    }

    // Lấy người dùng theo id


    // Cập nhật user (nếu password rỗng => giữ nguyên)


    // Xóa user (trả về số rows)
    public int deleteUser(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete("NguoiDung", "id=?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }
    // ----------------- MonHoc -----------------
    public long insertMonHoc(MonHoc mh) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("tenMon", mh.getTenMon());
        cv.put("soTiet", mh.getSoTiet());
        cv.put("teacherId", mh.getTeacherId());
        long id = db.insert("MonHoc", null, cv);
        db.close();
        return id;
    }

    public int updateMonHoc(MonHoc mh) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("tenMon", mh.getTenMon());
        cv.put("soTiet", mh.getSoTiet());
        cv.put("teacherId", mh.getTeacherId());
        int rows = db.update("MonHoc", cv, "id=?", new String[]{String.valueOf(mh.getId())});
        db.close();
        return rows;
    }

    public int deleteMonHoc(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete("MonHoc", "id=?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public List<MonHoc> getAllMonHoc() {
        List<MonHoc> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM MonHoc", null);
        while (c.moveToNext()) {
            list.add(new MonHoc(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("tenMon")),
                    c.getInt(c.getColumnIndexOrThrow("soTiet")),
                    c.getInt(c.getColumnIndexOrThrow("teacherId"))
            ));
        }
        c.close();
        db.close();
        return list;
    }

    public List<MonHoc> getMonByTeacher(int teacherUserId) {
        List<MonHoc> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM MonHoc WHERE teacherId=?", new String[]{String.valueOf(teacherUserId)});
        while (c.moveToNext()) {
            list.add(new MonHoc(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("tenMon")),
                    c.getInt(c.getColumnIndexOrThrow("soTiet")),
                    c.getInt(c.getColumnIndexOrThrow("teacherId"))
            ));
        }
        c.close();
        db.close();
        return list;
    }

    public String getTeacherNameById(int userId) {
        if (userId <= 0) return "";
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT tenDangNhap FROM NguoiDung WHERE id=?", new String[]{String.valueOf(userId)});
        String name = "";
        if (c.moveToFirst()) {
            name = c.getString(0);
            c.close();
        } else c.close();
        db.close();
        return name;
    }

    public MonHoc getMonHocById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM MonHoc WHERE id=?", new String[]{String.valueOf(id)});
        MonHoc m = null;
        if (c.moveToFirst()) {
            m = new MonHoc(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("tenMon")),
                    c.getInt(c.getColumnIndexOrThrow("soTiet")),
                    c.getInt(c.getColumnIndexOrThrow("teacherId"))
            );
            c.close();
        } else c.close();
        db.close();
        return m;
    }
    public boolean isTeacherTeachesSubject(int teacherId, int monId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT 1 FROM MonHoc WHERE id=? AND gvPhuTrachId=?",
                new String[]{String.valueOf(monId), String.valueOf(teacherId)});
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    // ---------------- HOC SINH ----------------
    public List<HocSinh> getAllHocSinh() {
        List<HocSinh> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM HocSinh", null);
        if (c.moveToFirst()) {
            do {
                HocSinh hs = new HocSinh(0, null, null, null, null, 1);
                hs.setId(c.getInt(c.getColumnIndexOrThrow("id")));
                hs.setHoTen(c.getString(c.getColumnIndexOrThrow("hoTen")));
                hs.setNgaySinh(c.getString(c.getColumnIndexOrThrow("ngaySinh")));
                hs.setGioiTinh(c.getString(c.getColumnIndexOrThrow("gioiTinh")));
                hs.setQueQuan(c.getString(c.getColumnIndexOrThrow("queQuan")));
                hs.setMaLop(c.getInt(c.getColumnIndexOrThrow("maLop")));
                list.add(hs);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public long insertHocSinh(HocSinh hs) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("hoTen", hs.getHoTen());
        values.put("ngaySinh", hs.getNgaySinh());
        values.put("gioiTinh", hs.getGioiTinh());
        values.put("queQuan", hs.getQueQuan());
        values.put("maLop", hs.getMaLop());
        return db.insert("HocSinh", null, values);
    }

    public int updateHocSinh(HocSinh hs) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("hoTen", hs.getHoTen());
        values.put("ngaySinh", hs.getNgaySinh());
        values.put("gioiTinh", hs.getGioiTinh());
        values.put("queQuan", hs.getQueQuan());
        values.put("maLop", hs.getMaLop());
        return db.update("HocSinh", values, "id=?", new String[]{String.valueOf(hs.getId())});
    }

    public int deleteHocSinh(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("HocSinh", "id=?", new String[]{String.valueOf(id)});
    }

    // ----------------- Diem (upsert, get) -----------------
    private static float computeTB(float hs1, float hs2, float thi) {
        return (float) ((hs1 + 2.0 * hs2 + 3.0 * thi) / 6.0);
    }

    public long upsertDiem(int hocSinhId, int monId, float hs1, float hs2, float thi, String nhanXet) {
        SQLiteDatabase db = getWritableDatabase();
        float tb = computeTB(hs1, hs2, thi);
        ContentValues cv = new ContentValues();
        cv.put("diemHS1", hs1);
        cv.put("diemHS2", hs2);
        cv.put("diemThi", thi);
        cv.put("diemTB", tb);
        cv.put("nhanXet", nhanXet);

        int rows = db.update("Diem", cv, "hocSinhId=? AND monId=?",
                new String[]{String.valueOf(hocSinhId), String.valueOf(monId)});
        if (rows == 0) {
            cv.put("hocSinhId", hocSinhId);
            cv.put("monId", monId);
            long id = db.insert("Diem", null, cv);
            db.close();
            return id;
        } else {
            db.close();
            return rows;
        }
    }

    // Get Diem rows for parent (students mapped)
    public List<Diem> getDiemByParent(int parentUserId) {
        List<Diem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT d.*, hs.hoTen, l.tenLop, m.tenMon " +
                        "FROM Diem d " +
                        "JOIN HocSinh hs ON d.hocSinhId = hs.id " +
                        "JOIN LopHoc l ON hs.maLop = l.id " +
                        "JOIN MonHoc m ON d.monId = m.id " +
                        "JOIN ParentStudent ps ON hs.id = ps.studentId " +
                        "WHERE ps.userId = ?",
                new String[]{String.valueOf(parentUserId)}
        );
        while (c.moveToNext()) {
            Diem d = buildDiemFromCursor(c);
            list.add(d);
        }
        c.close();
        return list;
    }
    // Diem by teacher
    public List<Diem> getDiemByTeacher(int gvId) {
        List<Diem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT d.*, hs.hoTen AS tenHocSinh, l.tenLop AS tenLop, m.tenMon AS tenMon " +
                "FROM Diem d " +
                "JOIN HocSinh hs ON d.hocSinhId = hs.id " +
                "JOIN LopHoc l ON hs.maLop = l.id " +
                "JOIN MonHoc m ON d.monId = m.id " +
                "WHERE m.teacherId = ? " +
                "ORDER BY hs.hoTen";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(gvId)});
        while (c.moveToNext()) {
            Diem d = buildDiemFromCursor(c); // dùng helper
            list.add(d);
        }
        c.close();
        return list;
    }


    // ----------------- Attendance -----------------
    public String getAttendanceStatus(int hocSinhId, int monId, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT status FROM Attendance WHERE hocSinhId=? AND monId=? AND ngay=?";
        Cursor c = db.rawQuery(sql, new String[]{
                String.valueOf(hocSinhId),
                String.valueOf(monId),
                date
        });
        String result = "Vắng không phép"; // default
        if (c.moveToFirst()) {
            result = c.getString(0);
        }
        c.close();
        return result;
    }

    public long markAttendance(int hocSinhId, int monId, String ngay, int status) {
        SQLiteDatabase db = getWritableDatabase();
        // update if exists
        if (exists(db, "Attendance", "hocSinhId=? AND monId=? AND ngay=?", new String[]{String.valueOf(hocSinhId), String.valueOf(monId), ngay})) {
            ContentValues cv = new ContentValues();
            cv.put("status", status);
            int rows = db.update("Attendance", cv, "hocSinhId=? AND monId=? AND ngay=?", new String[]{String.valueOf(hocSinhId), String.valueOf(monId), ngay});
            db.close();
            return rows;
        } else {
            ContentValues cv = new ContentValues();
            cv.put("hocSinhId", hocSinhId);
            cv.put("monId", monId);
            cv.put("ngay", ngay);
            cv.put("status", status);
            long id = db.insert("Attendance", null, cv);
            db.close();
            return id;
        }
    }

    // ----------------- TKB -----------------

    /**
     * Insert TKB entry
     */
    public long insertTKB(TKB tkb) {
        SQLiteDatabase wdb = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("maLop", tkb.getMaLop());
        cv.put("maMon", tkb.getMaMon());
        cv.put("thu", tkb.getThu());
        cv.put("tiet", tkb.getTiet());
        long id = wdb.insert("TKB", null, cv);
        wdb.close();
        return id;
    }

    /**
     * Update TKB entry by id
     */
    public int updateTKB(TKB tkb) {
        SQLiteDatabase wdb = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("maLop", tkb.getMaLop());
        cv.put("maMon", tkb.getMaMon());
        cv.put("thu", tkb.getThu());
        cv.put("tiet", tkb.getTiet());
        int rows = wdb.update("TKB", cv, "id=?", new String[]{String.valueOf(tkb.getId())});
        wdb.close();
        return rows;
    }

    /**
     * Delete TKB by id
     */
    public int deleteTKB(int id) {
        SQLiteDatabase wdb = getWritableDatabase();
        int rows = wdb.delete("TKB", "id=?", new String[]{String.valueOf(id)});
        wdb.close();
        return rows;
    }

    /**
     * Get all TKB with joined display fields (tenMon, tenLop, tenGv)
     */
    public List<TKB> getAllTKB() {
        List<TKB> list = new ArrayList<>();
        SQLiteDatabase rdb = getReadableDatabase();
        String sql = "SELECT t.id, t.maLop, t.maMon, t.thu, t.tiet, m.tenMon as tenMon, l.tenLop as tenLop, n.tenDangNhap as tenGv " +
                "FROM TKB t " +
                "LEFT JOIN MonHoc m ON t.maMon = m.id " +
                "LEFT JOIN LopHoc l ON t.maLop = l.id " +
                "LEFT JOIN NguoiDung n ON m.teacherId = n.id " +
                "ORDER BY t.maLop, t.thu, t.tiet";
        Cursor c = rdb.rawQuery(sql, null);
        while (c.moveToNext()) {
            TKB t = new TKB();
            t.setId(c.getInt(c.getColumnIndexOrThrow("id")));
            t.setMaLop(c.getInt(c.getColumnIndexOrThrow("maLop")));
            t.setMaMon(c.getInt(c.getColumnIndexOrThrow("maMon")));
            t.setThu(c.getInt(c.getColumnIndexOrThrow("thu")));
            t.setTiet(c.getInt(c.getColumnIndexOrThrow("tiet")));
            // optional display fields
            try {
                t.setTenMon(c.getString(c.getColumnIndexOrThrow("tenMon")));
            } catch (Exception ignored) {
            }
            try {
                t.setTenLop(c.getString(c.getColumnIndexOrThrow("tenLop")));
            } catch (Exception ignored) {
            }
            try {
                t.setTenGv(c.getString(c.getColumnIndexOrThrow("tenGv")));
            } catch (Exception ignored) {
            }
            list.add(t);
        }
        c.close();
        rdb.close();
        return list;
    }

    /**
     * Get TKB entries for a teacher (based on the teacherId registered on MonHoc.teacherId)
     * Returns joined fields (tenMon, tenLop).
     */
    public List<TKB> getTKBByTeacher(int teacherId) {
        List<TKB> list = new ArrayList<>();
        SQLiteDatabase rdb = getReadableDatabase();
        String sql = "SELECT t.id, t.maLop, t.maMon, t.thu, t.tiet, m.tenMon as tenMon, l.tenLop as tenLop " +
                "FROM TKB t " +
                "LEFT JOIN MonHoc m ON t.maMon = m.id " +
                "LEFT JOIN LopHoc l ON t.maLop = l.id " +
                "WHERE m.teacherId = ? " +
                "ORDER BY t.thu, t.tiet";
        Cursor c = rdb.rawQuery(sql, new String[]{String.valueOf(teacherId)});
        if (c != null) {
            while (c.moveToNext()) {
                TKB t = new TKB();
                t.setId(c.getInt(c.getColumnIndexOrThrow("id")));
                t.setMaLop(c.getInt(c.getColumnIndexOrThrow("maLop")));
                t.setMaMon(c.getInt(c.getColumnIndexOrThrow("maMon")));
                t.setThu(c.getInt(c.getColumnIndexOrThrow("thu")));
                t.setTiet(c.getInt(c.getColumnIndexOrThrow("tiet")));
                try {
                    t.setTenMon(c.getString(c.getColumnIndexOrThrow("tenMon")));
                } catch (Exception ignored) {
                }
                try {
                    t.setTenLop(c.getString(c.getColumnIndexOrThrow("tenLop")));
                } catch (Exception ignored) {
                }
                list.add(t);
            }
            c.close();
        }
        rdb.close();
        return list;
    }

    /**
     * Get TKB entries for a student -> find the student's class (maLop) and return all TKB for that class
     */
    public List<TKB> getTKBByStudent(int studentId) {
        List<TKB> list = new ArrayList<>();
        SQLiteDatabase rdb = getReadableDatabase();
        // get class
        int maLop = -1;
        Cursor c1 = rdb.rawQuery("SELECT maLop FROM HocSinh WHERE id=?", new String[]{String.valueOf(studentId)});
        if (c1.moveToFirst()) maLop = c1.getInt(0);
        c1.close();
        if (maLop <= 0) {
            rdb.close();
            return list;
        }

        String sql = "SELECT t.id, t.maLop, t.maMon, t.thu, t.tiet, m.tenMon as tenMon, l.tenLop as tenLop " +
                "FROM TKB t " +
                "LEFT JOIN MonHoc m ON t.maMon = m.id " +
                "LEFT JOIN LopHoc l ON t.maLop = l.id " +
                "WHERE t.maLop = ? " +
                "ORDER BY t.thu, t.tiet";
        Cursor c = rdb.rawQuery(sql, new String[]{String.valueOf(maLop)});
        while (c.moveToNext()) {
            TKB t = new TKB();
            t.setId(c.getInt(c.getColumnIndexOrThrow("id")));
            t.setMaLop(c.getInt(c.getColumnIndexOrThrow("maLop")));
            t.setMaMon(c.getInt(c.getColumnIndexOrThrow("maMon")));
            t.setThu(c.getInt(c.getColumnIndexOrThrow("thu")));
            t.setTiet(c.getInt(c.getColumnIndexOrThrow("tiet")));
            try {
                t.setTenMon(c.getString(c.getColumnIndexOrThrow("tenMon")));
            } catch (Exception ignored) {
            }
            try {
                t.setTenLop(c.getString(c.getColumnIndexOrThrow("tenLop")));
            } catch (Exception ignored) {
            }
            list.add(t);
        }
        c.close();
        rdb.close();
        return list;
    }

    /**
     * Get TKB for a specific class (useful for admin to view class schedules)
     */

    // ----------------- LopHoc -----------------
    public List<LopHoc> getAllLopHoc() {
        List<LopHoc> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT l.*, n.tenDangNhap as gvName FROM LopHoc l LEFT JOIN NguoiDung n ON l.gvcn = n.id", null);
        while (c.moveToNext()) {
            LopHoc l = new LopHoc(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("tenLop")),
                    c.getString(c.getColumnIndexOrThrow("khoi")),
                    c.getString(c.getColumnIndexOrThrow("namHoc")),
                    c.getInt(c.getColumnIndexOrThrow("siSo")),
                    c.getInt(c.getColumnIndexOrThrow("gvcn"))
            );
            list.add(l);
        }
        c.close();
        db.close();
        return list;
    }
    public String getLopNameById(int id) {
        String s = "";
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT tenLop FROM LopHoc WHERE id=?", new String[]{String.valueOf(id)});
        if (c.moveToFirst()) {
            s = c.getString(0);
            c.close();
        } else {
            c.close();
        }
        db.close();
        return s;
    }
    // ----------------- Parent mapping -----------------
    public long assignStudentToParent(int userId, int studentId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("userId", userId);
        cv.put("studentId", studentId);
        long id = db.insertWithOnConflict("ParentStudent", null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
        return id;
    }

    public List<HocSinh> getStudentsByParent(int userId) {
        List<HocSinh> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT hs.* FROM HocSinh hs JOIN ParentStudent ps ON hs.id = ps.studentId WHERE ps.userId = ?", new String[]{String.valueOf(userId)});
        while (c.moveToNext()) {
            list.add(new HocSinh(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("hoTen")),
                    c.getString(c.getColumnIndexOrThrow("ngaySinh")),
                    c.getString(c.getColumnIndexOrThrow("gioiTinh")),
                    c.getString(c.getColumnIndexOrThrow("queQuan")),
                    c.getInt(c.getColumnIndexOrThrow("maLop"))
            ));
        }
        c.close();
        db.close();
        return list;
    }
    // Lấy học sinh theo giáo viên chủ nhiệm
    public List<HocSinh> getHocSinhByTeacher(int teacherUserId) {
        List<HocSinh> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT hs.* FROM HocSinh hs " +
                        "JOIN LopHoc l ON hs.maLop = l.id " +
                        "WHERE l.gvcn = ?",
                new String[]{String.valueOf(teacherUserId)}
        );
        if (c != null) {
            while (c.moveToNext()) {
                list.add(new HocSinh(
                        c.getInt(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("hoTen")),
                        c.getString(c.getColumnIndexOrThrow("ngaySinh")),
                        c.getString(c.getColumnIndexOrThrow("gioiTinh")),
                        c.getString(c.getColumnIndexOrThrow("queQuan")),
                        c.getInt(c.getColumnIndexOrThrow("maLop"))
                ));
            }
            c.close();
        }
        db.close();
        return list;
    }

    // ----------------- LopHoc -----------------
    public long insertLopHoc(LopHoc l) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("tenLop", l.getTenLop());
        cv.put("khoi", l.getKhoi());
        cv.put("namHoc", l.getNamHoc());
        cv.put("siSo", l.getSiSo());
        cv.put("gvcn", l.getGvcn());
        long id = db.insert("LopHoc", null, cv);
        db.close();
        return id;
    }

    public int updateLopHoc(LopHoc l) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("tenLop", l.getTenLop());
        cv.put("khoi", l.getKhoi());
        cv.put("namHoc", l.getNamHoc());
        cv.put("siSo", l.getSiSo());
        cv.put("gvcn", l.getGvcn());
        int rows = db.update("LopHoc", cv, "id=?", new String[]{String.valueOf(l.getId())});
        db.close();
        return rows;
    }

    public int deleteLopHoc(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete("LopHoc", "id=?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    // Lấy tất cả lớp theo giáo viên chủ nhiệm

    // Lấy lớp theo giáo viên chủ nhiệm
    public List<LopHoc> getLopByTeacher(int teacherUserId) {
        List<LopHoc> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT * FROM LopHoc WHERE gvcn = ?",
                new String[]{String.valueOf(teacherUserId)}
        );
        while (c.moveToNext()) {
            list.add(new LopHoc(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("tenLop")),
                    c.getString(c.getColumnIndexOrThrow("khoi")),
                    c.getString(c.getColumnIndexOrThrow("namHoc")),
                    c.getInt(c.getColumnIndexOrThrow("siSo")),
                    c.getInt(c.getColumnIndexOrThrow("gvcn"))
            ));
        }
        c.close();
        db.close();
        return list;
    }

    public List<Diem> getAllDiem() {
        List<Diem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT d.*, hs.hoTen, l.tenLop, m.tenMon " +
                        "FROM Diem d " +
                        "JOIN HocSinh hs ON d.hocSinhId = hs.id " +
                        "JOIN LopHoc l ON hs.maLop = l.id " +
                        "JOIN MonHoc m ON d.monId = m.id",
                null
        );
        while (c.moveToNext()) {
            Diem d = buildDiemFromCursor(c);
            list.add(d);
        }
        c.close();
        return list;
    }

    public long insertDiem(Diem d) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("hocSinhId", d.getHocSinhId());
        cv.put("monId", d.getMonId());
        cv.put("diemHS1", d.getDiemHS1());
        cv.put("diemHS2", d.getDiemHS2());
        cv.put("diemThi", d.getDiemThi());
        cv.put("diemTB", computeTB(d.getDiemHS1(), d.getDiemHS2(), d.getDiemThi()));
        cv.put("nhanXet", d.getNhanXet());
        long id = db.insert("Diem", null, cv);
        db.close();
        return id;
    }

    public int updateDiem(Diem d) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("diemHS1", d.getDiemHS1());
        cv.put("diemHS2", d.getDiemHS2());
        cv.put("diemThi", d.getDiemThi());
        cv.put("diemTB", computeTB(d.getDiemHS1(), d.getDiemHS2(), d.getDiemThi()));
        cv.put("nhanXet", d.getNhanXet());
        int rows = db.update("Diem", cv, "hocSinhId=? AND monId=?",
                new String[]{String.valueOf(d.getHocSinhId()), String.valueOf(d.getMonId())});
        db.close();
        return rows;
    }

    public int deleteDiem(int hocSinhId, int monId) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete("Diem", "hocSinhId=? AND monId=?",
                new String[]{String.valueOf(hocSinhId), String.valueOf(monId)});
        db.close();
        return rows;
    }
    // Lấy thông tin người dùng theo id
    public NguoiDung getUserById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        NguoiDung user = null;
        Cursor c = db.rawQuery("SELECT id, hoTen, tenDangNhap, matKhau, vaiTro, email, sdt FROM NguoiDung WHERE id=?",
                new String[]{String.valueOf(id)});
        if (c.moveToFirst()) {
            user = new NguoiDung();
            user.setId(c.getInt(0));
            user.setHoTen(c.getString(1));
            user.setTenDangNhap(c.getString(2));
            user.setMatKhau(c.getString(3));
            user.setVaiTro(c.getString(4));
            user.setEmail(c.getString(5));
            user.setSdt(c.getString(6));
        }
        c.close();
        return user;
    }

    // Cập nhật thông tin (không đổi mật khẩu ở đây)
    public int updateUserInfo(NguoiDung u) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("hoTen", u.getHoTen());
        values.put("email", u.getEmail());
        values.put("sdt", u.getSdt());
        return db.update("NguoiDung", values, "id=?", new String[]{String.valueOf(u.getId())});
    }

    // Đổi mật khẩu: so sánh mật khẩu cũ trước
// DBHelper.java
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT matKhau FROM NguoiDung WHERE id = ?",
                new String[]{String.valueOf(userId)})) {
            // Lấy mật khẩu hiện tại
            if (cursor.moveToFirst()) {
                String storedHash = cursor.getString(0);
                String oldHash = HashUtils.sha256(oldPassword);

                if (!storedHash.equals(oldHash)) {
                    // Sai mật khẩu cũ
                    return false;
                }

                // Đúng -> cập nhật mật khẩu mới
                String newHash = HashUtils.sha256(newPassword);
                ContentValues values = new ContentValues();
                values.put("matKhau", newHash);
                int rows = db.update("NguoiDung", values, "id=?", new String[]{String.valueOf(userId)});
                return rows > 0;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    // Thống kê chuyên cần theo môn
    public int getStudentIdForParent(int parentId) {
        int id = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT studentId FROM ParentStudent WHERE userId=? LIMIT 1", new String[]{String.valueOf(parentId)});
        if (c.moveToFirst()) id = c.getInt(0);
        c.close();
        return id;
    }
    // Trả về danh sách AttendanceReportItem cho 1 học sinh
    /**
     * Trả về Map<monId, int[4]>: counts[0]=Có mặt, [1]=Vắng có phép, [2]=Vắng không phép, [3]=Muộn
     */
    public List<SubjectAttendance> getAttendanceStatsByStudent(int studentId) {
        List<SubjectAttendance> list = new ArrayList<>();
        SQLiteDatabase rdb = getReadableDatabase();
        String sql = "SELECT a.monId, m.tenMon, a.status, COUNT(*) as cnt " +
                "FROM Attendance a LEFT JOIN MonHoc m ON a.monId = m.id " +
                "WHERE a.hocSinhId = ? " +
                "GROUP BY a.monId, a.status";

        Cursor c = rdb.rawQuery(sql, new String[]{ String.valueOf(studentId) });
        Map<Integer, SubjectAttendance> map = new LinkedHashMap<>();
        while (c.moveToNext()) {
            int monId = c.getInt(c.getColumnIndexOrThrow("monId"));
            String tenMon = c.getString(c.getColumnIndexOrThrow("tenMon"));
            int status = c.getInt(c.getColumnIndexOrThrow("status"));
            int cnt = c.getInt(c.getColumnIndexOrThrow("cnt"));

            SubjectAttendance sa = map.computeIfAbsent(monId, i -> new SubjectAttendance(i, tenMon));

            switch (status) {
                case 0:
                    sa.addPresent(cnt);
                    break;
                case 1:
                    sa.addPermitLeave(cnt);
                    break;
                case 2:
                    sa.addAbsent(cnt);
                    break;
                case 3:
                    sa.addLate(cnt);
                    break;
            }
        }
        c.close();
        rdb.close();
        list.addAll(map.values());
        return list;
    }
    // Trả về điểm TB theo môn cho 1 HS => List<Pair<tenMon, diemTB>>
    public List<TwoColumn> getAverageScoresByStudent(int studentId) {
        List<TwoColumn> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT m.id as monId, m.tenMon as tenMon, AVG(d.diemTB) as avgTB " +
                "FROM Diem d " +
                "LEFT JOIN MonHoc m ON d.monId = m.id " +
                "WHERE d.hocSinhId = ? " +
                "GROUP BY m.id, m.tenMon";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(studentId)});
        while (c.moveToNext()) {
            TwoColumn t = new TwoColumn();
            t.setId(c.getInt(c.getColumnIndexOrThrow("monId")));
            t.setLabel(c.getString(c.getColumnIndexOrThrow("tenMon")));
            t.setValue((float) c.getDouble(c.getColumnIndexOrThrow("avgTB")));
            list.add(t);
        }
        c.close();
        db.close();
        return list;
    }
    // insert
    public long insertThongBao(ThongBao tb) {
        SQLiteDatabase wdb = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("guiTu", tb.getGuiTu());
        cv.put("tieuDe", tb.getTieuDe());
        cv.put("noiDung", tb.getNoiDung());
        cv.put("thoiGian", tb.getThoiGian());
        cv.put("targetRole", tb.getTargetRole());
        if (tb.getSenderRole() != null) cv.put("senderRole", tb.getSenderRole());
        long id = wdb.insert("ThongBao", null, cv);
        wdb.close();
        return id;
    }

    public int deleteThongBao(int id) {
        SQLiteDatabase wdb = getWritableDatabase();
        int rows = wdb.delete("ThongBao", "id=?", new String[]{String.valueOf(id)});
        wdb.close();
        return rows;
    }

    public List<ThongBao> getAllThongBao() {
        List<ThongBao> list = new ArrayList<>();
        SQLiteDatabase rdb = getReadableDatabase();
        Cursor c = rdb.rawQuery("SELECT * FROM ThongBao ORDER BY thoiGian DESC", null);
        while (c.moveToNext()) {
            ThongBao tb = new ThongBao(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("guiTu")),
                    c.getString(c.getColumnIndexOrThrow("tieuDe")),
                    c.getString(c.getColumnIndexOrThrow("noiDung")),
                    c.getString(c.getColumnIndexOrThrow("thoiGian")),
                    c.getString(c.getColumnIndexOrThrow("targetRole")),
                    c.getString(c.getColumnIndexOrThrow("senderRole"))
            );
            list.add(tb);
        }
        c.close();
        rdb.close();
        return list;
    }
    public List<ThongBao> getThongBaoByTarget(String targetRole) {
        List<ThongBao> list = new ArrayList<>();
        SQLiteDatabase rdb = getReadableDatabase();
        Cursor c = rdb.rawQuery("SELECT * FROM ThongBao WHERE targetRole=? OR targetRole='all' ORDER BY thoiGian DESC", new String[]{targetRole});
        while (c.moveToNext()) {
            ThongBao tb = new ThongBao(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("guiTu")),
                    c.getString(c.getColumnIndexOrThrow("tieuDe")),
                    c.getString(c.getColumnIndexOrThrow("noiDung")),
                    c.getString(c.getColumnIndexOrThrow("thoiGian")),
                    c.getString(c.getColumnIndexOrThrow("targetRole")),
                    c.getString(c.getColumnIndexOrThrow("senderRole"))
            );
            list.add(tb);
        }
        c.close();
        rdb.close();
        return list;
    }

    /**
     * Lấy thông báo mà user có quyền xem:
     * - targetRole == 'all'
     * - OR targetRole == role (phuhuynh/giaovien)
     * - OR targetUserId == userId (direct)
     */
    public List<ThongBao> getThongBaoForUser(int userId) {
        List<ThongBao> list = new ArrayList<>();
        // lấy role của user
        String role = "";
        SQLiteDatabase rdb = getReadableDatabase();
        Cursor rc = rdb.rawQuery("SELECT vaiTro FROM NguoiDung WHERE id=?", new String[]{String.valueOf(userId)});
        if (rc.moveToFirst()) {
            role = rc.getString(0);
            rc.close();
        }
        if (role == null) role = "";
        role = role.toLowerCase();

        Cursor c = rdb.rawQuery(
                "SELECT * FROM ThongBao WHERE targetRole='all' OR targetRole=? OR targetUserId=? ORDER BY thoiGian DESC",
                new String[]{role, String.valueOf(userId)}
        );
        while (c.moveToNext()) {
            ThongBao tb = new ThongBao(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("guiTu")),
                    c.getString(c.getColumnIndexOrThrow("tieuDe")),
                    c.getString(c.getColumnIndexOrThrow("noiDung")),
                    c.getString(c.getColumnIndexOrThrow("thoiGian")),
                    c.getString(c.getColumnIndexOrThrow("targetRole")),
                    c.getString(c.getColumnIndexOrThrow("senderRole"))
            );
            list.add(tb);
        }
        c.close();
        rdb.close();
        return list;
    }
    public ThongBao getThongBaoById(int id) {
        SQLiteDatabase rdb = getReadableDatabase();
        Cursor c = rdb.rawQuery("SELECT id, guiTu, targetRole, tieuDe, noiDung, thoiGian FROM ThongBao WHERE id=?", new String[]{String.valueOf(id)});
        ThongBao tb = null;
        if (c.moveToFirst()) {
            tb = new ThongBao(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("guiTu")),
                    c.getString(c.getColumnIndexOrThrow("targetRole")),
                    c.getString(c.getColumnIndexOrThrow("tieuDe")),
                    c.getString(c.getColumnIndexOrThrow("noiDung")),
                    c.getString(c.getColumnIndexOrThrow("thoiGian")),
                    c.getString(c.getColumnIndexOrThrow("senderRole"))
            );
            c.close();
        } else c.close();
        rdb.close();
        return tb;
    }
    // Xóa học sinh, xóa mapping ParentStudent, nếu phụ huynh không còn mapping nào => xóa phụ huynh luôn
    public boolean deleteHocSinhCascade(int hsId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();

            // tìm parent nếu có (lấy 1 parent đầu tiên nếu nhiều)
            Integer parentId = null;
            Cursor c = db.rawQuery("SELECT userId FROM ParentStudent WHERE studentId=?", new String[]{String.valueOf(hsId)});
            if (c.moveToFirst()) parentId = c.getInt(0);
            c.close();

            // xóa mapping và học sinh
            db.delete("ParentStudent", "studentId=?", new String[]{String.valueOf(hsId)});
            db.delete("HocSinh", "id=?", new String[]{String.valueOf(hsId)});

            // xóa Diem liên quan
            db.delete("Diem", "hocSinhId=?", new String[]{String.valueOf(hsId)});

            // nếu parent không còn học sinh nào -> xóa parent
            if (parentId != null) {
                Cursor c2 = db.rawQuery("SELECT COUNT(*) FROM ParentStudent WHERE userId=?", new String[]{String.valueOf(parentId)});
                if (c2.moveToFirst() && c2.getInt(0) == 0) {
                    db.delete("NguoiDung", "id=?", new String[]{String.valueOf(parentId)});
                }
                c2.close();
            }

            db.setTransactionSuccessful();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }
}