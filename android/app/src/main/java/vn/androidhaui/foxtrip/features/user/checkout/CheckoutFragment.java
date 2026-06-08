package vn.androidhaui.foxtrip.features.user.checkout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.FragmentCheckoutBinding;
import vn.androidhaui.foxtrip.features.user.account.AccountViewModel;
import vn.androidhaui.foxtrip.features.user.account.VerifyEmailLoggedInFragment;
import vn.androidhaui.foxtrip.features.user.cart.CartAdapter;
import vn.androidhaui.foxtrip.features.user.cart.CartSharedViewModel;
import vn.androidhaui.foxtrip.models.dto.response.CartItemDTO;
import vn.androidhaui.foxtrip.models.dto.response.CreateOrderResDTO;
import vn.androidhaui.foxtrip.ui.MainActivity;

public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;
    private CheckoutViewModel checkoutViewModel;
    private CartSharedViewModel sharedViewModel;
    private AccountViewModel accountViewModel;

    private CartAdapter cartAdapter;
    private AddonPickerAdapter addonAdapter;

    private CartItemDTO checkoutItem;
    private List<AddonPickerAdapter.SelectedAddon> selectedAddons = new ArrayList<>();

    private final ActivityResultLauncher<Intent> paymentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Sau khi WebView đóng, xóa giỏ hàng trên server.
                    // cartCleared LiveData sẽ bắt kết quả và thực hiện navigate an toàn.
                    checkoutViewModel.clearCartAfterPayment();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        checkoutViewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(CartSharedViewModel.class);
        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);

        setupCartList();
        setupAddonPicker();
        observeCartItems();
        autoFillUserInfo();
        observeViewModel();
        setupPlaceOrderButton();
    }

    // ── Setup RecyclerViews ──────────────────────────────────────────────

    private void setupCartList() {
        cartAdapter = new CartAdapter(requireContext());
        cartAdapter.setMode(CartAdapter.MODE_READONLY);
        binding.rvCheckoutItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCheckoutItems.setAdapter(cartAdapter);
    }

    private void setupAddonPicker() {
        addonAdapter = new AddonPickerAdapter();
        binding.rvAddons.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAddons.setAdapter(addonAdapter);
        addonAdapter.setOnSelectionChangedListener(selected -> {
            selectedAddons = selected;
            recalcTotal();
        });
    }

    // ── Observe selected item from SharedViewModel ──────────────────────────

    private void observeCartItems() {
        sharedViewModel.getCheckoutItem().observe(getViewLifecycleOwner(), item -> {
            if (item == null) {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                binding.tvCheckoutItems.setText("Giỏ hàng trống");
                binding.tvAddonTitle.setVisibility(View.GONE);
                binding.rvAddons.setVisibility(View.GONE);
                return;
            }
            checkoutItem = item;
            binding.tvEmpty.setVisibility(View.GONE);
            binding.tvCheckoutItems.setText("Tour đã chọn (1)");

            List<CartItemDTO> list = new ArrayList<>();
            list.add(item);
            cartAdapter.submitList(list);

            if (item.tour != null && !item.tour.getSafeAddons().isEmpty()) {
                binding.tvAddonTitle.setVisibility(View.VISIBLE);
                binding.rvAddons.setVisibility(View.VISIBLE);
                addonAdapter.setAddons(item.tour.getSafeAddons(), item.getSafeQuantity());
            } else {
                binding.tvAddonTitle.setVisibility(View.GONE);
                binding.rvAddons.setVisibility(View.GONE);
            }
            recalcTotal();
        });
    }

    // ── Observe ViewModel LiveData ──────────────────────────────────────────

    private void observeViewModel() {
        checkoutViewModel.isLoading().observe(getViewLifecycleOwner(), loading ->
                binding.progressOverlay.setVisibility(loading ? View.VISIBLE : View.GONE));

        checkoutViewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null && !err.isEmpty()) {
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
            }
        });

        checkoutViewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            if (event == CheckoutViewModel.Event.EMAIL_NOT_VERIFIED) {
                checkoutViewModel.clearEvent();
                showVerificationRequiredDialog();
            }
        });

        checkoutViewModel.getOrderCreated().observe(getViewLifecycleOwner(), order -> {
            if (order != null) {
                showPaymentDialog(order);
            }
        });

        checkoutViewModel.getPaymentUrl().observe(getViewLifecycleOwner(), url -> {
            if (url != null && !url.isEmpty()) {
                Intent intent = new Intent(requireContext(), PaymentWebViewActivity.class);
                intent.putExtra("paymentUrl", url);
                paymentLauncher.launch(intent);
                checkoutViewModel.clearPaymentUrl();
            }
        });

        // Sau khi clearCart xong: cập nhật badge và chuyển sang My Orders
        checkoutViewModel.getCartCleared().observe(getViewLifecycleOwner(), cleared -> {
            if (cleared != null && cleared) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).updateCartBadge(0);
                    ((MainActivity) getActivity()).navigateToOrders();
                }
            }
        });
    }

    // ── Auto-fill user info ───────────────────────────────────────────────

    private void autoFillUserInfo() {
        accountViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            if (user.getUsername() != null && binding.etName.getText().toString().isEmpty()) {
                binding.etName.setText(user.getUsername());
            }
            if (user.getPhoneNumber() != null && binding.etPhone.getText().toString().isEmpty()) {
                binding.etPhone.setText(user.getPhoneNumber());
            }
            if (user.getEmail() != null) {
                binding.etEmail.setText(user.getEmail());
                binding.etEmail.setEnabled(false);
                binding.etEmail.setFocusable(false);
                binding.etEmail.setAlpha(0.6f);
            }
        });
        accountViewModel.loadProfile();
    }

    // ── Tính tổng phía client ────────────────────────────────────────────

    private void recalcTotal() {
        if (checkoutItem == null) return;
        double total = 0;
        if (checkoutItem.tour != null) {
            total += checkoutItem.tour.getFinalPrice() * checkoutItem.getSafeQuantity();
        }
        for (AddonPickerAdapter.SelectedAddon sa : selectedAddons) {
            total += (sa.addon.price != null ? sa.addon.price : 0) * sa.quantity;
        }
        binding.tvCheckoutTotal.setText(String.format("%,.0f VND", total));
    }

    // ── Nút Đặt hàng ───────────────────────────────────────────────────

    private void setupPlaceOrderButton() {
        binding.btnPlaceOrder.setText("Xác nhận Đặt hàng");
        binding.btnPlaceOrder.setOnClickListener(v -> {
            String name = binding.etName.getText().toString().trim();
            String phone = binding.etPhone.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            if (phone.length() < 10 || phone.length() > 11) {
                Toast.makeText(requireContext(), "Số điện thoại phải có 10-11 số", Toast.LENGTH_SHORT).show();
                return;
            }
            if (checkoutItem == null) {
                Toast.makeText(requireContext(), "Chưa chọn tour nào", Toast.LENGTH_SHORT).show();
                return;
            }
            checkoutViewModel.createOrder(name, phone, email,
                    checkoutItem.tourId, checkoutItem.getSafeQuantity(), selectedAddons);
        });
    }

    private void showVerificationRequiredDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Yêu cầu xác thực Email")
                .setMessage("Bạn cần xác thực email trước khi có thể đặt hàng. Bạn có muốn xác thực ngay bây giờ?")
                .setPositiveButton("Xác thực ngay", (dialog, which) -> {
                    VerifyEmailLoggedInFragment verifyFragment = new VerifyEmailLoggedInFragment();
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, verifyFragment)
                            .addToBackStack(null)
                            .commit();
                })
                .setNegativeButton("Để sau", null)
                .show();
    }

    private void showPaymentDialog(CreateOrderResDTO order) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Tạo đơn thành công!")
                .setMessage("Đơn hàng của bạn đã được ghi nhận. Bạn có muốn thanh toán ngay bây giờ?")
                .setPositiveButton("Thanh toán ngay", (dialog, which) ->
                        checkoutViewModel.initPayment(order.orderId))
                .setNegativeButton("Để sau", (dialog, which) -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).navigateToOrders();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}