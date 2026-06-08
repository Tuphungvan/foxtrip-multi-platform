package vn.androidhaui.foxtrip.features.user.tour;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import vn.androidhaui.foxtrip.databinding.ItemTourBinding;
import vn.androidhaui.foxtrip.models.dto.response.TourCardResponse;
import vn.androidhaui.foxtrip.utils.DateTimeUtils;
import vn.androidhaui.foxtrip.utils.ProvinceUtils;

public class TourCardAdapter extends ListAdapter<TourCardResponse, TourCardAdapter.VH> {

    private final Context ctx;
    private OnClickListener listener;
    private boolean isHorizontal = false;

    public interface OnClickListener { void onClick(TourCardResponse t); }
    public void setOnClickListener(OnClickListener l) { this.listener = l; }
    public void setHorizontal(boolean horizontal) { this.isHorizontal = horizontal; }

    public TourCardAdapter(Context ctx) { super(DIFF_CALLBACK); this.ctx = ctx; }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTourBinding b = ItemTourBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        if (!isHorizontal) {
            // Force width to match_parent in grid setup
            ViewGroup.LayoutParams lp = b.getRoot().getLayoutParams();
            if (lp != null) {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                b.getRoot().setLayoutParams(lp);
            }
        } else {
            // Fix width for horizontal carousel
            ViewGroup.LayoutParams lp = b.getRoot().getLayoutParams();
            if (lp != null) {
                lp.width = (int) (200 * parent.getContext().getResources().getDisplayMetrics().density);
                b.getRoot().setLayoutParams(lp);
            }
        }
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        TourCardResponse t = getItem(position);

        holder.binding.tvTourName.setText(t.name);
        holder.binding.tvTourProvince.setText("✈\uFE0F " + ProvinceUtils.getProvinceDisplay(t.province));
        
        if (t.thumbnailUrl != null && !t.thumbnailUrl.isEmpty()) {
            Glide.with(ctx).load(t.thumbnailUrl).into(holder.binding.ivTourImage);
        }

        if (t.discount != null && t.discount > 0) {
            holder.binding.tvTourPrice.setText(String.format("%,.0f VND", t.finalPrice));
            holder.binding.tvTourOldPrice.setText(String.format("%,.0f VND", t.price));
            holder.binding.tvTourOldPrice.setVisibility(View.VISIBLE);
            holder.binding.tvTourOldPrice.setPaintFlags(
                    holder.binding.tvTourOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
            );
        } else {
            holder.binding.tvTourPrice.setText(String.format("%,.0f VND", t.price != null ? t.price : 0.0));
            holder.binding.tvTourOldPrice.setVisibility(View.GONE);
        }
        
        if (t.startDate != null) {
            holder.binding.tvTourSlots.setText("Bắt đầu: " + DateTimeUtils.toDisplayDate(t.startDate));
        } else {
            holder.binding.tvTourSlots.setText("Bắt đầu: Đang cập nhật");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(t);
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemTourBinding binding;
        VH(@NonNull ItemTourBinding b) { super(b.getRoot()); binding = b; }
    }

    private static final DiffUtil.ItemCallback<TourCardResponse> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<TourCardResponse>() {
                @Override
                public boolean areItemsTheSame(@NonNull TourCardResponse oldItem, @NonNull TourCardResponse newItem) {
                    return oldItem.slug.equals(newItem.slug);
                }

                @Override
                public boolean areContentsTheSame(@NonNull TourCardResponse oldItem, @NonNull TourCardResponse newItem) {
                    return oldItem.id.equals(newItem.id);
                }
            };
}
