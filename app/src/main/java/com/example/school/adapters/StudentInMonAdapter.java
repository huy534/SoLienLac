package com.example.school.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school.R;
import com.example.school.model.HocSinh;

import java.util.List;

public class StudentInMonAdapter extends RecyclerView.Adapter<StudentInMonAdapter.VH> {

    public interface OnClick { void onClick(HocSinh hs); }
    public interface OnLongClick { void onLongClick(HocSinh hs); }

    private Context ctx;
    private List<HocSinh> data;
    private OnClick click;
    private OnLongClick longClick;

    public StudentInMonAdapter(Context ctx, List<HocSinh> data, OnClick click, OnLongClick longClick) {
        this.ctx = ctx;
        this.data = data;
        this.click = click;
        this.longClick = longClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_student_in_mon, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        HocSinh hs = data.get(position);
        holder.tvId.setText(String.valueOf(hs.getId()));
        holder.tvName.setText(hs.getHoTen());
        holder.tvClass.setText("Lớp: " + hs.getMaLop());

        holder.itemView.setOnClickListener(v -> {
            if (click != null) click.onClick(hs);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClick != null) {
                longClick.onLongClick(hs);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() { return data == null ? 0 : data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvId, tvName, tvClass;
        VH(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvStudentIdInMon);
            tvName = itemView.findViewById(R.id.tvStudentNameInMon);
            tvClass = itemView.findViewById(R.id.tvStudentClassInMon);
        }
    }
}
