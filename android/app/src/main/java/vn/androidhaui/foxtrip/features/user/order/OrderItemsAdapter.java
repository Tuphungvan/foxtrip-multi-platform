package vn.androidhaui.foxtrip.features.user.order;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.ItemOrderItemBinding;
import vn.androidhaui.foxtrip.models.domain.OrderItem;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.VH> {
    private final List<OrderItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(@NonNull OrderItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<OrderItem> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderItemBinding binding = ItemOrderItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        OrderItem item = items.get(position);
        holder.bind(item);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && item.getSlug() != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        private final ItemOrderItemBinding b;

        VH(ItemOrderItemBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(OrderItem it) {
            b.tvName.setText(it.getName() != null ? it.getName() : "—");
            b.tvQty.setText(String.valueOf(it.getQuantity()));
            b.tvPrice.setText(String.format("%,.0f đ", it.getFinalPrice()));

            if (it.getImage() != null && !it.getImage().isEmpty()) {
                b.ivThumb.setVisibility(android.view.View.VISIBLE);
                Glide.with(b.getRoot().getContext())
                        .load(it.getImage())
                        .placeholder(R.drawable.ic_image_placeholder)
                        .into(b.ivThumb);
            } else {
                b.ivThumb.setVisibility(android.view.View.GONE);
            }
        }
    }
}