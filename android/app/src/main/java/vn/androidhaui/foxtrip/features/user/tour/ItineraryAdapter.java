package vn.androidhaui.foxtrip.features.user.tour;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import vn.androidhaui.foxtrip.databinding.ItemItineraryBinding;
import vn.androidhaui.foxtrip.models.dto.response.ItineraryItemResDTO;

public class ItineraryAdapter extends ListAdapter<ItineraryAdapter.DisplayItem, ItineraryAdapter.VH> {

    private final Context ctx;
    private OnLocationClickListener locationClickListener;

    public interface OnLocationClickListener {
        void onLocationClick(String locationId);
    }

    public void setOnLocationClickListener(OnLocationClickListener listener) {
        this.locationClickListener = listener;
    }

    public static class DisplayItem {
        public boolean isHeader;
        public int dayNumber;
        public ItineraryItemResDTO data;

        public static DisplayItem header(int day) {
            DisplayItem item = new DisplayItem();
            item.isHeader = true;
            item.dayNumber = day;
            return item;
        }

        public static DisplayItem activity(ItineraryItemResDTO data) {
            DisplayItem item = new DisplayItem();
            item.isHeader = false;
            item.data = data;
            return item;
        }
    }

    public ItineraryAdapter(Context ctx) {
        super(DIFF_CALLBACK);
        this.ctx = ctx;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isHeader ? 0 : 1;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemItineraryBinding b = ItemItineraryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        DisplayItem item = getItem(position);

        if (item.isHeader) {
            holder.binding.tvDayHeader.setVisibility(View.VISIBLE);
            holder.binding.layoutActivity.setVisibility(View.GONE);
            holder.binding.tvDayHeader.setText("Ngày " + item.dayNumber + ":");
        } else {
            holder.binding.tvDayHeader.setVisibility(View.GONE);
            holder.binding.layoutActivity.setVisibility(View.VISIBLE);
            
            holder.binding.tvActivityContent.setText(item.data.activity);
            
            if (item.data.locationId != null) {
                holder.binding.btnLocationDetail.setVisibility(View.VISIBLE);
                holder.binding.btnLocationDetail.setOnClickListener(v -> {
                    // Open Google Maps if coordinates are available
                    if (item.data.lat != null && item.data.lng != null) {
                        String uri = "geo:" + item.data.lat + "," + item.data.lng + "?q=" + Uri.encode(item.data.locationName);
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        intent.setPackage("com.google.android.apps.maps");
                        if (intent.resolveActivity(ctx.getPackageManager()) != null) {
                            ctx.startActivity(intent);
                        } else {
                            String browserUri = "https://www.google.com/maps/search/?api=1&query=" + item.data.lat + "," + item.data.lng;
                            ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(browserUri)));
                        }
                    } else if (locationClickListener != null) {
                        locationClickListener.onLocationClick(item.data.locationId);
                    } else {
                        Toast.makeText(ctx, "Địa điểm: " + item.data.locationName, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                holder.binding.btnLocationDetail.setVisibility(View.GONE);
            }
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemItineraryBinding binding;
        VH(@NonNull ItemItineraryBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }

    private static final DiffUtil.ItemCallback<DisplayItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<DisplayItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull DisplayItem oldItem, @NonNull DisplayItem newItem) {
                    if (oldItem.isHeader && newItem.isHeader) return oldItem.dayNumber == newItem.dayNumber;
                    if (!oldItem.isHeader && !newItem.isHeader) return oldItem.data.id.equals(newItem.data.id);
                    return false;
                }

                @Override
                public boolean areContentsTheSame(@NonNull DisplayItem oldItem, @NonNull DisplayItem newItem) {
                    return true;
                }
            };
}
