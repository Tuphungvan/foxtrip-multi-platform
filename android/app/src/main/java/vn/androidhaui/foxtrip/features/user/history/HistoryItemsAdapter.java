package vn.androidhaui.foxtrip.features.user.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.ItemHistoryItemBinding;
import vn.androidhaui.foxtrip.models.domain.History;

public class HistoryItemsAdapter extends RecyclerView.Adapter<HistoryItemsAdapter.VH> {
    private final List<History.Item> items = new ArrayList<>();

    public void setItems(List<History.Item> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryItemBinding binding = ItemHistoryItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        private final ItemHistoryItemBinding b;

        VH(ItemHistoryItemBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(History.Item it) {
            // Tên tour
            b.tvName.setText(it.getName() != null ? it.getName() : "—");

            // Số lượng
            b.tvQty.setText(String.valueOf(it.getQuantity()));

            // Tổng tiền (giá cuối * số lượng)
            double totalPrice = it.getFinalPrice() * it.getQuantity();
            b.tvPrice.setText(String.format("%,.0f đ", totalPrice));

            // Hiển thị thông tin giá gốc và giảm giá nếu có
            if (it.getDiscount() > 0) {
                // Có giảm giá
                b.llOriginalPrice.setVisibility(View.VISIBLE);
                b.llDiscount.setVisibility(View.VISIBLE);

                // Giá gốc
                double originalTotal = it.getPrice() * it.getQuantity();
                b.tvOriginalPrice.setText(String.format("%,.0f đ", originalTotal));

                // % giảm giá
                b.tvDiscount.setText(String.format("-%.0f%%", it.getDiscount()));
            } else {
                // Không có giảm giá
                b.llOriginalPrice.setVisibility(View.GONE);
                b.llDiscount.setVisibility(View.GONE);
            }

            // Load ảnh
            if (it.getImage() != null && !it.getImage().isEmpty()) {
                Glide.with(b.getRoot().getContext())
                        .load(it.getImage())
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .into(b.ivThumb);
            } else {
                b.ivThumb.setImageResource(R.drawable.ic_image_placeholder);
            }
        }
    }
}