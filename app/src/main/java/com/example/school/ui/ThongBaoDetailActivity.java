package com.example.school.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.school.R;
import com.example.school.auth.SessionManager;
import com.example.school.data.DBHelper;
import com.example.school.model.ThongBao;

public class ThongBaoDetailActivity extends AppCompatActivity {
    public static final String EXTRA_TB_ID = "tb_id";

    private DBHelper db;
    private SessionManager session;
    private TextView tvSender, tvTitle, tvContent, tvTime, tvTarget;
    private Button btnDelete;

    private int tbId;
    private ThongBao current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thongbao_detail);

        db = new DBHelper(this);
        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbarThongBaoDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết thông báo");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvSender = findViewById(R.id.tvTB_Sender);
        tvTitle = findViewById(R.id.tvTB_Title);
        tvContent = findViewById(R.id.tvTB_Content);
        tvTime = findViewById(R.id.tvTB_Time);
        tvTarget = findViewById(R.id.tvTB_Target);
        btnDelete = findViewById(R.id.btnTB_Delete);

        tbId = getIntent().getIntExtra(EXTRA_TB_ID, -1);
        if (tbId <= 0) {
            Toast.makeText(this, "Thông báo không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadAndShow();

        // Delete button only visible to admin
        String role = session.getUserRole() == null ? "" : session.getUserRole().toLowerCase();
        boolean isAdmin = "admin".equalsIgnoreCase(role) || role.contains("admin");
        btnDelete.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(ThongBaoDetailActivity.this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc muốn xóa thông báo này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        int rows = db.deleteThongBao(tbId);
                        Toast.makeText(ThongBaoDetailActivity.this, rows>0 ? "Đã xóa" : "Xóa thất bại", Toast.LENGTH_SHORT).show();
                        if (rows>0) finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void loadAndShow() {
        current = db.getThongBaoById(tbId);
        if (current == null) {
            Toast.makeText(this, "Không tìm thấy thông báo", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvSender.setText(current.getGuiTu() == null ? "-" : current.getGuiTu());
        tvTitle.setText(current.getTieuDe() == null ? "-" : current.getTieuDe());
        tvContent.setText(current.getNoiDung() == null ? "-" : current.getNoiDung());
        tvTime.setText(current.getThoiGian() == null ? "-" : current.getThoiGian());
        tvTarget.setText(current.getTargetRole() == null ? "-" : current.getTargetRole());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) db.close();
    }
}