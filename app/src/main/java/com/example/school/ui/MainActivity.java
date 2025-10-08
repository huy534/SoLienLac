package com.example.school.ui;

import static com.example.school.R.*;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.school.R;
import com.example.school.auth.SessionManager;
import com.example.school.data.DBHelper;

public class MainActivity extends AppCompatActivity {

    private SessionManager session;
    private DBHelper db;

    private Toolbar toolbarMain;
    private Button btnHocSinh, btnLopHoc, btnMonHoc, btnDiem, btnUsers, btnLogout, btnTKB, btnBaoCao, btnThongBao, btnTaiKhoan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(this);
        db = new DBHelper(this);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        toolbarMain = findViewById(R.id.toolbarMain);
        btnHocSinh = findViewById(R.id.btnHocSinh);
        btnLopHoc = findViewById(R.id.btnLopHoc);
        btnMonHoc = findViewById(R.id.btnMonHoc);
        btnDiem = findViewById(R.id.btnDiem);
        btnLogout = findViewById(R.id.btnLogout);
        btnUsers = findViewById(R.id.btnUsers);
        btnTKB = findViewById(R.id.btnTKB);
        btnBaoCao = findViewById(R.id.btnBaoCao);
        btnThongBao = findViewById(R.id.btnThongBao);
        btnTaiKhoan = findViewById(R.id.btnTaiKhoan);


        int userId = session.getUserId();
        String role = session.getUserRole(); 
        String username = "";
        try {
            username = db.getTeacherNameById(userId);
        } catch (Exception e) {
            username = "";
        }
        if (username == null || username.isEmpty()) username = "Người dùng " + (userId > 0 ? userId : "");
        String roleDisplay = (role == null || role.isEmpty()) ? "user" : role;
        toolbarMain.setTitle("Sổ liên lạc - Xin chào " + username);
        applyRolePermissions(role);
        btnHocSinh.setOnClickListener(v -> startActivity(new Intent(this, HocSinhActivity.class)));
        btnLopHoc.setOnClickListener(v -> startActivity(new Intent(this, LopHocActivity.class)));
        btnMonHoc.setOnClickListener(v -> startActivity(new Intent(this, MonHocActivity.class)));
        btnDiem.setOnClickListener(v -> startActivity(new Intent(this, DiemActivity.class)));

        btnBaoCao.setOnClickListener(v -> startActivity(new Intent(this, BaoCaoActivity.class)));

        if (btnUsers != null) btnUsers.setOnClickListener(v -> startActivity(new Intent(this, com.example.school.ui.UserManagementActivity.class)));
        btnTaiKhoan.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, TaiKhoanActivity.class);
            startActivity(i);
        });
        btnLogout.setOnClickListener(v -> {
            session.clearSession();
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }

    private void applyRolePermissions(String role) {
        if (role == null) role = "";

        role = role.toLowerCase().trim();

       
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
                btnBaoCao.setVisibility(View.GONE);
                break;
            case "giaovien":
                btnHocSinh.setVisibility(View.GONE);
                btnLopHoc.setVisibility(View.VISIBLE);
                btnMonHoc.setVisibility(View.VISIBLE);
                btnDiem.setVisibility(View.VISIBLE);
                btnBaoCao.setVisibility(View.GONE);
                break;
            case "phuhuynh":
                btnDiem.setVisibility(View.VISIBLE);
                btnHocSinh.setVisibility(View.VISIBLE);
                         break;
            default:
                btnDiem.setVisibility(View.VISIBLE);
                break;
        }
        btnTKB.setOnClickListener(v -> {
            int userId = session.getUserId();
            String rl = session.getUserRole() == null ? "" : session.getUserRole().toLowerCase().trim();

            if (rl.equals("admin")) {
                // Admin: chọn loại TKB (HS / GV)
                startActivity(new Intent(this, ChooseTKBActivity.class));
            } else if (rl.equals("giaovien")) {
                Intent i = new Intent(this, TKBActivity.class);
                i.putExtra("mode", "teacher");
                i.putExtra("id", session.getUserId());
                startActivity(i);
            } else if (rl.equals("phuhuynh")) {
                // Phụ huynh: vào trực tiếp TKB của con
                startActivity(new Intent(this, TKBActivity.class)
                        .putExtra("type", "hocsinh")
                        .putExtra("id", userId));
            }
        });
        btnThongBao.setOnClickListener(v -> {
            String r = session.getUserRole();
            if ("admin".equalsIgnoreCase(r)) {
                // admin chọn gửi cho GV hay PH -> open choose activity
                startActivity(new Intent(this, ThongBaoChooseActivity.class));
            } else if ("giaovien".equalsIgnoreCase(r)) {
                startActivity(new Intent(this, ThongBaoListActivity.class).putExtra("targetRole","giaovien"));
            } else if ("phuhuynh".equalsIgnoreCase(r)) {
                startActivity(new Intent(this, ThongBaoListActivity.class).putExtra("targetRole","phuhuynh"));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }
}
