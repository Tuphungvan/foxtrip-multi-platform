package vn.androidhaui.foxtrip.features.user.chatbot;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.ItemLocationCardBinding;
import vn.androidhaui.foxtrip.models.dto.response.LocationResponse;

public class LocationCardAdapter extends RecyclerView.Adapter<LocationCardAdapter.VH> {

    private final List<LocationResponse> locations;
    private final OnClickListener listener;

    public interface OnClickListener { void onClick(LocationResponse l); }

    public LocationCardAdapter(List<LocationResponse> locations, OnClickListener listener) {
        this.locations = locations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLocationCardBinding binding = ItemLocationCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        LocationResponse l = locations.get(position);
        ItemLocationCardBinding binding = holder.binding;
        binding.tvLocationName.setText(l.name);
        binding.tvLocationAddress.setText(l.address);
        
        if (l.imageUrl != null && !l.imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(l.imageUrl).into(binding.ivLocationImage);
        } else {
            binding.ivLocationImage.setImageResource(R.drawable.placeholder_image);
        }

        binding.btnViewMap.setOnClickListener(v -> {
            if (l.lat != null && l.lng != null) {
                String uri = "geo:" + l.lat + "," + l.lng + "?q=" + Uri.encode(l.name);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                if (intent.resolveActivity(holder.itemView.getContext().getPackageManager()) != null) {
                    holder.itemView.getContext().startActivity(intent);
                } else {
                    // Fallback to browser
                    String browserUri = "https://www.google.com/maps/search/?api=1&query=" + l.lat + "," + l.lng;
                    holder.itemView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(browserUri)));
                }
            } else {
                Toast.makeText(holder.itemView.getContext(), "Không có tọa độ bản đồ", Toast.LENGTH_SHORT).show();
            }
        });

        holder.itemView.setOnClickListener(v -> listener.onClick(l));
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemLocationCardBinding binding;

        VH(ItemLocationCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
