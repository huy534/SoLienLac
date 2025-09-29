package com.example.school.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school.R;
import com.example.school.adapters.LopHocAdapter;
import com.example.school.auth.SessionManager;
import com.example.school.data.DBHelper;
import com.example.school.model.LopHoc;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.util.List;

public class LopHocActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private List<LopHoc> list;
    private LopHocAdapter adapter;
    private DBHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lophoc);

        db = new DBHelper(this);
        session = new SessionManager(this);

        recyclerView = findViewById(R.id.recyclerViewLopHoc);
        fabAdd = findViewById(R.id.fabAddLopHoc);

        String role = session.getUserRole();
        int userId = session.getUserId();
        MaterialToolbar toolbar = findViewById(R.id.toolbarLopHoc);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setTitle("Danh sách lớp học");
        if ("GiaoVien".equalsIgnoreCase(role)) {
            // teacher: see only their classes; optionally disable add
            list = db.getLopByTeacher(userId);
            fabAdd.setVisibility(View.GONE); // or allow creation per policy
            adapter = new LopHocAdapter(list, lop -> {
                // long click delete only if gvcn == userId
                if (lop.getGvcn() == userId) {
                    new AlertDialog.Builder(this)
                            .setTitle("Xóa lớp")
                            .setMessage("Bạn có chắc muốn xóa " + lop.getTenLop() + " ?")
                            .setPositiveButton("Xóa", (d, w) -> {
                                db.deleteLopHoc(lop.getId());
                                refresh();
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                } else {
                    Toast.makeText(this, "Bạn không có quyền xóa lớp này", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // admin/others: see all
            list = db.getAllLopHoc();
            adapter = new LopHocAdapter(list, lop -> {
                new AlertDialog.Builder(this)
                        .setTitle("Xóa lớp")
                        .setMessage("Bạn có chắc muốn xóa " + lop.getTenLop() + " ?")
                        .setPositiveButton("Xóa", (d, w) -> {
                            db.deleteLopHoc(lop.getId());
                            refresh();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
            fabAdd.setOnClickListener(v -> showAddEditDialog(null));
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        refresh();
    }

    private void refresh() {
        String role = session.getUserRole();
        int userId = session.getUserId();

        list.clear();
        if ("GiaoVien".equalsIgnoreCase(role)) {
            list.addAll(db.getLopByTeacher(userId));
        } else {
            list.addAll(db.getAllLopHoc());
        }
        adapter.notifyDataSetChanged();
        findViewById(R.id.tvEmptyLopHoc).setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showAddEditDialog(LopHoc editing) {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_edit_lophoc, null);
        EditText etTen = view.findViewById(R.id.etTenLop);
        EditText etKhoi = view.findViewById(R.id.etKhoi);
        EditText etNam = view.findViewById(R.id.etNamHoc);
        EditText etSiSo = view.findViewById(R.id.etSiSo);
        EditText etGVCN = view.findViewById(R.id.etGvcn);

        if (editing != null) {
            etTen.setText(editing.getTenLop());
            etKhoi.setText(editing.getKhoi());
            etNam.setText(editing.getNamHoc());
            etSiSo.setText(String.valueOf(editing.getSiSo()));
            etGVCN.setText(String.valueOf(editing.getGvcn()));
        }

        new AlertDialog.Builder(this)
                .setTitle(editing == null ? "Thêm lớp học" : "Sửa lớp học")
                .setView(view)
                .setPositiveButton("Lưu", (d, w) -> {
                    String ten = etTen.getText().toString().trim();
                    String khoi = etKhoi.getText().toString().trim();
                    String nam = etNam.getText().toString().trim();
                    int siSo = 0;
                    int gvcn = -1;
                    try { siSo = Integer.parseInt(etSiSo.getText().toString().trim()); } catch (Exception ignored) {}
                    try { gvcn = Integer.parseInt(etGVCN.getText().toString().trim()); } catch (Exception ignored) {}

                    if (editing == null) {
                        db.insertLopHoc(new LopHoc(0, ten, khoi, nam, siSo, gvcn));
                    } else {
                        db.updateLopHoc(new LopHoc(editing.getId(), ten, khoi, nam, siSo, gvcn));
                    }
                    refresh();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
