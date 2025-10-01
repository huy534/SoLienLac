package com.example.school.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school.R;
import com.example.school.model.AttendanceReportItem;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;

public class AttendanceReportAdapter extends RecyclerView.Adapter<AttendanceReportAdapter.VH> {
    private List<AttendanceReportItem> list = new ArrayList<>();

    public AttendanceReportAdapter(List<AttendanceReportItem> l) {
        if (l != null) list.addAll(l);
    }

    public void updateData(List<AttendanceReportItem> l) {
        list.clear();
        if (l != null) list.addAll(l);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_report, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        AttendanceReportItem it = list.get(position);
        holder.tvMon.setText(it.getTenMon() == null ? "Môn " + it.getMonId() : it.getTenMon());

        List<PieEntry> entries = new ArrayList<>();
        if (it.getPresent() > 0) entries.add(new PieEntry(it.getPresent(), "Có mặt"));
        if (it.getExcused() > 0) entries.add(new PieEntry(it.getExcused(), "Vắng có phép"));
        if (it.getUnexcused() > 0) entries.add(new PieEntry(it.getUnexcused(), "Vắng không phép"));
        if (it.getLate() > 0) entries.add(new PieEntry(it.getLate(), "Muộn"));

        if (entries.isEmpty()) {
            // hiển thị 1 entry 'Không có dữ liệu'
            entries.add(new PieEntry(1f, "Không có dữ liệu"));
        }

        PieDataSet ds = new PieDataSet(entries, "");
        ds.setSliceSpace(2f);
        PieData pd = new PieData(ds);
        pd.setDrawValues(true);

        PieChart chart = holder.pieChart;
        chart.getDescription().setEnabled(false);
        chart.setDrawEntryLabels(false);
        chart.setUsePercentValues(true);
        chart.setData(pd);
        chart.invalidate();
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvMon;
        PieChart pieChart;
        VH(View v) {
            super(v);
            tvMon = v.findViewById(R.id.tvMon);
            pieChart = v.findViewById(R.id.pieChart);
        }
    }
}
