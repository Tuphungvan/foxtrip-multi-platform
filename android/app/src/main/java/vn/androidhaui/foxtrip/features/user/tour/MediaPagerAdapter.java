package vn.androidhaui.foxtrip.features.user.tour;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;

import java.util.ArrayList;
import java.util.List;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.ItemTourImageBinding;

public class MediaPagerAdapter extends RecyclerView.Adapter<MediaPagerAdapter.ViewHolder> {

    private final Context context;
    private final List<String> images = new ArrayList<>();

    public MediaPagerAdapter(Context context) {
        this.context = context;
    }

    public void submitList(List<String> newList) {
        images.clear();
        if (newList != null) images.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTourImageBinding binding = ItemTourImageBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context)
                .load(images.get(position))
                .transform(new CenterCrop())
                .placeholder(R.drawable.ic_image_placeholder)
                .into(holder.binding.ivMedia);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemTourImageBinding binding;

        ViewHolder(ItemTourImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
