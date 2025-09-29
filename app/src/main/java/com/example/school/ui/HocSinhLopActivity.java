package com.example.school.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school.R;
import com.example.school.adapters.HocSinhAdapter;
import com.example.school.auth.SessionManager;
import com.example.school.data.DBHelper;
import com.example.school.model.HocSinh;

import java.util.List;

public class HocSinhLopActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private HocSinhAdapter adapter;
    private SearchView searchView;
    private DBHelper db;
    private SessionManager session;
    private TextView tvEmptyHS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hocsinh_lop);

        db = new DBHelper(this);
        session = new SessionManager(this);

        recyclerView = findViewById(R.id.recyclerHocSinh);
        searchView = findViewById(R.id.searchHocSinh);
        tvEmptyHS = findViewById(R.id.tvEmptyHS);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        int gvId = session.getUserId();
        int lopId = db.getLopIdByGVCN(gvId); // phương thức lấy lớp chủ nhiệm

        List<HocSinh> list = db.getHocSinhByLop(lopId);
        adapter = new HocSinhAdapter(list, null);
        recyclerView.setAdapter(adapter);

        tvEmptyHS.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

        // tìm kiếm
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { adapter.filter(query); return true; }
            @Override public boolean onQueryTextChange(String newText) { adapter.filter(newText); return true; }
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Danh sách học sinh"); // đổi tiêu đề tùy activity
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // thoát Activity hiện tại
        return true;
    }

}
