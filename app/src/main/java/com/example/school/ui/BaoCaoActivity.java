package com.example.school.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.school.R;
import com.example.school.auth.SessionManager;
import com.example.school.data.DBHelper;

public class BaoCaoActivity extends AppCompatActivity {
    private SessionManager session;
    private DBHelper db;
    private Button btnBaoCaoChuyenCan, btnBaoCaoHocTap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baocao);

        session = new SessionManager(this);
        db = new DBHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbarBaoCao);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Báo cáo");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        btnBaoCaoChuyenCan = findViewById(R.id.btnBaoCaoChuyenCan);
        btnBaoCaoHocTap = findViewById(R.id.btnBaoCaoHocTap);

        String role = session.getUserRole() == null ? "" : session.getUserRole().toLowerCase().trim();
        if (!role.contains("phuhuynh")) {
            Toast.makeText(this, "Chỉ phụ huynh được xem báo cáo", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int parentId = session.getUserId();
        int studentId = db.getStudentIdForParent(parentId);

        btnBaoCaoChuyenCan.setOnClickListener(v -> {
            Intent i = new Intent(this, BaoCaoChuyenCanActivity.class);
            i.putExtra("studentId", studentId);
            startActivity(i);
        });

        btnBaoCaoHocTap.setOnClickListener(v -> {
            Intent i = new Intent(this, BaoCaoHocTapActivity.class);
            i.putExtra("studentId", studentId);
            startActivity(i);
        });
    }
}
