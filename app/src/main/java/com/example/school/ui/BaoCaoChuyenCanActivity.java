package com.example.school.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.school.R;
import com.example.school.auth.SessionManager;
import com.example.school.data.DBHelper;
import com.example.school.data.DBHelper.SubjectAttendance;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BaoCaoChuyenCanActivity extends AppCompatActivity {

    private DBHelper db;
    private SessionManager session;
    private Spinner spinnerStudents;
    private LinearLayout containerCharts;
    private TextView tvEmpty;
    private MaterialButton btnExport;
    private List<Integer> studentIds; // parallel list to spinner items

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baocao_chuyencan); // tên file xml ở trên
        db = new DBHelper(this);
        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbarReport);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        spinnerStudents = findViewById(R.id.spinnerStudents);
        containerCharts = findViewById(R.id.containerCharts);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnExport = findViewById(R.id.btnExportPdf);

        loadStudentsForParent(); // hoặc cho teacher: getHocSinhByTeacher

        spinnerStudents.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                int studentId = studentIds.get(position);
                showChartsForStudent(studentId);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnExport.setOnClickListener(v -> exportPdfForAllCharts());
    }

    private void loadStudentsForParent() {
        // nếu role là phuhuynh: getStudentsByParent
        String role = session.getUserRole() == null ? "" : session.getUserRole().toLowerCase();
        List<com.example.school.model.HocSinh> list;
        if (role.contains("phuhuynh")) {
            list = db.getStudentsByParent(session.getUserId());
        } else if (role.contains("giaovien")) {
            list = db.getHocSinhByTeacher(session.getUserId());
        } else {
            list = db.getAllHocSinh();
        }

        ArrayList<String> names = new ArrayList<>();
        studentIds = new ArrayList<>();
        for (com.example.school.model.HocSinh hs : list) {
            names.add(hs.getHoTen());
            studentIds.add(hs.getId());
        }
        if (names.isEmpty()) {
            names.add("Không có học sinh");
            spinnerStudents.setEnabled(false);
            btnExport.setEnabled(false);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStudents.setAdapter(adapter);
    }

    private void showChartsForStudent(int studentId) {
        containerCharts.removeAllViews();
        List<SubjectAttendance> stats = db.getAttendanceStatsByStudent(studentId);
        if (stats == null || stats.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }
        tvEmpty.setVisibility(View.GONE);

        for (SubjectAttendance sa : stats) {
            // create a PieChart programmatically
            PieChart chart = new PieChart(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 600);
            lp.setMargins(8,12,8,12);
            chart.setLayoutParams(lp);

            // prepare entries
            List<PieEntry> entries = new ArrayList<>();
            if (sa.countPresent > 0) entries.add(new PieEntry(sa.countPresent, "Có mặt"));
            if (sa.countPermitLeave > 0) entries.add(new PieEntry(sa.countPermitLeave, "Vắng phép"));
            if (sa.countAbsent > 0) entries.add(new PieEntry(sa.countAbsent, "Vắng không phép"));
            if (sa.countLate > 0) entries.add(new PieEntry(sa.countLate, "Muộn"));

            PieDataSet set = new PieDataSet(entries, sa.tenMon);
            // let MPAndroidChart pick default colors
            set.setSliceSpace(2f);
            set.setValueTextSize(12f);

            PieData data = new PieData(set);
            chart.setData(data);

            Description desc = new Description();
            desc.setText(String.format(Locale.getDefault(), "%s (tổng %d buổi)", sa.tenMon, sa.total()));
            chart.setDescription(desc);
            chart.setCenterText(sa.tenMon);
            chart.invalidate();

            containerCharts.addView(chart);
        }
    }

    private void exportPdfForAllCharts() {
        // nếu không có chart -> warn
        if (containerCharts.getChildCount() == 0) {
            Toast.makeText(this, "Không có biểu đồ để xuất", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdf = new PdfDocument();
        int pageIndex = 0;
        try {
            for (int i = 0; i < containerCharts.getChildCount(); i++) {
                View v = containerCharts.getChildAt(i);
                // get bitmap of view (chart)
                Bitmap bmp = getBitmapFromView(v);
                if (bmp == null) continue;

                // create page with same size or scaled to A4-like
                int pageWidth = 595;  // A4 pts ~ 595x842 at 72dpi
                int pageHeight = 842;
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex+1).create();
                PdfDocument.Page page = pdf.startPage(pageInfo);
                Canvas canvas = page.getCanvas();

                // scale bitmap to fit page width
                float scale = (float) pageWidth / bmp.getWidth();
                int scaledH = (int) (bmp.getHeight() * scale);
                Bitmap scaled = Bitmap.createScaledBitmap(bmp, pageWidth, scaledH, true);
                int top = Math.max(0, (pageHeight - scaledH) / 2);
                canvas.drawBitmap(scaled, 0, top, null);

                pdf.finishPage(page);
                pageIndex++;
            }

            // save to app external files dir (no storage permission required)
            File docsDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (docsDir != null && !docsDir.exists()) docsDir.mkdirs();
            String fname = "BaoCao_ChuyenCan_" + System.currentTimeMillis() + ".pdf";
            File out = new File(docsDir, fname);
            FileOutputStream fos = new FileOutputStream(out);
            pdf.writeTo(fos);
            fos.close();
            Toast.makeText(this, "Đã lưu PDF: " + out.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(this, "Lỗi xuất PDF: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            pdf.close();
        }
    }

    private Bitmap getBitmapFromView(View v) {
        // nếu là PieChart, dùng getChartBitmap (phiên bản MPChart có method)
        if (v instanceof com.github.mikephil.charting.charts.Chart) {
            try {
                com.github.mikephil.charting.charts.Chart chart = (com.github.mikephil.charting.charts.Chart) v;
                Bitmap b = chart.getChartBitmap();
                return b;
            } catch (Exception ignored) {}
        }
        // fallback: draw view to bitmap
        int w = v.getWidth();
        int h = v.getHeight();
        if (w <= 0 || h <= 0) {
            // measure
            v.measure(View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY));
            w = v.getMeasuredWidth();
            h = v.getMeasuredHeight();
            v.layout(0, 0, w, h);
        }
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        v.draw(c);
        return bmp;
    }
}
