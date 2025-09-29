package com.example.school.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.school.R;
import com.example.school.data.DBHelper;
import com.example.school.model.HocSinh;


public class AddEditHocSinhActivity extends AppCompatActivity {
    private EditText etName, etDob, etGioiTinh, etQueQuan, etMaLop;
    private Button btnSave;
    private DBHelper db;
    private int editingId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_hocsinh);

        etName = findViewById(R.id.etName);
        etDob = findViewById(R.id.etDob);
        etGioiTinh = findViewById(R.id.etGioiTinh);
        etQueQuan = findViewById(R.id.etQueQuan);
        etMaLop = findViewById(R.id.etMaLop);
        btnSave = findViewById(R.id.btnSave);

        db = new DBHelper(this);

        if (getIntent() != null && getIntent().hasExtra("id")) {
            editingId = getIntent().getIntExtra("id", -1);
            // load student by id — for brevity, we search in getAllHocSinh
            for (HocSinh hs : db.getAllHocSinh()) {
                if (hs.getId() == editingId) {
                    etName.setText(hs.getHoTen());
                    etDob.setText(hs.getNgaySinh());
                    etGioiTinh.setText(hs.getGioiTinh());
                    etQueQuan.setText(hs.getQueQuan());
                    etMaLop.setText(String.valueOf(hs.getMaLop()));
                    break;
                }
            }
        }

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Nhập họ tên", Toast.LENGTH_SHORT).show();
                return;
            }
            String dob = etDob.getText().toString().trim();
            String gt = etGioiTinh.getText().toString().trim();
            String qq = etQueQuan.getText().toString().trim();
            int malop = -1;
            try { malop = Integer.parseInt(etMaLop.getText().toString().trim()); } catch (Exception ignored) {}

            HocSinh hs = new HocSinh(editingId, name, dob, gt, qq, malop);
            if (editingId == -1) {
                long id = db.insertHocSinh(hs);
                if (id > 0) {
                    Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else Toast.makeText(this, "Thêm thất bại", Toast.LENGTH_SHORT).show();
            } else {
                int rows = db.updateHocSinh(hs);
                if (rows > 0) {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
