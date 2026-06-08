package vn.androidhaui.foxtrip.features.user.cart;

import android.content.Context;
import android.content.SharedPreferences;
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

import java.util.ArrayList;
import java.util.List;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.FragmentCartBinding;
import vn.androidhaui.foxtrip.features.user.checkout.CheckoutFragment;
import vn.androidhaui.foxtrip.features.user.tour.TourDetailFragment;
import vn.androidhaui.foxtrip.models.dto.response.CartItemDTO;
import vn.androidhaui.foxtrip.ui.MainActivity;

public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private CartViewModel viewModel;
    private CartSharedViewModel sharedViewModel;
    private CartAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CartViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(CartSharedViewModel.class);

        adapter = new CartAdapter(requireContext());
        binding.rvCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCart.setAdapter(adapter);

        adapter.setCartActionListener(new CartAdapter.CartActionListener() {
            @Override
            public void onIncrease(@NonNull CartItemDTO item) {
                int newQty = item.getSafeQuantity() + 1;
                upsertItem(item.tourId, newQty);
            }

            @Override
            public void onDecrease(@NonNull CartItemDTO item) {
                int newQty = item.getSafeQuantity() - 1;
                if (newQty <= 0) {
                    upsertItem(item.tourId, 0); // xóa
                } else {
                    upsertItem(item.tourId, newQty);
                }
            }

            @Override
            public void onRemove(@NonNull CartItemDTO item) {
                if (item.tourId != null && item.tourId.equals(adapter.getSelectedTourId())) {
                    adapter.setSelectedTourId(null);
                }
                upsertItem(item.tourId, 0);
            }

            @Override
            public void onItemSelected(@NonNull CartItemDTO item) {
                updateTotalDisplay();
            }

            @Override
            public void onItemClick(@NonNull CartItemDTO item) {
                if (item.tour != null && item.tour.slug != null) {
                    ((MainActivity) requireActivity())
                            .loadFragment(TourDetailFragment.newInstance(item.tour.slug), false);
                }
            }
        });

        binding.swipeRefresh.setOnRefreshListener(this::loadCart);

        binding.btnCheckout.setOnClickListener(v -> {
            List<CartItemDTO> current = adapter.getCurrentList();
            if (current == null || current.isEmpty()) {
                Toast.makeText(requireContext(), "Giỏ hàng đang trống", Toast.LENGTH_SHORT).show();
            } else {
                String selectedId = adapter.getSelectedTourId();
                if (selectedId == null) {
                    Toast.makeText(requireContext(), "Vui lòng chọn 1 tour để thanh toán", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                CartItemDTO selectedItem = null;
                for (CartItemDTO it : current) {
                    if (selectedId.equals(it.tourId)) {
                        selectedItem = it;
                        break;
                    }
                }
                
                if (selectedItem != null) {
                    sharedViewModel.setCheckoutItem(selectedItem);
                    ((MainActivity) requireActivity())
                            .loadFragment(new CheckoutFragment(), true);
                }
            }
        });

        loadCart();
    }

    private void updateTotalDisplay() {
        updateTotalDisplay(adapter.getCurrentList());
    }

    private void updateTotalDisplay(List<CartItemDTO> items) {
        String selectedId = adapter.getSelectedTourId();
        double total = 0;
        if (selectedId != null && items != null) {
            for (CartItemDTO it : items) {
                if (selectedId.equals(it.tourId) && it.tour != null) {
                    total = it.tour.getFinalPrice() * it.getSafeQuantity();
                    break;
                }
            }
        }
        binding.tvTotal.setText(String.format("%,.0f VND", total));
    }

    private void loadCart() {
        // Kiểm tra đăng nhập
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        if (prefs.getString("accessToken", null) == null) {
            binding.pbLoading.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);
            adapter.submitList(new ArrayList<>());
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.tvEmpty.setText("Bạn chưa đăng nhập\nVui lòng đăng nhập để xem giỏ hàng");
            binding.tvTotal.setText("0 VND");
            return;
        }

        binding.pbLoading.setVisibility(View.VISIBLE);
        viewModel.loadCart().observe(getViewLifecycleOwner(), items -> {
            binding.pbLoading.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);

            if (items == null) {
                Toast.makeText(requireContext(), "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (items.isEmpty()) {
                adapter.submitList(new ArrayList<>());
                binding.tvEmpty.setVisibility(View.VISIBLE);
                binding.tvTotal.setText("0 VND");
                return;
            }

            binding.tvEmpty.setVisibility(View.GONE);
            adapter.submitList(new ArrayList<>(items));
            sharedViewModel.setCartItems(new ArrayList<>(items));

            // Default selection like Web
            if (adapter.getSelectedTourId() == null && !items.isEmpty()) {
                adapter.setSelectedTourId(items.get(0).tourId);
            }
            updateTotalDisplay(items);
        });
    }

    private void upsertItem(String tourId, int quantity) {
        viewModel.upsertItem(tourId, quantity).observe(getViewLifecycleOwner(), items -> {
            if (items == null) {
                Toast.makeText(requireContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                return;
            }
            // Cập nhật list ngay sau khi server phản hồi
            adapter.submitList(new ArrayList<>(items));
            sharedViewModel.setCartItems(new ArrayList<>(items));

            // Default selection if current selection is lost
            if (adapter.getSelectedTourId() == null && !items.isEmpty()) {
                adapter.setSelectedTourId(items.get(0).tourId);
            }
            updateTotalDisplay(items);

            binding.tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            ((MainActivity) requireActivity()).setBottomNavigationVisibility(View.VISIBLE);
        } catch (Exception ignored) {}
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
