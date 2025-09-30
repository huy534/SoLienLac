package com.example.school.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.school.R;
import com.example.school.auth.SessionManager;
import com.example.school.data.DBHelper;
import com.example.school.model.NguoiDung;

public class TaiKhoanActivity extends AppCompatActivity {
    private EditText etHoTen, etUsername, etEmail, etPhone;
    private DBHelper db;
    private SessionManager session;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taikhoan);

        db = new DBHelper(this);
        session = new SessionManager(this);
        userId = session.getUserId();

        etHoTen = findViewById(R.id.etHoTen);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        Toolbar toolbar = findViewById(R.id.toolbarTK);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) { getSupportActionBar().setDisplayHomeAsUpEnabled(true); getSupportActionBar().setTitle("THồ sơ cá nhân"); }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Load user info từ DB
        NguoiDung user = db.getUserById(userId);
        if (user != null) {
            etHoTen.setText(user.getHoTen());
            etUsername.setText(user.getTenDangNhap());
            etEmail.setText(user.getEmail());
            etPhone.setText(user.getSdt());
        }

        findViewById(R.id.btnUpdateInfo).setOnClickListener(v -> {
            user.setHoTen(etHoTen.getText().toString().trim());
            user.setEmail(etEmail.getText().toString().trim());
            user.setSdt(etPhone.getText().toString().trim());

            int rows = db.updateUserInfo(user);
            Toast.makeText(this, rows > 0 ? "Cập nhật thành công" : "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnChangePassword).setOnClickListener(v -> showChangePasswordDialog());
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        EditText etOld = dialogView.findViewById(R.id.etOldPassword);
        EditText etNew = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirm = dialogView.findViewById(R.id.etConfirmPassword);

        new AlertDialog.Builder(this)
                .setTitle("Đổi mật khẩu")
                .setView(dialogView)
                .setPositiveButton("OK", (d, w) -> {
                    String oldPass = etOld.getText().toString();
                    String newPass = etNew.getText().toString();
                    String confirmPass = etConfirm.getText().toString();

                    if (!newPass.equals(confirmPass)) {
                        Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean success = db.changePassword(userId, oldPass, newPass);
                    Toast.makeText(this, success ? "Đổi mật khẩu thành công" : "Sai mật khẩu cũ", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Đóng", null)
                .show();
    }
}
