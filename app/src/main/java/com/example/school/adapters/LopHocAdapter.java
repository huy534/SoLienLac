package com.example.school.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school.R;
import com.example.school.model.LopHoc;

import java.util.ArrayList;
import java.util.List;

public class LopHocAdapter extends RecyclerView.Adapter<LopHocAdapter.VH> {
    private List<LopHoc> list;
    private OnItemClickListener<LopHoc> clickListener;
    private OnItemLongClickListener<LopHoc> longClickListener;

    public LopHocAdapter(List<LopHoc> list) {
        this.list = list == null ? new ArrayList<>() : new ArrayList<>(list);
    }

    // Listeners
    public interface OnItemClickListener<T> {
        void onItemClick(T item);
    }
    public interface OnItemLongClickListener<T> {
        void onItemLongClick(T item);
    }

    public void setOnItemClickListener(OnItemClickListener<LopHoc> l) { this.clickListener = l; }
    public void setOnItemLongClickListener(OnItemLongClickListener<LopHoc> l) { this.longClickListener = l; }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lophoc, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        LopHoc lop = list.get(position);

        holder.tvTenLop.setText(lop.getTenLop() == null ? "" : lop.getTenLop());
        holder.tvKhoi.setText("Khối: " + (lop.getKhoi() == null ? "" : lop.getKhoi()));
        holder.tvNamHoc.setText("Năm học: " + (lop.getNamHoc() == null ? "" : lop.getNamHoc()));
        holder.tvSiSo.setText("Sĩ số: " + lop.getSiSo());
        holder.tvGVCN.setText("GVCN ID: " + lop.getGvcn());

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(lop);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onItemLongClick(lop);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    // Thay dữ liệu (sử dụng khi refresh từ Activity)
    public void updateData(List<LopHoc> newList) {
        this.list = newList == null ? new ArrayList<>() : new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    // Lấy item theo vị trí
    public LopHoc getItem(int pos) {
        return (list != null && pos >= 0 && pos < list.size()) ? list.get(pos) : null;
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
