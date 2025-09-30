package com.example.school.ui;
import static com.example.school.R.*;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school.R;
import com.example.school.adapters.UserAdapter;
import com.example.school.auth.HashUtils;
import com.example.school.auth.SessionManager;
import com.example.school.data.DBHelper;
import com.example.school.model.NguoiDung;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class UserManagementActivity extends AppCompatActivity {
    private DBHelper db;
    private SessionManager session;
    private RecyclerView rv;
    private UserAdapter adapter;
    private SearchView searchView;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        db = new DBHelper(this);
        session = new SessionManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbarUsers);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        rv = findViewById(R.id.recyclerViewUsers);
        rv.setLayoutManager(new LinearLayoutManager(this));
        searchView = findViewById(R.id.searchUsers);
        fab = findViewById(R.id.fabAddUser);
        String role = session.getUserRole();
        fab.setVisibility("admin".equalsIgnoreCase(role) ? View.VISIBLE : View.GONE);
        loadUsers();

        fab.setOnClickListener(v -> showAddEditDialog(null));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { adapter.filter(query); return true; }
            @Override public boolean onQueryTextChange(String newText) { adapter.filter(newText); return true; }
        });
    }

    private void loadUsers() {
        List<NguoiDung> list = db.getAllUsers();
        adapter = new UserAdapter(list);
        adapter.setOnItemLongClickListener(user -> showAddEditDialog(user));
        rv.setAdapter(adapter);
    }

    private void refresh() {
        adapter.updateData(db.getAllUsers());
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }

    private void showAddEditDialog(NguoiDung editing) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_user, null);
        EditText etUser = v.findViewById(R.id.etUsername);
        EditText etPass = v.findViewById(R.id.etPassword);
        Spinner spRole = v.findViewById(R.id.spRole);
        EditText etEmail = v.findViewById(R.id.etEmail);
        EditText etPhone = v.findViewById(R.id.etPhone);

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"admin","giaovien","phuhuynh"});
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(roleAdapter);

        if (editing != null) {
            etUser.setText(editing.getTenDangNhap());
            etPass.setHint("Nhập mật khẩu mới (không bắt buộc)");
            etEmail.setText(editing.getEmail());
            etPhone.setText(editing.getSdt());
            for (int i = 0; i < roleAdapter.getCount(); i++) {
                if (roleAdapter.getItem(i).equalsIgnoreCase(editing.getVaiTro())) {
                    spRole.setSelection(i);
                    break;
                }
            }
        }

        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle(editing == null ? "Thêm người dùng" : "Sửa người dùng")
                .setView(v)
                .setNegativeButton("Hủy", (d, w) -> {})
                .setPositiveButton("Lưu", null);

        if (editing != null) {
            b.setNeutralButton("Xóa", (d, w) -> {
                int rows = db.deleteUser(editing.getId());
                Toast.makeText(this, rows > 0 ? "Đã xóa" : "Xóa thất bại", Toast.LENGTH_SHORT).show();
                refresh();
            });
        }

        AlertDialog dlg = b.create();
        dlg.setOnShowListener(dialog -> {
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
                String username = etUser.getText().toString().trim();
                String password = String.valueOf(etPass.getText()); // note: CharSequence -> String
                if (password == null) password = "";
                else password = password.toString();
                String roleSel = spRole.getSelectedItem().toString();
                String email = etEmail.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();

                if (username.isEmpty()) { etUser.setError("Bắt buộc"); return; }

                // Use readable DB to check existence
                SQLiteDatabase rdb = db.getReadableDatabase();
                Cursor c = null;
                try {
                    if (editing == null) {
                        c = rdb.rawQuery("SELECT id FROM NguoiDung WHERE tenDangNhap=? LIMIT 1",
                                new String[]{ username });
                        boolean exists = (c != null && c.moveToFirst());
                        if (c != null) { c.close(); c = null; }
                        if (exists) {
                            etUser.setError("Tên đăng nhập đã tồn tại");
                            return;
                        }
                        boolean ok = db.registerUser(username, password.isEmpty() ? "123" : password,
                                roleSel, email, phone);
                        if (ok) {
                            Toast.makeText(this, "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show();
                            dlg.dismiss();
                            refresh();
                        } else {
                            Toast.makeText(this, "Tạo tài khoản thất bại", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        c = rdb.rawQuery("SELECT id FROM NguoiDung WHERE tenDangNhap=? AND id<>? LIMIT 1",
                                new String[]{ username, String.valueOf(editing.getId()) });
                        boolean conflict = (c != null && c.moveToFirst());
                        if (c != null) { c.close(); c = null; }
                        if (conflict) {
                            etUser.setError("Tên đăng nhập đã được dùng");
                            return;
                        }
                        SQLiteDatabase wdb = db.getWritableDatabase();
                        android.content.ContentValues cv = new android.content.ContentValues();
                        cv.put("tenDangNhap", username);
                        if (!password.isEmpty()) {
                            String hashed = HashUtils.sha256(password);
                            cv.put("matKhau", hashed);
                        }
                        cv.put("vaiTro", roleSel);
                        cv.put("email", email);
                        cv.put("sdt", phone);

                        int rows = wdb.update("NguoiDung", cv, "id=?", new String[]{ String.valueOf(editing.getId()) });
                        if (rows > 0) {
                            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                            dlg.dismiss();
                            refresh();
                        } else {
                            Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception ex) {
                    Toast.makeText(this, "Lỗi: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                } finally {
                    if (c != null) c.close();
                     }
            });
        });
        dlg.show();
        MaterialToolbar toolbar = findViewById(id.toolbarUsers);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> finish());
        toolbar.setTitle("Danh sách người dùng");
    }
}
