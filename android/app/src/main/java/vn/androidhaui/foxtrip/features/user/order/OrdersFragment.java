package vn.androidhaui.foxtrip.features.user.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import vn.androidhaui.foxtrip.databinding.FragmentOrdersBinding;
import vn.androidhaui.foxtrip.features.user.checkout.PaymentWebViewActivity;
import vn.androidhaui.foxtrip.ui.MainActivity;

public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;
    private OrdersViewModel vm;
    private OrdersAdapter adapter;

    /**
     * Launcher để bắt kết quả từ PaymentWebViewActivity.
     * Sau khi thanh toán xong (RESULT_OK), reload danh sách đơn hàng
     * để status tự động cập nhật mà không cần chuyển tab.
     */
    private final ActivityResultLauncher<Intent> paymentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                // Dù thành công hay hủy đều reload để lấy trạng thái mới nhất từ server
                vm.loadOrders(true);
            });

    public OrdersFragment() {}

    public static OrdersFragment newInstance() { return new OrdersFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(OrdersViewModel.class);

        adapter = new OrdersAdapter(
            order -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).loadFragment(
                        OrderDetailFragment.newInstance(order.orderId), true
                    );
                }
            },
            order -> {
                // Click "Thanh toán ngay" trong list -> gọi ViewModel, không gọi API trực tiếp
                vm.initPayment(order.orderId);
            }
        );

        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recycler.setAdapter(adapter);

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                vm.setTabMode(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        binding.swipeRefresh.setOnRefreshListener(() -> vm.loadOrders(true));

        binding.recycler.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull androidx.recyclerview.widget.RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (lm != null && lm.findLastCompletelyVisibleItemPosition() == adapter.getItemCount() - 1) {
                        vm.loadOrders(false);
                    }
                }
            }
        });

        vm.getOrders().observe(getViewLifecycleOwner(), orders -> {
            binding.swipeRefresh.setRefreshing(false);
            if (orders == null || orders.isEmpty()) {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                adapter.setItems(new ArrayList<>());
            } else {
                binding.tvEmpty.setVisibility(View.GONE);
                adapter.setItems(orders);
            }
        });

        // Khi có payment URL: mở WebView qua launcher để bắt kết quả
        vm.getPaymentUrl().observe(getViewLifecycleOwner(), url -> {
            binding.progress.setVisibility(View.GONE);
            if (url != null && !url.isEmpty()) {
                Intent intent = new Intent(requireContext(), PaymentWebViewActivity.class);
                intent.putExtra("paymentUrl", url);
                paymentLauncher.launch(intent);
                vm.clearPaymentUrl();
            }
        });

        vm.isLoading().observe(getViewLifecycleOwner(), loading -> {
            if (loading != null && loading && !binding.swipeRefresh.isRefreshing()) {
                binding.progress.setVisibility(View.VISIBLE);
            } else {
                binding.progress.setVisibility(View.GONE);
            }
        });

        binding.btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        vm.loadOrders(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
