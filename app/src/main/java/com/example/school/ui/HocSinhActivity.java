package com.example.school.ui;

import static com.example.school.R.*;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school.R;
import com.example.school.adapters.HocSinhAdapter;
import com.example.school.auth.SessionManager;
import com.example.school.data.DBHelper;
import com.example.school.model.HocSinh;
import com.example.school.model.LopHoc;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

public class HocSinhActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private List<HocSinh> list;
    private HocSinhAdapter adapter;
    private DBHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_hocsinh);

        db = new DBHelper(this);
        session = new SessionManager(this);

        recyclerView = findViewById(R.id.recyclerViewHocSinh);
        fabAdd = findViewById(R.id.fabAddHocSinh);

        String roleRaw = session.getUserRole() == null ? "" : session.getUserRole();
        String role = roleRaw.toLowerCase(Locale.ROOT);
        int userId = session.getUserId();

        MaterialToolbar toolbar = findViewById(R.id.toolbarHocSinh);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setTitle("Danh sách học sinh");

        // tải dữ liệu & adapter theo role
        if (role.contains("phuhuynh")) {
            // parent: chỉ xem con mình
            fabAdd.setVisibility(View.GONE);
            list = db.getStudentsByParent(userId);
            adapter = new HocSinhAdapter(list, null); // không có long click
        } else if (role.contains("giaovien")) {
            // teacher: xem HS trong lớp do mình chủ nhiệm; cho phép thêm (tuỳ bạn)
            fabAdd.setVisibility(View.VISIBLE);
            list = db.getHocSinhByTeacher(userId);
            adapter = new HocSinhAdapter(list, hs -> onStudentLongPressed(hs, userId));
            // nếu bạn không muốn cho giáo viên thêm HS bỏ hoặc set GONE:
            fabAdd.setOnClickListener(v -> showAddEditDialog(null, userId));
        } else {
            // admin / khác: full access
            fabAdd.setVisibility(View.VISIBLE);
            list = db.getAllHocSinh();
            adapter = new HocSinhAdapter(list, hs -> {
                 onStudentLongPressed(hs, -1);
            });
            fabAdd.setOnClickListener(v -> showAddEditDialog(null, -1));
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        refresh();
    }

    private void refresh() {
        String roleRaw = session.getUserRole() == null ? "" : session.getUserRole();
        String role = roleRaw.toLowerCase(Locale.ROOT);
        int userId = session.getUserId();

        list.clear();
        if (role.contains("phuhuynh")) {
            list.addAll(db.getStudentsByParent(userId));
        } else if (role.contains("giaovien")) {
            list.addAll(db.getHocSinhByTeacher(userId));
        } else {
            list.addAll(db.getAllHocSinh());
        }
        adapter.notifyDataSetChanged();
        findViewById(R.id.tvEmptyHocSinh).setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /**
     * Xử lý khi long-press 1 học sinh trong danh sách: show Sửa / Xóa
     * actingTeacherId = id của gv đang thao tác (nếu gv) hoặc -1 nếu admin
     */
    private void onStudentLongPressed(HocSinh hs, int actingTeacherId) {
        String roleRaw = session.getUserRole() == null ? "" : session.getUserRole();
        String role = roleRaw.toLowerCase(Locale.ROOT);
        boolean isAdmin = role.contains("admin");
        boolean isTeacher = role.contains("giaovien");

        // teacher chỉ thao tác nếu HS thuộc lớp do mình chủ nhiệm
        if (isTeacher && actingTeacherId != -1) {
            boolean allowed = false;
            List<LopHoc> myClasses = db.getLopByTeacher(actingTeacherId);
            for (LopHoc l : myClasses) {
                if (l.getId() == hs.getMaLop()) { allowed = true; break; }
            }
            if (!allowed) {
                Toast.makeText(this, "Bạn chỉ có quyền sửa/xóa học sinh trong lớp do bạn chủ nhiệm", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (!isAdmin && !isTeacher) {
            Toast.makeText(this, "Bạn không có quyền thực hiện thao tác này", Toast.LENGTH_SHORT).show();
            return;
        }

        CharSequence[] items = {"Sửa", "Xóa", "Hủy"};
        new AlertDialog.Builder(this)
                .setTitle(hs.getHoTen())
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        // Sửa
                        showAddEditDialog(hs, actingTeacherId);
                    } else if (which == 1) {
                        // Xóa (xác nhận)
                        new AlertDialog.Builder(this)
                                .setTitle("Xóa học sinh")
                                .setMessage("Bạn có chắc muốn xóa học sinh này không?")
                                .setPositiveButton("Xóa", (d, w) -> {
                                    // Nếu bạn đã thêm deleteHocSinhCascade trong DBHelper thì gọi:
                                    boolean ok = db.deleteHocSinhCascade(hs.getId());
                                    // Nếu chưa có phương thức cascade, bạn có thể làm db.deleteHocSinh(hs.getId()) và db.delete ParentStudent / Diem tương ứng
                                    if (ok) Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show();
                                    else Toast.makeText(this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                                    refresh();
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                    }
                })
                .show();
    }

    /**
     * Dialog thêm / sửa học sinh
     * teacherUserId: nếu != -1 sẽ kiểm tra lớp phải là lớp do giáo viên đó chủ nhiệm
     */
    private void showAddEditDialog(HocSinh editing, int teacherUserId) {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_edit_hocsinh, null);
        EditText etHoTen = view.findViewById(R.id.etHoTen);
        EditText etNgay = view.findViewById(R.id.etNgaySinh);
        EditText etGT = view.findViewById(R.id.etGioiTinh);
        EditText etQQ = view.findViewById(R.id.etQueQuan);
        EditText etMaLop = view.findViewById(R.id.etMaLop);

        if (editing != null) {
            etHoTen.setText(editing.getHoTen());
            etNgay.setText(editing.getNgaySinh());
            etGT.setText(editing.getGioiTinh());
            etQQ.setText(editing.getQueQuan());
            etMaLop.setText(String.valueOf(editing.getMaLop()));
        }

        new AlertDialog.Builder(this)
                .setTitle(editing == null ? "Thêm học sinh" : "Sửa học sinh")
                .setView(view)
                .setPositiveButton("Lưu", (d, w) -> {
                    String ten = etHoTen.getText().toString().trim();
                    String ngay = etNgay.getText().toString().trim();
                    String gt = etGT.getText().toString().trim();
                    String qq = etQQ.getText().toString().trim();
                    int maLop = -1;
                    try { maLop = Integer.parseInt(etMaLop.getText().toString().trim()); } catch (Exception ex) { maLop = -1; }

                    // Nếu teacherUserId != -1 thì bắt buộc lớp phải thuộc teacher
                    if (teacherUserId != -1) {
                        boolean ok = false;
                        List<LopHoc> myClasses = db.getLopByTeacher(teacherUserId);
                        for (LopHoc l : myClasses) {
                            if (l.getId() == maLop) { ok = true; break; }
                        }
                        if (!ok) {
                            Toast.makeText(this, "Bạn chỉ có thể thêm/sửa học sinh vào lớp do bạn chủ nhiệm", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    if (editing == null) {
                        long id = db.insertHocSinh(new HocSinh(0, ten, ngay, gt, qq, maLop));
                        if (id > 0) {
                            Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                            promptAddParentForNewStudent((int) id);
                        } else {
                            Toast.makeText(this, "Thêm thất bại", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        int rows = db.updateHocSinh(new HocSinh(editing.getId(), ten, ngay, gt, qq, maLop));
                        if (rows > 0) Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        else Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    }
                    refresh();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Hỏi có muốn thêm phụ huynh ngay sau khi tạo HS
    private void promptAddParentForNewStudent(int newStudentId) {
        new AlertDialog.Builder(this)
                .setTitle("Thêm phụ huynh")
                .setMessage("Bạn có muốn thêm thông tin phụ huynh cho học sinh vừa tạo không?")
                .setPositiveButton("Có", (d, w) -> showAddParentDialog(newStudentId))
                .setNegativeButton("Không", null)
                .show();
    }

    // Form thêm phụ huynh: lưu user + mapping ParentStudent
    private void showAddParentDialog(int studentId) {
        View v = getLayoutInflater().inflate(R.layout.dialog_add_parent, null);
        EditText etHoTen = v.findViewById(R.id.etHoTenPH);
        EditText etUsername = v.findViewById(R.id.etUsernamePH);
        EditText etPassword = v.findViewById(R.id.etPasswordPH);
        EditText etEmail = v.findViewById(R.id.etEmailPH);
        EditText etPhone = v.findViewById(R.id.etPhonePH);

        new AlertDialog.Builder(this)
                .setTitle("Thêm phụ huynh")
                .setView(v)
                .setPositiveButton("Hoàn thành", (d, w) -> {
                    String hoTen = etHoTen.getText().toString().trim();
                    String username = etUsername.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();

                    if (username.isEmpty() || password.isEmpty()) {
                        Toast.makeText(this, "Username và mật khẩu là bắt buộc", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // tạo user phụ huynh
                    boolean created = db.registerUser(username, password, "phuhuynh", email, phone);
                    if (!created) {
                        Toast.makeText(this, "Tạo phụ huynh thất bại (có thể username đã tồn tại)", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // tìm id phụ huynh vừa tạo (fallback bằng getAllUsers)
                    int parentId = -1;
                    List<com.example.school.model.NguoiDung> all = db.getAllUsers();
                    for (com.example.school.model.NguoiDung u : all) {
                        if (u.getTenDangNhap() != null && u.getTenDangNhap().equals(username)) {
                            parentId = u.getId();
                            break;
                        }
                    }
                    if (parentId == -1) {
                        Toast.makeText(this, "Không tìm được user vừa tạo để gán phụ huynh", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long mapId = db.assignStudentToParent(parentId, studentId);
                    if (mapId > 0) {
                        Toast.makeText(this, "Đã thêm phụ huynh và gán cho học sinh", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Gán phụ huynh thất bại (có thể đã gán trước đó)", Toast.LENGTH_SHORT).show();
                    }
                    refresh();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
