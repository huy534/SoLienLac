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
import com.example.school.adapters.DiemAdapter;
import com.example.school.auth.SessionManager;
import com.example.school.data.DBHelper;
import com.example.school.model.Diem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.util.List;

public class DiemGVActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private List<Diem> list;
    private DiemAdapter adapter;
    private DBHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diem);

        db = new DBHelper(this);
        session = new SessionManager(this);

        recyclerView = findViewById(R.id.recyclerViewDiem);
        fabAdd = findViewById(R.id.fabAddDiem);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String role = session.getUserRole();
        int userId = session.getUserId();

        if ("PhuHuynh".equalsIgnoreCase(role)) {
            // parent: see only their children's scores
            list = db.getDiemByParent(userId);
            fabAdd.setVisibility(View.GONE);
        } else if ("GiaoVien".equalsIgnoreCase(role)) {
            // teacher: see scores of students in classes they manage
            list = db.getDiemByTeacher(userId);
            // optionally allow teacher to add/update scores:
            fabAdd.setVisibility(View.VISIBLE);
            fabAdd.setOnClickListener(v -> showAddEditDialog(null));
        } else {
            // admin/other: full access
            list = db.getAllDiem();
            fabAdd.setVisibility(View.VISIBLE);
            fabAdd.setOnClickListener(v -> showAddEditDialog(null));
        }

        adapter = new DiemAdapter(list);
        recyclerView.setAdapter(adapter);
        findViewById(R.id.tvEmptyDiem).setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void refresh() {
        String role = session.getUserRole();
        int userId = session.getUserId();
        list.clear();
        if ("PhuHuynh".equalsIgnoreCase(role)) list.addAll(db.getDiemByParent(userId));
        else if ("GiaoVien".equalsIgnoreCase(role)) list.addAll(db.getDiemByTeacher(userId));
        else list.addAll(db.getAllDiem());
        adapter.notifyDataSetChanged();
        findViewById(R.id.tvEmptyDiem).setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showAddEditDialog(Diem editing) {
        View v = getLayoutInflater().inflate(R.layout.dialog_add_edit_diem, null);
        EditText etHS = v.findViewById(R.id.etHocSinhId);
        EditText etMon = v.findViewById(R.id.etMonId);
        EditText etHS1 = v.findViewById(R.id.etDiemHS1);
        EditText etHS2 = v.findViewById(R.id.etDiemHS2);
        EditText etThi = v.findViewById(R.id.etDiemThi);
        EditText etNhanXet = v.findViewById(R.id.etNhanXet);

        if (editing != null) {
            etHS.setText(String.valueOf(editing.getHocSinhId()));
            etMon.setText(String.valueOf(editing.getMonId()));
            etHS1.setText(String.valueOf(editing.getDiemHS1()));
            etHS2.setText(String.valueOf(editing.getDiemHS2()));
            etThi.setText(String.valueOf(editing.getDiemThi()));
            etNhanXet.setText(editing.getNhanXet());
        }

        new AlertDialog.Builder(this)
                .setTitle(editing == null ? "Thêm điểm" : "Sửa điểm")
                .setView(v)
                .setPositiveButton("Lưu", (d, w) -> {
                    int hsId = -1, monId = -1;
                    float hs1 = 0f, hs2 = 0f, thi = 0f;
                    String nhanXet = "";

                    try { hsId = Integer.parseInt(etHS.getText().toString().trim()); } catch (Exception ignored) {}
                    try { monId = Integer.parseInt(etMon.getText().toString().trim()); } catch (Exception ignored) {}
                    try { hs1 = Float.parseFloat(etHS1.getText().toString().trim()); } catch (Exception ignored) {}
                    try { hs2 = Float.parseFloat(etHS2.getText().toString().trim()); } catch (Exception ignored) {}
                    try { thi = Float.parseFloat(etThi.getText().toString().trim()); } catch (Exception ignored) {}
                    nhanXet = etNhanXet.getText().toString().trim();

                    if (editing == null) {
                        db.insertDiem(new Diem(hsId, monId, hs1, hs2, thi, 0f, nhanXet));
                    } else {
                        db.updateDiem(new Diem(hsId, monId, hs1, hs2, thi, 0f, nhanXet));
                    }
                    refresh();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
