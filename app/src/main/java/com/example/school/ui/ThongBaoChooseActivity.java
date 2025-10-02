package com.example.school.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;

import com.example.school.R;
import com.example.school.auth.SessionManager;

public class ThongBaoChooseActivity extends AppCompatActivity {

    private SessionManager session;
    private CardView cardTeacher, cardParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thongbao_choose);

        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbarThongBaoChoose);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thông báo");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        cardTeacher = findViewById(R.id.cardTeacher);
        cardParent = findViewById(R.id.cardParent);

        String role = session.getUserRole() == null ? "" : session.getUserRole().toLowerCase();

        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) { getSupportActionBar().setDisplayHomeAsUpEnabled(true); getSupportActionBar().setTitle("Chọn thông báo"); }
        toolbar.setNavigationOnClickListener(v -> finish());
        // Nếu user không phải admin -> chuyển trực tiếp tới danh sách thông báo phù hợp
        if (!"admin".equalsIgnoreCase(role)) {
            if (role.contains("giaovien")) {
                openListFor("teacher");
            } else if (role.contains("phuhuynh")) {
                openListFor("parent");
            } else {
                // default: show both (or open all)
                cardTeacher.setVisibility(View.VISIBLE);
                cardParent.setVisibility(View.VISIBLE);
            }
            return;
        }

        // Nếu admin -> hiển thị hai ô để chọn
        cardTeacher.setOnClickListener(v -> openListFor("teacher"));
        cardParent.setOnClickListener(v -> openListFor("parent"));
    }

    private void openListFor(String target) {
        Intent i = new Intent(this, ThongBaoListActivity.class);
        i.putExtra(ThongBaoListActivity.EXTRA_TARGET, target);
        startActivity(i);
        // nếu user không phải admin thì finish activity này để không phải back lại
        if (!"admin".equalsIgnoreCase(session.getUserRole())) finish();
    }
}
