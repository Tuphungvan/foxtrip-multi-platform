package vn.androidhaui.foxtrip.features.user.checkout;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.androidhaui.foxtrip.databinding.ItemAddonPickerBinding;
import vn.androidhaui.foxtrip.models.dto.response.TourAddonItem;

public class AddonPickerAdapter extends RecyclerView.Adapter<AddonPickerAdapter.VH> {

    private List<TourAddonItem> addons = new ArrayList<>();
    private final Map<String, Integer> selectedQuantities = new HashMap<>();
    private OnSelectionChangedListener listener;
    private int maxQuantity = 1;

    public static class SelectedAddon {
        public TourAddonItem addon;
        public int quantity;
        public SelectedAddon(TourAddonItem addon, int quantity) {
            this.addon = addon;
            this.quantity = quantity;
        }
    }

    public interface OnSelectionChangedListener {
        void onChanged(List<SelectedAddon> selectedAddons);
    }

    public void setAddons(List<TourAddonItem> list, int maxQty) {
        this.addons = list != null ? list : new ArrayList<>();
        this.maxQuantity = maxQty;
        selectedQuantities.clear();
        notifyDataSetChanged();
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener l) {
        this.listener = l;
    }

    public List<SelectedAddon> getSelectedAddons() {
        List<SelectedAddon> result = new ArrayList<>();
        for (TourAddonItem a : addons) {
            if (selectedQuantities.containsKey(a.id)) {
                result.add(new SelectedAddon(a, selectedQuantities.get(a.id)));
            }
        }
        return result;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAddonPickerBinding binding = ItemAddonPickerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        TourAddonItem addon = addons.get(position);
        ItemAddonPickerBinding binding = holder.binding;

        binding.tvAddonName.setText(addon.name != null ? addon.name : "");
        binding.tvAddonPrice.setText(String.format("+%,.0f VND", addon.price != null ? addon.price : 0));
        
        if (addon.description != null && !addon.description.isEmpty()) {
            binding.tvAddonDesc.setText(addon.description);
            binding.tvShowDetail.setVisibility(View.VISIBLE);
        } else {
            binding.tvShowDetail.setVisibility(View.GONE);
            binding.tvAddonDesc.setVisibility(View.GONE);
        }

        binding.tvShowDetail.setOnClickListener(v -> {
            if (binding.tvAddonDesc.getVisibility() == View.VISIBLE) {
                binding.tvAddonDesc.setVisibility(View.GONE);
                binding.tvShowDetail.setText("Xem chi tiết");
            } else {
                binding.tvAddonDesc.setVisibility(View.VISIBLE);
                binding.tvShowDetail.setText("Ẩn chi tiết");
            }
        });

        boolean isSelected = selectedQuantities.containsKey(addon.id);
        
        // Update selection UI (Giống hệt Cart nhưng màu Cam)
        if (isSelected) {
            binding.cardAddon.setStrokeColor(Color.parseColor("#FF9800"));
            binding.layoutQty.setVisibility(View.VISIBLE);
            int qty = selectedQuantities.get(addon.id);
            binding.tvQty.setText(String.valueOf(qty));
        } else {
            binding.cardAddon.setStrokeColor(Color.parseColor("#F1F5F9"));
            binding.layoutQty.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (isSelected) {
                selectedQuantities.remove(addon.id);
            } else {
                selectedQuantities.put(addon.id, 1);
            }
            notifyDataSetChanged();
            notifyListener();
        });

        binding.btnPlus.setOnClickListener(v -> {
            int current = selectedQuantities.getOrDefault(addon.id, 1);
            if (current < maxQuantity) {
                selectedQuantities.put(addon.id, current + 1);
                notifyItemChanged(position);
                notifyListener();
            }
        });

        binding.btnMinus.setOnClickListener(v -> {
            int current = selectedQuantities.getOrDefault(addon.id, 1);
            if (current > 1) {
                selectedQuantities.put(addon.id, current - 1);
                notifyItemChanged(position);
                notifyListener();
            }
        });
    }

    @Override
    public int getItemCount() { return addons.size(); }

    private void notifyListener() {
        if (listener != null) listener.onChanged(getSelectedAddons());
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemAddonPickerBinding binding;

        VH(ItemAddonPickerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
