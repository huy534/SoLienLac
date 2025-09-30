package com.example.school.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school.R;
import com.example.school.adapters.TeacherAdapter;
import com.example.school.data.DBHelper;
import com.example.school.model.NguoiDung;

import java.util.List;

public class SelectGiaoVienActivity extends AppCompatActivity {
    private DBHelper db;
    private RecyclerView rv;
    private TeacherAdapter adapter;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_teacher);

        db = new DBHelper(this);
        Toolbar toolbar = findViewById(R.id.toolbarSelectTeacher);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) { getSupportActionBar().setDisplayHomeAsUpEnabled(true); getSupportActionBar().setTitle("Chọn giáo viên"); }
        toolbar.setNavigationOnClickListener(v -> finish());

        rv = findViewById(R.id.recyclerViewSelectTeacher);
        rv.setLayoutManager(new LinearLayoutManager(this));
        searchView = findViewById(R.id.searchSelectTeacher);

        loadTeachers();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { adapter.filter(query); return true; }
            @Override public boolean onQueryTextChange(String newText) { adapter.filter(newText); return true; }
        });

        adapter.setOnItemClickListener(teacher -> {
            Intent data = new Intent();
            data.putExtra("teacherId", teacher.getId());
            data.putExtra("teacherName", teacher.getTenDangNhap());
            setResult(RESULT_OK, data);
            finish();
        });
    }

    private void loadTeachers() {
        List<NguoiDung> list = db.getUsersByRole("giaovien");
        adapter = new TeacherAdapter(list);
        rv.setAdapter(adapter);
    }
}
