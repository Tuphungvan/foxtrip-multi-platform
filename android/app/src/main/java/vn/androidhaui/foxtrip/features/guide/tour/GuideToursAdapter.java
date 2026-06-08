package vn.androidhaui.foxtrip.features.guide.tour;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.ItemGuideTourBinding;
import vn.androidhaui.foxtrip.models.dto.response.TourListResDTO;
import vn.androidhaui.foxtrip.utils.DateTimeUtils;

public class GuideToursAdapter extends RecyclerView.Adapter<GuideToursAdapter.ViewHolder> {

    private final List<TourListResDTO> items = new ArrayList<>();
    private final Listener listener;

    public interface Listener {
        void onClick(TourListResDTO tour);
    }

    public GuideToursAdapter(Listener listener) {
        this.listener = listener;
    }

    public void reloadData(List<TourListResDTO> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGuideTourBinding binding = ItemGuideTourBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TourListResDTO item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemGuideTourBinding binding;

        public ViewHolder(ItemGuideTourBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(TourListResDTO item) {
            binding.txtTourName.setText(item.name);
            binding.txtFinalPrice.setText(String.format("%,.0f VND", item.finalPrice));
            binding.txtSlots.setText(String.format("Chỗ: %d", item.availableSlots));
            
            // Relative date logic
            Date startDate = DateTimeUtils.parseBackendDate(item.startDate);
            if (startDate != null) {
                binding.txtStartDate.setText(DateTimeUtils.toRelativeDateString(startDate));
            } else {
                binding.txtStartDate.setText(item.startDate);
            }

            Glide.with(itemView.getContext())
                    .load(item.thumbnailUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(binding.imgTour);

            itemView.setOnClickListener(v -> listener.onClick(item));
        }
    }
}
