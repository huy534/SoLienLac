package com.example.school.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school.R;
import com.example.school.model.HocSinh;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HocSinhAdapter extends RecyclerView.Adapter<HocSinhAdapter.VH> {
    private List<HocSinh> items;        // danh sách hiển thị (có lọc)
    private final List<HocSinh> originals;    // danh sách gốc để lọc

    private OnItemClickListener<HocSinh> clickListener;
    private OnItemLongClickListener<HocSinh> longClickListener;

    // Interfaces để Activity set listener
    public interface OnItemClickListener<T> {
        void onItemClick(T item);
    }
    public interface OnItemLongClickListener<T> {
        void onItemLongClick(T item);
    }

    // Constructor: nhận list và optional longClick listener
    public HocSinhAdapter(List<HocSinh> list, OnItemLongClickListener<HocSinh> longClickListener) {
        this.items = list == null ? new ArrayList<>() : new ArrayList<>(list);
        this.originals = new ArrayList<>(this.items);
        this.longClickListener = longClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener<HocSinh> l) {
        this.clickListener = l;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener<HocSinh> l) {
        this.longClickListener = l;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hocsinh, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        HocSinh hs = items.get(position);
        holder.tvMaHS.setText("Mã HS: " + hs.getId());
        holder.tvTenHS.setText("Tên: " + (hs.getHoTen() == null ? "" : hs.getHoTen()));
        holder.tvNgaySinh.setText("Ngày sinh: " + (hs.getNgaySinh() == null ? "" : hs.getNgaySinh()));
        holder.tvGioiTinh.setText("Giới tính: " + (hs.getGioiTinh() == null ? "" : hs.getGioiTinh()));
        holder.tvQueQuan.setText("Quê quán: " + (hs.getQueQuan() == null ? "" : hs.getQueQuan()));
        holder.tvMaLop.setText("Mã lớp: " + hs.getMaLop());

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(hs);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onItemLongClick(hs);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    // 🔎 Hàm lọc tìm kiếm (theo tên hoặc id)
    public void filter(String text) {
        if (text == null) text = "";
        String q = text.trim().toLowerCase(Locale.getDefault());
        items = new ArrayList<>();
        if (q.isEmpty()) {
            items.addAll(originals);
        } else {
            for (HocSinh hs : originals) {
                String name = hs.getHoTen() == null ? "" : hs.getHoTen().toLowerCase(Locale.getDefault());
                String idStr = String.valueOf(hs.getId());
                if (name.contains(q) || idStr.contains(q)) {
                    items.add(hs);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Cập nhật lại dữ liệu khi refresh
    public void updateData(List<HocSinh> newList) {
        this.originals.clear();
        if (newList != null) this.originals.addAll(newList);
        this.items = new ArrayList<>(this.originals);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvMaHS, tvTenHS, tvNgaySinh, tvGioiTinh, tvQueQuan, tvMaLop;

        VH(View v) {
            super(v);
            tvMaHS = v.findViewById(R.id.tvMaHS);
            tvTenHS = v.findViewById(R.id.tvTenHS);
            tvNgaySinh = v.findViewById(R.id.tvNgaySinh);
            tvGioiTinh = v.findViewById(R.id.tvGioiTinh);
            tvQueQuan = v.findViewById(R.id.tvQueQuan);
            tvMaLop = v.findViewById(R.id.tvMaLop);
        }
    }
}
