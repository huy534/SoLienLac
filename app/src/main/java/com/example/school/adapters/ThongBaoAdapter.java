package com.example.school.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.school.R;
import com.example.school.model.ThongBao;
import java.util.ArrayList;
import java.util.List;

public class ThongBaoAdapter extends RecyclerView.Adapter<ThongBaoAdapter.VH> {
    private List<ThongBao> items = new ArrayList<>();
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(ThongBao tb);
    }

    public void setOnItemClickListener(OnItemClickListener l) { this.clickListener = l; }

    public ThongBaoAdapter(List<ThongBao> data) {
        if (data != null) items = data;
    }

    public void updateData(List<ThongBao> data) {
        items = data == null ? new ArrayList<>() : data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_thongbao, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ThongBao tb = items.get(position);
        holder.tvTitle.setText(tb.getTieuDe());
        String summary = tb.getNoiDung() == null ? "" : (tb.getNoiDung().length() > 120 ? tb.getNoiDung().substring(0, 120) + "..." : tb.getNoiDung());
        holder.tvSummary.setText(summary);
        holder.tvTime.setText(tb.getThoiGian() == null ? "" : tb.getThoiGian());
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(tb);
        });
    }

    @Override
    public int getItemCount() { return items == null ? 0 : items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSummary, tvTime;
        VH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTbTitle);
            tvSummary = v.findViewById(R.id.tvTbSummary);
            tvTime = v.findViewById(R.id.tvTbTime);
        }
    }
}
