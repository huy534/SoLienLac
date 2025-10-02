package com.example.school.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.school.R;
import com.example.school.adapters.ThongBaoAdapter;
import com.example.school.auth.SessionManager;
import com.example.school.data.DBHelper;
import com.example.school.model.ThongBao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ThongBaoListActivity extends AppCompatActivity {

    public static final String EXTRA_TARGET = "target"; // "teacher" or "parent" or "all"
    private DBHelper db;
    private SessionManager session;
    private RecyclerView rv;
    private ThongBaoAdapter adapter;
    private FloatingActionButton fab;
    private SwipeRefreshLayout swipe;

    private String target; // target shown (if admin chose teacher/parent)
    private int userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thongbao_list); // layout provided earlier

        db = new DBHelper(this);
        session = new SessionManager(this);
        userId = session.getUserId();

        rv = findViewById(R.id.recyclerViewThongBao);
        fab = findViewById(R.id.fabAddThongBao);
        swipe = findViewById(R.id.swipeRefresh);

        rv.setLayoutManager(new LinearLayoutManager(this));
        Toolbar toolbar = findViewById(R.id.toolbarThongBaoList);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) { getSupportActionBar().setDisplayHomeAsUpEnabled(true); getSupportActionBar().setTitle("Danh sách thông báo"); }
        toolbar.setNavigationOnClickListener(v -> finish());
        target = getIntent().getStringExtra(EXTRA_TARGET); // admin may pass "teacher" or "parent"
        String role = session.getUserRole() == null ? "" : session.getUserRole().toLowerCase();

        // If a non-admin opens without explicit target, and is teacher/parent, show their relevant list
        if (!"admin".equalsIgnoreCase(role)) {
            if (role.contains("phuhuynh")) target = "parent";
            else if (role.contains("giaovien")) target = "teacher";
            else target = "all";
        }

        // FAB visible only for admin
        fab.setVisibility("admin".equalsIgnoreCase(role) ? View.VISIBLE : View.GONE);

        adapter = new ThongBaoAdapter(null);
        rv.setAdapter(adapter);

        adapter.setOnItemClickListener(tb -> showDetailDialog(tb, "admin".equalsIgnoreCase(role)));

        fab.setOnClickListener(v -> showAddDialog());

        swipe.setOnRefreshListener(this::loadData);

        // initial load
        loadData();
    }

    private void loadData() {
        swipe.setRefreshing(true);
        String role = session.getUserRole() == null ? "" : session.getUserRole().toLowerCase();
        List<ThongBao> list;
        if ("admin".equalsIgnoreCase(role)) {
            // admin: if opened with target param, show those target notifications; otherwise show all
            if (!TextUtils.isEmpty(target) && (target.equalsIgnoreCase("teacher") || target.equalsIgnoreCase("parent"))) {
                list = db.getThongBaoByTarget(target);
            } else {
                list = db.getAllThongBao();
            }
        } else {
            // teacher/parent only see messages intended for them or 'all' (or addressed to their userId)
            list = db.getThongBaoForUser(userId);
        }
        adapter.updateData(list);
        swipe.setRefreshing(false);
    }

    private void showAddDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_add_thongbao, null);
        EditText etGuiTu = v.findViewById(R.id.etGuiTu);
        EditText etTieuDe = v.findViewById(R.id.etTieuDe);
        EditText etThoiGian = v.findViewById(R.id.etThoiGian);
        EditText etNoiDung = v.findViewById(R.id.etNoiDung);

        // default thoi gian = now
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        etThoiGian.setText(now);

        new AlertDialog.Builder(this)
                .setTitle("Thêm thông báo")
                .setView(v)
                .setPositiveButton("Lưu", (d, w) -> {
                    String guiTu = etGuiTu.getText().toString().trim();
                    String tieuDe = etTieuDe.getText().toString().trim();
                    String thoiGian = etThoiGian.getText().toString().trim();
                    String noiDung = etNoiDung.getText().toString().trim();

                    if (tieuDe.isEmpty() || noiDung.isEmpty()) {
                        Toast.makeText(this, "Tiêu đề và nội dung không được rỗng", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // For simplicity: admin adds to current 'target' selection. If no target, default to 'all'
                    String tgt = target == null ? "all" : target;
                    ThongBao tb = new ThongBao();
                    tb.setGuiTu(guiTu.isEmpty() ? "Admin" : guiTu);
                    tb.setTieuDe(tieuDe);
                    tb.setNoiDung(noiDung);
                    tb.setThoiGian(thoiGian);
                    tb.setTargetRole(tgt);
                    tb.setSenderRole("admin"); // optional: allow admin to choose specific user by id

                    long id = db.insertThongBao(tb);
                    if (id > 0) {
                        Toast.makeText(this, "Đã thêm thông báo", Toast.LENGTH_SHORT).show();
                        loadData();
                    } else {
                        Toast.makeText(this, "Thêm thất bại", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDetailDialog(ThongBao tb, boolean isAdmin) {
        StringBuilder sb = new StringBuilder();
        sb.append("Gửi từ: ").append(tb.getGuiTu() == null ? "" : tb.getGuiTu()).append("\n");
        sb.append("Thời gian: ").append(tb.getThoiGian() == null ? "" : tb.getThoiGian()).append("\n\n");
        sb.append(tb.getNoiDung() == null ? "" : tb.getNoiDung());

        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle(tb.getTieuDe())
                .setMessage(sb.toString())
                .setPositiveButton("Đóng", null);

        if (isAdmin) {
            b.setNeutralButton("Xóa", (d, w) -> {
                int rows = db.deleteThongBao(tb.getId());
                Toast.makeText(this, rows > 0 ? "Đã xóa" : "Xóa thất bại", Toast.LENGTH_SHORT).show();
                loadData();
            });
        }
        b.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) db.close();
    }
}