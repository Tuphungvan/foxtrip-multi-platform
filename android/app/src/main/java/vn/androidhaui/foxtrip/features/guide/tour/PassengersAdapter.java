package vn.androidhaui.foxtrip.features.guide.tour;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.ItemPassengerBinding;
import vn.androidhaui.foxtrip.models.dto.response.PassengerResDTO;

public class PassengersAdapter extends RecyclerView.Adapter<PassengersAdapter.ViewHolder> {

    private final List<PassengerResDTO> items = new ArrayList<>();

    public void reloadData(List<PassengerResDTO> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPassengerBinding binding = ItemPassengerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPassengerBinding binding;

        public ViewHolder(ItemPassengerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(PassengerResDTO item) {
            binding.txtName.setText(item.customerName);
            binding.txtQuantity.setText("Số lượng: " + item.quantity);
            binding.txtOrderCode.setText("Mã đơn: " + item.orderCode);
            
            if (item.checkedIn) {
                binding.txtStatus.setText("Đã Check-in");
                binding.txtStatus.setTextColor(itemView.getContext().getColor(R.color.appMainColor));
            } else {
                binding.txtStatus.setText("Chưa Check-in");
                binding.txtStatus.setTextColor(itemView.getContext().getColor(R.color.danger_color));
            }
        }
    }
}
