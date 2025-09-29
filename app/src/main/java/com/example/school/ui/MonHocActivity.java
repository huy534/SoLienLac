package com.example.school.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school.R;
import com.example.school.adapters.MonHocAdapter;
import com.example.school.auth.SessionManager;
import com.example.school.data.DBHelper;
import com.example.school.model.MonHoc;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MonHocActivity extends AppCompatActivity {

    private DBHelper db;
    private SessionManager session;
    private RecyclerView rv;
    private MonHocAdapter adapter;
    private FloatingActionButton fabAdd;
    private List<MonHoc> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monhoc); // your layout

        db = new DBHelper(this);
        session = new SessionManager(this);

        rv = findViewById(R.id.recyclerViewMonHoc);
        fabAdd = findViewById(R.id.fabAddMonHoc);

        rv.setLayoutManager(new LinearLayoutManager(this));

        loadDataAndSetup();
        MaterialToolbar toolbar = findViewById(R.id.toolbarMonHoc);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setTitle("Danh sách môn học");
    }

    private void loadDataAndSetup() {
        String role = session.getUserRole();
        int userId = session.getUserId();

        if (role == null) role = "";

        if (role.equalsIgnoreCase("giaovien")) {
            // teacher: only subjects they teach
            list = db.getMonByTeacher(userId);
            fabAdd.setVisibility(View.GONE);
        } else if (role.equalsIgnoreCase("admin")) {
            // admin: see all, can add
            list = db.getAllMonHoc();
            fabAdd.setVisibility(View.VISIBLE);
        } else {
            // other roles (phuhuynh,...): no access to manage subjects, show all readonly maybe
            list = db.getAllMonHoc();
            fabAdd.setVisibility(View.GONE);
            // if you want to block completely: uncomment following lines
            // Toast.makeText(this, "Không có quyền truy cập Môn học", Toast.LENGTH_SHORT).show();
            // finish();
            // return;
        }

        adapter = new MonHocAdapter(this, list, db,
                // click callback: open detail activity
                (monHoc) -> {
                    // open MonHocDetailActivity (implement separately)
                    // Intent it = new Intent(MonHocActivity.this, MonHocDetailActivity.class);
                    // it.putExtra("monId", monHoc.getId());
                    // startActivity(it);
                    Toast.makeText(MonHocActivity.this, "Chi tiết môn: " + monHoc.getTenMon(), Toast.LENGTH_SHORT).show();
                },
                // long click callback for admin: show options edit/delete
                (monHoc) -> {
                    if (!"admin".equalsIgnoreCase(session.getUserRole())) {
                        Toast.makeText(MonHocActivity.this, "Không có quyền", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showLongPressOptions(monHoc);
                });

        rv.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            if (!"admin".equalsIgnoreCase(session.getUserRole())) {
                Toast.makeText(this, "Chỉ admin mới được thêm môn học", Toast.LENGTH_SHORT).show();
                return;
            }
            showAddEditDialog(null);
        });
    }

    private void showLongPressOptions(MonHoc monHoc) {
        String[] items = new String[]{"Sửa", "Xóa", "Hủy"};
        new AlertDialog.Builder(this)
                .setTitle(monHoc.getTenMon())
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        showAddEditDialog(monHoc);
                    } else if (which == 1) {
                        // confirm delete
                        new AlertDialog.Builder(this)
                                .setTitle("Xóa môn học")
                                .setMessage("Bạn có muốn xóa môn \"" + monHoc.getTenMon() + "\" không?")
                                .setPositiveButton("Xóa", (d, w) -> {
                                    int rows = db.deleteMonHoc(monHoc.getId());
                                    if (rows > 0) {
                                        Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show();
                                        refresh();
                                    } else {
                                        Toast.makeText(this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                    }
                })
                .show();
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // thoát Activity hiện tại
        return true;
    }
    private void showAddEditDialog(MonHoc editing) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_monhoc, null);
        EditText etName = view.findViewById(R.id.etTenMon);
        EditText etSoTiet = view.findViewById(R.id.etSoTiet);
        EditText etTeacherId = view.findViewById(R.id.etTeacherId); // simplest: enter teacher userId (int)

        if (editing != null) {
            etName.setText(editing.getTenMon());
            etSoTiet.setText(String.valueOf(editing.getSoTiet()));
            etTeacherId.setText(String.valueOf(editing.getTeacherId()));
        }

        new AlertDialog.Builder(this)
                .setTitle(editing == null ? "Thêm môn học" : "Sửa môn học")
                .setView(view)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String ten = etName.getText().toString().trim();
                    String soTietStr = etSoTiet.getText().toString().trim();
                    String teacherIdStr = etTeacherId.getText().toString().trim();

                    if (TextUtils.isEmpty(ten) || TextUtils.isEmpty(soTietStr)) {
                        Toast.makeText(this, "Vui lòng nhập tên và số tiết", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int soTiet = 0;
                    int teacherId = -1;
                    try {
                        soTiet = Integer.parseInt(soTietStr);
                    } catch (NumberFormatException ex) { soTiet = 0; }
                    try {
                        if (!TextUtils.isEmpty(teacherIdStr)) teacherId = Integer.parseInt(teacherIdStr);
                    } catch (NumberFormatException ex) { teacherId = -1; }

                    if (editing == null) {
                        MonHoc mh = new MonHoc();
                        mh.setTenMon(ten);
                        mh.setSoTiet(soTiet);
                        mh.setTeacherId(teacherId);
                        long id = db.insertMonHoc(mh);
                        if (id > 0) {
                            Toast.makeText(this, "Đã thêm", Toast.LENGTH_SHORT).show();
                            refresh();
                        } else {
                            Toast.makeText(this, "Thêm thất bại", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        editing.setTenMon(ten);
                        editing.setSoTiet(soTiet);
                        editing.setTeacherId(teacherId);
                        int rows = db.updateMonHoc(editing);
                        if (rows > 0) {
                            Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                            refresh();
                        } else {
                            Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void refresh() {
        String role = session.getUserRole();
        int userId = session.getUserId();
        if ("giaovien".equalsIgnoreCase(role)) {
            list.clear();
            list.addAll(db.getMonByTeacher(userId));
        } else {
            list.clear();
            list.addAll(db.getAllMonHoc());
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // refresh in case underlying data changed
        refresh();
    }
}
