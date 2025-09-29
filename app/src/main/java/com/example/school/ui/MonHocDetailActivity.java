package com.example.school.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school.R;
import com.example.school.adapters.StudentInMonAdapter;
import com.example.school.auth.SessionManager;
import com.example.school.data.DBHelper;
import com.example.school.model.Diem;
import com.example.school.model.HocSinh;
import com.example.school.model.MonHoc;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MonHocDetailActivity extends AppCompatActivity {

    public static final String EXTRA_MON_ID = "monId";

    private DBHelper db;
    private SessionManager session;
    private RecyclerView rvStudents;
    private TextView tvMonId, tvMonName, tvSoTiet, tvTeacher;
    private StudentInMonAdapter adapter;
    private List<HocSinh> students;
    private int monId;
    private MonHoc mon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monhoc_detail);

        db = new DBHelper(this);
        session = new SessionManager(this);

        tvMonId = findViewById(R.id.tvDetailMonId);
        tvMonName = findViewById(R.id.tvDetailMonTen);
        tvSoTiet = findViewById(R.id.tvDetailMonSoTiet);
        tvTeacher = findViewById(R.id.tvDetailMonGV);

        rvStudents = findViewById(R.id.recyclerStudentsInMon);
        rvStudents.setLayoutManager(new LinearLayoutManager(this));

        monId = getIntent().getIntExtra(EXTRA_MON_ID, -1);
        if (monId == -1) {
            Toast.makeText(this, "Môn học không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        MaterialToolbar toolbar = findViewById(R.id.toolbarMonDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        loadMonInfo();
        loadStudents();
    }

    private void loadMonInfo() {
        mon = db.getMonHocById(monId);
        if (mon == null) {
            Toast.makeText(this, "Không tìm thấy môn học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvMonId.setText("Mã môn: " + mon.getId());
        tvMonName.setText("Tên môn: " + mon.getTenMon());
        tvSoTiet.setText("Số tiết: " + mon.getSoTiet());
        String gvName = db.getTeacherNameById(mon.getTeacherId());
        if (TextUtils.isEmpty(gvName)) gvName = "Chưa phân công";
        tvTeacher.setText("GV: " + gvName);
    }

    private void loadStudents() {
        students = db.getStudentsByMon(monId);
        adapter = new StudentInMonAdapter(this, students, (hocSinh) -> {
            // click student -> show quick info or open detailed student view
            Toast.makeText(this, hocSinh.getHoTen(), Toast.LENGTH_SHORT).show();
        }, (hocSinh) -> {
            // long-press student -> open score/attendance dialog (allowed for admin or teacher of this mon)
            boolean isAdmin = "admin".equalsIgnoreCase(session.getUserRole());
            boolean isTeacher = "giaovien".equalsIgnoreCase(session.getUserRole())
                    && session.getUserId() == mon.getTeacherId();

            if (!isAdmin && !isTeacher) {
                Toast.makeText(this, "Bạn không có quyền chỉnh điểm/điểm danh", Toast.LENGTH_SHORT).show();
                return;
            }
            showStudentDialog(hocSinh);
        });
        rvStudents.setAdapter(adapter);
    }

    private void showStudentDialog(HocSinh hs) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_student_score_attendance, null);
        EditText etHS1 = v.findViewById(R.id.etHS1);
        EditText etHS2 = v.findViewById(R.id.etHS2);
        EditText etThi = v.findViewById(R.id.etThi);
        EditText etNhanXet = v.findViewById(R.id.etNhanXet);
        RadioGroup rgAttendance = v.findViewById(R.id.rgAttendance);

        // load existing diem
        Diem d = db.getDiemByStudentAndMon(hs.getId(), monId);
        if (d != null) {
            if (d.getDiemHS1() != 0) etHS1.setText(String.valueOf(d.getDiemHS1()));
            if (d.getDiemHS2() != 0) etHS2.setText(String.valueOf(d.getDiemHS2()));
            if (d.getDiemThi() != 0) etThi.setText(String.valueOf(d.getDiemThi()));
            if (d.getNhanXet() != null) etNhanXet.setText(d.getNhanXet());
        }

        // load attendance for today
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int status = db.getAttendanceStatus(hs.getId(), monId, today);
        // map status to radio button ids
        if (status == 0) rgAttendance.check(R.id.rbPresent);
        else if (status == 1) rgAttendance.check(R.id.rbAbsentPermit);
        else if (status == 2) rgAttendance.check(R.id.rbAbsentNoPermit);
        else if (status == 3) rgAttendance.check(R.id.rbLate);

        new AlertDialog.Builder(this)
                .setTitle(hs.getHoTen())
                .setView(v)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    float hs1 = parseFloatSafe(etHS1.getText().toString());
                    float hs2 = parseFloatSafe(etHS2.getText().toString());
                    float thi = parseFloatSafe(etThi.getText().toString());
                    String nx = etNhanXet.getText().toString();

                    db.upsertDiem(hs.getId(), monId, hs1, hs2, thi, nx);

                    // attendance
                    int checked = rgAttendance.getCheckedRadioButtonId();
                    int st = 0;
                    if (checked == R.id.rbPresent) st = 0;
                    else if (checked == R.id.rbAbsentPermit) st = 1;
                    else if (checked == R.id.rbAbsentNoPermit) st = 2;
                    else if (checked == R.id.rbLate) st = 3;

                    db.markAttendance(hs.getId(), monId, today, st);

                    Toast.makeText(this, "Đã lưu điểm & điểm danh", Toast.LENGTH_SHORT).show();
                    refresh();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private float parseFloatSafe(String s) {
        if (TextUtils.isEmpty(s)) return 0f;
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException ex) {
            return 0f;
        }
    }

    private void refresh() {
        students.clear();
        students.addAll(db.getStudentsByMon(monId));
        adapter.notifyDataSetChanged();
    }
}
