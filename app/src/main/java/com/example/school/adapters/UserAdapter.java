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

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {
    private List<NguoiDung> list;
    private List<NguoiDung> original;
    private OnItemLongClickListener<NguoiDung> longClickListener;

    public UserAdapter(List<NguoiDung> list) {
        this.list = new ArrayList<>(list);
        this.original = new ArrayList<>(list);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener<NguoiDung> l) { this.longClickListener = l; }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        NguoiDung u = list.get(position);
        holder.tvUsername.setText(u.getTenDangNhap());
        holder.tvRole.setText("Vai trò: " + u.getVaiTro());
        holder.tvEmail.setText(u.getEmail());
        holder.tvPhone.setText(u.getSdt());

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onItemLongClick(u);
            return true;
        });
    }

    @Override public int getItemCount() { return list.size(); }

    public void filter(String q) {
        list.clear();
        if (q == null || q.trim().isEmpty()) list.addAll(original);
        else {
            String t = q.toLowerCase();
            for (NguoiDung u : original) {
                if (u.getTenDangNhap().toLowerCase().contains(t)
                        || u.getVaiTro().toLowerCase().contains(t)
                        || (u.getEmail()!=null && u.getEmail().toLowerCase().contains(t)))
                    list.add(u);
            }
        }
        notifyDataSetChanged();
    }

    public void updateData(List<NguoiDung> newList) {
        original.clear(); original.addAll(newList);
        list.clear(); list.addAll(newList);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvUsername, tvRole, tvEmail, tvPhone;
        VH(View v) {
            super(v);
            tvUsername = v.findViewById(R.id.tvUsername);
            tvRole = v.findViewById(R.id.tvRole);
            tvEmail = v.findViewById(R.id.tvEmail);
            tvPhone = v.findViewById(R.id.tvPhone);
        }
    }
}
