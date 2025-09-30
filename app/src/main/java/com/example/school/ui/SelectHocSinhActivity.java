package com.example.school.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school.R;
import com.example.school.adapters.HocSinhAdapter;
import com.example.school.data.DBHelper;
import com.example.school.model.HocSinh;

import java.util.List;

public class SelectHocSinhActivity extends AppCompatActivity {
    private DBHelper db;
    private RecyclerView rv;
    private HocSinhAdapter adapter;
    private SearchView searchView;
    private int parentId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_student);

        db = new DBHelper(this);
        Toolbar toolbar = findViewById(R.id.toolbarSelectStudent);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) { getSupportActionBar().setDisplayHomeAsUpEnabled(true); getSupportActionBar().setTitle("Chọn học sinh"); }
        toolbar.setNavigationOnClickListener(v -> finish());

        rv = findViewById(R.id.recyclerViewSelectStudent);
        rv.setLayoutManager(new LinearLayoutManager(this));
        searchView = findViewById(R.id.searchSelectStudent);

        parentId = getIntent().getIntExtra("parentId", -1);

        loadStudents();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { adapter.filter(query); return true; }
            @Override public boolean onQueryTextChange(String newText) { adapter.filter(newText); return true; }
        });

        adapter.setOnItemClickListener(hs -> {
            Intent data = new Intent();
            data.putExtra("studentId", hs.getId());
            data.putExtra("studentName", hs.getHoTen());
            setResult(RESULT_OK, data);
            finish();
        });
    }

    private void loadStudents() {
        List<HocSinh> list;
        if (parentId > 0) list = db.getStudentsByParent(parentId);
        else list = db.getAllHocSinh();
        adapter = new HocSinhAdapter(list, null);
        rv.setAdapter(adapter);
    }
}
