package vn.androidhaui.foxtrip.features.user.cart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.ItemCartBinding;
import vn.androidhaui.foxtrip.models.dto.response.CartItemDTO;

public class CartAdapter extends ListAdapter<CartItemDTO, CartAdapter.VH> {

    public static final int MODE_EDITABLE = 0;
    public static final int MODE_READONLY  = 1;

    private int mode = MODE_EDITABLE;
    private final Context ctx;
    private CartActionListener listener;
    
    private String selectedTourId = null;

    public interface CartActionListener {
        void onIncrease(@NonNull CartItemDTO item);
        void onDecrease(@NonNull CartItemDTO item);
        void onRemove(@NonNull CartItemDTO item);
        void onItemClick(@NonNull CartItemDTO item);
        void onItemSelected(@NonNull CartItemDTO item);
    }

    public CartAdapter(Context ctx) {
        super(DIFF_CALLBACK);
        this.ctx = ctx;
    }

    public void setCartActionListener(CartActionListener l) { this.listener = l; }

    public void setMode(int mode) {
        this.mode = mode;
        notifyDataSetChanged();
    }
    
    public String getSelectedTourId() {
        return selectedTourId;
    }

    public void setSelectedTourId(String tourId) {
        this.selectedTourId = tourId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding b = ItemCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CartItemDTO item = getItem(position);
        CartItemDTO.TourInfo tour = item.tour;

        // Tên tour
        holder.binding.tvName.setText(tour != null && tour.name != null ? tour.name : "");

        // Giá sau giảm
        double finalPrice = tour != null ? tour.getFinalPrice() : 0;
        holder.binding.tvFinalPrice.setText(String.format("%,.0f VND", finalPrice));

        // Số lượng
        if (mode == MODE_READONLY) {
            holder.binding.tvQuantity.setText("Số lượng: " + item.getSafeQuantity());
        } else {
            holder.binding.tvQuantity.setText(String.valueOf(item.getSafeQuantity()));
        }

        // Thumbnail
        String img = tour != null ? tour.thumbnailUrl : null;
        if (img != null && !img.isEmpty()) {
            Glide.with(ctx).load(img).into(holder.binding.ivImage);
        } else {
            holder.binding.ivImage.setImageResource(android.R.color.transparent);
        }

        // Ẩn/Hiện nút theo mode
        int ctrl = mode == MODE_READONLY ? View.GONE : View.VISIBLE;
        holder.binding.btnIncrease.setVisibility(ctrl);
        holder.binding.btnDecrease.setVisibility(ctrl);
        holder.binding.btnRemove.setVisibility(ctrl);
        
        // Cập nhật trạng thái hiển thị Stroke cho Card
        boolean isSelected = item.tourId != null && item.tourId.equals(selectedTourId);
        if (isSelected && mode == MODE_EDITABLE) {
            holder.binding.getRoot().setStrokeColor(ctx.getColor(R.color.appMainColor));
            holder.binding.getRoot().setStrokeWidth(4); // 2dp
        } else {
            holder.binding.getRoot().setStrokeColor(ctx.getColor(R.color.stoke_color));
            holder.binding.getRoot().setStrokeWidth(2); // 1dp
        }

        holder.binding.btnIncrease.setOnClickListener(v -> {
            if (listener != null && mode == MODE_EDITABLE) listener.onIncrease(item);
        });
        holder.binding.btnDecrease.setOnClickListener(v -> {
            if (listener != null && mode == MODE_EDITABLE) listener.onDecrease(item);
        });
        holder.binding.btnRemove.setOnClickListener(v -> {
            if (listener != null && mode == MODE_EDITABLE) listener.onRemove(item);
        });

        // Click card để chọn thanh toán (Web style)
        holder.itemView.setOnClickListener(v -> {
            if (mode == MODE_EDITABLE) {
                if (item.tourId != null && item.tourId.equals(selectedTourId)) {
                    selectedTourId = null; // Unselect if click again
                } else {
                    selectedTourId = item.tourId;
                }
                notifyDataSetChanged();
                if (listener != null) listener.onItemSelected(item);
            } else {
                if (listener != null) listener.onItemClick(item);
            }
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemCartBinding binding;
        VH(@NonNull ItemCartBinding b) { super(b.getRoot()); binding = b; }
    }

    private static final DiffUtil.ItemCallback<CartItemDTO> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CartItemDTO>() {
                @Override
                public boolean areItemsTheSame(@NonNull CartItemDTO a, @NonNull CartItemDTO b) {
                    return a.tourId != null && a.tourId.equals(b.tourId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull CartItemDTO a, @NonNull CartItemDTO b) {
                    return a.getSafeQuantity() == b.getSafeQuantity()
                            && a.tourId != null && a.tourId.equals(b.tourId);
                }
            };
}
