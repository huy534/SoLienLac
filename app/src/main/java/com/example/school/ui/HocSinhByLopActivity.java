package com.example.school.ui;

import static com.example.school.R.*;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school.R;
import com.example.school.adapters.HocSinhAdapter;
import com.example.school.auth.SessionManager;
import com.example.school.data.DBHelper;
import com.example.school.model.HocSinh;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

public class HocSinhByLopActivity extends AppCompatActivity {
    public static final String EXTRA_LOP_ID = "lopId";

    private DBHelper db;
    private SessionManager session;
    private RecyclerView rv;
    private HocSinhAdapter adapter;
    private FloatingActionButton fab;
    private SearchView searchView;
    private TextView tvEmpty;
    private int lopId = -1;
    private String role;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hocsinh_by_lop);

        db = new DBHelper(this);
        session = new SessionManager(this);
        role = session.getUserRole() == null ? "" : session.getUserRole().toLowerCase(Locale.getDefault());
        userId = session.getUserId();

        MaterialToolbar toolbar = findViewById(R.id.toolbarHocSinhByLop);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        rv = findViewById(R.id.recyclerViewHocSinhByLop);
        rv.setLayoutManager(new LinearLayoutManager(this));
        fab = findViewById(R.id.fabAddHocSinhByLop);
        searchView = findViewById(R.id.searchHocSinhByLop);
        tvEmpty = findViewById(R.id.tvEmptyHocSinhByLop);

        // LopId có thể được truyền từ Activity gọi (khi admin/chọn lớp)
        Intent it = getIntent();
        if (it != null && it.hasExtra(EXTRA_LOP_ID)) {
            lopId = it.getIntExtra(EXTRA_LOP_ID, -1);
        }

        // Nếu là giáo viên và không truyền lopId -> lấy lớp chủ nhiệm
        if ((role.contains("giaovien") || role.contains("giao vien")) && lopId <= 0) {
            lopId = db.getLopIdByGVCN(userId);
        }

        // Load dữ liệu
        loadStudents();

        // Quyền hiển thị FAB: chỉ admin được thêm HS
        if (role.contains("admin")) {
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(v -> showAddEditDialog(null));
        } else {
            fab.setVisibility(View.GONE);
        }

        // Search
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override public boolean onQueryTextSubmit(String query) { adapter.filter(query); return true; }
                @Override public boolean onQueryTextChange(String newText) { adapter.filter(newText); return true; }
            });
        }

        // Long click handled below via adapter listener assignment in loadStudents()
    }

    private void loadStudents() {
        List<HocSinh> list;
        if (lopId > 0) {
            list = db.getHocSinhByLop(lopId);
            // đổi tiêu đề toolbar để biết lớp hiện tại
            String tenLop = db.getLopNameById(lopId);
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Học sinh - " + (tenLop == null ? ("Lớp " + lopId) : tenLop));
        } else {
            // nếu không xác định lớp: admin xem tất cả
            list = db.getAllHocSinh();
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Tất cả học sinh");
        }

        adapter = new HocSinhAdapter(list, null);
        rv.setAdapter(adapter);

        // long click: admin -> sửa/xóa; giáo viên -> chỉ xem chi tiết; phụ huynh -> không cần
        adapter.setOnItemLongClickListener(hs -> {
            if (role.contains("admin")) {
                showAddEditDialog(hs);
            } else if (role.contains("giaovien")) {
                // giáo viên chỉ xem chi tiết (dialog readonly)
                showViewDialog(hs);
            } else if (role.contains("phuhuynh")) {
                showViewDialog(hs);
            } else {
                showViewDialog(hs);
            }
        });

        findViewById(R.id.tvEmptyHocSinhByLop).setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void refresh() {
        if (lopId > 0) {
            adapter.updateData(db.getHocSinhByLop(lopId));
        } else {
            adapter.updateData(db.getAllHocSinh());
        }
        findViewById(R.id.tvEmptyHocSinhByLop).setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void showViewDialog(HocSinh hs) {
        View v = LayoutInflater.from(this).inflate(layout.dialog_add_edit_hocsinh_lop, null);
        EditText etHoTen = v.findViewById(R.id.etHoTen);
        EditText etNgaySinh = v.findViewById(R.id.etNgaySinh);
        EditText etGioiTinh = v.findViewById(R.id.etGioiTinh);
        EditText etQueQuan = v.findViewById(R.id.etQueQuan);
        EditText etMaLop = v.findViewById(R.id.etMaLopHS);

        etHoTen.setText(hs.getHoTen());
        etNgaySinh.setText(hs.getNgaySinh());
        etGioiTinh.setText(hs.getGioiTinh());
        etQueQuan.setText(hs.getQueQuan());
        etMaLop.setText(String.valueOf(hs.getMaLop()));

        // disable editing for view-only
        etHoTen.setEnabled(false);
        etNgaySinh.setEnabled(false);
        etGioiTinh.setEnabled(false);
        etQueQuan.setEnabled(false);
        etMaLop.setEnabled(false);

        new AlertDialog.Builder(this)
                .setTitle("Chi tiết học sinh")
                .setView(v)
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void showAddEditDialog(HocSinh editing) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_hocsinh_lop, null);
        EditText etHoTen = v.findViewById(R.id.etHoTen);
        EditText etNgaySinh = v.findViewById(R.id.etNgaySinh);
        EditText etGioiTinh = v.findViewById(R.id.etGioiTinh);
        EditText etQueQuan = v.findViewById(R.id.etQueQuan);
        EditText etMaLop = v.findViewById(R.id.etMaLopHS);

        if (editing != null) {
            etHoTen.setText(editing.getHoTen());
            etNgaySinh.setText(editing.getNgaySinh());
            etGioiTinh.setText(editing.getGioiTinh());
            etQueQuan.setText(editing.getQueQuan());
            etMaLop.setText(String.valueOf(editing.getMaLop()));
        } else if (lopId > 0) {
            // nếu thêm và activity đã có lopId, gợi ý set maLop
            etMaLop.setText(String.valueOf(lopId));
        }

        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle(editing == null ? "Thêm học sinh" : "Sửa học sinh")
                .setView(v)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null);

        if (editing != null) {
            b.setNeutralButton("Xóa", (d, w) -> {
                int rows = db.deleteHocSinh(editing.getId());
                Toast.makeText(this, rows > 0 ? "Đã xóa" : "Xóa thất bại", Toast.LENGTH_SHORT).show();
                refresh();
            });
        }

        AlertDialog dlg = b.create();
        dlg.setOnShowListener(dialog -> {
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
                String ten = etHoTen.getText().toString().trim();
                String ns = etNgaySinh.getText().toString().trim();
                String gt = etGioiTinh.getText().toString().trim();
                String qq = etQueQuan.getText().toString().trim();
                int maLopVal = parseIntSafe(etMaLop.getText().toString().trim(), -1);

                if (ten.isEmpty() || maLopVal <= 0) {
                    Toast.makeText(HocSinhByLopActivity.this, "Nhập tên và mã lớp hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (editing == null) {
                    // insert
                    long id = db.insertHocSinh(new HocSinh(0, ten, ns, gt, qq, maLopVal));
                    Toast.makeText(HocSinhByLopActivity.this, id > 0 ? "Đã thêm" : "Thất bại", Toast.LENGTH_SHORT).show();
                } else {
                    editing.setHoTen(ten);
                    editing.setNgaySinh(ns);
                    editing.setGioiTinh(gt);
                    editing.setQueQuan(qq);
                    editing.setMaLop(maLopVal);
                    int rows = db.updateHocSinh(editing);
                    Toast.makeText(HocSinhByLopActivity.this, rows > 0 ? "Đã cập nhật" : "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
                dlg.dismiss();
                refresh();
            });
        });
        dlg.show();
    }

    private int parseIntSafe(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (Exception ex) { return fallback; }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) db.close();
    }
}
