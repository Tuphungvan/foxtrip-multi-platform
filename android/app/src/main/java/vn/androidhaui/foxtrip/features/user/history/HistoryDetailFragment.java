package vn.androidhaui.foxtrip.features.user.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import vn.androidhaui.foxtrip.databinding.FragmentHistoryDetailBinding;
import vn.androidhaui.foxtrip.models.domain.History;
import vn.androidhaui.foxtrip.utils.DateTimeUtils;

public class HistoryDetailFragment extends Fragment {
    private static final String ARG_HISTORY_ID = "history_id";
    private FragmentHistoryDetailBinding binding;
    private HistoryItemsAdapter itemsAdapter;

    public static HistoryDetailFragment newInstance(String historyId) {
        HistoryDetailFragment f = new HistoryDetailFragment();
        Bundle b = new Bundle();
        b.putString(ARG_HISTORY_ID, historyId);
        f.setArguments(b);
        return f;
    }

    public HistoryDetailFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        itemsAdapter = new HistoryItemsAdapter();
        binding.rvItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvItems.setAdapter(itemsAdapter);

        String historyId = getArguments() != null ? getArguments().getString(ARG_HISTORY_ID) : null;
        if (historyId == null) {
            Toast.makeText(requireContext(), "Không tìm thấy lịch sử", Toast.LENGTH_SHORT).show();
        }
    }

    private void bindHistory(History h) {
        if (h == null) return;

        // Mã lịch sử
        binding.tvHistoryId.setText(h.get_id() != null ? h.get_id() : "—");

        // Mã đơn hàng gốc
        binding.tvOrderId.setText(h.getOrderId() != null ? h.getOrderId() : "—");

        // Ngày hoàn thành
        binding.tvCompletedAt.setText(h.getCompletedAt() != null ?
                DateTimeUtils.toDisplayString(h.getCompletedAt()) : "—");

        // Ngày kết thúc tour
        binding.tvEndDate.setText(h.getEndDate() != null ?
                DateTimeUtils.toDisplayString(h.getEndDate()) : "—");

        // Thông tin khách hàng
        if (h.getCustomerInfo() != null) {
            binding.tvName.setText(h.getCustomerInfo().getUsername() != null ?
                    h.getCustomerInfo().getUsername() : "—");
            binding.tvPhone.setText(h.getCustomerInfo().getPhoneNumber() != null ?
                    h.getCustomerInfo().getPhoneNumber() : "—");
            binding.tvAddress.setText(h.getCustomerInfo().getAddress() != null ?
                    h.getCustomerInfo().getAddress() : "—");
            binding.tvEmail.setText(h.getCustomerInfo().getEmail() != null ?
                    h.getCustomerInfo().getEmail() : "—");
        } else {
            binding.tvName.setText("—");
            binding.tvPhone.setText("—");
            binding.tvAddress.setText("—");
            binding.tvEmail.setText("—");
        }

        // Danh sách tour đã trải nghiệm
        if (h.getItems() != null && !h.getItems().isEmpty()) {
            itemsAdapter.setItems(h.getItems());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}