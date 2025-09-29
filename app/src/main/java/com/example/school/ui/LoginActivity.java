package com.example.school.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.school.R;
import com.example.school.auth.SessionManager;
import com.example.school.data.DBHelper;
import com.example.school.model.NguoiDung;

public class LoginActivity extends AppCompatActivity {

    private DBHelper db;
    private SessionManager session;
    private EditText etUser, etPass;
    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DBHelper(this);
        session = new SessionManager(this);

        // Nếu đã đăng nhập -> vào thẳng MainActivity
        if (session.isLoggedIn()) {
            startMainAndFinish();
            return;
        }

        etUser = findViewById(R.id.etUsername);
        etPass = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String username = etUser.getText().toString().trim();
            String password = etPass.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Nhập tên đăng nhập và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            // db.login sẽ kiểm tra user + hash password (DBHelper đã làm hash)
            NguoiDung user = db.login(username, password);
            if (user != null) {
                // Lưu session (lưu id, username, role). SessionManager bên dưới hỗ trợ createLoginSession.
                session.createLoginSession(user.getId(), user.getTenDangNhap(), user.getVaiTro());

                // Chuyển tới MainActivity
                startMainAndFinish();
            } else {
                Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startMainAndFinish() {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        // clear back stack so user cannot come back to login with back button
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }
}
