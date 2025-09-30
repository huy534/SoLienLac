package com.example.school.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school.R;
import com.example.school.adapters.TKBAdapter;
import com.example.school.auth.SessionManager;
import com.example.school.data.DBHelper;
import com.example.school.model.TKB;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class TKBActivity extends AppCompatActivity {
    private DBHelper db;
    private SessionManager session;
    private RecyclerView rv;
    private TKBAdapter adapter;
    private FloatingActionButton fab;
    private SearchView searchView;
    private String mode;
    private int id; // teacherId or studentId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tkb);

        db = new DBHelper(this);
        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbarTKB);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) { getSupportActionBar().setDisplayHomeAsUpEnabled(true); getSupportActionBar().setTitle("Thời khóa biểu"); }
        toolbar.setNavigationOnClickListener(v -> finish());

        rv = findViewById(R.id.recyclerViewTKB);
        rv.setLayoutManager(new LinearLayoutManager(this));
        fab = findViewById(R.id.fabAddTKB);
        searchView = findViewById(R.id.searchTKB);

        mode = getIntent().getStringExtra("mode");
        id = getIntent().getIntExtra("id", -1);

        loadData();

        fab.setOnClickListener(v -> {
            String role = session.getUserRole() == null ? "" : session.getUserRole().toLowerCase();
            if (role.contains("admin")) {
                showAddEditDialog(null);
            } else {
                Toast.makeText(this, "Chỉ admin được thêm TKB", Toast.LENGTH_SHORT).show();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { adapter.filter(query); return true; }
            @Override public boolean onQueryTextChange(String newText) { adapter.filter(newText); return true; }
        });

        adapter.setOnItemLongClickListener(item -> {
            String role = session.getUserRole() == null ? "" : session.getUserRole().toLowerCase();
            if (!role.contains("admin")) {
                Toast.makeText(TKBActivity.this, "Chỉ admin được sửa/xóa", Toast.LENGTH_SHORT).show();
                return;
            }
            showAddEditDialog(item);
        });
    }

    private void loadData() {
        List<TKB> list;
        if ("teacher".equalsIgnoreCase(mode)) {
            list = db.getTKBByTeacher(id);
        } else if ("student".equalsIgnoreCase(mode)) {
            list = db.getTKBByStudent(id);
        } else {
            list = db.getAllTKB();
        }
        adapter = new TKBAdapter(list);
        rv.setAdapter(adapter);
    }

    private void refresh() {
        loadData();
        adapter.notifyDataSetChanged();
    }


    private void showAddEditDialog(TKB editing) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_tkb, null);
        EditText etMaLop = v.findViewById(R.id.etMaLop);
        EditText etMaMon = v.findViewById(R.id.etMaMon);
        EditText etThu = v.findViewById(R.id.etThu);
        EditText etTiet = v.findViewById(R.id.etTiet);

        if (editing != null) {
            etMaLop.setText(String.valueOf(editing.getMaLop()));
            etMaMon.setText(String.valueOf(editing.getMaMon()));
            etThu.setText(String.valueOf(editing.getThu()));
            etTiet.setText(String.valueOf(editing.getTiet()));
        }

        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle(editing == null ? "Thêm TKB" : "Sửa TKB")
                .setView(v)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null);

        if (editing != null) {
            b.setNeutralButton("Xóa", (d,w) -> {
                int rows = db.deleteTKB(editing.getId());
                Toast.makeText(this, rows>0 ? "Đã xóa" : "Xóa thất bại", Toast.LENGTH_SHORT).show();
                refresh();
            });
        }

        AlertDialog dlg = b.create();
        dlg.setOnShowListener(dialog -> {
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
                int maLop = parseIntSafe(etMaLop.getText().toString().trim(), -1);
                int maMon = parseIntSafe(etMaMon.getText().toString().trim(), -1);
                int thu = parseIntSafe(etThu.getText().toString().trim(), 2);
                int tiet = parseIntSafe(etTiet.getText().toString().trim(), 1);

                if (maLop <= 0 || maMon <= 0) {
                    Toast.makeText(TKBActivity.this, "Nhập mã lớp và mã môn hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (editing == null) {
                    TKB t = new TKB();
                    t.setMaLop(maLop); t.setMaMon(maMon); t.setThu(thu); t.setTiet(tiet);
                    long id = db.insertTKB(t);
                    Toast.makeText(this, id>0 ? "Đã thêm" : "Thất bại", Toast.LENGTH_SHORT).show();
                } else {
                    editing.setMaLop(maLop); editing.setMaMon(maMon); editing.setThu(thu); editing.setTiet(tiet);
                    int rows = db.updateTKB(editing);
                    Toast.makeText(this, rows>0 ? "Đã cập nhật" : "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
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
}
