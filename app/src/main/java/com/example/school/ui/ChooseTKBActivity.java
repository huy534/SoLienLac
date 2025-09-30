package com.example.school.ui;

import static com.example.school.R.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.school.R;
import com.example.school.auth.SessionManager;
import com.example.school.data.DBHelper;
import com.example.school.model.HocSinh;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class ChooseTKBActivity extends AppCompatActivity {
    private Button btnTeacherTKB, btnStudentTKB;
    private SessionManager session;
    private DBHelper db;

    public static final int REQ_SELECT_TEACHER = 1001;
    public static final int REQ_SELECT_STUDENT = 1002;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_tkb);
        MaterialToolbar toolbar = findViewById(R.id.toolbarChooseTkb);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setTitle("Chọn TKB");

        session = new SessionManager(this);
        db = new DBHelper(this);

        btnTeacherTKB = findViewById(R.id.btnTeacherTKB);
        btnStudentTKB = findViewById(R.id.btnStudentTKB);

        String role = session.getUserRole() == null ? "" : session.getUserRole().toLowerCase();
        int userId = session.getUserId();

        // Nếu là giáo viên: trực tiếp mở TKB của chính GV
        btnTeacherTKB.setOnClickListener(v -> {
            if (role.contains("giaovien")) {
                Intent i = new Intent(ChooseTKBActivity.this, TKBActivity.class);
                i.putExtra("mode", "teacher");
                i.putExtra("id", userId);
                startActivity(i);
            } else if (role.contains("phuhuynh")) {
                // phụ huynh không có quyền xem TKB giáo viên (mặc định admin/others có)
                // mở danh sách giáo viên để chọn (admin)
                startActivityForResult(new Intent(ChooseTKBActivity.this, SelectGiaoVienActivity.class), REQ_SELECT_TEACHER);
            } else {
                // admin/other -> chọn GV
                startActivityForResult(new Intent(ChooseTKBActivity.this, SelectGiaoVienActivity.class), REQ_SELECT_TEACHER);
            }
        });

        // Nếu là phụ huynh: show danh sách con (nếu chỉ 1 con mở thẳng)
        btnStudentTKB.setOnClickListener(v -> {
            if (role.contains("phuhuynh")) {
                List<HocSinh> children = db.getStudentsByParent(userId);
                if (children != null && children.size() == 1) {
                    Intent i = new Intent(ChooseTKBActivity.this, TKBActivity.class);
                    i.putExtra("mode", "student");
                    i.putExtra("id", children.get(0).getId());
                    startActivity(i);
                } else {
                    Intent ii = new Intent(ChooseTKBActivity.this, SelectHocSinhActivity.class);
                    // pass parent id to filter if needed
                    ii.putExtra("parentId", userId);
                    startActivityForResult(ii, REQ_SELECT_STUDENT);
                }
            } else if (role.contains("giaovien")) {
                // GV xem TKB lớp HS? open select student (or show his class)
                startActivityForResult(new Intent(ChooseTKBActivity.this, SelectHocSinhActivity.class), REQ_SELECT_STUDENT);
            } else {
                // admin/other
                startActivityForResult(new Intent(ChooseTKBActivity.this, SelectHocSinhActivity.class), REQ_SELECT_STUDENT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;
        if (requestCode == REQ_SELECT_TEACHER) {
            int teacherId = data.getIntExtra("teacherId", -1);
            if (teacherId > 0) {
                Intent i = new Intent(this, TKBActivity.class);
                i.putExtra("mode", "teacher");
                i.putExtra("id", teacherId);
                startActivity(i);
            }
        } else if (requestCode == REQ_SELECT_STUDENT) {
            int studentId = data.getIntExtra("studentId", -1);
            if (studentId > 0) {
                Intent i = new Intent(this, TKBActivity.class);
                i.putExtra("mode", "student");
                i.putExtra("id", studentId);
                startActivity(i);
            }
        }
          }
}
