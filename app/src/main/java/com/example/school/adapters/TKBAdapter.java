package com.example.school.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.school.R;
import com.example.school.model.TKB;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TKBAdapter extends RecyclerView.Adapter<TKBAdapter.VH> {
    private List<TKB> items;
    private List<TKB> originals;
    private OnItemLongClickListener<TKB> longClick;

    public interface OnItemLongClickListener<T> { void onItemLongClick(T item); }

    public void setOnItemLongClickListener(OnItemLongClickListener<TKB> l) { this.longClick = l; }

    public TKBAdapter(List<TKB> list) {
        this.items = list == null ? new ArrayList<>() : new ArrayList<>(list);
        this.originals = new ArrayList<>(this.items);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tkb, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        TKB t = items.get(position);
        holder.tvMon.setText(t.getTenMon() == null ? "Môn ID: " + t.getMaMon() : t.getTenMon());
        holder.tvLop.setText(t.getTenLop() == null ? "Lớp ID: " + t.getMaLop() : "Lớp: " + t.getTenLop());
        holder.tvThuTiet.setText("Thứ " + t.getThu() + " - Tiết " + t.getTiet());
        holder.tvGv.setText(t.getTenGv() == null ? "" : "GV: " + t.getTenGv());

        holder.itemView.setOnLongClickListener(v -> {
            if (longClick != null) longClick.onItemLongClick(t);
            return true;
        });
    }

    @Override public int getItemCount() { return items.size(); }

    public void filter(String q) {
        if (q == null) q = "";
        q = q.trim().toLowerCase(Locale.getDefault());
        items.clear();
        if (q.isEmpty()) items.addAll(originals);
        else {
            for (TKB t : originals) {
                String mon = (t.getTenMon() == null ? "" : t.getTenMon().toLowerCase());
                String lop = (t.getTenLop() == null ? "" : t.getTenLop().toLowerCase());
                if (mon.contains(q) || lop.contains(q) || String.valueOf(t.getMaLop()).contains(q) || String.valueOf(t.getMaMon()).contains(q)) {
                    items.add(t);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvMon, tvLop, tvThuTiet, tvGv;
        VH(View v) {
            super(v);
            tvMon = v.findViewById(R.id.tvMon);
            tvLop = v.findViewById(R.id.tvLop);
            tvThuTiet = v.findViewById(R.id.tvThuTiet);
            tvGv = v.findViewById(R.id.tvGv);
        }
    }
}
