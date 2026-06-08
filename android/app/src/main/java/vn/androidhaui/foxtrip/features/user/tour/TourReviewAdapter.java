package vn.androidhaui.foxtrip.features.user.tour;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.ItemTourReviewBinding;
import vn.androidhaui.foxtrip.models.dto.response.TourReviewDTO;
import vn.androidhaui.foxtrip.utils.DateTimeUtils;

public class TourReviewAdapter extends RecyclerView.Adapter<TourReviewAdapter.VH> {
    private final List<TourReviewDTO> items = new ArrayList<>();

    public void setItems(List<TourReviewDTO> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTourReviewBinding binding = ItemTourReviewBinding.inflate(
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
        private final ItemTourReviewBinding b;

        VH(ItemTourReviewBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(TourReviewDTO item) {
            b.tvUsername.setText(item.user != null ? item.user.username : "Người dùng");
            
            float rating = item.rating != null ? item.rating.floatValue() : 5f;
            b.tvRatingBadge.setText(String.format("%.1f", rating));
            if (rating >= 4.0) {
                b.tvRatingLabel.setText("Tuyệt vời");
            } else {
                b.tvRatingLabel.setText("Tốt");
            }

            b.tvContent.setText(item.content);
            
            if (item.createdAt != null) {
                java.util.Date date = DateTimeUtils.parseBackendDate(item.createdAt);
                b.tvDate.setText(DateTimeUtils.toDisplayDate(date));
            }

            if (item.user != null && item.user.avatarUrl != null && !item.user.avatarUrl.isEmpty()) {
                Glide.with(b.getRoot().getContext())
                        .load(item.user.avatarUrl)
                        .placeholder(R.drawable.ic_avatar)
                        .into(b.ivAvatar);
            } else {
                b.ivAvatar.setImageResource(R.drawable.ic_avatar);
            }
        }
    }
}
