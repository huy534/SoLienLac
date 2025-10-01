package com.example.school.ui;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.school.R;
import com.example.school.auth.SessionManager;
import com.example.school.data.DBHelper;
import com.example.school.model.TwoColumn;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class BaoCaoHocTapActivity extends AppCompatActivity {
    private DBHelper db;
    private SessionManager session;
    private Spinner spStudent;
    private BarChart barChart;
    private Button btnExport;
    private List<Integer> studentIds = new ArrayList<>();
    private List<String> studentNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baocao_hoc_tap);

        db = new DBHelper(this);
        session = new SessionManager(this);

        spStudent = findViewById(R.id.spStudent2);
        barChart = findViewById(R.id.barChart);
        btnExport = findViewById(R.id.btnExportPdf2);
        Toolbar toolbar = findViewById(R.id.toolbarReport2);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) { getSupportActionBar().setDisplayHomeAsUpEnabled(true); getSupportActionBar().setTitle("Báo cáo học tập"); }
        toolbar.setNavigationOnClickListener(v -> finish());

        // load students similar như activity trước
        List<com.example.school.model.HocSinh> childs = db.getStudentsByParent(session.getUserId());
        if (childs.isEmpty()) {
            // admin/gv -> list all
            for (com.example.school.model.HocSinh hs : db.getAllHocSinh()) {
                studentIds.add(hs.getId()); studentNames.add(hs.getHoTen());
            }
        } else {
            for (com.example.school.model.HocSinh hs : childs) { studentIds.add(hs.getId()); studentNames.add(hs.getHoTen()); }
        }
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, studentNames);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStudent.setAdapter(spAdapter);

        if (!studentIds.isEmpty()) loadForStudent(studentIds.get(0));

        spStudent.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                loadForStudent(studentIds.get(position));
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnExport.setOnClickListener(v -> exportPdfBarChart());
    }

    private void loadForStudent(int studentId) {
        List<TwoColumn> data = db.getAverageScoresByStudent(studentId);
        List<BarEntry> entries = new ArrayList<>();
        final List<String> labels = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            entries.add(new BarEntry(i, data.get(i).getValue()));
            labels.add(data.get(i).getLabel());
        }
        BarDataSet ds = new BarDataSet(entries, "Điểm TB");
        BarData bd = new BarData(ds);
        barChart.setData(bd);
        XAxis x = barChart.getXAxis();
        x.setGranularity(1f);
        x.setGranularityEnabled(true);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setValueFormatter(new IndexAxisValueFormatter(labels)); // <-- bản đồ index -> label

        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.invalidate();
        barChart.invalidate();
    }

    private void exportPdfBarChart() {
        Bitmap bmp = barChart.getChartBitmap();
        if (bmp == null) { Toast.makeText(this, "Không có biểu đồ", Toast.LENGTH_SHORT).show(); return; }
        PdfDocument pdf = new PdfDocument();
        try {
            PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(bmp.getWidth(), bmp.getHeight(), 1).create();
            PdfDocument.Page page = pdf.startPage(info);
            page.getCanvas().drawBitmap(bmp, 0f, 0f, null);
            pdf.finishPage(page);
            File f = new File(getExternalFilesDir(null), "BaoCao_HocTap.pdf");
            FileOutputStream out = new FileOutputStream(f);
            pdf.writeTo(out);
            out.close();
            Toast.makeText(this, "Đã xuất PDF: " + f.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(this, "Lỗi xuất PDF: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        } finally { pdf.close(); }
    }
}
