package vn.androidhaui.foxtrip.features.user.order;

import android.content.Intent;
import android.graphics.Bitmap;
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
import java.util.Date;
import java.util.List;

import vn.androidhaui.foxtrip.databinding.FragmentOrderDetailBinding;
import vn.androidhaui.foxtrip.features.user.checkout.PaymentWebViewActivity;
import vn.androidhaui.foxtrip.models.domain.OrderItem;
import vn.androidhaui.foxtrip.models.dto.request.CancelOrderReqDTO;
import vn.androidhaui.foxtrip.models.dto.response.OrderDetailResDTO;
import vn.androidhaui.foxtrip.utils.DateTimeUtils;
import vn.androidhaui.foxtrip.utils.QRUtils;

public class OrderDetailFragment extends Fragment {

    private static final String ARG_ORDER_ID = "order_id";
    private FragmentOrderDetailBinding binding;
    private OrderDetailViewModel vm;
    private OrderItemsAdapter itemsAdapter;
    private String currentOrderId;

    /**
     * Launcher bắt kết quả từ WebView thanh toán.
     * Sau khi VNPay hoàn tất, reload chi tiết đơn để cập nhật status ngay lập tức.
     */
    private final ActivityResultLauncher<Intent> paymentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (currentOrderId != null) {
                    vm.loadOrder(currentOrderId);
                }
            });

    public static OrderDetailFragment newInstance(String orderId) {
        OrderDetailFragment f = new OrderDetailFragment();
        Bundle b = new Bundle();
        b.putString(ARG_ORDER_ID, orderId);
        f.setArguments(b);
        return f;
    }

    public OrderDetailFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrderDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(OrderDetailViewModel.class);

        itemsAdapter = new OrderItemsAdapter();
        binding.rvItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvItems.setAdapter(itemsAdapter);

        currentOrderId = getArguments() != null ? getArguments().getString(ARG_ORDER_ID) : null;
        if (currentOrderId == null) {
            Toast.makeText(requireContext(), "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        observeViewModel();
        setupButtons();
        vm.loadOrder(currentOrderId);
    }

    // ── Observe ViewModel LiveData ──────────────────────────────────────────

    private void observeViewModel() {
        vm.getOrder().observe(getViewLifecycleOwner(), this::bindOrder);

        vm.isLoading().observe(getViewLifecycleOwner(), loading ->
                binding.progressOverlay.setVisibility(loading ? View.VISIBLE : View.GONE));

        vm.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                vm.clearMessage();
            }
        });

        vm.getActionSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                vm.loadOrder(currentOrderId);
            }
        });

        vm.getHasReviewed().observe(getViewLifecycleOwner(), hasReviewed -> {
            if (hasReviewed != null && hasReviewed) {
                binding.btnReview.setVisibility(View.GONE);
            }
        });

        // Khi có payment URL: dùng launcher để bắt kết quả -> reload tự động
        vm.getPaymentUrl().observe(getViewLifecycleOwner(), url -> {
            binding.progressOverlay.setVisibility(View.GONE);
            if (url != null && !url.isEmpty()) {
                Intent intent = new Intent(requireContext(), PaymentWebViewActivity.class);
                intent.putExtra("paymentUrl", url);
                paymentLauncher.launch(intent);
                vm.clearPaymentUrl();
            }
        });
    }

    // ── Setup buttons ──────────────────────────────────────────────────────

    private void setupButtons() {
        binding.btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        binding.btnPayNow.setOnClickListener(v -> {
            binding.progressOverlay.setVisibility(View.VISIBLE);
            vm.initPayment(currentOrderId);
        });

        binding.btnCancelOrder.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Xác nhận hủy tour")
                        .setMessage("LƯU Ý QUAN TRỌNG:\n\n" +
                                "1. Bạn sẽ bị trừ 50% giá trị đơn hàng làm phí hủy tour.\n" +
                                "2. Sau khi gửi yêu cầu, bạn KHÔNG THỂ rút lại.\n" +
                                "3. Admin sẽ hoàn 50% còn lại trong vòng 3-5 ngày làm việc.\n\n" +
                                "Bạn có chắc chắn muốn hủy không?")
                        .setPositiveButton("Xác nhận hủy", (dialog, which) -> {
                            CancelOrderReqDTO req = new CancelOrderReqDTO();
                            req.reason = "Khách hàng xác nhận hủy và chấp nhận phí 50%";
                            vm.cancelOrder(currentOrderId, req);
                        })
                        .setNegativeButton("Quay lại", null)
                        .show());

        binding.btnReview.setOnClickListener(v -> {
            OrderDetailResDTO currentOrder = vm.getOrder().getValue();
            if (currentOrder == null || currentOrder.tour == null || currentOrder.tour.tourId == null) {
                Toast.makeText(requireContext(), "Không xác định được tour để đánh giá", Toast.LENGTH_SHORT).show();
                return;
            }
            new ReviewDialog(requireContext(), currentOrder.tour.tourId.toString(), () ->
                    binding.btnReview.setVisibility(View.GONE)).show();
        });
    }

    // ── Bind order data to UI ──────────────────────────────────────────────

    private void bindOrder(OrderDetailResDTO o) {
        if (o == null) return;

        binding.tvOrderId.setText(o.orderCode != null ? o.orderCode : "—");
        binding.tvStatus.setText(toVietnameseStatus(o.status));
        binding.tvTotal.setText(String.format("%,.0f ₫", o.totalAmount));

        String createdStr = "—";
        if (o.createdAt != null) {
            Date parsedDate = DateTimeUtils.parseBackendDate(o.createdAt);
            createdStr = parsedDate != null ? DateTimeUtils.toDisplayDate(parsedDate) : o.createdAt;
        }
        binding.tvCreatedAt.setText(createdStr);
        binding.tvName.setText(o.customerName != null ? o.customerName : "—");
        binding.tvPhone.setText(o.customerPhone != null ? o.customerPhone : "—");
        binding.tvEmail.setText(o.customerEmail != null ? o.customerEmail : "—");

        // ── Quyết định hiển thị các nút action ──────────────────────────
        binding.btnPayNow.setVisibility(View.GONE);
        binding.btnCancelOrder.setVisibility(View.GONE);
        binding.btnReview.setVisibility(View.GONE);

        if ("PENDING".equals(o.status)) {
            binding.btnPayNow.setVisibility(View.VISIBLE);
        }

        if ("PAID".equals(o.status) && o.tour != null && o.tour.startDateAtTime != null) {
            Date startDate = DateTimeUtils.parseBackendDate(o.tour.startDateAtTime);
            boolean moreThan24h = startDate != null
                    && (startDate.getTime() - System.currentTimeMillis()) >= 24L * 60 * 60 * 1000;
            if (moreThan24h) {
                binding.btnCancelOrder.setVisibility(View.VISIBLE);
            }
        }

        if ("COMPLETED".equals(o.status)) {
            binding.btnReview.setVisibility(View.VISIBLE);
        }

        // Ẩn container nếu không có nút nào
        boolean anyVisible = binding.btnPayNow.getVisibility() == View.VISIBLE
                || binding.btnCancelOrder.getVisibility() == View.VISIBLE
                || binding.btnReview.getVisibility() == View.VISIBLE;
        binding.llActionButtons.setVisibility(anyVisible ? View.VISIBLE : View.GONE);

        // ── Danh sách sản phẩm ─────────────────────────────────────────
        List<OrderItem> displayItems = new ArrayList<>();
        if (o.tour != null) {
            displayItems.add(new OrderItem(
                    o.tour.tourNameAtTime,
                    o.totalAmount != null ? o.totalAmount.doubleValue() : 0.0,
                    o.quantity,
                    o.tour.thumbnailAtTime));
        }
        if (o.addons != null) {
            for (OrderDetailResDTO.AddonSnapshotDTO addon : o.addons) {
                displayItems.add(new OrderItem(
                        addon.nameAtTime,
                        addon.unitPriceAtTime != null ? addon.unitPriceAtTime.doubleValue() : 0.0,
                        addon.quantity,
                        null));
            }
        }
        itemsAdapter.setItems(displayItems);

        // ── QR Code ────────────────────────────────────────────────────
        if (o.qrPayload != null && !o.qrPayload.isEmpty()) {
            binding.cardQR.setVisibility(View.VISIBLE);
            Bitmap qrBitmap = QRUtils.generateQRCode(o.qrPayload, 500, 500);
            if (qrBitmap != null) binding.ivQRCode.setImageBitmap(qrBitmap);
        } else {
            binding.cardQR.setVisibility(View.GONE);
        }
    }

    // ── Chuyển đổi status sang tiếng Việt ─────────────────────────────────

    private String toVietnameseStatus(String status) {
        if (status == null) return "Không rõ";
        switch (status) {
            case "PENDING":          return "Chờ thanh toán";
            case "PAID":             return "Đã thanh toán";
            case "PROCESSING":       return "Đang xử lý";
            case "COMPLETED":        return "Hoàn thành";
            case "CANCELLED":        return "Đã hủy";
            case "CANCEL_REQUESTED": return "Đang chờ xét duyệt hủy";
            case "REFUND_PENDING":   return "Đang xử lý hoàn tiền";
            case "REFUNDED":         return "Đã hoàn tiền";
            case "EXPIRED":          return "Hết hạn thanh toán";
            case "FAILED":           return "Thất bại";
            default:                 return status;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}