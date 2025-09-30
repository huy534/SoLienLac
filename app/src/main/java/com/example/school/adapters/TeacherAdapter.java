package com.example.school.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school.R;
import com.example.school.model.NguoiDung;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.VH> {
    private List<NguoiDung> items;
    private List<NguoiDung> originals;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener { void onItemClick(NguoiDung user); }

    public TeacherAdapter(List<NguoiDung> list) {
        this.items = list == null ? new ArrayList<>() : new ArrayList<>(list);
        this.originals = new ArrayList<>(this.items);
    }

    public void setOnItemClickListener(OnItemClickListener l) { this.onItemClickListener = l; }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        NguoiDung u = items.get(position);
        holder.tv1.setText(u.getTenDangNhap());
        holder.tv2.setText(u.getEmail() == null ? "" : u.getEmail());
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) onItemClickListener.onItemClick(u);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    public void filter(String q) {
        if (q == null) q = "";
        q = q.trim().toLowerCase(Locale.getDefault());
        items.clear();
        if (q.isEmpty()) items.addAll(originals);
        else {
            for (NguoiDung u : originals) {
                if (u.getTenDangNhap().toLowerCase(Locale.getDefault()).contains(q) ||
                        (u.getEmail() != null && u.getEmail().toLowerCase(Locale.getDefault()).contains(q))) {
                    items.add(u);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv1, tv2;
        VH(View v) {
            super(v);
            tv1 = v.findViewById(android.R.id.text1);
            tv2 = v.findViewById(android.R.id.text2);
        }
    }
}
