package com.example.school.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.school.R;
import com.example.school.auth.SessionManager;
import com.example.school.data.DBHelper;

public class MainActivity extends AppCompatActivity {

    private SessionManager session;
    private DBHelper db;

    private TextView tvWelcome;
    private Button btnHocSinh, btnLopHoc, btnMonHoc, btnDiem, btnUsers, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(this);
        db = new DBHelper(this);

        // nếu chưa login -> về LoginActivity
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Ánh xạ view
        tvWelcome = findViewById(R.id.tvWelcome);
        btnHocSinh = findViewById(R.id.btnHocSinh);
        btnLopHoc = findViewById(R.id.btnLopHoc);
        btnMonHoc = findViewById(R.id.btnMonHoc);
        btnDiem = findViewById(R.id.btnDiem);
        btnLogout = findViewById(R.id.btnLogout);
        btnUsers = findViewById(R.id.btnUsers); // ensure this id exists in activity_main.xml

        // Lấy thông tin session
        int userId = session.getUserId();
        String role = session.getUserRole(); // SessionManager lưu role đã lower-case
        String username = "";

        // Cố gắng lấy tên người dùng từ DB (DBHelper có hàm getTeacherNameById - trả tenDangNhap)
        try {
            username = db.getTeacherNameById(userId);
        } catch (Exception e) {
            username = "";
        }
        if (username == null || username.isEmpty()) username = "Người dùng " + (userId > 0 ? userId : "");

        // Hiển thị chào mừng
        String roleDisplay = (role == null || role.isEmpty()) ? "user" : role;
        tvWelcome.setText("Xin chào " + username + " (" + roleDisplay + ")");

        // Ẩn/hiện chức năng theo vai trò
        applyRolePermissions(role);

        // Sự kiện nút
        btnHocSinh.setOnClickListener(v -> startActivity(new Intent(this, HocSinhActivity.class)));
        btnLopHoc.setOnClickListener(v -> startActivity(new Intent(this, LopHocActivity.class)));
        btnMonHoc.setOnClickListener(v -> startActivity(new Intent(this, MonHocActivity.class)));
        btnDiem.setOnClickListener(v -> startActivity(new Intent(this, DiemActivity.class)));
        if (btnUsers != null) btnUsers.setOnClickListener(v -> startActivity(new Intent(this, com.example.school.ui.UserManagementActivity.class)));
        // Logout: clear session và quay về Login
        btnLogout.setOnClickListener(v -> {
            session.clearSession();
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }

    /**
     * Quyền hiển thị:
     * - admin: thấy tất cả (HocSinh, LopHoc, MonHoc, Diem)
     * - giaovien: thấy LopHoc, MonHoc, Diem (vì giáo viên quản lý lớp/môn của mình)
     * - phuhuynh: chỉ thấy Diem (xem điểm con)
     * - khác: ẩn hết (mặc định hiện Diem)
     */
    private void applyRolePermissions(String role) {
        if (role == null) role = "";

        role = role.toLowerCase().trim();

        // default: ẩn tất cả, bật những cái cần thiết
        btnUsers.setVisibility(View.GONE);
        btnHocSinh.setVisibility(View.GONE);
        btnLopHoc.setVisibility(View.GONE);
        btnMonHoc.setVisibility(View.GONE);
        btnDiem.setVisibility(View.GONE);

        switch (role) {
            case "admin":
                btnHocSinh.setVisibility(View.VISIBLE);
                btnLopHoc.setVisibility(View.VISIBLE);
                btnMonHoc.setVisibility(View.VISIBLE);
                btnDiem.setVisibility(View.VISIBLE);
                btnUsers.setVisibility(View.VISIBLE);
                break;
            case "giaovien":
                btnHocSinh.setVisibility(View.GONE); // optional: ẩn hoặc hiển thị tùy bạn
                btnLopHoc.setVisibility(View.VISIBLE);
                btnMonHoc.setVisibility(View.VISIBLE);
                btnDiem.setVisibility(View.VISIBLE);
                break;
            case "phuhuynh":
                btnDiem.setVisibility(View.VISIBLE);
                // phụ huynh chỉ xem điểm (và sau này có thể thêm "HocPhi" nếu cần)
                break;
            default:
                // roles khác: cho hiển thị Diem read-only
                btnDiem.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }
}
