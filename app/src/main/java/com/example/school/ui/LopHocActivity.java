package com.example.school.ui;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LopHocActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private List<LopHoc> list = new ArrayList<>();
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

        // role xử lý (lưu ý: SessionManager lưu role thường là lower-case "giaovien")
        String role = session.getUserRole() == null ? "" : session.getUserRole().toLowerCase(Locale.getDefault());
        int userId = session.getUserId();

        boolean isAdmin = role.contains("admin");
        boolean isGiaoVien = role.contains("giaovien");

        MaterialToolbar toolbar = findViewById(R.id.toolbarLopHoc);
        if (toolbar != null) {
            toolbar.setTitle("Danh sách lớp học");
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // load dữ liệu tuỳ role
        if (isGiaoVien) {
            list.clear();
            list.addAll(db.getLopByTeacher(userId));
            if (fabAdd != null) fabAdd.setVisibility(View.GONE); // GV ko được thêm lớp
        } else {
            list.clear();
            list.addAll(db.getAllLopHoc());
            if (fabAdd != null) fabAdd.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        }

        // khởi tạo adapter
        adapter = new LopHocAdapter(list); // <-- nếu adapter của bạn có constructor khác, điều chỉnh ở đây
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // click: mở chi tiết lớp (bạn có thể chuyển đến Activity hiển thị HS theo lớp)
        adapter.setOnItemClickListener(lop -> {
            Intent i = new Intent(LopHocActivity.this, com.example.school.ui.HocSinhByLopActivity.class);
            i.putExtra("maLop", lop.getId());
            startActivity(i);
        });

        // long click: chỉ admin / (gv: chỉ lớp gvcn của họ đã lọc) -> cho xem danh sách HS hoặc xóa nếu admin
        adapter.setOnItemLongClickListener(lop -> {
            if (isAdmin) {
                // admin: show edit/delete dialog (ví dụ xóa)
                new AlertDialog.Builder(LopHocActivity.this)
                        .setTitle("Xóa lớp")
                        .setMessage("Bạn có chắc muốn xóa " + lop.getTenLop() + " ?")
                        .setPositiveButton("Xóa", (d, w) -> {
                            db.deleteLopHoc(lop.getId());
                            refresh();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            } else {
                // non-admin: long click -> mở danh sách HS lớp (xem) (GV sẽ chỉ thấy lớp mình do list đã lọc)
                Intent i = new Intent(LopHocActivity.this, com.example.school.ui.HocSinhByLopActivity.class);
                i.putExtra("maLop", lop.getId());
                startActivity(i);
            }
        });

        // fab: chỉ admin được thêm
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                if (!isAdmin) {
                    Toast.makeText(LopHocActivity.this, "Bạn không có quyền thêm lớp", Toast.LENGTH_SHORT).show();
                    return;
                }
                showAddEditDialog(null);
            });
        }

        refresh();
    }

    private void refresh() {
        String role = session.getUserRole() == null ? "" : session.getUserRole().toLowerCase(Locale.getDefault());
        int userId = session.getUserId();
        list.clear();
        if (role.contains("giaovien")) {
            list.addAll(db.getLopByTeacher(userId));
        } else {
            list.addAll(db.getAllLopHoc());
        }
        adapter.updateData(list); // giả sử adapter có method updateData(List<LopHoc>)
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
