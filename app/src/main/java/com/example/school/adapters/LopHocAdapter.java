package com.example.school.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school.R;
import com.example.school.model.LopHoc;

import java.util.List;

public class LopHocAdapter extends RecyclerView.Adapter<LopHocAdapter.VH> {
    private List<LopHoc> list;
    private OnItemLongClickListener<LopHoc> longClickListener;

    public LopHocAdapter(List<LopHoc> list, OnItemLongClickListener<LopHoc> longClickListener) {
        this.list = list;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lophoc, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        LopHoc lop = list.get(position);
        holder.tvTenLop.setText(lop.getTenLop());
        holder.tvKhoi.setText("Khối: " + lop.getKhoi());
        holder.tvNamHoc.setText("Năm học: " + lop.getNamHoc());
        holder.tvSiSo.setText("Sĩ số: " + lop.getSiSo());
        holder.tvGVCN.setText("GVCN ID: " + lop.getGvcn());

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onItemLongClick(lop);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTenLop, tvKhoi, tvNamHoc, tvSiSo, tvGVCN;

        VH(View v) {
            super(v);
            tvTenLop = v.findViewById(R.id.tvTenLop);
            tvKhoi = v.findViewById(R.id.tvKhoi);
            tvNamHoc = v.findViewById(R.id.tvNamHoc);
            tvSiSo = v.findViewById(R.id.tvSiSo);
            tvGVCN = v.findViewById(R.id.tvGVCN);
        }
    }
}
