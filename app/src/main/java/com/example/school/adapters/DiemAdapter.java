package com.example.school.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school.R;
import com.example.school.model.Diem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class DiemAdapter extends RecyclerView.Adapter<DiemAdapter.VH> {
    private List<Diem> items;           
    private final List<Diem> originals; 

    private OnItemClickListener<Diem> clickListener;
    private OnItemLongClickListener<Diem> longClickListener;

    public DiemAdapter(List<Diem> list) {
        this.items = list == null ? new ArrayList<>() : list;
        this.originals = new ArrayList<>(this.items);
    }

    // Interfaces để Activity set listener
    public interface OnItemClickListener<T> {
        void onItemClick(T item);
    }
    public interface OnItemLongClickListener<T> {
        void onItemLongClick(T item);
    }

    public void setOnItemClickListener(OnItemClickListener<Diem> l) { this.clickListener = l; }
    public void setOnItemLongClickListener(OnItemLongClickListener<Diem> l) { this.longClickListener = l; }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diem, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Diem d = items.get(position);

        // Ten hoc sinh (fallback to HS id)
        String tenHS = d.getTenHocSinh();
        if (tenHS == null || tenHS.trim().isEmpty()) {
            tenHS = "HS ID: " + d.getHocSinhId();
        }
        holder.tvTenHocSinh.setText(tenHS);
        String tenLop = d.getTenLop();
        if (tenLop == null || tenLop.trim().isEmpty()) {
            tenLop = "(Chưa có lớp)";
        }
        holder.tvTenLop.setText("Lớp: " + tenLop);

        String tenMon = d.getTenMon();
        if (tenMon == null || tenMon.trim().isEmpty()) {
            tenMon = "Môn ID: " + d.getMonId();
        }
        holder.tvTenMon.setText("Môn: " + tenMon);
        float tb = d.getDiemTB();
        String tbText = String.format(Locale.getDefault(), "TB: %.2f", tb);
        holder.tvDiemTB.setText(tbText);

        
        String note = d.getNhanXet();
        if (note == null || note.trim().isEmpty()) {
            holder.tvNote.setVisibility(View.GONE);
        } else {
            holder.tvNote.setVisibility(View.VISIBLE);
            holder.tvNote.setText("Nhận xét: " + note);
        }
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(d);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onItemLongClick(d);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    // Thay dữ liệu mới (ví dụ sau refresh)
    public void updateData(List<Diem> newList) {
        this.items = newList == null ? new ArrayList<>() : newList;
        this.originals.clear();
        this.originals.addAll(this.items);
        notifyDataSetChanged();
    }

    /**
     * Filter theo tên học sinh, tên môn hoặc mã học sinh (id)
     * @param query chuỗi tìm kiếm
     */
    public void filter(String query) {
        if (query == null) query = "";
        String q = query.trim().toLowerCase(Locale.getDefault());
        items = new ArrayList<>();
        if (q.isEmpty()) {
            items.addAll(originals);
        } else {
            for (Diem d : originals) {
                String tenHS = d.getTenHocSinh() == null ? "" : d.getTenHocSinh().toLowerCase(Locale.getDefault());
                String tenMon = d.getTenMon() == null ? "" : d.getTenMon().toLowerCase(Locale.getDefault());
                String idHS = String.valueOf(d.getHocSinhId());
                String idMon = String.valueOf(d.getMonId());
                if (tenHS.contains(q) || tenMon.contains(q) || idHS.contains(q) || idMon.contains(q)) {
                    items.add(d);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTenHocSinh, tvTenLop, tvTenMon, tvDiemTB, tvNote;
        VH(View v) {
            super(v);
            tvTenHocSinh = v.findViewById(R.id.tvTenHocSinh);
            tvTenLop = v.findViewById(R.id.tvTenLop);
            tvTenMon = v.findViewById(R.id.tvTenMon);
            tvDiemTB = v.findViewById(R.id.tvDiemTB);
            tvNote = v.findViewById(R.id.tvNote);
        }
    }
}
