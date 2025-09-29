package com.example.school.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school.R;
import com.example.school.data.DBHelper;
import com.example.school.model.MonHoc;

import java.util.List;

public class MonHocAdapter extends RecyclerView.Adapter<MonHocAdapter.VH> {

    public interface OnItemClick {
        void onClick(MonHoc m);
    }
    public interface OnItemLongClick {
        void onLongClick(MonHoc m);
    }

    private Context ctx;
    private List<MonHoc> data;
    private DBHelper db;
    private OnItemClick clickListener;
    private OnItemLongClick longClickListener;

    public MonHocAdapter(Context ctx, List<MonHoc> data, DBHelper db,
                         OnItemClick clickListener, OnItemLongClick longClickListener) {
        this.ctx = ctx;
        this.data = data;
        this.db = db;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_monhoc, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        MonHoc m = data.get(position);
        holder.tvName.setText(m.getTenMon());
        holder.tvSoTiet.setText("Số tiết: " + m.getSoTiet());

        // show teacher name instead of id
        String teacherName = "";
        try {
            teacherName = db.getTeacherNameById(m.getTeacherId());
        } catch (Exception ex) {
            teacherName = "";
        }
        if (teacherName == null || teacherName.isEmpty()) teacherName = "Chưa phân công";
        holder.tvTeacher.setText("GV: " + teacherName);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(m);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onLongClick(m);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvSoTiet, tvTeacher;
        public VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMonName);
            tvSoTiet = itemView.findViewById(R.id.tvMonSoTiet);
            tvTeacher = itemView.findViewById(R.id.tvMonTeacher);
        }
    }
}
