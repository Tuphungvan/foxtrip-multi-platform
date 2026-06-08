package vn.androidhaui.foxtrip.features.user.order;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.ItemOrderBinding;
import vn.androidhaui.foxtrip.models.dto.response.MyOrderListItemDTO;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.VH> {
    private final List<MyOrderListItemDTO> items = new ArrayList<>();
    private final OnOrderClickListener listener;
    private final OnPayClickListener payListener;

    public interface OnOrderClickListener {
        void onClick(MyOrderListItemDTO order);
    }

    public interface OnPayClickListener {
        void onPayClick(MyOrderListItemDTO order);
    }

    public OrdersAdapter(OnOrderClickListener listener, OnPayClickListener payListener) {
        this.listener = listener;
        this.payListener = payListener;
    }

    public void setItems(List<MyOrderListItemDTO> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        MyOrderListItemDTO order = items.get(position);
        holder.binding.tvCode.setText("Mã Đơn: " + (order.orderCode != null ? order.orderCode : "N/A"));
        
        String vnStatus = getVietnameseStatus(order.status);
        holder.binding.tvStatus.setText(vnStatus);

        int colorRes;
        int payBtnVisibility = View.GONE;
        if ("PENDING".equals(order.status)) {
            colorRes = R.color.warning_color;
            payBtnVisibility = View.VISIBLE;
        } else if ("PAID".equals(order.status) || "COMPLETED".equals(order.status)) {
            colorRes = R.color.success_color;
        } else {
            colorRes = R.color.danger_color;
        }
        holder.binding.tvStatus.setTextColor(
                holder.itemView.getContext().getResources().getColor(colorRes, null));
        holder.binding.btnPayNow.setVisibility(payBtnVisibility);

        holder.binding.tvTotal.setText(String.format("Tổng: %,.0f VND", order.totalAmount != null ? order.totalAmount : 0));

        if (order.tour != null) {
            holder.binding.tvTourName.setText(order.tour.tourNameAtTime);
            holder.binding.tvQuantity.setText("Số lượng: " + order.quantity);
            if (order.tour.thumbnailAtTime != null) {
                Glide.with(holder.itemView.getContext()).load(order.tour.thumbnailAtTime).into(holder.binding.ivImage);
            } else {
                holder.binding.ivImage.setImageResource(android.R.color.transparent);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(order);
        });

        holder.binding.btnPayNow.setOnClickListener(v -> {
            if (payListener != null) payListener.onPayClick(order);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    private String getVietnameseStatus(String status) {
        if (status == null) return "Không rõ";
        switch (status) {
            case "PENDING":          return "Chờ thanh toán";
            case "PAID":             return "Đã thanh toán";
            case "PROCESSING":       return "Đang xử lý";
            case "COMPLETED":        return "Hoàn thành";
            case "CANCELLED":        return "Đã hủy";
            case "CANCEL_REQUESTED": return "Đang chờ xét duyệt hủy";
            case "REFUND_PENDING":   return "Đang xử lý hoàn tiền";
            case "REFUNDED":         return "Đã hoàn tiền";
            case "EXPIRED":          return "Hết hạn thanh toán";
            case "FAILED":           return "Thất bại";
            default:                 return status;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemOrderBinding binding;
        VH(ItemOrderBinding b) { super(b.getRoot()); binding = b; }
    }
}
