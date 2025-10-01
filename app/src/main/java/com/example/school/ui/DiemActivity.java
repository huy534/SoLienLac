package com.example.school.ui;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school.R;
import com.example.school.adapters.DiemAdapter;
import com.example.school.auth.SessionManager;
import com.example.school.data.DBHelper;
import com.example.school.model.Diem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

public class DiemActivity extends AppCompatActivity {

    private DBHelper db;
    private SessionManager session;
    private RecyclerView recyclerView;
    private DiemAdapter adapter;
    private SearchView searchView;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diem);

        db = new DBHelper(this);
        session = new SessionManager(this);

        recyclerView = findViewById(R.id.recyclerViewDiem);
        searchView = findViewById(R.id.searchDiem);
        fabAdd = findViewById(R.id.fabAddDiem);

        Toolbar toolbar = findViewById(R.id.toolbarDiem);
        if (toolbar != null) setSupportActionBar(toolbar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String role = session.getUserRole() == null ? "" : session.getUserRole().trim().toLowerCase(Locale.getDefault());
        int userId = session.getUserId();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Danh sách điểm và nhận xét");
        }

        List<Diem> list;
        if (role.contains("giaovien")) {
            list = db.getDiemByTeacher(userId);
            fabAdd.setVisibility(View.VISIBLE);
            fabAdd.setOnClickListener(v -> showDetailDialog(null, true));
        } else if (role.contains("phuhuynh")) {
            list = db.getDiemByParent(userId);
            fabAdd.setVisibility(View.GONE);
        } else {
            list = db.getAllDiem(); // admin
            fabAdd.setVisibility(View.VISIBLE);
            fabAdd.setOnClickListener(v -> showDetailDialog(null, true));
        }

        adapter = new DiemAdapter(list);
        recyclerView.setAdapter(adapter);

        // set long click theo role
        if (role.contains("phuhuynh")) {
            adapter.setOnItemLongClickListener(item -> showDetailDialog(item, false)); // chỉ xem
        } else {
            adapter.setOnItemLongClickListener(item -> showDetailDialog(item, true)); // sửa/xóa
        }

        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override public boolean onQueryTextSubmit(String query) { adapter.filter(query); return true; }
                @Override public boolean onQueryTextChange(String newText) { adapter.filter(newText); return true; }
            });
        }

        findViewById(R.id.tvEmptyDiem).setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void refreshList() {
        String role = session.getUserRole() == null ? "" : session.getUserRole().trim().toLowerCase(Locale.getDefault());
        int userId = session.getUserId();
        List<Diem> fresh;
        if (role.contains("giaovien")) fresh = db.getDiemByTeacher(userId);
        else if (role.contains("phuhuynh")) fresh = db.getDiemByParent(userId);
        else fresh = db.getAllDiem();
        adapter.updateData(fresh);
        findViewById(R.id.tvEmptyDiem).setVisibility(fresh == null || fresh.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private String loadStudentName(int studentId) {
        String name = "";
        SQLiteDatabase rdb = db.getReadableDatabase();
        Cursor c = rdb.rawQuery("SELECT hoTen FROM HocSinh WHERE id=?", new String[]{String.valueOf(studentId)});
        if (c != null) {
            if (c.moveToFirst()) name = c.getString(0);
            c.close();
        }
        return name == null ? "" : name;
    }

    private String loadSubjectName(int monId) {
        String name = "";
        SQLiteDatabase rdb = db.getReadableDatabase();
        Cursor c = rdb.rawQuery("SELECT tenMon FROM MonHoc WHERE id=?", new String[]{String.valueOf(monId)});
        if (c != null) {
            if (c.moveToFirst()) name = c.getString(0);
            c.close();
        }
        return name == null ? "" : name;
    }

    private void showDetailDialog(Diem diem, boolean editable) {
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_add_edit_diem, null);

        TextView tvStudentName = v.findViewById(R.id.tvStudentName);
        TextView tvSubjectName = v.findViewById(R.id.tvSubjectName);
        EditText etHocSinhId = v.findViewById(R.id.etHocSinhId);
        EditText etMonId = v.findViewById(R.id.etMonId);
        EditText etDiemHS1 = v.findViewById(R.id.etDiemHS1);
        EditText etDiemHS2 = v.findViewById(R.id.etDiemHS2);
        EditText etDiemThi = v.findViewById(R.id.etDiemThi);
        EditText etNhanXet = v.findViewById(R.id.etNhanXet);
        Spinner spDiemDanh = v.findViewById(R.id.spDiemDanh);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Có mặt", "Vắng có phép", "Vắng không phép", "Muộn"});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDiemDanh.setAdapter(spinnerAdapter);

        if (diem != null) {
            etHocSinhId.setText(String.valueOf(diem.getHocSinhId()));
            etMonId.setText(String.valueOf(diem.getMonId()));
            etDiemHS1.setText(String.valueOf(diem.getDiemHS1()));
            etDiemHS2.setText(String.valueOf(diem.getDiemHS2()));
            etDiemThi.setText(String.valueOf(diem.getDiemThi()));
            etNhanXet.setText(diem.getNhanXet() == null ? "" : diem.getNhanXet());

            tvStudentName.setText(loadStudentName(diem.getHocSinhId()));
            tvSubjectName.setText(loadSubjectName(diem.getMonId()));
        }

        if (!editable) {
            etHocSinhId.setEnabled(false);
            etMonId.setEnabled(false);
            etDiemHS1.setEnabled(false);
            etDiemHS2.setEnabled(false);
            etDiemThi.setEnabled(false);
            etNhanXet.setEnabled(false);
            spDiemDanh.setEnabled(false);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(editable ? (diem == null ? "Thêm điểm" : "Chi tiết điểm") : "Chi tiết điểm")
                .setView(v)
                .setNegativeButton("Đóng", null);

        if (editable) {
            builder.setPositiveButton("Lưu", null);
            if (diem != null) builder.setNeutralButton("Xóa", null);
        }

        AlertDialog dialog = builder.create();
        if (editable) {
            dialog.setOnShowListener(dialogInterface -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                    int hsId = parseIntSafe(etHocSinhId.getText().toString().trim(), -1);
                    int monId = parseIntSafe(etMonId.getText().toString().trim(), -1);
                    float hs1 = parseFloatSafe(etDiemHS1.getText().toString().trim(), 0f);
                    float hs2 = parseFloatSafe(etDiemHS2.getText().toString().trim(), 0f);
                    float thi = parseFloatSafe(etDiemThi.getText().toString().trim(), 0f);
                    String nhanXet = etNhanXet.getText().toString().trim();
                    int attendance = spDiemDanh.getSelectedItemPosition();

                    if (hsId <= 0 || monId <= 0) {
                        Toast.makeText(DiemActivity.this, "Nhập mã học sinh và mã môn hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        db.upsertDiem(hsId, monId, hs1, hs2, thi, nhanXet);
                        db.markAttendance(hsId, monId, today(), attendance);
                        Toast.makeText(DiemActivity.this, "Lưu thành công", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        refreshList();
                    } catch (Exception ex) {
                        Toast.makeText(DiemActivity.this, "Lỗi lưu: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

                if (diem != null) {
                    dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(vBtn -> {
                        new AlertDialog.Builder(DiemActivity.this)
                                .setTitle("Xác nhận")
                                .setMessage("Bạn có muốn xóa điểm này không?")
                                .setPositiveButton("Xóa", (dd, ww) -> {
                                    SQLiteDatabase wdb = db.getWritableDatabase();
                                    int rows = wdb.delete("Diem", "hocSinhId=? AND monId=?", new String[]{String.valueOf(diem.getHocSinhId()), String.valueOf(diem.getMonId())});
                                    Toast.makeText(DiemActivity.this, rows > 0 ? "Đã xóa" : "Xóa thất bại", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    refreshList();
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                    });
                }
            });
        }
        dialog.show();
    }

    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private String today() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new java.util.Date());
    }

    private int parseIntSafe(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (Exception ex) { return fallback; }
    }

    private float parseFloatSafe(String s, float fallback) {
        try { return Float.parseFloat(s); } catch (Exception ex) { return fallback; }
    }
}
