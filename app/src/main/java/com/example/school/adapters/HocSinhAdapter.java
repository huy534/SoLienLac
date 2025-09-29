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

public class HocSinhAdapter extends RecyclerView.Adapter<HocSinhAdapter.VH> {
    private List<HocSinh> list;        // danh sách hiển thị
    private List<HocSinh> original;    // danh sách gốc để lọc
    private OnItemClickListener<HocSinh> clickListener;
    private OnItemLongClickListener<HocSinh> longClickListener;

    public HocSinhAdapter(List<HocSinh> list, Object o) {
        this.list = new ArrayList<>(list);
        this.original = new ArrayList<>(list);
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
        HocSinh hs = list.get(position);
        holder.tvMaHS.setText("Mã HS: " + hs.getId());
        holder.tvTenHS.setText("Tên: " + hs.getHoTen());
        holder.tvNgaySinh.setText("Ngày sinh: " + hs.getNgaySinh());
        holder.tvGioiTinh.setText("Giới tính: " + hs.getGioiTinh());
        holder.tvQueQuan.setText("Quê quán: " + hs.getQueQuan());
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
        return list.size();
    }

    // 🔎 Hàm lọc tìm kiếm
    public void filter(String text) {
        list.clear();
        if (text.isEmpty()) {
            list.addAll(original);
        } else {
            text = text.toLowerCase();
            for (HocSinh hs : original) {
                if (hs.getHoTen().toLowerCase().contains(text)
                        || String.valueOf(hs.getId()).contains(text)) {
                    list.add(hs);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Cập nhật lại dữ liệu khi refresh
    public void updateData(List<HocSinh> newList) {
        this.original.clear();
        this.original.addAll(newList);
        this.list.clear();
        this.list.addAll(newList);
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
