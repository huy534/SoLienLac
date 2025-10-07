package com.example.school.ui;

import static com.example.school.R.*;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school.R;
import com.example.school.adapters.HocSinhAdapter;
import com.example.school.auth.SessionManager;
import com.example.school.data.DBHelper;
import com.example.school.model.HocSinh;
import com.example.school.model.LopHoc;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class HocSinhActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private List<HocSinh> list;
    private HocSinhAdapter adapter;
    private DBHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hocsinh);

        db = new DBHelper(this);
        session = new SessionManager(this);

        recyclerView = findViewById(R.id.recyclerViewHocSinh);
        fabAdd = findViewById(R.id.fabAddHocSinh);

        String role = session.getUserRole();
        int userId = session.getUserId();
        MaterialToolbar toolbar = findViewById(R.id.toolbarHocSinh);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setTitle("Danh sách học sinh");
        if ("PhuHuynh".equalsIgnoreCase(role)) {
            fabAdd.setVisibility(View.GONE);
            list = db.getStudentsByParent(userId);
            adapter = new HocSinhAdapter(list, null);
            recyclerView.setAdapter(adapter);
        } else if ("GiaoVien".equalsIgnoreCase(role)) {
            fabAdd.setVisibility(View.VISIBLE);;

            fabAdd.setOnClickListener(v -> showAddEditDialog(null, userId));
        } else {
            // Admin / others: full access
            fabAdd.setVisibility(View.VISIBLE);
            list = db.getAllHocSinh();
            adapter = new HocSinhAdapter(list, null);
            recyclerView.setAdapter(adapter);

            fabAdd.setOnClickListener(v -> showAddEditDialog(null, -1));
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        refresh();
    }

    private void refresh() {
        String role = session.getUserRole();
        int userId = session.getUserId();

        if ("PhuHuynh".equalsIgnoreCase(role)) {
            list.clear();
            list.addAll(db.getStudentsByParent(userId));
        } else if ("GiaoVien".equalsIgnoreCase(role)) {
            list.clear();
            list.addAll(db.getHocSinhByTeacher(userId));
        } else {
            list.clear();
            list.addAll(db.getAllHocSinh());
        }
        adapter.notifyDataSetChanged();
        findViewById(R.id.tvEmptyHocSinh).setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }
    private void showAddEditDialog(HocSinh editing, int teacherUserId) {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_edit_hocsinh, null);
        EditText etHoTen = view.findViewById(R.id.etHoTen);
        EditText etNgay = view.findViewById(R.id.etNgaySinh);
        EditText etGT = view.findViewById(R.id.etGioiTinh);
        EditText etQQ = view.findViewById(R.id.etQueQuan);
        EditText etMaLop = view.findViewById(R.id.etMaLop);

        if (editing != null) {
            etHoTen.setText(editing.getHoTen());
            etNgay.setText(editing.getNgaySinh());
            etGT.setText(editing.getGioiTinh());
            etQQ.setText(editing.getQueQuan());
            etMaLop.setText(String.valueOf(editing.getMaLop()));
        }

        new AlertDialog.Builder(this)
                .setTitle(editing == null ? "Thêm học sinh" : "Sửa học sinh")
                .setView(view)
                .setPositiveButton("Lưu", (d, w) -> {
                    String ten = etHoTen.getText().toString().trim();
                    String ngay = etNgay.getText().toString().trim();
                    String gt = etGT.getText().toString().trim();
                    String qq = etQQ.getText().toString().trim();
                    int maLop = -1;
                    try {
                        maLop = Integer.parseInt(etMaLop.getText().toString().trim());
                    } catch (Exception ex) { maLop = -1; }

                    // If teacherUserId != -1, enforce that class belongs to teacher
                    if (teacherUserId != -1) {
                        boolean ok = false;
                        List<LopHoc> myClasses = db.getLopByTeacher(teacherUserId);
                        for (LopHoc l : myClasses) {
                            if (l.getId() == maLop) { ok = true; break; }
                        }
                        if (!ok) {
                            Toast.makeText(this, "Bạn chỉ có thể thêm/sửa học sinh vào lớp do bạn chủ nhiệm", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    if (editing == null) {
                        long id = db.insertHocSinh(new HocSinh(0, ten, ngay, gt, qq, maLop));
                        if (id > 0) Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                        else Toast.makeText(this, "Thêm thất bại", Toast.LENGTH_SHORT).show();
                    } else {
                        int rows = db.updateHocSinh(new HocSinh(editing.getId(), ten, ngay, gt, qq, maLop));
                        if (rows > 0) Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        else Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    }
                    refresh();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
